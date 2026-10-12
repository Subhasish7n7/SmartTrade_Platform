package smarttrade.backend.UnitTest.User;

import org.junit.jupiter.api.Test;
import smarttrade.backend.Mappers.UserMapper;
import smarttrade.backend.TestDataUtil.userTestData;
import smarttrade.backend.dto.auth.CurrentUserResponse;
import smarttrade.backend.dto.user.MyUserProfileResponse;
import smarttrade.backend.dto.user.PublicUserProfileResponse;
import smarttrade.backend.dto.user.UserDto;
import smarttrade.backend.entities.user.UserEntity;

import static org.junit.jupiter.api.Assertions.*;

public class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    @Test
    void shouldReturnNullWhenMappingNullUserDtoToEntity() {
        assertNull(userMapper.mapTo(null));
    }

    @Test
    void shouldMapUserDtoToEntity() {
        UserDto dto = new UserDto(
                10L,
                "user@test.com",
                "Ashutosh",
                "password",
                "9876543210",
                4.2,
                12,
                8,
                20.2961,
                85.8245
        );

        UserEntity result = userMapper.mapTo(dto);

        assertEquals(10L, result.getUserId());
        assertEquals("user@test.com", result.getEmail());
        assertEquals("Ashutosh", result.getName());
        assertEquals("9876543210", result.getPhone_no());
        assertEquals(20.2961, result.getLatitude());
        assertEquals(85.8245, result.getLongitude());

        // Mapper intentionally does not copy these fields.
        assertNull(result.getPassword());
        assertEquals(500, result.getTrustScore());
        assertEquals(0, result.getTotalListings());
        assertEquals(0, result.getSuccessfulTrades());
        assertNull(result.getAvatar());
    }

    @Test
    void shouldReturnNullWhenMappingNullEntityToDto() {
        assertNull(userMapper.mapFrom(null));
    }

    @Test
    void shouldMapEntityToUserDto() {
        UserEntity entity = userTestData.CreateUserA();
        entity.setUserId(10L);

        UserDto result = userMapper.mapFrom(entity);

        assertEquals(10L, result.getUserId());
        assertEquals(entity.getEmail(), result.getEmail());
        assertEquals(entity.getName(), result.getName());
        assertEquals(entity.getPhone_no(), result.getPhone_no());
        assertEquals(entity.getLatitude(), result.getLatitude());
        assertEquals(entity.getLongitude(), result.getLongitude());

        // Mapper intentionally does not expose these fields.
        assertNull(result.getPassword());
        assertEquals(0.0, result.getTrustScore());
        assertEquals(0, result.getTotalListings());
        assertEquals(0, result.getSuccessfulTrades());
    }

    @Test
    void shouldMapCurrentUser() {
        UserEntity entity = userTestData.CreateUserA();
        entity.setUserId(10L);

        CurrentUserResponse result =
                userMapper.mapToCurrentUser(entity);

        assertEquals(10L, result.getId());
        assertEquals(entity.getName(), result.getName());
        assertEquals(entity.getEmail(), result.getEmail());
        assertEquals(entity.getPhone_no(), result.getPhoneNo());
        assertEquals(entity.getAvatar().getSecureUrl(), result.getAvatarUrl());
    }

    @Test
    void shouldMapPublicProfile() {
        UserEntity entity = userTestData.CreateUserA();
        entity.setUserId(10L);

        PublicUserProfileResponse result =
                userMapper.mapToPublicProfile(entity);

        assertEquals(10L, result.getId());
        assertEquals(entity.getName(), result.getName());
        assertEquals(entity.getAvatar().getSecureUrl(), result.getAvatarUrl());
        assertEquals(entity.getTrustScore(), result.getTrustScore());
        assertEquals(entity.getTotalListings(), result.getTotalListings());
        assertEquals(entity.getSuccessfulTrades(), result.getSuccessfulTrades());
    }

    @Test
    void shouldReturnNullForNullPublicProfileUser() {
        assertNull(userMapper.mapToPublicProfile(null));
    }

    @Test
    void shouldMapMyProfile() {
        UserEntity entity = userTestData.CreateUserA();
        entity.setUserId(10L);

        MyUserProfileResponse result =
                userMapper.mapToMyProfile(entity);

        assertEquals(10L, result.getId());
        assertEquals(entity.getName(), result.getName());
        assertEquals(entity.getEmail(), result.getEmail());
        assertEquals(entity.getPhone_no(), result.getPhoneNo());
        assertEquals(entity.getAvatar().getSecureUrl(), result.getAvatarUrl());
        assertEquals(entity.getTrustScore(), result.getTrustScore());
        assertEquals(entity.getTotalListings(), result.getTotalListings());
        assertEquals(entity.getSuccessfulTrades(), result.getSuccessfulTrades());
    }

    @Test
    void shouldReturnNullForNullMyProfileUser() {
        assertNull(userMapper.mapToMyProfile(null));
    }
}