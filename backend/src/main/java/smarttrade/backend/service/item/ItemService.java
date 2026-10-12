package smarttrade.backend.service.item;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import smarttrade.backend.Mappers.ItemMapper;
import smarttrade.backend.dto.item.*;
import smarttrade.backend.entities.item.*;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.events.itemPriceFetch.ItemPersistedEvent;
import smarttrade.backend.exceptions.ForbiddenOperationException;
import smarttrade.backend.repository.item.ItemRepo;
import smarttrade.backend.security.AuthenticatedUserService;
import smarttrade.backend.service.pricing.*;
import smarttrade.backend.events.trust.TrustEvent;
import smarttrade.backend.events.trust.TrustEventSourceType;
import smarttrade.backend.events.trust.TrustEventType;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final int ENTRY_RECENT_ITEM_LIMIT = 20;

    private final ItemRepo itemRepo;
    private final ItemMapper itemMapper;
    private final AuthenticatedUserService authenticatedUserService;
    private final PriceEstimationService priceEstimationService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PricingJobService pricingJobService;
    private final PricingBackgroundService pricingBackgroundService;
    private final ItemImageService itemImageService;
    private final PublicItemVisibilityService publicItemVisibilityService;

    public List<ItemEntity> GetAllItems() {
        return itemRepo.findAllByModerationStatus(ItemModerationStatus.ACTIVE);
    }

    public List<ItemEntity> getRecentMarketplaceItems(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int requiredVisibleCount = (safePage + 1) * safeSize;
        int fetchSize = Math.max(ENTRY_RECENT_ITEM_LIMIT, requiredVisibleCount);
        List<ItemEntity> visibleItems;

        // Fetch a growing prefix so seller-specific hidden unavailable items do not leave
        // recent pages unexpectedly short. This endpoint returns a list rather than page metadata.
        while (true) {
            List<ItemEntity> fetched = itemRepo.findRecentMarketplaceItems(PageRequest.of(0, fetchSize));
            visibleItems = publicItemVisibilityService.filterUnavailableListings(fetched);
            if (visibleItems.size() >= requiredVisibleCount || fetched.size() < fetchSize) {
                break;
            }
            fetchSize = Math.multiplyExact(fetchSize, 2);
        }

        int start = safePage * safeSize;
        if (start >= visibleItems.size()) return List.of();
        int end = Math.min(start + safeSize, visibleItems.size());
        return List.copyOf(visibleItems.subList(start, end));
    }

    public List<ItemEntity> getInventory(Long userId) {
        return itemRepo.findByUser_UserIdAndModerationStatus(
                userId,
                ItemModerationStatus.ACTIVE
        );
    }

    public List<ItemEntity> getOwnInventory() {
        UserEntity currentUser = authenticatedUserService.getCurrentUser();
        return itemRepo.findByUser_UserIdAndModerationStatus(
                currentUser.getUserId(),
                ItemModerationStatus.ACTIVE
        );
    }

    public Optional<ItemEntity> GetItem(Long itemId) {
        return itemRepo.findByItemIdAndModerationStatus(
                itemId,
                ItemModerationStatus.ACTIVE
        );
    }

    @Transactional
    public ItemEntity addItems(ItemEntity item) {
        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        item.setUser(currentUser);
        item.setModerationStatus(ItemModerationStatus.ACTIVE);

        validateCategory(item);
        normalizeSpecifications(item);

        if (item.getPricingVersion() == null || item.getPricingVersion() <= 0L) {
            item.setPricingVersion(1L);
        }

        item.setPricingStatus(ItemPricingStatus.PENDING);

        log.info(
                "User {} creating item {}",
                currentUser.getEmail(),
                item.getItemName()
        );

        ItemEntity savedItem = itemRepo.save(item);
        currentUser.setTotalListings(currentUser.getTotalListings() + 1);

        applicationEventPublisher.publishEvent(
                new ItemPersistedEvent(savedItem.getItemId())
        );
        applicationEventPublisher.publishEvent(
                TrustEvent.now(
                        currentUser.getUserId(),
                        TrustEventType.LISTING_CHANGED,
                        TrustEventSourceType.ITEM,
                        savedItem.getItemId(),
                        null,
                        "Listing created: " + savedItem.getItemId()
                )
        );

        return savedItem;
    }

    @Transactional
    public ItemEntity updateItem(Long itemId, UpdateItemRequest request) {
        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        ItemEntity existingItem = itemRepo.findByItemIdAndUser_UserIdForUpdate(
                        itemId,
                        currentUser.getUserId()
                )
                .orElseThrow(() -> new ForbiddenOperationException("Item not owned by user"));

        ItemCondition previousCondition = existingItem.getCondition();
        String previousItemName = existingItem.getItemName();
        ItemCategory previousCategory = existingItem.getCategory();
        String previousCustomCategory = existingItem.getCustomCategory();
        List<String> previousLabels = existingItem.getLabels() == null
                ? List.of()
                : List.copyOf(existingItem.getLabels());
        List<ItemSpecification> previousSpecifications = copySpecifications(existingItem.getSpecifications());

        itemMapper.updateEntityFromRequest(request, existingItem);

        validateCategory(existingItem);
        normalizeSpecifications(existingItem);

        itemImageService.applyUpdateImages(
                existingItem,
                request.getImages(),
                request.getRemovedImageIds()
        );

        boolean pricingInputsChanged =
                !Objects.equals(previousItemName, existingItem.getItemName())
                        || !Objects.equals(previousCategory, existingItem.getCategory())
                        || !Objects.equals(previousCustomCategory, existingItem.getCustomCategory())
                        || !Objects.equals(
                        previousLabels,
                        existingItem.getLabels() == null
                                ? List.of()
                                : existingItem.getLabels()
                )
                        || !Objects.equals(
                        previousSpecifications,
                        copySpecifications(existingItem.getSpecifications())
                );

        if (pricingInputsChanged) {
            long currentVersion = existingItem.getPricingVersion() == null
                    ? 0L
                    : existingItem.getPricingVersion();

            existingItem.setPricingVersion(currentVersion + 1L);

            /*
             * PricingVersion belongs to the listing's pricing inputs, not
             * to individual jobs. An in-flight older job becomes stale.
             */
            existingItem.setPricingStatus(ItemPricingStatus.PENDING);
            existingItem.setPricingErrorCode(null);
            existingItem.setPricingErrorMessage(null);
        }

        if (existingItem.getCondition() != previousCondition) {
            existingItem.setGeneratedPrice(
                    priceEstimationService.generateUsedPrice(
                            existingItem.getNewPrice(),
                            existingItem.getCondition()
                    )
            );
        }

        log.info(
                "User {} updated item {}",
                currentUser.getEmail(),
                itemId
        );

        ItemEntity savedItem = itemRepo.save(existingItem);

        if (pricingInputsChanged) {
            applicationEventPublisher.publishEvent(
                    new ItemPersistedEvent(savedItem.getItemId())
            );
        }

        applicationEventPublisher.publishEvent(
                TrustEvent.now(
                        currentUser.getUserId(),
                        TrustEventType.LISTING_CHANGED,
                        TrustEventSourceType.ITEM,
                        savedItem.getItemId(),
                        null,
                        "Listing updated: " + savedItem.getItemId()
                )
        );

        return savedItem;
    }

    public PricingRefreshResponse refreshItemPrice(Long itemId) {
        PricingJobDecision decision = pricingJobService.requestRefresh(itemId);

        if (decision.job() != null) {
            pricingBackgroundService.execute(decision.job());
        }

        return decision.response();
    }

    @Transactional
    public void deleteItem(Long itemId) {
        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        ItemEntity item = itemRepo.findByItemIdAndUser_UserId(
                        itemId,
                        currentUser.getUserId()
                )
                .orElseThrow(() -> new ForbiddenOperationException("Item not owned by user"));

        log.info(
                "User {} deleted item {}",
                currentUser.getEmail(),
                itemId
        );

        itemImageService.markAllImagesForDeletion(item);
        itemRepo.delete(item);

        applicationEventPublisher.publishEvent(
                TrustEvent.now(
                        currentUser.getUserId(),
                        TrustEventType.LISTING_CHANGED,
                        TrustEventSourceType.ITEM,
                        itemId,
                        null,
                        "Listing deleted: " + itemId
                )
        );
    }

    public ItemSearchResponse searchItems(ItemSearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Search request cannot be null");
        }

        if (request.getMinPrice() != null
                && request.getMaxPrice() != null
                && request.getMinPrice() > request.getMaxPrice()) {
            throw new IllegalArgumentException("minPrice cannot be greater than maxPrice");
        }

        if (request.getRadiusKm() != null && request.getRadiusKm() <= 0) {
            throw new IllegalArgumentException("radiusKm must be greater than 0");
        }

        boolean hasCoordinates = request.getLatitude() != null
                && request.getLongitude() != null;
        boolean hasLatitude = request.getLatitude() != null;
        boolean hasLongitude = request.getLongitude() != null;

        if (hasLatitude != hasLongitude) {
            throw new IllegalArgumentException(
                    "latitude and longitude must be provided together"
            );
        }

        if (request.getSort() == ItemSort.DISTANCE && !hasCoordinates) {
            throw new IllegalArgumentException(
                    "latitude and longitude are required when sorting by distance"
            );
        }

        boolean locationFilterActive = hasCoordinates
                && request.getRadiusKm() != null;

        Double radiusMeters = locationFilterActive
                ? request.getRadiusKm() * 1000.0
                : null;

        int page = Math.max(request.getPage(), 0);
        int size = Math.min(Math.max(request.getSize(), 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size);

        String name = request.getName();
        if (name != null) {
            name = name.trim();
            if (name.isBlank()) name = null;
        }

        String category = request.getCategory() != null
                ? request.getCategory().name()
                : null;

        boolean conditionsActive = request.getConditions() != null
                && !request.getConditions().isEmpty();

        List<String> conditions = conditionsActive
                ? request.getConditions().stream()
                .filter(condition -> condition != null)
                .map(ItemCondition::name)
                .toList()
                : List.of("__NO_CONDITION_FILTER__");

        List<String> normalizedLabels = request.getLabels() == null
                ? List.of()
                : request.getLabels().stream()
                .filter(label -> label != null && !label.isBlank())
                .map(String::trim)
                .map(String::toLowerCase)
                .distinct()
                .toList();

        boolean labelsActive = !normalizedLabels.isEmpty();
        List<String> labels = labelsActive
                ? normalizedLabels
                : List.of("__NO_LABEL_FILTER__");

        String listingType = request.getListingType() != null
                ? request.getListingType().name()
                : null;

        String sort = request.getSort() != null
                ? request.getSort().name()
                : ItemSort.RELEVANCE.name();

        List<ItemEntity> selectedItems = new java.util.ArrayList<>();
        long visibleCount = 0L;
        int scanPage = 0;
        Page<ItemEntity> scanResult;
        do {
            scanResult = itemRepo.searchItems(
                    name,
                    category,
                    conditions,
                    conditionsActive,
                    labels,
                    labelsActive,
                    request.getMinPrice(),
                    request.getMaxPrice(),
                    listingType,
                    request.getLatitude(),
                    request.getLongitude(),
                    radiusMeters,
                    locationFilterActive,
                    hasCoordinates,
                    sort,
                    PageRequest.of(scanPage, MAX_PAGE_SIZE)
            );

            List<ItemEntity> visibleItems = publicItemVisibilityService
                    .filterUnavailableListings(scanResult.getContent());
            for (ItemEntity visibleItem : visibleItems) {
                if (visibleCount >= (long) page * size && selectedItems.size() < size) {
                    selectedItems.add(visibleItem);
                }
                visibleCount++;
            }
            scanPage++;
        } while (scanResult.hasNext());

        List<ItemResponse> items = selectedItems.stream()
                .map(itemMapper::mapFrom)
                .toList();

        return new ItemSearchResponse(
                items,
                page,
                size,
                visibleCount,
                ((long) page + 1L) * size < visibleCount
        );
    }

    private void validateCategory(ItemEntity item) {
        if (item.getCategory() == null) {
            throw new IllegalArgumentException("Category is required");
        }

        if (item.getCategory() == ItemCategory.OTHER) {
            if (item.getCustomCategory() == null || item.getCustomCategory().isBlank()) {
                throw new IllegalArgumentException(
                        "customCategory is required when category is OTHER"
                );
            }
        } else {
            item.setCustomCategory(null);
        }
    }

    private void normalizeSpecifications(ItemEntity item) {
        if (item.getSpecifications() == null || item.getSpecifications().isEmpty()) {
            item.setSpecifications(new java.util.ArrayList<>());
            return;
        }

        List<ItemSpecification> normalized = new java.util.ArrayList<>();
        int sortOrder = 0;

        for (SpecificationTier tier : SpecificationTier.values()) {
            for (ItemSpecification specification : item.getSpecifications()) {
                if (specification == null || specification.getTier() != tier) continue;
                if (specification.getSpecification() == null || specification.getSpecification().isBlank()) continue;

                String value = specification.getSpecification().trim();
                boolean duplicate = normalized.stream()
                        .anyMatch(existing -> existing.getTier() == tier
                                && existing.getSpecification().equalsIgnoreCase(value));
                if (duplicate) continue;

                normalized.add(new ItemSpecification(tier, value, sortOrder++));
            }
        }

        item.setSpecifications(normalized);
    }

    private List<ItemSpecification> copySpecifications(List<ItemSpecification> specifications) {
        if (specifications == null || specifications.isEmpty()) {
            return List.of();
        }

        return specifications.stream()
                .map(specification -> new ItemSpecification(
                        specification.getTier(),
                        specification.getSpecification(),
                        specification.getSortOrder()
                ))
                .toList();
    }

    private List<String> copy(List<String> values) {
        return values == null ? List.of() : List.copyOf(values);
    }

    @Transactional
    public void updateAvailability(Long itemId, boolean available) {
        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        ItemEntity item = itemRepo.findByItemIdAndUser_UserId(
                        itemId,
                        currentUser.getUserId()
                )
                .orElseThrow(() -> new ForbiddenOperationException("Item not owned by user"));

        if (item.getModerationStatus() != ItemModerationStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Hidden items cannot be changed through the public item API."
            );
        }

        item.setAvailable(available);

        log.info(
                "User {} changed availability for item {} to {}",
                currentUser.getEmail(),
                itemId,
                available
        );

        itemRepo.save(item);
    }
}
