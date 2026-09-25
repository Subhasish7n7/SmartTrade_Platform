package smarttrade.backend.TestDataUtil;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.entities.item.ItemCategory;
import smarttrade.backend.entities.item.ItemCondition;
import smarttrade.backend.entities.item.ItemEntity;
import smarttrade.backend.entities.item.ItemPricingStatus;

import java.time.LocalDateTime;
import java.util.List;

public final class ItemTestData {

    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);

    private ItemTestData() {
    }

    public static UserEntity user(Long id, String email, String name) {
        return UserEntity.builder()
                .userId(id)
                .email(email)
                .password("encoded-password")
                .name(name)
                .trustScore(4.5)
                .totalListings(3)
                .successfulTrades(2)
                .build();
    }

    public static ItemEntity computer(UserEntity user) {
        return item(
                user,
                "Gaming Laptop",
                50000,
                ItemCategory.COMPUTERS,
                ItemCondition.GOOD,
                85.8245,
                20.2961
        );
    }

    public static ItemEntity phone(UserEntity user) {
        return item(
                user,
                "Phone",
                30000,
                ItemCategory.PHONES,
                ItemCondition.LIKE_NEW,
                85.8245,
                20.2961
        );
    }

    public static ItemEntity item(
            UserEntity user,
            String name,
            int userPrice,
            ItemCategory category,
            ItemCondition condition,
            double longitude,
            double latitude
    ) {
        return ItemEntity.builder()
                .itemName(name)
                .newPrice(80000)
                .generatedPrice(55000)
                .userPrice(userPrice)
                .pricingStatus(ItemPricingStatus.READY)
                .priceFetchedAt(LocalDateTime.now().minusHours(1))
                .pricingVersion(1L)
                .user(user)
                .description("Test item")
                .category(category)
                .condition(condition)
                .labels(List.of("test", "listing"))
                .location(point(longitude, latitude))
                .city("Bhubaneswar")
                .state("Odisha")
                .locality("Patia")
                .available(true)
                .forSale(true)
                .forTrade(false)
                .sold(false)
                .traded(false)
                .locked(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Point point(double longitude, double latitude) {
        return GEOMETRY_FACTORY.createPoint(
                new Coordinate(longitude, latitude)
        );
    }
}
