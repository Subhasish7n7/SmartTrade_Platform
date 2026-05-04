package smarttrade.backend.Mappers;

import org.springframework.stereotype.Component;
import smarttrade.backend.dto.itemDto;
import smarttrade.backend.entities.itemEntity;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import smarttrade.backend.repository.userRepo;

@Component
public class itemMapperImpl implements Mapper<itemEntity, itemDto> {

    private static final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);
    private final userRepo userRepo;

    public itemMapperImpl(userRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public itemEntity mapTo(itemDto dto) {
        if (dto == null) return null;
        itemEntity entity = new itemEntity();

        if (dto.getUserId() != null) {
            entity.setUser(
                    userRepo.findById(dto.getUserId())
                            .orElseThrow(() -> new RuntimeException("User not found"))
            );
        }

        entity.setItemId(dto.getItemId());
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
        entity.setAvailable(dto.isAvailable());

        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            entity.setLocation(
                    geometryFactory.createPoint(
                            new Coordinate(dto.getLongitude(), dto.getLatitude())
                    )
            );
        }

        return entity;
    }

    @Override
    public itemDto mapFrom(itemEntity entity) {
        if (entity == null) return null;

        itemDto dto = new itemDto();
        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getUserId());
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
}