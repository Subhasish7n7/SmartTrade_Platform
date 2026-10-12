package smarttrade.backend.IntegrationTest;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import smarttrade.backend.entities.review.ReviewEntity;
import smarttrade.backend.entities.review.ReviewStatus;
import smarttrade.backend.entities.trade.DisputeReason;
import smarttrade.backend.entities.trade.DisputeResolution;
import smarttrade.backend.entities.trade.DisputeStatus;
import smarttrade.backend.entities.trade.TradeEntity;
import smarttrade.backend.entities.trade.TradeStatus;
import smarttrade.backend.entities.trust.TrustScoreHistoryEntity;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.events.trust.TrustEvent;
import smarttrade.backend.events.trust.TrustEventSourceType;
import smarttrade.backend.events.trust.TrustEventType;
import smarttrade.backend.repository.ReviewRepo;
import smarttrade.backend.repository.trades.TradeDisputeRepo;
import smarttrade.backend.repository.trades.TradeRepo;
import smarttrade.backend.repository.TrustScoreHistoryRepo;
import smarttrade.backend.repository.user.UserRepo;
import smarttrade.backend.security.CookieService;
import smarttrade.backend.security.JwtService;
import smarttrade.backend.service.ChatService;
import smarttrade.backend.service.NotificationService;
import smarttrade.backend.service.pricing.PriceEstimationService;
import smarttrade.backend.service.pricing.PricingBackgroundService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class TrustScoreIntegrationTest {

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
    private UserRepo userRepo;

    @Autowired
    private TradeRepo tradeRepo;

    @Autowired
    private TradeDisputeRepo tradeDisputeRepo;

    @Autowired
    private ReviewRepo reviewRepo;

    @Autowired
    private TrustScoreHistoryRepo trustScoreHistoryRepo;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private ChatService chatService;

    @MockitoBean
    private PriceEstimationService priceEstimationService;

    @MockitoBean
    private PricingBackgroundService pricingBackgroundService;

    private UserEntity user;
    private UserEntity reviewer;
    private UserEntity admin;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        trustScoreHistoryRepo.deleteAll();
        reviewRepo.deleteAll();
        tradeDisputeRepo.deleteAll();
        tradeRepo.deleteAll();
        userRepo.deleteAll();

        user = saveUser("trust-user@test.com", "Trust User", false);
        reviewer = saveUser("reviewer@test.com", "Reviewer", false);
        admin = saveUser("admin@test.com", "Admin", true);

        userToken = jwtService.generateToken(user.getEmail());
        adminToken = jwtService.generateToken(admin.getEmail());
    }

    @Test
    void trustEventUpdatesScoreAndPersistsExplainableHistory() {
        user.setSuccessfulTrades(1);
        userRepo.save(user);

        Long sourceId = 1001L;

        applicationEventPublisher.publishEvent(
                TrustEvent.now(
                        user.getUserId(),
                        TrustEventType.TRADE_COMPLETED,
                        TrustEventSourceType.TRADE,
                        sourceId,
                        reviewer.getUserId(),
                        "Completed trade in integration test"
                )
        );

        UserEntity updated = userRepo.findById(user.getUserId()).orElseThrow();
        assertEquals(583.33, updated.getTrustScore(), 0.01);

        TrustScoreHistoryEntity history = trustScoreHistoryRepo.findAll()
                .stream()
                .findFirst()
                .orElseThrow();

        assertEquals(user.getUserId(), history.getUser().getUserId());
        assertEquals(TrustEventType.TRADE_COMPLETED, history.getEventType());
        assertEquals(TrustEventSourceType.TRADE, history.getSourceType());
        assertEquals(sourceId, history.getSourceId());
        assertEquals(reviewer.getUserId(), history.getRelatedUserId());
        assertEquals(500.0, history.getScoreBefore(), 0.01);
        assertEquals(583.33, history.getScoreAfter(), 0.01);
        assertEquals(83.33, history.getScoreChange(), 0.01);
    }

    @Test
    void completedTradeFaultOutcomeLowersOnlyFaultUsersScore() {
        TradeEntity trade = tradeRepo.save(
                TradeEntity.builder()
                        .initiator(user)
                        .receiver(reviewer)
                        .status(TradeStatus.CANCELLED)
                        .createdAt(LocalDateTime.now())
                        .cancelledAt(LocalDateTime.now())
                        .build()
        );

        tradeDisputeRepo.save(
                smarttrade.backend.entities.trade.TradeDisputeEntity.builder()
                        .trade(trade)
                        .openedBy(reviewer)
                        .status(DisputeStatus.RESOLVED)
                        .reason(DisputeReason.ITEM_NOT_AS_DESCRIBED)
                        .resolution(DisputeResolution.CANCEL_WITH_FAULT)
                        .faultUser(user)
                        .description("Fault assigned in integration test")
                        .createdAt(LocalDateTime.now())
                        .resolvedAt(LocalDateTime.now())
                        .build()
        );

        applicationEventPublisher.publishEvent(
                TrustEvent.now(
                        user.getUserId(),
                        TrustEventType.TRADE_FAULT_ASSIGNED,
                        TrustEventSourceType.DISPUTE,
                        trade.getTradeId(),
                        reviewer.getUserId(),
                        "Fault assigned by admin"
                )
        );

        assertEquals(
                416.67,
                userRepo.findById(user.getUserId()).orElseThrow().getTrustScore(),
                0.01
        );

        assertEquals(
                500.0,
                userRepo.findById(reviewer.getUserId()).orElseThrow().getTrustScore(),
                0.01
        );
    }

    @Test
    void activeReviewsAffectTrustButHiddenReviewsDoNot() {
        TradeEntity trade1 = completedTrade(user, reviewer);
        TradeEntity trade2 = completedTrade(user, admin);

        reviewRepo.save(
                ReviewEntity.builder()
                        .trade(trade1)
                        .reviewer(reviewer)
                        .reviewee(user)
                        .rating(5)
                        .comment("Active review")
                        .status(ReviewStatus.ACTIVE)
                        .build()
        );

        reviewRepo.save(
                ReviewEntity.builder()
                        .trade(trade2)
                        .reviewer(admin)
                        .reviewee(user)
                        .rating(1)
                        .comment("Hidden review")
                        .status(ReviewStatus.HIDDEN)
                        .build()
        );

        applicationEventPublisher.publishEvent(
                TrustEvent.now(
                        user.getUserId(),
                        TrustEventType.REVIEW_RECEIVED,
                        TrustEventSourceType.REVIEW,
                        trade1.getTradeId(),
                        reviewer.getUserId(),
                        "Review evidence"
                )
        );

        assertEquals(
                575.0,
                userRepo.findById(user.getUserId()).orElseThrow().getTrustScore(),
                0.01
        );
    }

    @Test
    void repeatedProcessingWithNoChangedEvidenceDoesNotCreateExtraHistory() {
        user.setSuccessfulTrades(1);
        userRepo.save(user);

        TrustEvent event = TrustEvent.now(
                user.getUserId(),
                TrustEventType.TRADE_COMPLETED,
                TrustEventSourceType.TRADE,
                2001L,
                reviewer.getUserId(),
                "Same evidence"
        );

        applicationEventPublisher.publishEvent(event);
        applicationEventPublisher.publishEvent(event);

        assertEquals(
                583.33,
                userRepo.findById(user.getUserId()).orElseThrow().getTrustScore(),
                0.01
        );

        assertEquals(1, trustScoreHistoryRepo.findAll().size());
    }

    @Test
    void currentUserCanReadOwnTrustHistory() throws Exception {
        user.setSuccessfulTrades(1);
        userRepo.save(user);

        applicationEventPublisher.publishEvent(
                TrustEvent.now(
                        user.getUserId(),
                        TrustEventType.TRADE_COMPLETED,
                        TrustEventSourceType.TRADE,
                        3001L,
                        reviewer.getUserId(),
                        "History endpoint test"
                )
        );

        mockMvc.perform(
                        get("/trust/me/history")
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, userToken))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userId").value(user.getUserId()))
                .andExpect(jsonPath("$.content[0].eventType").value("TRADE_COMPLETED"))
                .andExpect(jsonPath("$.content[0].scoreAfter").value(583.33));
    }

    @Test
    void nonAdminCannotReadAnotherUsersTrustHistory() throws Exception {
        applicationEventPublisher.publishEvent(
                TrustEvent.now(
                        user.getUserId(),
                        TrustEventType.TRADE_COMPLETED,
                        TrustEventSourceType.TRADE,
                        3002L,
                        reviewer.getUserId(),
                        "Protected history test"
                )
        );

        mockMvc.perform(
                        get("/trust/users/" + user.getUserId() + "/history")
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, userToken))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanReadAnotherUsersTrustHistory() throws Exception {
        user.setSuccessfulTrades(1);
        userRepo.save(user);

        applicationEventPublisher.publishEvent(
                TrustEvent.now(
                        user.getUserId(),
                        TrustEventType.TRADE_COMPLETED,
                        TrustEventSourceType.TRADE,
                        3003L,
                        reviewer.getUserId(),
                        "Admin history test"
                )
        );

        mockMvc.perform(
                        get("/trust/users/" + user.getUserId() + "/history")
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, adminToken))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userId").value(user.getUserId()))
                .andExpect(jsonPath("$.content[0].eventType").value("TRADE_COMPLETED"));
    }

    private UserEntity saveUser(String email, String name, boolean isAdmin) {
        return userRepo.save(
                UserEntity.builder()
                        .email(email)
                        .password("password")
                        .name(name)
                        .trustScore(500.0)
                        .successfulTrades(0)
                        .totalListings(0)
                        .createdAt(LocalDateTime.now())
                        .role(isAdmin
                                ? smarttrade.backend.entities.user.UserRole.ADMIN
                                : smarttrade.backend.entities.user.UserRole.USER)
                        .build()
        );
    }

    private TradeEntity completedTrade(
            UserEntity tradeInitiator,
            UserEntity tradeReceiver
    ) {
        return tradeRepo.save(
                TradeEntity.builder()
                        .initiator(tradeInitiator)
                        .receiver(tradeReceiver)
                        .status(TradeStatus.COMPLETED)
                        .createdAt(LocalDateTime.now())
                        .completedAt(LocalDateTime.now())
                        .build()
        );
    }
}
