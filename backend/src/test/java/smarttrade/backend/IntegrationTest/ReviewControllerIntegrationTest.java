package smarttrade.backend.IntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import smarttrade.backend.dto.review.CreateReviewRequest;
import smarttrade.backend.entities.review.ReviewEntity;
import smarttrade.backend.entities.trade.TradeEntity;
import smarttrade.backend.entities.trade.TradeStatus;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.repository.ReviewRepo;
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
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class ReviewControllerIntegrationTest {

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
    private TradeRepo tradeRepo;

    @Autowired
    private ReviewRepo reviewRepo;

    @Autowired
    private TrustScoreHistoryRepo trustScoreHistoryRepo;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private ChatService chatService;

    @MockitoBean
    private PriceEstimationService priceEstimationService;

    @MockitoBean
    private PricingBackgroundService pricingBackgroundService;

    private UserEntity initiator;
    private UserEntity receiver;
    private UserEntity outsider;
    private String initiatorToken;
    private String receiverToken;
    private String outsiderToken;

    @BeforeEach
    void setUp() {
        trustScoreHistoryRepo.deleteAll();
        reviewRepo.deleteAll();
        tradeRepo.deleteAll();
        userRepo.deleteAll();

        initiator = saveUser("alice@test.com", "Alice");
        receiver = saveUser("bob@test.com", "Bob");
        outsider = saveUser("outsider@test.com", "Outsider");

        initiatorToken = jwtService.generateToken(initiator.getEmail());
        receiverToken = jwtService.generateToken(receiver.getEmail());
        outsiderToken = jwtService.generateToken(outsider.getEmail());
    }

    @Test
    void shouldCreateReviewUsingJwtCookieAndCsrfCookie() throws Exception {
        TradeEntity trade = completedTrade();
        CreateReviewRequest request = new CreateReviewRequest(
                5,
                "  Great trader. Smooth exchange.  "
        );

        mockMvc.perform(
                        post("/reviews/trades/" + trade.getTradeId())
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, initiatorToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tradeId").value(trade.getTradeId()))
                .andExpect(jsonPath("$.reviewerId").value(initiator.getUserId()))
                .andExpect(jsonPath("$.revieweeId").value(receiver.getUserId()))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Great trader. Smooth exchange."))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        assertEquals(1, reviewRepo.count());

        UserEntity updatedReceiver = userRepo.findById(receiver.getUserId()).orElseThrow();
        assertEquals(575.0, updatedReceiver.getTrustScore(), 0.01);
        assertEquals(1, trustScoreHistoryRepo.findAll().size());
    }

    @Test
    void shouldRejectPostReviewWithoutCsrfEvenWithJwtCookie() throws Exception {
        TradeEntity trade = completedTrade();
        CreateReviewRequest request = new CreateReviewRequest(5, "Should fail");

        mockMvc.perform(
                        post("/reviews/trades/" + trade.getTradeId())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, initiatorToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());

        assertEquals(0, reviewRepo.count());
    }

    @Test
    void shouldRejectPostReviewWithoutJwtCookie() throws Exception {
        TradeEntity trade = completedTrade();
        CreateReviewRequest request = new CreateReviewRequest(5, "Should fail");

        mockMvc.perform(
                        post("/reviews/trades/" + trade.getTradeId())
                                .with(csrfCookie())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());

        assertEquals(0, reviewRepo.count());
    }

    @Test
    void shouldRejectReviewFromNonParticipant() throws Exception {
        TradeEntity trade = completedTrade();
        CreateReviewRequest request = new CreateReviewRequest(5, "Should fail");

        mockMvc.perform(
                        post("/reviews/trades/" + trade.getTradeId())
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, outsiderToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().is4xxClientError());

        assertEquals(0, reviewRepo.count());
    }

    @Test
    void bothParticipantsCanReviewOnce() throws Exception {
        TradeEntity trade = completedTrade();

        CreateReviewRequest initiatorReview =
                new CreateReviewRequest(5, "Excellent");

        mockMvc.perform(
                        post("/reviews/trades/" + trade.getTradeId())
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, initiatorToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(initiatorReview))
                )
                .andExpect(status().isCreated());

        CreateReviewRequest receiverReview =
                new CreateReviewRequest(4, "Good experience");

        mockMvc.perform(
                        post("/reviews/trades/" + trade.getTradeId())
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, receiverToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(receiverReview))
                )
                .andExpect(status().isCreated());

        assertEquals(2, reviewRepo.count());

        assertEquals(
                575.0,
                userRepo.findById(receiver.getUserId()).orElseThrow().getTrustScore(),
                0.01
        );

        assertEquals(
                537.5,
                userRepo.findById(initiator.getUserId()).orElseThrow().getTrustScore(),
                0.01
        );
    }

    @Test
    void duplicateReviewFromSameParticipantIsRejected() throws Exception {
        TradeEntity trade = completedTrade();
        CreateReviewRequest request = new CreateReviewRequest(5, "First review");

        mockMvc.perform(
                        post("/reviews/trades/" + trade.getTradeId())
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, initiatorToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/reviews/trades/" + trade.getTradeId())
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, initiatorToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().is4xxClientError());

        assertEquals(1, reviewRepo.count());
    }

    @Test
    void publicTradeReviewsCanBeFetchedWithoutAuthentication() throws Exception {
        TradeEntity trade = completedTrade();
        ReviewEntity review = reviewRepo.save(
                ReviewEntity.builder()
                        .trade(trade)
                        .reviewer(initiator)
                        .reviewee(receiver)
                        .rating(5)
                        .comment("Visible review")
                        .build()
        );

        mockMvc.perform(get("/reviews/trades/" + trade.getTradeId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reviewId").value(review.getReviewId()))
                .andExpect(jsonPath("$[0].reviewerId").value(initiator.getUserId()))
                .andExpect(jsonPath("$[0].revieweeId").value(receiver.getUserId()));
    }

    @Test
    void publicUserReviewsCanBeFetchedWithoutAuthentication() throws Exception {
        TradeEntity trade = completedTrade();
        ReviewEntity review = reviewRepo.save(
                ReviewEntity.builder()
                        .trade(trade)
                        .reviewer(initiator)
                        .reviewee(receiver)
                        .rating(4)
                        .comment("Good")
                        .build()
        );

        mockMvc.perform(get("/reviews/users/" + receiver.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reviewId").value(review.getReviewId()))
                .andExpect(jsonPath("$[0].revieweeId").value(receiver.getUserId()));
    }

    @Test
    void eligibilityRequiresAuthentication() throws Exception {
        TradeEntity trade = completedTrade();

        mockMvc.perform(get("/reviews/trades/" + trade.getTradeId() + "/eligibility"))
                .andExpect(status().isForbidden());
    }

    @Test
    void participantCanCheckEligibilityAndThenSeeAlreadyReviewedState() throws Exception {
        TradeEntity trade = completedTrade();

        mockMvc.perform(
                        get("/reviews/trades/" + trade.getTradeId() + "/eligibility")
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, initiatorToken))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tradeId").value(trade.getTradeId()))
                .andExpect(jsonPath("$.eligible").value(true))
                .andExpect(jsonPath("$.alreadyReviewed").value(false))
                .andExpect(jsonPath("$.revieweeId").value(receiver.getUserId()))
                .andExpect(jsonPath("$.reason").doesNotExist());

        mockMvc.perform(
                        post("/reviews/trades/" + trade.getTradeId())
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, initiatorToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new CreateReviewRequest(5, "Done")
                                ))
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        get("/reviews/trades/" + trade.getTradeId() + "/eligibility")
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, initiatorToken))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligible").value(false))
                .andExpect(jsonPath("$.alreadyReviewed").value(true))
                .andExpect(jsonPath("$.reason").value("You have already reviewed this trade."));
    }

    @Test
    void eligibilityIsFalseForIncompleteTrade() throws Exception {
        TradeEntity trade = trade(TradeStatus.ACCEPTED);

        mockMvc.perform(
                        get("/reviews/trades/" + trade.getTradeId() + "/eligibility")
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, initiatorToken))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligible").value(false))
                .andExpect(jsonPath("$.alreadyReviewed").value(false))
                .andExpect(jsonPath("$.reason").value("Trade is not completed."));
    }

    @Test
    void shouldRejectReviewForCancelledOrDisputedTrade() throws Exception {
        for (TradeStatus tradeStatus : new TradeStatus[]{
                TradeStatus.CANCELLED,
                TradeStatus.DISPUTED,
                TradeStatus.OPEN,
                TradeStatus.ACCEPTED
        }) {
            TradeEntity trade = trade(tradeStatus);
            CreateReviewRequest request = new CreateReviewRequest(5, "Should fail");

            mockMvc.perform(
                            post("/reviews/trades/" + trade.getTradeId())
                                    .with(csrfCookie())
                                    .cookie(new Cookie(CookieService.ACCESS_COOKIE, initiatorToken))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().is4xxClientError());
        }

        assertEquals(0, reviewRepo.count());
    }

    @Test
    void beanValidationRejectsInvalidRatingAndOversizedComment() throws Exception {
        TradeEntity trade = completedTrade();

        CreateReviewRequest invalidRating =
                new CreateReviewRequest(6, "Invalid rating");

        mockMvc.perform(
                        post("/reviews/trades/" + trade.getTradeId())
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, initiatorToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRating))
                )
                .andExpect(status().isBadRequest());

        CreateReviewRequest oversizedComment =
                new CreateReviewRequest(5, "x".repeat(2001));

        mockMvc.perform(
                        post("/reviews/trades/" + trade.getTradeId())
                                .with(csrfCookie())
                                .cookie(new Cookie(CookieService.ACCESS_COOKIE, initiatorToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(oversizedComment))
                )
                .andExpect(status().isBadRequest());

        assertEquals(0, reviewRepo.count());
    }

    private UserEntity saveUser(String email, String name) {
        return userRepo.save(
                UserEntity.builder()
                        .email(email)
                        .password("password")
                        .name(name)
                        .trustScore(500.0)
                        .successfulTrades(0)
                        .totalListings(0)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    private TradeEntity completedTrade() {
        return trade(TradeStatus.COMPLETED);
    }

    private TradeEntity trade(TradeStatus status) {
        return tradeRepo.save(
                TradeEntity.builder()
                        .initiator(initiator)
                        .receiver(receiver)
                        .status(status)
                        .createdAt(LocalDateTime.now())
                        .completedAt(status == TradeStatus.COMPLETED ? LocalDateTime.now() : null)
                        .build()
        );
    }

    private RequestPostProcessor csrfCookie() {
        String csrfToken = UUID.randomUUID().toString();

        return request -> {
            Cookie csrfCookie = new Cookie("XSRF-TOKEN", csrfToken);

            Cookie[] existingCookies = request.getCookies();

            if (existingCookies == null || existingCookies.length == 0) {
                request.setCookies(csrfCookie);
            } else {
                Cookie[] combinedCookies = Arrays.copyOf(
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
}
