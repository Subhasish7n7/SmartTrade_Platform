package smarttrade.backend.IntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import smarttrade.backend.dto.item.CreateItemRequest;
import smarttrade.backend.dto.item.ItemSearchRequest;
import smarttrade.backend.dto.item.ItemSort;
import smarttrade.backend.dto.item.UpdateItemRequest;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.entities.item.ItemCategory;
import smarttrade.backend.entities.item.ItemCondition;
import smarttrade.backend.entities.item.ItemEntity;
import smarttrade.backend.entities.item.ItemPricingStatus;
import smarttrade.backend.entities.item.ItemSpecification;
import smarttrade.backend.entities.item.SpecificationTier;
import smarttrade.backend.repository.TrustScoreHistoryRepo;
import smarttrade.backend.repository.item.ItemRepo;
import smarttrade.backend.repository.user.UserRepo;
import smarttrade.backend.security.CookieService;
import smarttrade.backend.security.JwtService;
import smarttrade.backend.service.pricing.PriceEstimationService;
import smarttrade.backend.service.pricing.PricingBackgroundService;
import smarttrade.backend.service.pricing.PricingRefreshOutcome;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class ItemControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    DockerImageName.parse("postgis/postgis:16-3.4")
                            .asCompatibleSubstituteFor("postgres")
            )
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
        registry.add(
                "spring.jpa.properties.hibernate.dialect",
                () -> "org.hibernate.spatial.dialect.postgis.PostgisPG95Dialect"
        );
        registry.add("jwt.secret", () -> "thisisaverylongsecretkeyfortestingjwt123456");
        registry.add("frontend.url", () -> "http://localhost:3000");
        registry.add("serp.api.key", () -> "dummy");
        registry.add("pricing.refresh-window-hours", () -> "168");
    }

    private RequestPostProcessor csrfCookie() {
        String csrfToken = UUID.randomUUID().toString();

        return request -> {
            Cookie csrfCookie =
                    new Cookie("XSRF-TOKEN", csrfToken);

            Cookie[] existingCookies = request.getCookies();

            if (existingCookies == null || existingCookies.length == 0) {
                request.setCookies(csrfCookie);
            } else {
                Cookie[] combinedCookies =
                        Arrays.copyOf(
                                existingCookies,
                                existingCookies.length + 1
                        );

                combinedCookies[existingCookies.length] = csrfCookie;

                request.setCookies(combinedCookies);
            }

            request.addHeader("X-XSRF-TOKEN", csrfToken);

            return request;
        };
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRepo itemRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TrustScoreHistoryRepo trustScoreHistoryRepo;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private PricingBackgroundService pricingBackgroundService;

    @MockitoBean
    private PriceEstimationService priceEstimationService;

    private UserEntity user;
    private UserEntity otherUser;
    private String token;
    private String otherToken;

    @BeforeEach
    void setUp() {
        itemRepo.deleteAll();
        // Trust history rows reference users and must be removed first.
        trustScoreHistoryRepo.deleteAll();
        userRepo.deleteAll();

        user = userRepo.save(
                UserEntity.builder()
                        .email("test@test.com")
                        .password("password")
                        .name("Test User")
                        .build()
        );

        otherUser = userRepo.save(
                UserEntity.builder()
                        .email("other@test.com")
                        .password("password")
                        .name("Other User")
                        .build()
        );

        token = jwtService.generateToken(user.getEmail());
        otherToken = jwtService.generateToken(otherUser.getEmail());
    }

    @Test
    void shouldGetAllItems() throws Exception {
        itemRepo.save(createItem(user, "Laptop", 50000, ItemCategory.COMPUTERS, ItemCondition.GOOD));

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].itemName").value("Laptop"))
                .andExpect(jsonPath("$[0].seller.userId").value(user.getUserId()))
                .andExpect(jsonPath("$[0].seller.name").value("Test User"));
    }

    @Test
    void shouldGetItemById() throws Exception {
        ItemEntity saved = itemRepo.save(
                createItem(user, "Phone", 30000, ItemCategory.PHONES, ItemCondition.LIKE_NEW)
        );

        mockMvc.perform(get("/items/" + saved.getItemId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId").value(saved.getItemId()))
                .andExpect(jsonPath("$.itemName").value("Phone"))
                .andExpect(jsonPath("$.category").value("PHONES"))
                .andExpect(jsonPath("$.condition").value("LIKE_NEW"));
    }

    @Test
    void shouldReturnNotFoundForUnknownItem() throws Exception {
        mockMvc.perform(get("/items/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetUserInventory() throws Exception {
        itemRepo.save(createItem(user, "Laptop", 50000, ItemCategory.COMPUTERS, ItemCondition.GOOD));
        itemRepo.save(createItem(user, "Phone", 30000, ItemCategory.PHONES, ItemCondition.NEW));
        itemRepo.save(createItem(otherUser, "Camera", 40000, ItemCategory.CAMERAS, ItemCondition.GOOD));

        mockMvc.perform(
                        get("/items/user/" + user.getUserId())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, token)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seller.userId").value(user.getUserId()))
                .andExpect(jsonPath("$[1].seller.userId").value(user.getUserId()))
                .andExpect(jsonPath("$[2]").doesNotExist());
    }

    @Test
    void shouldCreateItem() throws Exception {
        CreateItemRequest request = new CreateItemRequest();

        request.setItemName("Phone");
        request.setUserPrice(50000);
        request.setCategory(ItemCategory.PHONES);
        request.setCondition(ItemCondition.GOOD);
        request.setDescription("Good condition phone");
        request.setLabels(List.of("phone", "android"));
        request.setCity("Bhubaneswar");
        request.setState("Odisha");
        request.setLocality("Patia");
        request.setLatitude(20.2961);
        request.setLongitude(85.8245);

        mockMvc.perform(
                        post("/items")
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, token))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemName").value("Phone"))
                .andExpect(jsonPath("$.userPrice").value(50000))
                .andExpect(jsonPath("$.category").value("PHONES"))
                .andExpect(jsonPath("$.condition").value("GOOD"))
                .andExpect(jsonPath("$.pricingStatus").value("PENDING"))
                .andExpect(jsonPath("$.latitude").value(20.2961))
                .andExpect(jsonPath("$.longitude").value(85.8245))
                .andExpect(jsonPath("$.seller.userId").value(user.getUserId()));

        List<ItemEntity> items = itemRepo.findAll();

        assertTrue(
                items.stream()
                        .anyMatch(i -> "Phone".equals(i.getItemName()))
        );
    }

    @Test
    void shouldRejectCreateWithoutJwt() throws Exception {
        CreateItemRequest request = validCreateRequest();

        mockMvc.perform(
                        post("/items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectCreateWithInvalidRequiredFields() throws Exception {
        CreateItemRequest request = new CreateItemRequest();
        request.setItemName("Phone");

        mockMvc.perform(
                        post("/items")
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, token))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateOwnedItem() throws Exception {
        ItemEntity saved = itemRepo.save(
                createItem(user, "Laptop", 50000, ItemCategory.COMPUTERS, ItemCondition.GOOD)
        );

        UpdateItemRequest request = new UpdateItemRequest();
        request.setItemName("Updated Laptop");
        request.setDescription("Updated description");

        mockMvc.perform(
                        patch("/items/" + saved.getItemId())
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, token))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemName").value("Updated Laptop"))
                .andExpect(jsonPath("$.description").value("Updated description"));

        ItemEntity updated = itemRepo.findById(saved.getItemId()).orElseThrow();
        assertTrue("Updated Laptop".equals(updated.getItemName()));
        assertTrue("Updated description".equals(updated.getDescription()));
    }

    @Test
    void shouldUpdateAndPersistSpecificationsAcrossAllTiers() throws Exception {
        ItemEntity saved = itemRepo.save(
                createItem(user, "ASUS Laptop", 50000, ItemCategory.COMPUTERS, ItemCondition.GOOD)
        );

        UpdateItemRequest request = new UpdateItemRequest();
        request.setPrimarySpecifications(List.of(" RTX 4060 ", "Ryzen 9", "rtx 4060"));
        request.setSecondarySpecifications(List.of("1TB SSD", "165Hz"));
        request.setTertiarySpecifications(List.of("WiFi 6"));

        mockMvc.perform(
                        patch("/items/" + saved.getItemId())
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, token))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pricingStatus").value(ItemPricingStatus.PENDING.name()));

        ItemEntity updated = itemRepo.findById(saved.getItemId()).orElseThrow();
        assertEquals(2L, updated.getPricingVersion());

        assertEquals(
                List.of("RTX 4060", "Ryzen 9"),
                specificationValuesFromDatabase(saved.getItemId(), SpecificationTier.PRIMARY)
        );
        assertEquals(
                List.of("1TB SSD", "165Hz"),
                specificationValuesFromDatabase(saved.getItemId(), SpecificationTier.SECONDARY)
        );
        assertEquals(
                List.of("WiFi 6"),
                specificationValuesFromDatabase(saved.getItemId(), SpecificationTier.TERTIARY)
        );
    }

    @Test
    void shouldLeaveSpecificationsUnchangedWhenTierFieldsAreNull() throws Exception {
        ItemEntity saved = createItem(
                user, "ASUS Laptop", 50000, ItemCategory.COMPUTERS, ItemCondition.GOOD
        );
        setExistingSpecifications(saved);
        saved = itemRepo.save(saved);
        Long itemId = saved.getItemId();

        UpdateItemRequest request = new UpdateItemRequest();
        request.setDescription("Updated description only");
        // All specification tier fields are intentionally null: null means unchanged.

        mockMvc.perform(
                        patch("/items/" + itemId)
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, token))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated description only"))
                .andExpect(jsonPath("$.pricingStatus").value(ItemPricingStatus.READY.name()));

        ItemEntity updated = itemRepo.findById(itemId).orElseThrow();
        assertEquals(1L, updated.getPricingVersion());
        assertEquals(List.of("RTX 4060", "Ryzen 9"),
                specificationValuesFromDatabase(itemId, SpecificationTier.PRIMARY));
        assertEquals(List.of("1TB SSD"),
                specificationValuesFromDatabase(itemId, SpecificationTier.SECONDARY));
        assertEquals(List.of("WiFi 6"),
                specificationValuesFromDatabase(itemId, SpecificationTier.TERTIARY));
    }

    @Test
    void shouldClearOnlySpecificationTierSuppliedAsEmptyList() throws Exception {
        ItemEntity saved = createItem(
                user, "ASUS Laptop", 50000, ItemCategory.COMPUTERS, ItemCondition.GOOD
        );
        setExistingSpecifications(saved);
        saved = itemRepo.save(saved);
        Long itemId = saved.getItemId();

        UpdateItemRequest request = new UpdateItemRequest();
        request.setPrimarySpecifications(List.of());
        // Secondary and tertiary remain null, so those tiers must be preserved.

        mockMvc.perform(
                        patch("/items/" + itemId)
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, token))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pricingStatus").value(ItemPricingStatus.PENDING.name()));

        ItemEntity updated = itemRepo.findById(itemId).orElseThrow();
        assertEquals(2L, updated.getPricingVersion());
        assertEquals(List.of(), specificationValuesFromDatabase(itemId, SpecificationTier.PRIMARY));
        assertEquals(List.of("1TB SSD"),
                specificationValuesFromDatabase(itemId, SpecificationTier.SECONDARY));
        assertEquals(List.of("WiFi 6"),
                specificationValuesFromDatabase(itemId, SpecificationTier.TERTIARY));
    }

    @Test
    void shouldRejectSpecificationListsThatExceedTierLimits() throws Exception {
        ItemEntity saved = itemRepo.save(
                createItem(user, "ASUS Laptop", 50000, ItemCategory.COMPUTERS, ItemCondition.GOOD)
        );

        UpdateItemRequest tooManyPrimary = new UpdateItemRequest();
        tooManyPrimary.setPrimarySpecifications(
                List.of("P1", "P2", "P3", "P4", "P5", "P6")
        );
        mockMvc.perform(
                        patch("/items/" + saved.getItemId())
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, token))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tooManyPrimary))
                )
                .andExpect(status().isBadRequest());

        UpdateItemRequest tooManySecondary = new UpdateItemRequest();
        tooManySecondary.setSecondarySpecifications(
                List.of("S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8", "S9")
        );
        mockMvc.perform(
                        patch("/items/" + saved.getItemId())
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, token))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tooManySecondary))
                )
                .andExpect(status().isBadRequest());

        UpdateItemRequest tooManyTertiary = new UpdateItemRequest();
        tooManyTertiary.setTertiarySpecifications(
                List.of("T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11")
        );
        mockMvc.perform(
                        patch("/items/" + saved.getItemId())
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, token))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tooManyTertiary))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectUpdateByDifferentUser() throws Exception {
        ItemEntity saved = itemRepo.save(
                createItem(user, "Laptop", 50000, ItemCategory.COMPUTERS, ItemCondition.GOOD)
        );

        UpdateItemRequest request = new UpdateItemRequest();
        request.setItemName("Hijacked Laptop");

        mockMvc.perform(
                        patch("/items/" + saved.getItemId())
                                .header("Authorization", "Bearer " + otherToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldDeleteOwnedItem() throws Exception {
        ItemEntity saved = itemRepo.save(
                createItem(user, "Laptop", 50000, ItemCategory.COMPUTERS, ItemCondition.GOOD)
        );

        mockMvc.perform(
                        delete("/items/" + saved.getItemId())
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, token))
                )
                .andExpect(status().isNoContent());

        assertTrue(itemRepo.findById(saved.getItemId()).isEmpty());
    }

    @Test
    void shouldRejectDeleteByDifferentUser() throws Exception {
        ItemEntity saved = itemRepo.save(
                createItem(user, "Laptop", 50000, ItemCategory.COMPUTERS, ItemCondition.GOOD)
        );

        mockMvc.perform(
                        delete("/items/" + saved.getItemId())
                                .header("Authorization", "Bearer " + otherToken)
                )
                .andExpect(status().isForbidden());

        assertTrue(itemRepo.findById(saved.getItemId()).isPresent());
    }

    @Test
    void shouldToggleAvailability() throws Exception {
        ItemEntity saved = itemRepo.save(
                createItem(user, "Laptop", 50000, ItemCategory.COMPUTERS, ItemCondition.GOOD)
        );

        mockMvc.perform(
                        patch("/items/" + saved.getItemId() + "/availability")
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, token))
                                .param("available", "false")
                )
                .andExpect(status().isOk());

        ItemEntity updated = itemRepo.findById(saved.getItemId()).orElseThrow();
        assertFalse(updated.isAvailable());
    }

    @Test
    void shouldRejectAvailabilityChangeByDifferentUser() throws Exception {
        ItemEntity saved = itemRepo.save(
                createItem(user, "Laptop", 50000, ItemCategory.COMPUTERS, ItemCondition.GOOD)
        );

        mockMvc.perform(
                        patch("/items/" + saved.getItemId() + "/availability")
                                .param("available", "false")
                                .header("Authorization", "Bearer " + otherToken)
                )
                .andExpect(status().isForbidden());

        assertTrue(itemRepo.findById(saved.getItemId()).orElseThrow().isAvailable());
    }

    @Test
    void shouldSearchItemsWithNameAndPriceFilters() throws Exception {
        ItemEntity laptop = createItem(user, "Gaming Laptop", 50000, ItemCategory.COMPUTERS, ItemCondition.GOOD);
        laptop.setLabels(List.of("gaming", "laptop"));
        itemRepo.save(laptop);

        ItemEntity phone = createItem(user, "Phone", 30000, ItemCategory.PHONES, ItemCondition.NEW);
        phone.setLabels(List.of("phone"));
        itemRepo.save(phone);

        ItemSearchRequest request = new ItemSearchRequest();
        request.setName("gaming");
        request.setMinPrice(40000);
        request.setMaxPrice(60000);
        request.setSort(ItemSort.PRICE_LOW_TO_HIGH);
        request.setPage(0);
        request.setSize(10);

        mockMvc.perform(
                        post("/items/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].itemName").value("Gaming Laptop"))
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void shouldSearchItemsByListingType() throws Exception {
        ItemEntity sale = createItem(user, "Sale Item", 10000, ItemCategory.OTHER, ItemCondition.GOOD);
        sale.setCustomCategory("Sale Category");
        sale.setForSale(true);
        sale.setForTrade(false);
        itemRepo.save(sale);

        ItemEntity trade = createItem(user, "Trade Item", 10000, ItemCategory.OTHER, ItemCondition.GOOD);
        trade.setCustomCategory("Trade Category");
        trade.setForSale(false);
        trade.setForTrade(true);
        itemRepo.save(trade);

        ItemSearchRequest request = new ItemSearchRequest();
        request.setListingType(smarttrade.backend.entities.item.ListingType.TRADE);

        mockMvc.perform(
                        post("/items/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].itemName").value("Trade Item"));
    }

    @Test
    void shouldSearchItemsByDistanceWithRadius() throws Exception {
        ItemEntity nearby = createItem(user, "Nearby", 20000, ItemCategory.PHONES, ItemCondition.GOOD);
        nearby.setLocation(point(85.8245, 20.2961));
        itemRepo.save(nearby);

        ItemEntity farAway = createItem(user, "Far Away", 20000, ItemCategory.PHONES, ItemCondition.GOOD);
        farAway.setLocation(point(-74.0060, 40.7128));
        itemRepo.save(farAway);

        ItemSearchRequest request = new ItemSearchRequest();
        request.setLatitude(20.2961);
        request.setLongitude(85.8245);
        request.setRadiusKm(5.0);
        request.setSort(ItemSort.DISTANCE);

        mockMvc.perform(
                        post("/items/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].itemName").value("Nearby"));
    }

    @Test
    void shouldRejectDistanceSearchWithoutCoordinates() throws Exception {
        ItemSearchRequest request = new ItemSearchRequest();
        request.setSort(ItemSort.DISTANCE);

        mockMvc.perform(
                        post("/items/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRefreshPriceAndReturnAccepted() throws Exception {
        ItemEntity saved = itemRepo.save(
                createItem(user, "Laptop", 50000, ItemCategory.COMPUTERS, ItemCondition.GOOD)
        );
        saved.setPricingStatus(ItemPricingStatus.PENDING);
        saved.setPriceFetchedAt(null);
        itemRepo.save(saved);

        mockMvc.perform(
                        post("/items/" + saved.getItemId() + "/price/refresh")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.itemId").value(saved.getItemId()))
                .andExpect(jsonPath("$.outcome").value(PricingRefreshOutcome.STARTED.name()))
                .andExpect(jsonPath("$.pricingStatus").value(ItemPricingStatus.IN_PROGRESS.name()))
                .andExpect(jsonPath("$.pricingVersion").value(1));

        verify(pricingBackgroundService).execute(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldReturnNotFoundWhenRefreshingUnknownItemPrice() throws Exception {
        mockMvc.perform(
                        post("/items/999999/price/refresh")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestForInvalidPriceRange() throws Exception {
        ItemSearchRequest request = new ItemSearchRequest();
        request.setMinPrice(50000);
        request.setMaxPrice(10000);

        mockMvc.perform(
                        post("/items/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    private CreateItemRequest validCreateRequest() {
        CreateItemRequest request = new CreateItemRequest();
        request.setItemName("Laptop");
        request.setUserPrice(50000);
        request.setCategory(ItemCategory.COMPUTERS);
        request.setCondition(ItemCondition.GOOD);
        return request;
    }

    private ItemEntity createItem(
            UserEntity owner,
            String name,
            int userPrice,
            ItemCategory category,
            ItemCondition condition
    ) {
        ItemEntity item = ItemEntity.builder()
                .itemName(name)
                .newPrice(80000)
                .generatedPrice(55000)
                .userPrice(userPrice)
                .pricingStatus(ItemPricingStatus.READY)
                .priceFetchedAt(LocalDateTime.now().minusHours(1))
                .pricingVersion(1L)
                .user(owner)
                .description("Test item")
                .category(category)
                .condition(condition)
                .labels(List.of("test"))
                .available(true)
                .forSale(true)
                .forTrade(false)
                .createdAt(LocalDateTime.now())
                .build();

        item.setLocation(point(85.8245, 20.2961));
        return item;
    }

    private void setExistingSpecifications(ItemEntity item) {
        item.setSpecifications(new java.util.ArrayList<>(List.of(
                new ItemSpecification(SpecificationTier.PRIMARY, "RTX 4060", 0),
                new ItemSpecification(SpecificationTier.PRIMARY, "Ryzen 9", 1),
                new ItemSpecification(SpecificationTier.SECONDARY, "1TB SSD", 2),
                new ItemSpecification(SpecificationTier.TERTIARY, "WiFi 6", 3)
        )));
    }

    /**
     * Load specification values inside a transaction because ItemEntity.specifications
     * is a lazy collection. The returned strings are materialized before the session closes.
     */
    private List<String> specificationValuesFromDatabase(
            Long itemId,
            SpecificationTier tier
    ) {
        return new TransactionTemplate(transactionManager).execute(status -> {
            ItemEntity persisted = itemRepo.findById(itemId).orElseThrow();
            return specificationValues(persisted, tier);
        });
    }

    private List<String> specificationValues(ItemEntity item, SpecificationTier tier) {
        if (item.getSpecifications() == null) {
            return List.of();
        }

        return item.getSpecifications().stream()
                .filter(specification -> specification.getTier() == tier)
                .sorted(Comparator.comparingInt(ItemSpecification::getSortOrder))
                .map(ItemSpecification::getSpecification)
                .toList();
    }

    private Point point(double longitude, double latitude) {
        GeometryFactory geometryFactory =
                new GeometryFactory(new PrecisionModel(), 4326);
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }
}
