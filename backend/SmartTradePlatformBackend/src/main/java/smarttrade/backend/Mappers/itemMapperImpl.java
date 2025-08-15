package smarttrade.backend.Mappers;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import smarttrade.backend.dto.itemDto;
import smarttrade.backend.entities.itemEntity;
@Component
public class itemMapperImpl implements Mapper<itemEntity, itemDto>{

    private ModelMapper modelMapper;

    public itemMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public itemEntity mapTo(itemDto itemDto) {
        return modelMapper.map(itemDto, itemEntity.class);
    }

    @Override
    public itemDto mapFrom(itemEntity itemEntity) {
        return modelMapper.map(itemEntity, itemDto.class);
    }
}

