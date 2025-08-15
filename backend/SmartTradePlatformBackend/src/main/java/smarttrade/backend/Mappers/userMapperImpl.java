package smarttrade.backend.Mappers;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import smarttrade.backend.dto.userDto;
import smarttrade.backend.entities.userEntity;
@Component
public class userMapperImpl implements Mapper<userEntity, userDto>{
    private ModelMapper modelMapper;
    userMapperImpl(ModelMapper modelMapper){
        this.modelMapper=modelMapper;
    }

    @Override
    public userEntity mapTo(userDto userDto) {
        return modelMapper.map(userDto, userEntity.class) ;
    }

    @Override
    public userDto mapFrom(userEntity userEntity) {
        return modelMapper.map(userEntity, userDto.class);
    }
}

