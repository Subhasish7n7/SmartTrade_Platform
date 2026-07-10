package smarttrade.backend.Mappers;

import org.springframework.stereotype.Component;
import smarttrade.backend.dto.item.CreateItemRequest;
import smarttrade.backend.dto.item.ItemResponse;
import smarttrade.backend.dto.item.SellerSummaryDto;
import smarttrade.backend.dto.item.UpdateItemRequest;
import smarttrade.backend.entities.ItemEntity;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import java.time.LocalDateTime;

@Component
public class ItemMapper {

    private static final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);




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
        dto.setImageUrls(entity.getImageUrls());
        dto.setCategory(entity.getCategory());
        dto.setCondition(entity.getCondition());
        dto.setDescription(entity.getDescription());
        dto.setLabels(entity.getLabels());

        dto.setForSale(entity.isForSale());
        dto.setForTrade(entity.isForTrade());
        dto.setAvailable(entity.isAvailable());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getLocation() != null) {
            dto.setLatitude(entity.getLocation().getY());
            dto.setLongitude(entity.getLocation().getX());
        }

        return dto;
    }
    public ItemEntity fromCreateRequest(CreateItemRequest dto) {

        if (dto == null) return null;

        ItemEntity entity = new ItemEntity();

        entity.setItemName(dto.getItemName());
        entity.setUserPrice(dto.getUserPrice());
        entity.setImageUrls(dto.getImageUrls());

        entity.setCategory(dto.getCategory());
        entity.setCondition(dto.getCondition());
        entity.setDescription(dto.getDescription());
        entity.setLabels(dto.getLabels());

        entity.setForSale(dto.isForSale());
        entity.setForTrade(dto.isForTrade());
        entity.setCreatedAt(LocalDateTime.now());

        if (dto.getLatitude() != null && dto.getLongitude() != null) {

            entity.setLocation(
                    geometryFactory.createPoint(
                            new Coordinate(
                                    dto.getLongitude(),
                                    dto.getLatitude()
                            )
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

        if (dto.getNewPrice() != null) {
            entity.setNewPrice(dto.getNewPrice());
        }

        if (dto.getUserPrice() != null) {
            entity.setUserPrice(dto.getUserPrice());
        }

        if (dto.getGeneratedPrice() != null) {
            entity.setGeneratedPrice(dto.getGeneratedPrice());
        }

        if(dto.getImageUrls() != null){
            entity.setImageUrls(dto.getImageUrls());
        }

        if (dto.getCategory() != null) {
            entity.setCategory(dto.getCategory());
        }

        if (dto.getCondition() != null) {
            entity.setCondition(dto.getCondition());
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

        if (dto.getLatitude() != null && dto.getLongitude() != null) {

            entity.setLocation(
                    geometryFactory.createPoint(
                            new Coordinate(
                                    dto.getLongitude(),
                                    dto.getLatitude()
                            )
                    )
            );
        }
    }
}