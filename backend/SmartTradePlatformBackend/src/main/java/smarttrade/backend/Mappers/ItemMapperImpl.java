package smarttrade.backend.Mappers;

import org.springframework.stereotype.Component;
import smarttrade.backend.dto.CreateItemRequest;
import smarttrade.backend.dto.ItemResponse;
import smarttrade.backend.dto.UpdateItemRequest;
import smarttrade.backend.entities.ItemEntity;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

@Component
public class ItemMapperImpl {

    private static final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);




    public ItemResponse mapFrom(ItemEntity entity) {
        if (entity == null) return null;

        ItemResponse dto = new ItemResponse();
        if (entity.getUser() != null) {
            dto.setOwnerId(entity.getUser().getUserId());
        }

        dto.setItemId(entity.getItemId());
        dto.setItem_name(entity.getItem_name());
        dto.setItem_NewPrice(entity.getItem_NewPrice());
        dto.setItem_UserPrice(entity.getItem_UserPrice());
        dto.setItem_GeneratedPrice(entity.getItem_GeneratedPrice());
        dto.setCategory(entity.getCategory());
        dto.setCondition(entity.getCondition());
        dto.setDescription(entity.getDescription());
        dto.setLabels(entity.getLabels());

        dto.setForSale(entity.isForSale());
        dto.setForTrade(entity.isForTrade());
        dto.setAvailable(entity.isAvailable());

        if (entity.getLocation() != null) {
            dto.setLatitude(entity.getLocation().getY());
            dto.setLongitude(entity.getLocation().getX());
        }

        return dto;
    }
    public ItemEntity fromCreateRequest(CreateItemRequest dto) {

        if (dto == null) return null;

        ItemEntity entity = new ItemEntity();

        entity.setItem_name(dto.getItem_name());
        entity.setItem_NewPrice(dto.getItem_NewPrice());
        entity.setItem_UserPrice(dto.getItem_UserPrice());
        entity.setItem_GeneratedPrice(dto.getItem_GeneratedPrice());

        entity.setCategory(dto.getCategory());
        entity.setCondition(dto.getCondition());
        entity.setDescription(dto.getDescription());
        entity.setLabels(dto.getLabels());

        entity.setForSale(dto.isForSale());
        entity.setForTrade(dto.isForTrade());

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

        if (dto.getItem_name() != null) {
            entity.setItem_name(dto.getItem_name());
        }

        if (dto.getItem_NewPrice() != null) {
            entity.setItem_NewPrice(dto.getItem_NewPrice());
        }

        if (dto.getItem_UserPrice() != null) {
            entity.setItem_UserPrice(dto.getItem_UserPrice());
        }

        if (dto.getItem_GeneratedPrice() != null) {
            entity.setItem_GeneratedPrice(dto.getItem_GeneratedPrice());
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