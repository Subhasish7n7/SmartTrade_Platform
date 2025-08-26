package smarttrade.backend.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import smarttrade.backend.dto.itemDto;
import smarttrade.backend.entities.itemEntity;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        TypeMap<itemDto, itemEntity> typeMap = modelMapper.createTypeMap(itemDto.class, itemEntity.class);
        typeMap.addMappings(mapper -> mapper.skip(itemEntity::setLocation));
        typeMap.setProvider(p -> new itemEntity());

        return modelMapper;
    }
}

