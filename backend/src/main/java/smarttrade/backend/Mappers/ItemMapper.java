package smarttrade.backend.Mappers;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import smarttrade.backend.dto.item.*;
import smarttrade.backend.entities.item.ItemEntity;
import smarttrade.backend.entities.item.ItemImageEntity;
import smarttrade.backend.entities.item.ItemImageStatus;
import smarttrade.backend.entities.item.ItemSpecification;
import smarttrade.backend.entities.item.SpecificationTier;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Component
public class ItemMapper {

    private static final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);

    @Value("${pricing.refresh-window-hours:168}")
    private long refreshWindowHours;

    private List<String> mapImageUrls(ItemEntity item) {
        return item.getImages().stream()
                .filter(image -> image.getStatus() == ItemImageStatus.ACTIVE)
                .sorted(Comparator.comparing(ItemImageEntity::getSortOrder))
                .map(ItemImageEntity::getSecureUrl)
                .toList();
    }

    public ItemResponse mapFrom(ItemEntity entity) {
        if (entity == null) return null;

        ItemResponse dto = new ItemResponse();
        SellerSummaryDto seller = getSellerSummaryDto(entity);

        dto.setSeller(seller);
        dto.setItemId(entity.getItemId());
        dto.setItemName(entity.getItemName());
        dto.setNewPrice(entity.getNewPrice());
        dto.setUserPrice(entity.getUserPrice());
        dto.setGeneratedPrice(entity.getGeneratedPrice());
        dto.setPricingStatus(entity.getPricingStatus());
        dto.setPriceFetchedAt(entity.getPriceFetchedAt());
        dto.setNextPriceRefreshAt(
                entity.getPriceFetchedAt() == null
                        ? null
                        : entity.getPriceFetchedAt().plusHours(refreshWindowHours)
        );
        dto.setPricingErrorCode(entity.getPricingErrorCode());
        dto.setPricingErrorMessage(entity.getPricingErrorMessage());

        dto.setImages(
                entity.getImages() == null
                        ? java.util.List.of()
                        : entity.getImages().stream()
                        .filter(image -> image.getStatus() == ItemImageStatus.ACTIVE)
                        .sorted(java.util.Comparator.comparing(ItemImageEntity::getSortOrder))
                        .map(image -> new ItemImageResponse(
                                image.getId(),
                                image.getSecureUrl(),
                                image.getPublicId(),
                                image.getAssetId(),
                                image.getWidth(),
                                image.getHeight(),
                                image.getFormat(),
                                image.getBytes(),
                                image.getSortOrder(),
                                image.getCreatedAt()
                        ))
                        .toList()
        );

        dto.setCategory(entity.getCategory());
        dto.setCondition(entity.getCondition());
        dto.setCustomCategory(entity.getCustomCategory());

        dto.setPrimarySpecifications(
                mapSpecificationTier(entity.getSpecifications(), SpecificationTier.PRIMARY)
        );

        dto.setSecondarySpecifications(
                mapSpecificationTier(entity.getSpecifications(), SpecificationTier.SECONDARY)
        );

        dto.setTertiarySpecifications(
                mapSpecificationTier(entity.getSpecifications(), SpecificationTier.TERTIARY)
        );

        dto.setDescription(entity.getDescription());
        dto.setLabels(entity.getLabels());
        dto.setForSale(entity.isForSale());
        dto.setForTrade(entity.isForTrade());
        dto.setAvailable(entity.isAvailable());
        dto.setCity(entity.getCity());
        dto.setState(entity.getState());
        dto.setLocality(entity.getLocality());
        dto.setCreatedAt(entity.getCreatedAt());

        Point location = entity.getLocation();
        if (location != null) {
            dto.setLatitude(location.getY());
            dto.setLongitude(location.getX());
        }

        return dto;
    }

    private List<String> mapSpecificationTier(
            List<ItemSpecification> specifications,
            SpecificationTier tier
    ) {
        if (specifications == null || specifications.isEmpty()) {
            return List.of();
        }

        return specifications.stream()
                .filter(specification ->
                        specification != null
                                && specification.getTier() == tier
                )
                .sorted(Comparator.comparingInt(ItemSpecification::getSortOrder))
                .map(ItemSpecification::getSpecification)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    private static SellerSummaryDto getSellerSummaryDto(ItemEntity entity) {
        SellerSummaryDto seller = new SellerSummaryDto();

        seller.setUserId(entity.getUser().getUserId());
        seller.setName(entity.getUser().getName());
        seller.setTrustScore(entity.getUser().getTrustScore());
        seller.setTotalListings(entity.getUser().getTotalListings());
        seller.setSuccessfulTrades(entity.getUser().getSuccessfulTrades());
        seller.setAvatar(
                entity.getUser().getAvatar() != null
                        ? entity.getUser().getAvatar().getSecureUrl()
                        : null
        );
        return seller;
    }

    public ItemEntity fromCreateRequest(CreateItemRequest dto) {
        if (dto == null) return null;

        ItemEntity entity = new ItemEntity();
        entity.setItemName(dto.getItemName());
        entity.setUserPrice(dto.getUserPrice());
        entity.setCategory(dto.getCategory());
        entity.setCondition(dto.getCondition());
        entity.setCustomCategory(dto.getCustomCategory());
        entity.setDescription(dto.getDescription());
        entity.setLabels(dto.getLabels());
        entity.setSpecifications(toSpecifications(
                dto.getPrimarySpecifications(),
                dto.getSecondarySpecifications(),
                dto.getTertiarySpecifications()
        ));
        entity.setForSale(dto.isForSale());
        entity.setForTrade(dto.isForTrade());
        entity.setCity(dto.getCity());
        entity.setState(dto.getState());
        entity.setLocality(dto.getLocality());
        entity.setCreatedAt(LocalDateTime.now());

        if (dto.getImages() != null) {
            int index = 0;
            for (ItemImageRequest imageRequest : dto.getImages()) {
                ItemImageEntity image = ItemImageEntity.builder()
                        .item(entity)
                        .secureUrl(imageRequest.getSecureUrl())
                        .publicId(imageRequest.getPublicId())
                        .assetId(imageRequest.getAssetId())
                        .width(imageRequest.getWidth())
                        .height(imageRequest.getHeight())
                        .format(imageRequest.getFormat())
                        .bytes(imageRequest.getBytes())
                        .sortOrder(index++)
                        .status(ItemImageStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .build();
                entity.getImages().add(image);
            }
        }

        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            entity.setLocation(
                    geometryFactory.createPoint(
                            new Coordinate(dto.getLongitude(), dto.getLatitude())
                    )
            );
        }

        return entity;
    }

    public void updateEntityFromRequest(
            UpdateItemRequest dto,
            ItemEntity entity
    ) {

        if (dto.getItemName() != null) {
            entity.setItemName(dto.getItemName());
        }
        if (dto.getUserPrice() != null) {
            entity.setUserPrice(dto.getUserPrice());
        }
        if (dto.getCategory() != null) {
            entity.setCategory(dto.getCategory());
        }
        if (dto.getCondition() != null) {
            entity.setCondition(dto.getCondition());
        }
        if (dto.getCustomCategory() != null) {
            entity.setCustomCategory(dto.getCustomCategory());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getLabels() != null) {
            entity.setLabels(dto.getLabels());
        }
        if (dto.getPrimarySpecifications() != null) {
            replaceSpecificationTier(entity, SpecificationTier.PRIMARY, dto.getPrimarySpecifications());
        }
        if (dto.getSecondarySpecifications() != null) {
            replaceSpecificationTier(entity, SpecificationTier.SECONDARY, dto.getSecondarySpecifications());
        }
        if (dto.getTertiarySpecifications() != null) {
            replaceSpecificationTier(entity, SpecificationTier.TERTIARY, dto.getTertiarySpecifications());
        }
        if (dto.getForSale() != null) {
            entity.setForSale(dto.getForSale());
        }
        if (dto.getForTrade() != null) {
            entity.setForTrade(dto.getForTrade());
        }
        if (dto.getCity() != null) {
            entity.setCity(dto.getCity().trim());
        }
        if (dto.getState() != null) {
            entity.setState(dto.getState().trim());
        }
        if (dto.getLocality() != null) {
            entity.setLocality(dto.getLocality().trim());
        }

        if (Boolean.TRUE.equals(dto.getClearCoordinates())) {
            entity.setLocation(null);
        } else if (dto.getLatitude() != null && dto.getLongitude() != null) {
            entity.setLocation(
                    geometryFactory.createPoint(
                            new Coordinate(dto.getLongitude(), dto.getLatitude())
                    )
            );
        }
    }

    private List<ItemSpecification> toSpecifications(
            List<String> primary,
            List<String> secondary,
            List<String> tertiary
    ) {
        List<ItemSpecification> specifications = new java.util.ArrayList<>();
        int sortOrder = 0;

        sortOrder = appendSpecifications(specifications, SpecificationTier.PRIMARY, primary, sortOrder);
        sortOrder = appendSpecifications(specifications, SpecificationTier.SECONDARY, secondary, sortOrder);
        appendSpecifications(specifications, SpecificationTier.TERTIARY, tertiary, sortOrder);

        return specifications;
    }

    private int appendSpecifications(
            List<ItemSpecification> target,
            SpecificationTier tier,
            List<String> values,
            int sortOrder
    ) {
        if (values == null) return sortOrder;

        for (String value : cleanSpecifications(values)) {
            target.add(new ItemSpecification(tier, value, sortOrder++));
        }
        return sortOrder;
    }

    private void replaceSpecificationTier(
            ItemEntity entity,
            SpecificationTier tier,
            List<String> values
    ) {
        List<ItemSpecification> existing = entity.getSpecifications() == null
                ? new java.util.ArrayList<>()
                : new java.util.ArrayList<>(entity.getSpecifications());

        existing.removeIf(specification -> specification.getTier() == tier);

        List<ItemSpecification> replacement = new java.util.ArrayList<>();
        int sortOrder = 0;
        for (ItemSpecification specification : existing) {
            specification.setSortOrder(sortOrder++);
        }
        for (String value : cleanSpecifications(values)) {
            replacement.add(new ItemSpecification(tier, value, sortOrder++));
        }

        existing.addAll(replacement);
        existing.sort(java.util.Comparator.comparingInt(ItemSpecification::getSortOrder));
        entity.setSpecifications(existing);
    }

    private List<String> cleanSpecifications(List<String> specifications) {
        if (specifications == null || specifications.isEmpty()) {
            return List.of();
        }

        return specifications.stream()
                .filter(specification -> specification != null && !specification.isBlank())
                .map(String::trim)
                .filter(specification -> !specification.isBlank())
                .distinct()
                .toList();
    }
}
