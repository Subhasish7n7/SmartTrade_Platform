package smarttrade.backend.Mappers;

import org.springframework.stereotype.Component;
import smarttrade.backend.dto.userDto;
import smarttrade.backend.entities.UserEntity;

@Component
public class userMapperImpl implements Mapper<UserEntity, userDto> {

    @Override
    public UserEntity mapTo(userDto dto) {
        if (dto == null) return null;

        UserEntity user = new UserEntity();

        user.setUserId(dto.getUserId());
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setPhone_no(dto.getPhone_no());
        user.setLatitude(dto.getLatitude());
        user.setLongitude(dto.getLongitude());

        return user;
    }

    @Override
    public userDto mapFrom(UserEntity entity) {
        if (entity == null) return null;

        userDto dto = new userDto();

        dto.setUserId(entity.getUserId());
        dto.setEmail(entity.getEmail());
        dto.setName(entity.getName());
        dto.setPhone_no(entity.getPhone_no());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());

        return dto;
    }
}