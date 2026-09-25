package smarttrade.backend.service;

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
import smarttrade.backend.entities.item.ItemCategory;
import smarttrade.backend.entities.item.ItemEntity;
import smarttrade.backend.entities.item.ItemCondition;
import smarttrade.backend.entities.item.ItemPricingStatus;
import smarttrade.backend.events.ItemPersistedEvent;
import smarttrade.backend.exceptions.ForbiddenOperationException;
import smarttrade.backend.repository.ItemRepo;
import smarttrade.backend.security.AuthenticatedUserService;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.service.pricing.*;

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

    public List<ItemEntity> GetAllItems() {
        return itemRepo.findAll();
    }

    public List<ItemEntity> getRecentMarketplaceItems(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        Pageable pageable = PageRequest.of(safePage, safeSize);

        return itemRepo.findRecentMarketplaceItems(pageable);
    }

    public List<ItemEntity> getInventory(Long userId) {
        return itemRepo.findByUser_UserId(userId);
    }

    public Optional<ItemEntity> GetItem(Long itemId) {
        return itemRepo.findById(itemId);
    }

    @Transactional
    public ItemEntity addItems(ItemEntity item) {

        UserEntity currentUser =
                authenticatedUserService.getCurrentUser();

        item.setUser(currentUser);

        validateCategory(item);

        if (item.getPricingVersion() == null
                || item.getPricingVersion() <= 0L) {
            item.setPricingVersion(1L);
        }

        item.setPricingStatus(ItemPricingStatus.PENDING);

        log.info(
                "User {} creating item {}",
                currentUser.getEmail(),
                item.getItemName()
        );

        ItemEntity savedItem = itemRepo.save(item);

        applicationEventPublisher.publishEvent(
                new ItemPersistedEvent(savedItem.getItemId())
        );

        return savedItem;
    }

    @Transactional
    public ItemEntity updateItem(Long itemId, UpdateItemRequest request) {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        ItemEntity existingItem = itemRepo.findByItemIdAndUser_UserIdForUpdate(itemId, currentUser.getUserId())
                .orElseThrow(() -> new ForbiddenOperationException("Item not owned by user"));

        ItemCondition previousCondition = existingItem.getCondition();
        String previousItemName = existingItem.getItemName();
        ItemCategory previousCategory = existingItem.getCategory();
        String previousCustomCategory = existingItem.getCustomCategory();
        List<String> previousLabels =
                existingItem.getLabels() == null
                        ? List.of()
                        : List.copyOf(existingItem.getLabels());

        itemMapper.updateEntityFromRequest(request, existingItem);

        validateCategory(existingItem);

        itemImageService.applyUpdateImages(
                existingItem,
                request.getImages(),
                request.getRemovedImageIds()
        );
        boolean pricingInputsChanged =
                !Objects.equals(
                        previousItemName,
                        existingItem.getItemName()
                )
                        || !Objects.equals(
                        previousCategory,
                        existingItem.getCategory()
                )
                        || !Objects.equals(
                        previousCustomCategory,
                        existingItem.getCustomCategory()
                )
                        || !Objects.equals(
                        previousLabels,
                        existingItem.getLabels() == null
                                ? List.of()
                                : existingItem.getLabels()
                );
        if (pricingInputsChanged) {

            long currentVersion =
                    existingItem.getPricingVersion() == null
                            ? 0L
                            : existingItem.getPricingVersion();

            existingItem.setPricingVersion(
                    currentVersion + 1L
            );

            /*
             * PricingVersion belongs to the listing's pricing inputs, not
             * to individual jobs. An in-flight older job becomes stale.
             */
            existingItem.setPricingStatus(
                    ItemPricingStatus.PENDING
            );

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

        return savedItem;
    }
    public PricingRefreshResponse refreshItemPrice(Long itemId) {

        PricingJobDecision decision = pricingJobService.requestRefresh(itemId);

        if (decision.job() != null) {
            pricingBackgroundService.execute(
                    decision.job()
            );
        }

        return decision.response();
    }

    @Transactional
    public void deleteItem(Long itemId) {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        ItemEntity item = itemRepo.findByItemIdAndUser_UserId(itemId, currentUser.getUserId())
                .orElseThrow(() -> new ForbiddenOperationException("Item not owned by user"));

        log.info(
                "User {} deleted item {}",
                currentUser.getEmail(),
                itemId
        );

        itemImageService.markAllImagesForDeletion(item);

        itemRepo.delete(item);
    }

    public ItemSearchResponse searchItems(ItemSearchRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Search request cannot be null");
        }

        if (request.getMinPrice() != null
                && request.getMaxPrice() != null
                && request.getMinPrice() > request.getMaxPrice()) {

            throw new IllegalArgumentException(
                    "minPrice cannot be greater than maxPrice"
            );
        }

        if (request.getRadiusKm() != null
                && request.getRadiusKm() <= 0) {

            throw new IllegalArgumentException(
                    "radiusKm must be greater than 0"
            );
        }

        boolean hasCoordinates =
                request.getLatitude() != null
                        && request.getLongitude() != null;

        boolean hasLatitude =
                request.getLatitude() != null;

        boolean hasLongitude =
                request.getLongitude() != null;

        if (hasLatitude != hasLongitude) {
            throw new IllegalArgumentException(
                    "latitude and longitude must be provided together"
            );
        }

        if (request.getSort() == ItemSort.DISTANCE
                && !hasCoordinates) {

            throw new IllegalArgumentException(
                    "latitude and longitude are required when sorting by distance"
            );
        }
        boolean locationFilterActive =
                hasCoordinates
                        && request.getRadiusKm() != null;

        Double radiusMeters =
                locationFilterActive
                        ? request.getRadiusKm() * 1000.0
                        : null;

        int page = Math.max(request.getPage(), 0);

        int size = Math.min(
                Math.max(request.getSize(), 1),
                MAX_PAGE_SIZE
        );

        Pageable pageable = PageRequest.of(page, size);

        String name = request.getName();

        if (name != null) {
            name = name.trim();

            if (name.isBlank()) {
                name = null;
            }
        }

        String category =
                request.getCategory() != null
                        ? request.getCategory().name()
                        : null;

        /*
         * Never pass null collections to the native query.
         */
        boolean conditionsActive =
                request.getConditions() != null
                        && !request.getConditions().isEmpty();

        List<String> conditions =
                conditionsActive
                        ? request.getConditions()
                        .stream()
                        .filter(condition -> condition != null)
                        .map(ItemCondition::name)
                        .toList()
                        : List.of("__NO_CONDITION_FILTER__");

        List<String> normalizedLabels =
                request.getLabels() == null
                        ? List.of()
                        : request.getLabels()
                        .stream()
                        .filter(label ->
                                label != null
                                        && !label.isBlank()
                        )
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .distinct()
                        .toList();

        boolean labelsActive =
                !normalizedLabels.isEmpty();

        List<String> labels =
                labelsActive
                        ? normalizedLabels
                        : List.of("__NO_LABEL_FILTER__");

        String listingType =
                request.getListingType() != null
                        ? request.getListingType().name()
                        : null;

        String sort =
                request.getSort() != null
                        ? request.getSort().name()
                        : ItemSort.RELEVANCE.name();

        Page<ItemEntity> result =
                itemRepo.searchItems(
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

                        pageable
                );

        List<ItemResponse> items =
                result.getContent()
                        .stream()
                        .map(itemMapper::mapFrom)
                        .toList();

        return new ItemSearchResponse(
                items,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.hasNext()
        );
    }

    private void validateCategory(ItemEntity item) {

        if (item.getCategory() == null) {
            throw new IllegalArgumentException("Category is required");
        }

        if (item.getCategory() == ItemCategory.OTHER)  {

            if (item.getCustomCategory() == null || item.getCustomCategory().isBlank()) {
                throw new IllegalArgumentException("customCategory is required when category is OTHER");
            }

        } else {
            item.setCustomCategory(null);
        }
    }

    @Transactional
    public void updateAvailability(Long itemId, boolean available) {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        ItemEntity item = itemRepo.findByItemIdAndUser_UserId(itemId, currentUser.getUserId())
                .orElseThrow(() -> new ForbiddenOperationException("Item not owned by user"));

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