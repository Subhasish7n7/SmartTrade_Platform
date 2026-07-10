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
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import smarttrade.backend.dto.item.CreateItemRequest;
import smarttrade.backend.entities.ItemEntity;
import smarttrade.backend.entities.UserEntity;
import smarttrade.backend.repository.ItemRepo;
import smarttrade.backend.repository.UserRepo;
import smarttrade.backend.security.JwtService;
import smarttrade.backend.service.PriceEstimationService;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class ItemControllerIntegrationTest {


    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgis/postgis:16-3.4")
                    .asCompatibleSubstituteFor("postgres"))
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

        registry.add("frontend.url", () -> "http://localhost:3000");

        registry.add("price.api.enabled", () -> "false");

        registry.add("serp.api.key", () -> "dummy");
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
    private JwtService jwtService;

    @MockitoBean
    private PriceEstimationService priceEstimationService;

    private String token;

    private UserEntity user;

    @BeforeEach
    void setup() {

        itemRepo.deleteAll();
        userRepo.deleteAll();

        user = userRepo.save(
                UserEntity.builder()
                        .email("test@test.com")
                        .password("password")
                        .name("Test User")
                        .build()
        );

        token = jwtService.generateToken(user.getEmail());

        when(priceEstimationService.fetchEstimatedNewPrice(anyString(), anyInt()))
                .thenReturn(1000);

        when(priceEstimationService.generateUsedPrice(any(), any()))
                .thenReturn(700);
    }

    @Test
    void shouldGetAllItems() throws Exception {

        itemRepo.save(createItem());

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].itemName").value("Laptop"));
    }

    @Test
    void shouldCreateItem() throws Exception {

        CreateItemRequest request = new CreateItemRequest();

        request.setItemName("Phone");
        request.setUserPrice(500);
        request.setCondition("used - good");
        request.setCategory("electronics");

        mockMvc.perform(
                        post("/items")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemName").value("Phone"));
    }

    @Test
    void shouldRejectCreateWithoutJwt() throws Exception {

        CreateItemRequest request = new CreateItemRequest();

        request.setItemName("Phone");

        mockMvc.perform(
                        post("/items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldDeleteOwnedItem() throws Exception {

        ItemEntity saved = itemRepo.save(createItem());

        mockMvc.perform(
                        delete("/items/" + saved.getItemId())
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldToggleAvailability() throws Exception {

        ItemEntity saved = itemRepo.save(createItem());

        mockMvc.perform(
                        patch("/items/" + saved.getItemId() + "/availability")
                                .param("available", "false")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk());

        ItemEntity updated =
                itemRepo.findById(saved.getItemId()).orElseThrow();

        assert !updated.isAvailable();
    }

    private ItemEntity createItem() {

        GeometryFactory geometryFactory =
                new GeometryFactory(new PrecisionModel(), 4326);

        Point point =
                geometryFactory.createPoint(
                        new Coordinate(85.8245, 20.2961)
                );

        return itemRepo.save(
                ItemEntity.builder()
                        .itemName("Laptop")
                        .user(user)
                        .category("electronics")
                        .condition("used - good")
                        .available(true)
                        .location(point)
                        .build()
        );
    }


}
