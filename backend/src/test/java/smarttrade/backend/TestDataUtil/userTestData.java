package smarttrade.backend.TestDataUtil;

import smarttrade.backend.entities.user.UserAvatarEntity;
import smarttrade.backend.entities.user.UserAvatarStatus;
import smarttrade.backend.entities.user.UserEntity;

import java.time.LocalDateTime;

public class userTestData {

    public static UserEntity CreateUserA() {
        UserEntity user = UserEntity.builder()
                .email("hari@gmail.com")
                .password("password")
                .name("hari mishra")
                .phone_no("+91 9999636321")
                .trustScore(8.28)
                .totalListings(1)
                .successfulTrades(0)
                .latitude(35.6895)
                .longitude(139.6917)
                .build();

        attachAvatar(
                user,
                "avatar-a.jpg",
                "avatar-a-public-id",
                "avatar-a-asset-id"
        );

        return user;
    }

    public static UserEntity CreateUserB() {
        UserEntity user = UserEntity.builder()
                .email("rama@gmail.com")
                .password("password")
                .name("rama baidya")
                .phone_no("+91 9999639872")
                .trustScore(9.56)
                .totalListings(3)
                .successfulTrades(2)
                .latitude(35.6895)
                .longitude(139.6917)
                .build();

        attachAvatar(
                user,
                "avatar-b.jpg",
                "avatar-b-public-id",
                "avatar-b-asset-id"
        );

        return user;
    }

    public static UserEntity CreateUserC() {
        UserEntity user = UserEntity.builder()
                .email("shyam@gmail.com")
                .password("password")
                .name("shaym gantayata")
                .phone_no("+91 7788866655")
                .trustScore(6.25)
                .totalListings(7)
                .successfulTrades(5)
                .latitude(35.6895)
                .longitude(139.6917)
                .build();

        attachAvatar(
                user,
                "avatar-c.jpg",
                "avatar-c-public-id",
                "avatar-c-asset-id"
        );

        return user;
    }

    public static UserAvatarEntity CreateAvatar(
            String secureUrl,
            String publicId,
            String assetId
    ) {
        return UserAvatarEntity.builder()
                .secureUrl(secureUrl)
                .publicId(publicId)
                .assetId(assetId)
                .width(500)
                .height(500)
                .format("webp")
                .bytes(10_000L)
                .status(UserAvatarStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private static void attachAvatar(
            UserEntity user,
            String secureUrl,
            String publicId,
            String assetId
    ) {
        UserAvatarEntity avatar = CreateAvatar(
                secureUrl,
                publicId,
                assetId
        );

        avatar.setUser(user);
        user.setAvatar(avatar);
    }
}