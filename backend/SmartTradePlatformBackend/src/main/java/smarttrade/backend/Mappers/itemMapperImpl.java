package smarttrade.backend.Mappers;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.stereotype.Component;
import smarttrade.backend.dto.itemDto;
import smarttrade.backend.dto.userDto;
import smarttrade.backend.entities.itemEntity;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import smarttrade.backend.entities.userEntity;

@Component
public class itemMapperImpl implements Mapper<itemEntity, itemDto> {

    private final ModelMapper modelMapper;
    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public itemMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public itemEntity mapTo(itemDto dto) {
        if (dto == null) return null;

        itemEntity entity = modelMapper.map(dto, itemEntity.class); // Will now use configured TypeMap

        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            entity.setLocation(geometryFactory.createPoint(
                    new Coordinate(dto.getLongitude(), dto.getLatitude())));
        }

        return entity;
    }

    @Override
    public itemDto mapFrom(itemEntity entity) {
        if (entity == null) return null;

        itemDto dto = modelMapper.map(entity, itemDto.class);

        if (entity.getLocation() != null) {
            dto.setLatitude(entity.getLocation().getY());
            dto.setLongitude(entity.getLocation().getX());
        }

        return dto;
    }
}


