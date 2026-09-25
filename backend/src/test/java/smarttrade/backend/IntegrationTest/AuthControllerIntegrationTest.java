package smarttrade.backend.IntegrationTest;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import smarttrade.backend.TestDataUtil.userTestData;
import smarttrade.backend.entities.user.UserAvatarEntity;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.repository.UserAvatarRepo;
import smarttrade.backend.repository.UserRepo;
import smarttrade.backend.security.CookieService;
import smarttrade.backend.security.JwtService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class AuthControllerIntegrationTest {

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
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userAvatarRepo.deleteAll();
        userAvatarRepo.flush();

        userRepo.deleteAll();
        userRepo.flush();
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
    void shouldRegisterUserAndIssueHttpOnlyJwtCookie() throws Exception {
        mockMvc.perform(
                        post("/auth/register")
                                .contentType("application/json")
                                .content("""
                                        {
                                          "email": "register@test.com",
                                          "name": "Register User",
                                          "password": "password",
                                          "phone_no": "9876543210"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(cookie().exists(CookieService.ACCESS_COOKIE));

        Optional<UserEntity> saved =
                userRepo.findByEmail("register@test.com");

        assertTrue(saved.isPresent());
        assertEquals("Register User", saved.get().getName());
        assertEquals("9876543210", saved.get().getPhone_no());
        assertNotEquals("password", saved.get().getPassword());

        assertTrue(
                passwordEncoder.matches(
                        "password",
                        saved.get().getPassword()
                )
        );
    }

    @Test
    void shouldRejectDuplicateRegistration() throws Exception {
        userRepo.save(
                UserEntity.builder()
                        .email("duplicate@test.com")
                        .name("Existing")
                        .password("password")
                        .build()
        );

        mockMvc.perform(
                        post("/auth/register")
                                .contentType("application/json")
                                .content("""
                                        {
                                          "email": "duplicate@test.com",
                                          "name": "Another",
                                          "password": "password"
                                        }
                                        """)
                )
                .andExpect(status().is5xxServerError());

        assertEquals(
                1,
                userRepo.findAll().stream()
                        .filter(u -> "duplicate@test.com".equals(u.getEmail()))
                        .count()
        );
    }

    @Test
    void shouldLoginAndIssueJwtCookie() throws Exception {
        userRepo.save(
                UserEntity.builder()
                        .email("login@test.com")
                        .name("Login User")
                        .password(passwordEncoder.encode("password"))
                        .build()
        );

        mockMvc.perform(
                        post("/auth/login")
                                .contentType("application/json")
                                .content("""
                                        {
                                          "email": "login@test.com",
                                          "password": "password"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(cookie().exists(CookieService.ACCESS_COOKIE));
    }

    @Test
    void shouldRejectLoginWithInvalidCredentials() throws Exception {
        userRepo.save(
                UserEntity.builder()
                        .email("invalid@test.com")
                        .name("Invalid")
                        .password(passwordEncoder.encode("password"))
                        .build()
        );

        mockMvc.perform(
                        post("/auth/login")
                                .contentType("application/json")
                                .content("""
                                        {
                                          "email": "invalid@test.com",
                                          "password": "wrong-password"
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldIssueCsrfToken() throws Exception {
        mockMvc.perform(get("/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void shouldGetCurrentUserUsingJwtCookie() throws Exception {
        UserEntity user = userTestData.CreateUserA();

        user.setPassword(
                passwordEncoder.encode("password")
        );

        user = saveUserWithAvatar(user);

        String token = jwtService.generateToken(user.getEmail());

        mockMvc.perform(
                        get("/auth/me")
                                .cookie(
                                        new Cookie(
                                                CookieService.ACCESS_COOKIE,
                                                token
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getUserId()))
                .andExpect(jsonPath("$.name").value(user.getName()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.phoneNo").value(user.getPhone_no()))
                .andExpect(jsonPath("$.avatarUrl")
                        .value(user.getAvatar().getSecureUrl()));
    }

    @Test
    void shouldRejectCurrentUserWithoutJwtCookie() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldLogoutAndClearJwtCookie() throws Exception {
        UserEntity user =
                userRepo.save(
                        UserEntity.builder()
                                .email("logout@test.com")
                                .name("Logout User")
                                .password(
                                        passwordEncoder.encode("password")
                                )
                                .build()
                );

        String token = jwtService.generateToken(user.getEmail());

        mockMvc.perform(
                        post("/auth/logout")
                                .with(
                                        org.springframework.security
                                                .test.web.servlet
                                                .request.SecurityMockMvcRequestPostProcessors
                                                .csrf()
                                )
                                .cookie(
                                        new Cookie(
                                                CookieService.ACCESS_COOKIE,
                                                token
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(cookie().exists(CookieService.ACCESS_COOKIE));
    }
}