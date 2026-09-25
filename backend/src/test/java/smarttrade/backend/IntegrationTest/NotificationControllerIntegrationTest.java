package smarttrade.backend.IntegrationTest;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.entities.notification.NotificationEntity;
import smarttrade.backend.entities.notification.NotificationResourceType;
import smarttrade.backend.entities.notification.NotificationType;
import smarttrade.backend.repository.NotificationRepo;
import smarttrade.backend.repository.UserRepo;
import smarttrade.backend.security.CookieService;
import smarttrade.backend.security.JwtService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class NotificationControllerIntegrationTest {

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
        registry.add("jwt.secret",
                () -> "thisisaverylongsecretkeyfortestingjwt123456");
        registry.add("frontend.url",
                () -> "http://localhost:3000");
        registry.add("serp.api.key", () -> "dummy");
        registry.add("pricing.refresh-window-hours", () -> "168");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private NotificationRepo notificationRepo;

    @Autowired
    private JwtService jwtService;

    private UserEntity currentUser;
    private UserEntity otherUser;
    private String token;

    @BeforeEach
    void setUp() {
        notificationRepo.deleteAll();
        userRepo.deleteAll();

        currentUser = userRepo.save(
                UserEntity.builder()
                        .email("notification-user@test.com")
                        .password("password")
                        .name("Notification User")
                        .build()
        );

        otherUser = userRepo.save(
                UserEntity.builder()
                        .email("notification-actor@test.com")
                        .password("password")
                        .name("Notification Actor")
                        .build()
        );

        token = jwtService.generateToken(currentUser.getEmail());
    }

    @Test
    void shouldGetNotifications() throws Exception {
        NotificationEntity notification = saveNotification(
                NotificationType.TRADE_ACCEPTED,
                NotificationResourceType.TRADE,
                100L,
                otherUser,
                "Notification Actor accepted your trade.",
                false
        );

        mockMvc.perform(
                        get("/notifications")
                                .cookie(new Cookie(
                                        CookieService.ACCESS_COOKIE,
                                        token
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(notification.getId()))
                .andExpect(jsonPath("$.content[0].type").value("TRADE_ACCEPTED"))
                .andExpect(jsonPath("$.content[0].resourceType").value("TRADE"))
                .andExpect(jsonPath("$.content[0].resourceId").value(100))
                .andExpect(jsonPath("$.content[0].actorId").value(otherUser.getUserId()))
                .andExpect(jsonPath("$.content[0].actorName").value("Notification Actor"))
                .andExpect(jsonPath("$.content[0].message")
                        .value("Notification Actor accepted your trade."))
                .andExpect(jsonPath("$.content[0].read").value(false));
    }

    @Test
    void shouldGetUnreadCountAndCounts() throws Exception {
        saveNotification(
                NotificationType.TRADE_ACCEPTED,
                NotificationResourceType.TRADE,
                100L,
                otherUser,
                "Trade alert",
                false
        );

        saveNotification(
                NotificationType.CHAT_MESSAGE,
                NotificationResourceType.CHAT_CONVERSATION,
                200L,
                otherUser,
                "Chat alert",
                false
        );

        saveNotification(
                NotificationType.SYSTEM,
                NotificationResourceType.SYSTEM,
                300L,
                null,
                "Already read",
                true
        );

        mockMvc.perform(
                        get("/notifications/unread-count")
                                .cookie(new Cookie(
                                        CookieService.ACCESS_COOKIE,
                                        token
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(content().string("2"));

        mockMvc.perform(
                        get("/notifications/counts")
                                .cookie(new Cookie(
                                        CookieService.ACCESS_COOKIE,
                                        token
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(2))
                .andExpect(jsonPath("$.tradeAlertCount").value(1))
                .andExpect(jsonPath("$.chatAlertCount").value(1));
    }

    @Test
    void shouldMarkNotificationRead() throws Exception {
        NotificationEntity notification = saveNotification(
                NotificationType.TRADE_ACCEPTED,
                NotificationResourceType.TRADE,
                100L,
                otherUser,
                "Trade alert",
                false
        );

        mockMvc.perform(
                        patch("/notifications/" + notification.getId() + "/read")
                                .with(csrfCookie())
                                .cookie(new Cookie(
                                        CookieService.ACCESS_COOKIE,
                                        token
                                ))
                )
                .andExpect(status().isOk());

        NotificationEntity updated =
                notificationRepo.findById(notification.getId()).orElseThrow();

        assertTrue(updated.isRead());
        assertNotNull(updated.getReadAt());
    }

    @Test
    void shouldMarkModuleRead() throws Exception {
        NotificationEntity tradeNotification = saveNotification(
                NotificationType.TRADE_ACCEPTED,
                NotificationResourceType.TRADE,
                100L,
                otherUser,
                "Trade alert",
                false
        );

        NotificationEntity chatNotification = saveNotification(
                NotificationType.CHAT_MESSAGE,
                NotificationResourceType.CHAT_CONVERSATION,
                200L,
                otherUser,
                "Chat alert",
                false
        );

        mockMvc.perform(
                        patch("/notifications/module-read")
                                .with(csrfCookie())
                                .cookie(new Cookie(
                                        CookieService.ACCESS_COOKIE,
                                        token
                                ))
                                .param(
                                        "resourceType",
                                        NotificationResourceType.TRADE.name()
                                )
                )
                .andExpect(status().isOk());

        assertTrue(
                notificationRepo.findById(tradeNotification.getId())
                        .orElseThrow()
                        .isRead()
        );

        assertFalse(
                notificationRepo.findById(chatNotification.getId())
                        .orElseThrow()
                        .isRead()
        );
    }

    @Test
    void shouldMarkAllNotificationsRead() throws Exception {
        NotificationEntity first = saveNotification(
                NotificationType.TRADE_ACCEPTED,
                NotificationResourceType.TRADE,
                100L,
                otherUser,
                "Trade alert",
                false
        );

        NotificationEntity second = saveNotification(
                NotificationType.CHAT_MESSAGE,
                NotificationResourceType.CHAT_CONVERSATION,
                200L,
                otherUser,
                "Chat alert",
                false
        );

        mockMvc.perform(
                        patch("/notifications/read-all")
                                .with(csrfCookie())
                                .cookie(new Cookie(
                                        CookieService.ACCESS_COOKIE,
                                        token
                                ))
                )
                .andExpect(status().isOk());

        assertTrue(notificationRepo.findById(first.getId()).orElseThrow().isRead());
        assertTrue(notificationRepo.findById(second.getId()).orElseThrow().isRead());
    }

    @Test
    void shouldDeleteNotification() throws Exception {
        NotificationEntity notification = saveNotification(
                NotificationType.TRADE_ACCEPTED,
                NotificationResourceType.TRADE,
                100L,
                otherUser,
                "Trade alert",
                false
        );

        mockMvc.perform(
                        delete("/notifications/" + notification.getId())
                                .with(csrfCookie())
                                .cookie(new Cookie(
                                        CookieService.ACCESS_COOKIE,
                                        token
                                ))
                )
                .andExpect(status().isOk());

        assertTrue(notificationRepo.findById(notification.getId()).isEmpty());
    }

    @Test
    void shouldNotDeleteAnotherUsersNotification() throws Exception {
        NotificationEntity notification = NotificationEntity.builder()
                .recipient(otherUser)
                .actor(currentUser)
                .type(NotificationType.TRADE_ACCEPTED)
                .resourceType(NotificationResourceType.TRADE)
                .resourceId(100L)
                .message("Other user's notification")
                .isRead(false)
                .build();

        notification = notificationRepo.save(notification);

        mockMvc.perform(
                        delete("/notifications/" + notification.getId())
                                .with(csrfCookie())
                                .cookie(new Cookie(
                                        CookieService.ACCESS_COOKIE,
                                        token
                                ))
                )
                .andExpect(status().isOk());

        assertTrue(notificationRepo.findById(notification.getId()).isPresent());
    }

    @Test
    void shouldRejectNotificationEndpointsWithoutJwtCookie() throws Exception {
        mockMvc.perform(get("/notifications/unread-count"))
                .andExpect(status().isForbidden());

        mockMvc.perform(
                        patch("/notifications/read-all")
                                .with(csrfCookie())
                )
                .andExpect(status().isForbidden());
    }

    private NotificationEntity saveNotification(
            NotificationType type,
            NotificationResourceType resourceType,
            Long resourceId,
            UserEntity actor,
            String message,
            boolean read
    ) {
        NotificationEntity notification = NotificationEntity.builder()
                .recipient(currentUser)
                .actor(actor)
                .type(type)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .message(message)
                .isRead(read)
                .createdAt(LocalDateTime.now().minusMinutes(1))
                .build();

        return notificationRepo.save(notification);
    }

    private RequestPostProcessor csrfCookie() {
        String csrfToken = UUID.randomUUID().toString();

        return request -> {
            Cookie csrfCookie = new Cookie("XSRF-TOKEN", csrfToken);

            Cookie[] existingCookies = request.getCookies();

            if (existingCookies == null || existingCookies.length == 0) {
                request.setCookies(csrfCookie);
            } else {
                Cookie[] combinedCookies =
                        Arrays.copyOf(existingCookies, existingCookies.length + 1);

                combinedCookies[existingCookies.length] = csrfCookie;
                request.setCookies(combinedCookies);
            }

            request.addHeader("X-XSRF-TOKEN", csrfToken);
            return request;
        };
    }
}
