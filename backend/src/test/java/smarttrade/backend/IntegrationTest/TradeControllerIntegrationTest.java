package smarttrade.backend.IntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import smarttrade.backend.dto.trade.CreateBuyRequest;
import smarttrade.backend.dto.trade.CreateTradeOfferRequest;
import smarttrade.backend.entities.item.*;
import smarttrade.backend.entities.trade.TradeEntity;
import smarttrade.backend.entities.trade.TradeOfferEntity;
import smarttrade.backend.entities.trade.TradeStatus;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.repository.*;
import smarttrade.backend.security.CookieService;
import smarttrade.backend.security.JwtService;
import smarttrade.backend.service.NotificationService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class TradeControllerIntegrationTest {

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

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ItemRepo itemRepo;
    @Autowired
    private TradeRepo tradeRepo;
    @Autowired
    private TradeOfferRepo tradeOfferRepo;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private ItemImageRepository itemImageRepository;
    @Autowired
    private ChatConversationRepo conversationRepo;
    @Autowired
    private ChatMessageRepo chatMessageRepo;
    @Autowired
    private UserAvatarRepo userAvatarRepo;
    @Autowired
    private ItemImageRepository itemImageRepo;


    // Trade tests should not exercise notification persistence/WebSocket behavior.
    @MockitoBean
    private NotificationService notificationService;

    private UserEntity sender;
    private UserEntity receiver;
    private String senderToken;
    private String receiverToken;

    @BeforeEach
    void setUp() {

        itemImageRepo.deleteAll();
        chatMessageRepo.deleteAll();
        conversationRepo.deleteAll();
        tradeOfferRepo.deleteAll();
        tradeRepo.deleteAll();
        itemRepo.deleteAll();
        userAvatarRepo.deleteAll();
        userRepo.deleteAll();
        sender = userRepo.save(
                UserEntity.builder()
                        .email("sender@test.com")
                        .password("password")
                        .name("Sender")
                        .build()
        );

        receiver = userRepo.save(
                UserEntity.builder()
                        .email("receiver@test.com")
                        .password("password")
                        .name("Receiver")
                        .build()
        );

        senderToken = jwtService.generateToken(sender.getEmail());
        receiverToken = jwtService.generateToken(receiver.getEmail());
    }

    @Test
    void shouldCreateTradeOfferWithCookieAuthentication() throws Exception {
        ItemEntity senderItem = itemRepo.save(item(sender, "Laptop", 50000));
        ItemEntity receiverItem = itemRepo.save(item(receiver, "Phone", 30000));

        CreateTradeOfferRequest request = new CreateTradeOfferRequest();
        request.setReceiverId(receiver.getUserId());
        request.setSenderItemIds(List.of(senderItem.getItemId()));
        request.setReceiverItemIds(List.of(receiverItem.getItemId()));
        request.setCashAdjustment(250.0);

        mockMvc.perform(
                        post("/trade/offer")
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, senderToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.senderId").value(sender.getUserId()))
                .andExpect(jsonPath("$.receiverId").value(receiver.getUserId()))
                .andExpect(jsonPath("$.cashAdjustment").value(250.0))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.senderItems[0].itemId").value(senderItem.getItemId()))
                .andExpect(jsonPath("$.receiverItems[0].itemId").value(receiverItem.getItemId()));

        assertEquals(1, tradeRepo.count());
        assertEquals(1, tradeOfferRepo.count());
    }

    @Test
    void shouldRejectTradeOfferWithoutJwtCookie() throws Exception {
        CreateTradeOfferRequest request = new CreateTradeOfferRequest();
        request.setReceiverId(receiver.getUserId());

        mockMvc.perform(
                        post("/trade/offer")
                                .with(csrfCookie())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());

        assertEquals(0, tradeRepo.count());
        assertEquals(0, tradeOfferRepo.count());
    }

    @Test
    void shouldCreateBuyOffer() throws Exception {
        ItemEntity target = item(receiver, "Camera", 40000);
        target.setForSale(true);
        target = itemRepo.save(target);

        CreateBuyRequest request = new CreateBuyRequest();
        request.setItemId(target.getItemId());
        request.setOfferedPrice(45000.0);

        mockMvc.perform(
                        post("/trade/buy")
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, senderToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.senderId").value(sender.getUserId()))
                .andExpect(jsonPath("$.receiverId").value(receiver.getUserId()))
                .andExpect(jsonPath("$.cashAdjustment").value(45000.0))
                .andExpect(jsonPath("$.receiverItems[0].itemId").value(target.getItemId()))
                .andExpect(jsonPath("$.senderItems.length()").value(0));
    }

    @Test
    void shouldAcceptTrade() throws Exception {
        TradeEntity trade = tradeRepo.save(trade(sender, receiver, TradeStatus.OPEN));

        tradeOfferRepo.save(
                TradeOfferEntity.builder()
                        .sender(sender)
                        .receiver(receiver)
                        .senderItems(List.of())
                        .receiverItems(List.of())
                        .trade(trade)
                        .cashAdjustment(100.0)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        mockMvc.perform(
                        patch("/trade/" + trade.getTradeId() + "/accept")
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, receiverToken))
                )
                .andExpect(status().isOk());

        assertEquals(
                TradeStatus.ACCEPTED,
                tradeRepo.findById(trade.getTradeId()).orElseThrow().getStatus()
        );
    }

    @Test
    void shouldRequestCompletion() throws Exception {
        TradeEntity trade = tradeRepo.save(trade(sender, receiver, TradeStatus.ACCEPTED));

        mockMvc.perform(
                        patch("/trade/" + trade.getTradeId() + "/request-completion")
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, senderToken))
                )
                .andExpect(status().isOk());

        TradeEntity updated = tradeRepo.findById(trade.getTradeId()).orElseThrow();
        assertTrue(updated.getInitiatorCompletionConfirmed());
        assertFalse(updated.getReceiverCompletionConfirmed());
        assertNotNull(updated.getCompletionRequestedAt());
    }

    @Test
    void shouldCompleteAfterBothParticipantsConfirm() throws Exception {
        ItemEntity senderItem = itemRepo.save(item(sender, "Laptop", 50000));
        ItemEntity receiverItem = itemRepo.save(item(receiver, "Phone", 30000));

        TradeEntity trade = tradeRepo.save(trade(sender, receiver, TradeStatus.ACCEPTED));
        trade.setInitiatorCompletionConfirmed(true);
        tradeRepo.save(trade);

        tradeOfferRepo.save(
                TradeOfferEntity.builder()
                        .sender(sender)
                        .receiver(receiver)
                        .senderItems(List.of(senderItem))
                        .receiverItems(List.of(receiverItem))
                        .trade(trade)
                        .cashAdjustment(200.0)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        mockMvc.perform(
                        patch("/trade/" + trade.getTradeId() + "/confirm-completion")
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, receiverToken))
                )
                .andExpect(status().isOk());

        TradeEntity updatedTrade = tradeRepo.findById(trade.getTradeId()).orElseThrow();
        ItemEntity updatedSenderItem = itemRepo.findById(senderItem.getItemId()).orElseThrow();
        ItemEntity updatedReceiverItem = itemRepo.findById(receiverItem.getItemId()).orElseThrow();

        assertEquals(TradeStatus.COMPLETED, updatedTrade.getStatus());
        assertFalse(updatedTrade.getInitiatorCompletionConfirmed());
        assertFalse(updatedTrade.getReceiverCompletionConfirmed());

        assertFalse(updatedSenderItem.isAvailable());
        assertTrue(updatedSenderItem.isTraded());
        assertNotNull(updatedSenderItem.getCompletedAt());

        assertFalse(updatedReceiverItem.isAvailable());
        assertTrue(updatedReceiverItem.isTraded());
        assertNotNull(updatedReceiverItem.getCompletedAt());
    }

    @Test
    void shouldCancelTrade() throws Exception {
        TradeEntity trade = tradeRepo.save(trade(sender, receiver, TradeStatus.OPEN));

        mockMvc.perform(
                        patch("/trade/" + trade.getTradeId() + "/cancel")
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, senderToken))
                )
                .andExpect(status().isOk());

        assertEquals(
                TradeStatus.CANCELLED,
                tradeRepo.findById(trade.getTradeId()).orElseThrow().getStatus()
        );
    }

    @Test
    void shouldGetTradeInbox() throws Exception {
        TradeEntity trade = tradeRepo.save(trade(sender, receiver, TradeStatus.NEGOTIATING));
        ItemEntity senderItem = itemRepo.save(item(sender, "Laptop", 50000));
        ItemEntity receiverItem = itemRepo.save(item(receiver, "Phone", 30000));

        tradeOfferRepo.save(
                TradeOfferEntity.builder()
                        .sender(sender)
                        .receiver(receiver)
                        .senderItems(List.of(senderItem))
                        .receiverItems(List.of(receiverItem))
                        .trade(trade)
                        .cashAdjustment(500.0)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        mockMvc.perform(
                        get("/trade/inbox")
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, senderToken))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tradeId").value(trade.getTradeId()))
                .andExpect(jsonPath("$[0].otherUserId").value(receiver.getUserId()))
                .andExpect(jsonPath("$[0].otherUserName").value("Receiver"))
                .andExpect(jsonPath("$[0].yourItemCount").value(1))
                .andExpect(jsonPath("$[0].theirItemCount").value(1))
                .andExpect(jsonPath("$[0].cashAdjustment").value(500.0))
                .andExpect(jsonPath("$[0].latestOffer.tradeOfferId").exists());
    }

    @Test
    void shouldGetTradeDetails() throws Exception {
        TradeEntity trade = tradeRepo.save(trade(sender, receiver, TradeStatus.OPEN));
        TradeOfferEntity offer = tradeOfferRepo.save(
                TradeOfferEntity.builder()
                        .sender(sender)
                        .receiver(receiver)
                        .senderItems(List.of())
                        .receiverItems(List.of())
                        .trade(trade)
                        .cashAdjustment(100.0)
                        .createdAt(LocalDateTime.now().minusMinutes(1))
                        .build()
        );

        mockMvc.perform(
                        get("/trade/" + trade.getTradeId())
                                .param("tradeOfferId", offer.getId().toString())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, senderToken))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tradeId").value(trade.getTradeId()))
                .andExpect(jsonPath("$.initiatorId").value(sender.getUserId()))
                .andExpect(jsonPath("$.initiatorName").value("Sender"))
                .andExpect(jsonPath("$.receiverId").value(receiver.getUserId()))
                .andExpect(jsonPath("$.receiverName").value("Receiver"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.offer.tradeOfferId").value(offer.getId()))
                .andExpect(jsonPath("$.history[0].tradeOfferId").value(offer.getId()))
                .andExpect(jsonPath("$.history[0].senderId").value(sender.getUserId()))
                .andExpect(jsonPath("$.history[0].senderName").value("Sender"));
    }

    @Test
    void shouldGetTradeOfferHistory() throws Exception {
        TradeEntity trade = tradeRepo.save(trade(sender, receiver, TradeStatus.NEGOTIATING));

        TradeOfferEntity first = tradeOfferRepo.save(
                offer(sender, receiver, trade, LocalDateTime.now().minusMinutes(2), 100.0)
        );
        TradeOfferEntity second = tradeOfferRepo.save(
                offer(receiver, sender, trade, LocalDateTime.now().minusMinutes(1), 200.0)
        );

        mockMvc.perform(
                        get("/trade/" + trade.getTradeId() + "/offers")
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, senderToken))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tradeOfferId").value(first.getId()))
                .andExpect(jsonPath("$[1].tradeOfferId").value(second.getId()));
    }

    @Test
    void shouldGetTradeableItems() throws Exception {
        ItemEntity tradeable = item(sender, "Tradeable", 10000);
        tradeable.setForSale(false);
        tradeable.setForTrade(true);
        tradeable.setAvailable(true);
        tradeable.setSold(false);
        tradeable.setTraded(false);

        ItemImageEntity activeImage = ItemImageEntity.builder()
                .secureUrl("https://example.com/active.jpg")
                .sortOrder(0)
                .status(ItemImageStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        activeImage.setItem(tradeable);
        tradeable.setImages(List.of(activeImage));

        ItemEntity unavailable = item(sender, "Unavailable", 11000);
        unavailable.setForTrade(true);
        unavailable.setAvailable(false);

        ItemEntity sold = item(sender, "Sold", 12000);
        sold.setForTrade(true);
        sold.setSold(true);

        ItemEntity traded = item(sender, "Traded", 13000);
        traded.setForTrade(true);
        traded.setTraded(true);

        itemRepo.save(tradeable);
        itemRepo.save(unavailable);
        itemRepo.save(sold);
        itemRepo.save(traded);

        mockMvc.perform(
                        get("/trade/users/" + sender.getUserId() + "/tradeable-items")
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, senderToken))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].itemId").value(tradeable.getItemId()))
                .andExpect(jsonPath("$[0].name").value("Tradeable"))
                .andExpect(jsonPath("$[0].imageUrl").value("https://example.com/active.jpg"));
    }

    private UserEntity user(String email, String name) {
        return UserEntity.builder()
                .email(email)
                .password("password")
                .name(name)
                .build();
    }

    private ItemEntity item(UserEntity owner, String name, int userPrice) {
        ItemEntity item = ItemEntity.builder()
                .itemName(name)
                .newPrice(80000)
                .generatedPrice(55000)
                .userPrice(userPrice)
                .user(owner)
                .description("Trade test item")
                .category(ItemCategory.COMPUTERS)
                .condition(ItemCondition.GOOD)
                .labels(List.of("trade-test"))
                .forSale(true)
                .forTrade(true)
                .available(true)
                .sold(false)
                .traded(false)
                .createdAt(LocalDateTime.now())
                .build();

        item.setLocation(point(85.8245, 20.2961));
        return item;
    }

    private TradeEntity trade(UserEntity initiator, UserEntity receiver, TradeStatus status) {
        return TradeEntity.builder()
                .initiator(initiator)
                .receiver(receiver)
                .status(status)
                .initiatorCompletionConfirmed(false)
                .receiverCompletionConfirmed(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private TradeOfferEntity offer(
            UserEntity sender,
            UserEntity receiver,
            TradeEntity trade,
            LocalDateTime createdAt,
            Double cashAdjustment
    ) {
        return TradeOfferEntity.builder()
                .sender(sender)
                .receiver(receiver)
                .senderItems(List.of())
                .receiverItems(List.of())
                .trade(trade)
                .cashAdjustment(cashAdjustment)
                .createdAt(createdAt)
                .build();
    }

    private Point point(double longitude, double latitude) {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }

    private RequestPostProcessor csrfCookie() {
        String csrfToken = UUID.randomUUID().toString();

        return request -> {
            Cookie csrfCookie = new Cookie("XSRF-TOKEN", csrfToken);
            Cookie[] existingCookies = request.getCookies();

            if (existingCookies == null || existingCookies.length == 0) {
                request.setCookies(csrfCookie);
            } else {
                Cookie[] combinedCookies = Arrays.copyOf(existingCookies, existingCookies.length + 1);
                combinedCookies[existingCookies.length] = csrfCookie;
                request.setCookies(combinedCookies);
            }

            request.addHeader("X-XSRF-TOKEN", csrfToken);
            return request;
        };
    }
}
