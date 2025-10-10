package smarttrade.backend.Mappers;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import smarttrade.backend.dto.userDto;
import smarttrade.backend.entities.UserEntity;
@Component
public class userMapperImpl implements Mapper<UserEntity, userDto>{
    private ModelMapper modelMapper;
    public userMapperImpl(ModelMapper modelMapper){
        this.modelMapper=modelMapper;
    }

    @Override
    public UserEntity mapTo(userDto userDto) {
        return modelMapper.map(userDto, UserEntity.class) ;
    }

    @Override
    public userDto mapFrom(UserEntity userEntity) {
        return modelMapper.map(userEntity, userDto.class);
    }
}

