package smarttrade.backend.Mappers;

import org.springframework.stereotype.Component;
import smarttrade.backend.dto.user.UserDto;
import smarttrade.backend.dto.auth.CurrentUserResponse;
import smarttrade.backend.dto.user.MyUserProfileResponse;
import smarttrade.backend.dto.user.PublicUserProfileResponse;
import smarttrade.backend.entities.UserEntity;

@Component
public class UserMapper {

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

    public CurrentUserResponse mapToCurrentUser(UserEntity currentUser) {
        return CurrentUserResponse.builder()
                .id(currentUser.getUserId())
                .name(currentUser.getName())
                .email(currentUser.getEmail())
                .phoneNo(currentUser.getPhone_no())
                .avatarUrl(currentUser.getAvatarUrl())
                .build();
    }

    public PublicUserProfileResponse mapToPublicProfile(UserEntity user) {
        if (user == null) return null;

        return new PublicUserProfileResponse(
                user.getUserId(),
                user.getName(),
                user.getAvatarUrl(),
                user.getTrustScore(),
                user.getTotalListings(),
                user.getSuccessfulTrades()
        );
    }

    public MyUserProfileResponse mapToMyProfile(UserEntity user) {
        if (user == null) return null;

        return new MyUserProfileResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getPhone_no(),
                user.getAvatarUrl(),
                user.getTrustScore(),
                user.getTotalListings(),
                user.getSuccessfulTrades()
        );
    }
}
