package smarttrade.backend.UnitTest.Item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.springframework.test.util.ReflectionTestUtils;
import smarttrade.backend.Mappers.ItemMapper;
import smarttrade.backend.dto.item.CreateItemRequest;
import smarttrade.backend.dto.item.ItemImageRequest;
import smarttrade.backend.dto.item.ItemResponse;
import smarttrade.backend.dto.item.UpdateItemRequest;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.entities.item.ItemCategory;
import smarttrade.backend.entities.item.ItemCondition;
import smarttrade.backend.entities.item.ItemEntity;
import smarttrade.backend.entities.item.ItemImageEntity;
import smarttrade.backend.entities.item.ItemImageStatus;
import smarttrade.backend.entities.item.ItemPricingStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTest {

    private ItemMapper itemMapper;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        itemMapper = new ItemMapper();
        ReflectionTestUtils.setField(itemMapper, "refreshWindowHours", 168L);

        user = UserEntity.builder()
                .userId(1L)
                .email("test@test.com")
                .password("encoded-password")
                .name("Test User")
                .trustScore(4.2)
                .totalListings(5)
                .successfulTrades(3)
                .build();
    }

    @Test
    void shouldMapEntityToResponseIncludingOnlyActiveImages() {
        LocalDateTime fetchedAt = LocalDateTime.of(2026, 9, 20, 10, 0);
        LocalDateTime createdAt = LocalDateTime.of(2026, 9, 19, 12, 0);

        ItemImageEntity second = image(2L, "second", 1, ItemImageStatus.ACTIVE, createdAt);
        ItemImageEntity first = image(1L, "first", 0, ItemImageStatus.ACTIVE, createdAt);
        ItemImageEntity deleted = image(3L, "deleted", 2, ItemImageStatus.PENDING_DELETION, createdAt);

        ItemEntity item = baseItem();
        item.setPriceFetchedAt(fetchedAt);
        item.setImages(List.of(second, deleted, first));

        ItemResponse response = itemMapper.mapFrom(item);

        assertEquals(1L, response.getItemId());
        assertEquals("Gaming Laptop", response.getItemName());
        assertEquals(ItemCategory.COMPUTERS, response.getCategory());
        assertEquals(ItemCondition.GOOD, response.getCondition());
        assertEquals(user.getUserId(), response.getSeller().getUserId());
        assertEquals(user.getName(), response.getSeller().getName());
        assertEquals(user.getTrustScore(), response.getSeller().getTrustScore());
        assertEquals(user.getTotalListings(), response.getSeller().getTotalListings());
        assertEquals(user.getSuccessfulTrades(), response.getSeller().getSuccessfulTrades());
        assertEquals(fetchedAt.plusHours(168), response.getNextPriceRefreshAt());
        assertEquals(2, response.getImages().size());
        assertEquals("first", response.getImages().get(0).getSecureUrl());
        assertEquals("second", response.getImages().get(1).getSecureUrl());
        assertEquals(20.2961, response.getLatitude(), 0.0000001);
        assertEquals(85.8245, response.getLongitude(), 0.0000001);
    }

    @Test
    void shouldMapNullEntityToNullResponse() {
        assertNull(itemMapper.mapFrom(null));
    }

    @Test
    void shouldReturnEmptyImagesWhenEntityImagesAreNull() {
        ItemEntity item = baseItem();
        item.setImages(null);

        ItemResponse response = itemMapper.mapFrom(item);

        assertNotNull(response.getImages());
        assertTrue(response.getImages().isEmpty());
    }

    @Test
    void shouldMapCreateRequestIncludingCoordinatesAndImages() {
        CreateItemRequest request = new CreateItemRequest();
        request.setItemName("Phone");
        request.setUserPrice(30000);
        request.setCategory(ItemCategory.PHONES);
        request.setCondition(ItemCondition.LIKE_NEW);
        request.setDescription("Good phone");
        request.setLabels(List.of("android", "phone"));
        request.setCity("Bhubaneswar");
        request.setState("Odisha");
        request.setLocality("Patia");
        request.setForSale(true);
        request.setForTrade(true);
        request.setLatitude(20.2961);
        request.setLongitude(85.8245);

        ItemImageRequest image = new ItemImageRequest();
        image.setSecureUrl("https://example.com/image.jpg");
        image.setPublicId("items/phone");
        image.setAssetId("asset-1");
        image.setWidth(1200);
        image.setHeight(800);
        image.setFormat("jpg");
        image.setBytes(120_000L);
        image.setSortOrder(99);
        request.setImages(List.of(image));

        ItemEntity entity = itemMapper.fromCreateRequest(request);

        assertEquals("Phone", entity.getItemName());
        assertEquals(30000, entity.getUserPrice());
        assertEquals(ItemCategory.PHONES, entity.getCategory());
        assertEquals(ItemCondition.LIKE_NEW, entity.getCondition());
        assertEquals("Good phone", entity.getDescription());
        assertEquals(List.of("android", "phone"), entity.getLabels());
        assertTrue(entity.isForSale());
        assertTrue(entity.isForTrade());
        assertEquals("Bhubaneswar", entity.getCity());
        assertEquals("Odisha", entity.getState());
        assertEquals("Patia", entity.getLocality());
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getLocation());
        assertEquals(85.8245, entity.getLocation().getX(), 0.0000001);
        assertEquals(20.2961, entity.getLocation().getY(), 0.0000001);

        assertEquals(1, entity.getImages().size());
        ItemImageEntity mappedImage = entity.getImages().get(0);
        assertSame(entity, mappedImage.getItem());
        assertEquals("asset-1", mappedImage.getAssetId());
        assertEquals(0, mappedImage.getSortOrder());
        assertEquals(ItemImageStatus.ACTIVE, mappedImage.getStatus());
        assertNotNull(mappedImage.getCreatedAt());
    }

    @Test
    void shouldNotCreateLocationWhenCoordinatesAreIncomplete() {
        CreateItemRequest request = new CreateItemRequest();
        request.setItemName("Phone");
        request.setLatitude(20.2961);

        ItemEntity entity = itemMapper.fromCreateRequest(request);

        assertNull(entity.getLocation());
    }

    @Test
    void shouldUpdateOnlyProvidedFieldsAndTrimLocationText() {
        ItemEntity item = baseItem();
        UpdateItemRequest request = new UpdateItemRequest();
        request.setItemName("Updated Laptop");
        request.setCity("  Cuttack  ");
        request.setState("  Odisha  ");
        request.setLocality("  CDA  ");
        request.setForTrade(true);
        request.setForSale(false);
        request.setLatitude(20.4625);
        request.setLongitude(85.8830);

        itemMapper.updateEntityFromRequest(request, item);

        assertEquals("Updated Laptop", item.getItemName());
        assertEquals("Cuttack", item.getCity());
        assertEquals("Odisha", item.getState());
        assertEquals("CDA", item.getLocality());
        assertTrue(item.isForTrade());
        assertFalse(item.isForSale());
        assertEquals(85.8830, item.getLocation().getX(), 0.0000001);
        assertEquals(20.4625, item.getLocation().getY(), 0.0000001);
        assertEquals(50000, item.getUserPrice());
        assertEquals(ItemCategory.COMPUTERS, item.getCategory());
    }

    @Test
    void shouldClearCoordinatesWhenRequested() {
        ItemEntity item = baseItem();
        UpdateItemRequest request = new UpdateItemRequest();
        request.setClearCoordinates(true);
        request.setLatitude(21.0);
        request.setLongitude(86.0);

        itemMapper.updateEntityFromRequest(request, item);

        assertNull(item.getLocation());
    }

    @Test
    void shouldMapCoordinatesBackToLatitudeAndLongitudeCorrectly() {
        ItemEntity item = baseItem();
        Point location = item.getLocation();

        ItemResponse response = itemMapper.mapFrom(item);

        assertEquals(location.getY(), response.getLatitude());
        assertEquals(location.getX(), response.getLongitude());
    }

    private ItemEntity baseItem() {
        Point point = ItemTestPoint.point(85.8245, 20.2961);

        return ItemEntity.builder()
                .itemId(1L)
                .itemName("Gaming Laptop")
                .newPrice(80000)
                .generatedPrice(55000)
                .userPrice(50000)
                .pricingStatus(ItemPricingStatus.READY)
                .pricingVersion(3L)
                .user(user)
                .description("Gaming laptop")
                .category(ItemCategory.COMPUTERS)
                .condition(ItemCondition.GOOD)
                .labels(List.of("gaming", "laptop"))
                .location(point)
                .city("Bhubaneswar")
                .state("Odisha")
                .locality("Patia")
                .forSale(true)
                .forTrade(false)
                .available(true)
                .createdAt(LocalDateTime.of(2026, 9, 18, 10, 0))
                .build();
    }

    private ItemImageEntity image(
            Long id,
            String secureUrl,
            int sortOrder,
            ItemImageStatus status,
            LocalDateTime createdAt
    ) {
        return ItemImageEntity.builder()
                .id(id)
                .secureUrl(secureUrl)
                .publicId("public-" + id)
                .assetId("asset-" + id)
                .width(1200)
                .height(800)
                .format("jpg")
                .bytes(100_000L)
                .sortOrder(sortOrder)
                .status(status)
                .createdAt(createdAt)
                .build();
    }

    private static final class ItemTestPoint {
        private static final org.locationtech.jts.geom.GeometryFactory FACTORY =
                new org.locationtech.jts.geom.GeometryFactory(
                        new org.locationtech.jts.geom.PrecisionModel(),
                        4326
                );

        private static Point point(double longitude, double latitude) {
            return FACTORY.createPoint(
                    new org.locationtech.jts.geom.Coordinate(longitude, latitude)
            );
        }
    }
}
