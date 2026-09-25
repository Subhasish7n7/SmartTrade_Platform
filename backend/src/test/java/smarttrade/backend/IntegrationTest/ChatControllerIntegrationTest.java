package smarttrade.backend.IntegrationTest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import smarttrade.backend.TestDataUtil.userTestData;
import smarttrade.backend.entities.chat.ChatConversationEntity;
import smarttrade.backend.entities.chat.ChatMessageEntity;
import smarttrade.backend.entities.trade.TradeEntity;
import smarttrade.backend.entities.trade.TradeStatus;
import smarttrade.backend.entities.user.UserAvatarEntity;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.repository.*;
import smarttrade.backend.security.CookieService;
import smarttrade.backend.security.JwtService;
import smarttrade.backend.service.NotificationService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class ChatControllerIntegrationTest {

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
        registry.add(
                "jwt.secret",
                () -> "thisisaverylongsecretkeyfortestingjwt123456"
        );
        registry.add("frontend.url", () -> "http://localhost:3000");
        registry.add("serp.api.key", () -> "dummy");
        registry.add("pricing.refresh-window-hours", () -> "168");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserAvatarRepo userAvatarRepo;

    @Autowired
    private ChatConversationRepo conversationRepo;

    @Autowired
    private ChatMessageRepo chatMessageRepo;

    @Autowired
    private TradeRepo tradeRepo;

    @Autowired
    private TradeOfferRepo tradeOfferRepo;

    @Autowired
    private JwtService jwtService;

    @PersistenceContext
    private EntityManager entityManager;

    @MockitoBean
    private NotificationService notificationService;


    private UserEntity user;
    private UserEntity otherUser;
    private UserEntity outsider;
    private String userToken;

    @BeforeEach
    void setUp() {
        clearChatData();

        user = saveUserWithAvatar(userTestData.CreateUserA());
        otherUser = saveUserWithAvatar(userTestData.CreateUserB());
        outsider = saveUserWithAvatar(userTestData.CreateUserC());

        userToken = jwtService.generateToken(user.getEmail());
    }

    private UserEntity saveUserWithAvatar(UserEntity user) {

        UserAvatarEntity avatar = user.getAvatar();

        /*
         * UserAvatarEntity is intentionally not cascade-persisted.
         * Save the user first.
         */
        user.setAvatar(null);

        UserEntity savedUser = userRepo.save(user);

        if (avatar != null) {
            avatar.setUser(savedUser);

            UserAvatarEntity savedAvatar =
                    userAvatarRepo.saveAndFlush(avatar);

            savedUser.setAvatar(savedAvatar);
        }

        return savedUser;
    }

    @Test
    void shouldReturnEmptyConversationWhenNoConversationExists()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/chat/"
                                        + otherUser.getUserId()
                                        + "/messages"
                        )
                                .cookie(
                                        new Cookie(
                                                CookieService.ACCESS_COOKIE,
                                                userToken
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(40));
    }

    @Test
    void shouldGetConversationMessagesAndMarkConversationRead()
            throws Exception {

        ChatConversationEntity conversation =
                conversationRepo.save(
                        ChatConversationEntity.builder()
                                .user1(user)
                                .user2(otherUser)
                                .build()
                );

        ChatMessageEntity first =
                chatMessageRepo.save(
                        ChatMessageEntity.builder()
                                .sender(otherUser)
                                .conversation(conversation)
                                .message("First")
                                .createdAt(
                                        LocalDateTime.now()
                                                .minusMinutes(2)
                                )
                                .build()
                );

        ChatMessageEntity second =
                chatMessageRepo.save(
                        ChatMessageEntity.builder()
                                .sender(otherUser)
                                .conversation(conversation)
                                .message("Second")
                                .createdAt(
                                        LocalDateTime.now()
                                                .minusMinutes(1)
                                )
                                .build()
                );

        mockMvc.perform(
                        get(
                                "/chat/"
                                        + otherUser.getUserId()
                                        + "/messages"
                        )
                                .cookie(
                                        new Cookie(
                                                CookieService.ACCESS_COOKIE,
                                                userToken
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(
                        jsonPath("$.content[0].message")
                                .value("Second")
                )
                .andExpect(
                        jsonPath("$.content[1].message")
                                .value("First")
                );

        ChatConversationEntity updated =
                conversationRepo
                        .findById(conversation.getId())
                        .orElseThrow();

        assertNotNull(updated.getUser1LastReadMessage());
        assertEquals(
                second.getId(),
                updated.getUser1LastReadMessage().getId()
        );
    }

    @Test
    void shouldGetInboxSortedByLatestMessage()
            throws Exception {

        ChatConversationEntity oldConversation =
                conversationRepo.save(
                        ChatConversationEntity.builder()
                                .user1(user)
                                .user2(otherUser)
                                .build()
                );

        UserEntity thirdUser = UserEntity.builder()
                .email("chat-third@test.com")
                .password("password")
                .name("Chat Third")
                .build();

        thirdUser = userRepo.save(thirdUser);

        ChatConversationEntity newConversation =
                conversationRepo.save(
                        ChatConversationEntity.builder()
                                .user1(user)
                                .user2(thirdUser)
                                .build()
                );

        chatMessageRepo.save(
                ChatMessageEntity.builder()
                        .sender(otherUser)
                        .conversation(oldConversation)
                        .message("Older")
                        .createdAt(
                                LocalDateTime.of(
                                        2026,
                                        9,
                                        20,
                                        10,
                                        0
                                )
                        )
                        .build()
        );

        chatMessageRepo.save(
                ChatMessageEntity.builder()
                        .sender(thirdUser)
                        .conversation(newConversation)
                        .message("Newer")
                        .createdAt(
                                LocalDateTime.of(
                                        2026,
                                        9,
                                        21,
                                        10,
                                        0
                                )
                        )
                        .build()
        );

        mockMvc.perform(
                        get("/chat/inbox")
                                .cookie(
                                        new Cookie(
                                                CookieService.ACCESS_COOKIE,
                                                userToken
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(
                        jsonPath("$[0].otherUserId")
                                .value(thirdUser.getUserId())
                )
                .andExpect(
                        jsonPath("$[0].lastMessage")
                                .value("Newer")
                )
                .andExpect(
                        jsonPath("$[1].otherUserId")
                                .value(otherUser.getUserId())
                )
                .andExpect(
                        jsonPath("$[1].lastMessage")
                                .value("Older")
                );
    }

    @Test
    void shouldPinTradeBelongingToConversation()
            throws Exception {

        TradeEntity trade =
                tradeRepo.save(
                        TradeEntity.builder()
                                .initiator(user)
                                .receiver(otherUser)
                                .status(TradeStatus.OPEN)
                                .createdAt(LocalDateTime.now())
                                .build()
                );

        mockMvc.perform(
                        put(
                                "/chat/"
                                        + otherUser.getUserId()
                                        + "/pin"
                        )
                                .with(csrfCookie())
                                .cookie(
                                        new Cookie(
                                                CookieService.ACCESS_COOKIE,
                                                userToken
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"tradeId\":"
                                                + trade.getTradeId()
                                                + "}"
                                )
                )
                .andExpect(status().isOk());

        ChatConversationEntity conversation =
                conversationRepo.findBetweenUsers(
                        user.getUserId(),
                        otherUser.getUserId()
                ).orElseThrow();

        assertNotNull(conversation.getPinnedTrade());
        assertEquals(
                trade.getTradeId(),
                conversation.getPinnedTrade().getTradeId()
        );
    }

    @Test
    void shouldRejectPinningTradeFromDifferentUsers()
            throws Exception {

        TradeEntity trade =
                tradeRepo.save(
                        TradeEntity.builder()
                                .initiator(user)
                                .receiver(outsider)
                                .status(TradeStatus.OPEN)
                                .createdAt(LocalDateTime.now())
                                .build()
                );

        mockMvc.perform(
                        put(
                                "/chat/"
                                        + otherUser.getUserId()
                                        + "/pin"
                        )
                                .with(csrfCookie())
                                .cookie(
                                        new Cookie(
                                                CookieService.ACCESS_COOKIE,
                                                userToken
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"tradeId\":"
                                                + trade.getTradeId()
                                                + "}"
                                )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectProtectedHttpEndpointWithoutJwtCookie()
            throws Exception {

        mockMvc.perform(get("/chat/inbox"))
                .andExpect(status().isForbidden());

        mockMvc.perform(
                        put("/chat/" + otherUser.getUserId() + "/pin")
                                .with(csrfCookie())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"tradeId\":1}")
                )
                .andExpect(status().isForbidden());
    }

    private void saveAvatar(UserEntity user) {
        UserAvatarEntity avatar = user.getAvatar();

        if (avatar == null) {
            return;
        }

        avatar.setUser(user);
        userAvatarRepo.save(avatar);
    }

    private void clearChatData() {
        List<ChatConversationEntity> conversations =
                conversationRepo.findAll();

        for (ChatConversationEntity conversation : conversations) {
            conversation.setUser1LastReadMessage(null);
            conversation.setUser2LastReadMessage(null);
            conversation.setPinnedTrade(null);
        }

        if (!conversations.isEmpty()) {
            conversationRepo.saveAll(conversations);
            conversationRepo.flush();
        }

        chatMessageRepo.deleteAll();
        chatMessageRepo.flush();

        conversationRepo.deleteAll();
        conversationRepo.flush();

        tradeOfferRepo.deleteAll();
        tradeRepo.deleteAll();

        userAvatarRepo.deleteAll();
        userAvatarRepo.flush();

        userRepo.deleteAll();
        userRepo.flush();
    }

    private RequestPostProcessor csrfCookie() {
        String csrfToken = UUID.randomUUID().toString();

        return request -> {
            Cookie csrfCookie =
                    new Cookie(
                            "XSRF-TOKEN",
                            csrfToken
                    );

            Cookie[] existingCookies =
                    request.getCookies();

            if (existingCookies == null
                    || existingCookies.length == 0) {

                request.setCookies(csrfCookie);

            } else {

                Cookie[] combinedCookies =
                        Arrays.copyOf(
                                existingCookies,
                                existingCookies.length + 1
                        );

                combinedCookies[existingCookies.length] =
                        csrfCookie;

                request.setCookies(combinedCookies);
            }

            request.addHeader(
                    "X-XSRF-TOKEN",
                    csrfToken
            );

            return request;
        };
    }
}