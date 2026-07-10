package smarttrade.backend.IntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import smarttrade.backend.dto.UserDto;
import smarttrade.backend.entities.UserEntity;
import smarttrade.backend.repository.UserRepo;
import smarttrade.backend.security.JwtService;
import static org.hamcrest.Matchers.hasItem;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class UserControllerIntegrationTest {

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

        registry.add("spring.datasource.url",
                postgres::getJdbcUrl);

        registry.add("spring.datasource.username",
                postgres::getUsername);

        registry.add("spring.datasource.password",
                postgres::getPassword);

        registry.add("spring.jpa.hibernate.ddl-auto",
                () -> "create");

        registry.add(
                "spring.jpa.properties.hibernate.dialect",
                () -> "org.hibernate.spatial.dialect.postgis.PostgisPG95Dialect"
        );

        registry.add("frontend.url",
                () -> "http://localhost:3000");

        registry.add("jwt.secret",
                () -> "testsecretkeytestsecretkey123456789");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private JwtService jwtService;

    private String token;

    @BeforeEach
    void setup() {

        userRepo.deleteAll();

        UserEntity authUser = userRepo.save(
                UserEntity.builder()
                        .email("auth@test.com")
                        .password("pass")
                        .name("Auth")
                        .build()
        );

        token = jwtService.generateToken(authUser.getEmail());
    }

    @Test
    void shouldCreateUser() throws Exception {

        UserDto request = new UserDto();

        request.setEmail("test@test.com");
        request.setName("Test User");
        request.setPassword("pass123");

        mockMvc.perform(
                        post("/users")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email")
                        .value("test@test.com"))
                .andExpect(jsonPath("$.name")
                        .value("Test User"));
    }

    @Test
    void shouldRejectInvalidEmail() throws Exception {

        UserDto request = new UserDto();

        request.setEmail("invalid-email");
        request.setName("Test");

        mockMvc.perform(
                        post("/users")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetAllUsers() throws Exception {

        userRepo.save(
                UserEntity.builder()
                        .email("user@test.com")
                        .name("User")
                        .password("pass")
                        .build()
        );

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email", hasItem("user@test.com")));
    }

    @Test
    void shouldGetSingleUser() throws Exception {

        UserEntity user = userRepo.save(
                UserEntity.builder()
                        .email("single@test.com")
                        .name("Single")
                        .password("pass")
                        .build()
        );

        mockMvc.perform(
                        get("/users/" + user.getUserId())
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email")
                        .value("single@test.com"));
    }

    @Test
    void shouldReturnNotFoundForMissingUser()
            throws Exception {

        mockMvc.perform(get("/users/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateUser() throws Exception {

        UserEntity user = userRepo.save(
                UserEntity.builder()
                        .email("before@test.com")
                        .name("Before")
                        .password("pass")
                        .build()
        );

        UserDto request = new UserDto();

        request.setEmail("after@test.com");
        request.setName("After");
        request.setPassword("updatedpass");


        mockMvc.perform(
                        patch("/users/" + user.getUserId())
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email")
                        .value("after@test.com"))
                .andExpect(jsonPath("$.name")
                        .value("After"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingUser()
            throws Exception {

        UserDto request = new UserDto();

        request.setEmail("missing@test.com");
        request.setName("Missing");

        mockMvc.perform(
                        patch("/users/999")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteUser() throws Exception {

        UserEntity user = userRepo.save(
                UserEntity.builder()
                        .email("delete@test.com")
                        .name("Delete")
                        .password("pass")
                        .build()
        );

        mockMvc.perform(
                        delete("/users/" + user.getUserId())
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isNoContent());

        assert(userRepo.findById(user.getUserId()).isEmpty());
    }
}