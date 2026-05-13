package smarttrade.backend.Mappers;

import org.springframework.stereotype.Component;
import smarttrade.backend.dto.UserDto;
import smarttrade.backend.entities.UserEntity;

@Component
public class UserMapperImpl {


    public UserEntity mapTo(UserDto dto) {
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

    public UserDto mapFrom(UserEntity entity) {
        if (entity == null) return null;

        UserDto dto = new UserDto();

        dto.setUserId(entity.getUserId());
        dto.setEmail(entity.getEmail());
        dto.setName(entity.getName());
        dto.setPhone_no(entity.getPhone_no());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());

        return dto;
    }
}