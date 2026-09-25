package smarttrade.backend.Mappers;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import smarttrade.backend.dto.item.CreateItemRequest;
import smarttrade.backend.dto.item.ItemImageRequest;
import smarttrade.backend.dto.item.ItemImageResponse;
import smarttrade.backend.dto.item.ItemResponse;
import smarttrade.backend.dto.item.SellerSummaryDto;
import smarttrade.backend.dto.item.UpdateItemRequest;
import smarttrade.backend.entities.item.ItemEntity;
import smarttrade.backend.entities.item.ItemImageEntity;
import smarttrade.backend.entities.item.ItemImageStatus;

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
        SellerSummaryDto seller = new SellerSummaryDto();

        seller.setUserId(entity.getUser().getUserId());
        seller.setName(entity.getUser().getName());
        seller.setTrustScore(entity.getUser().getTrustScore());
        seller.setTotalListings(entity.getUser().getTotalListings());
        seller.setSuccessfulTrades(entity.getUser().getSuccessfulTrades());

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
}