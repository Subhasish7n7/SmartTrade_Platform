package smarttrade.backend.IntegrationTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import smarttrade.backend.TestDataUtil.itemTestData;
import smarttrade.backend.TestDataUtil.userTestData;
import smarttrade.backend.entities.UserEntity;
import smarttrade.backend.entities.itemEntity;
import smarttrade.backend.service.itemService;
import smarttrade.backend.service.userService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserActionControllerIntegrationTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:15-3.4")
                    .asCompatibleSubstituteFor("postgres")
    )
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @BeforeAll
    public static void startContainer() {
        postgres.start();
    }

    private final MockMvc mockMvc;
    private final userService userService;
    private final itemService itemService;

    @Autowired
    public UserActionControllerIntegrationTest(MockMvc mockMvc,
                                               userService userService,
                                               itemService itemService) {
        this.mockMvc = mockMvc;
        this.userService = userService;
        this.itemService = itemService;
    }

    @Test
    public void testCreateCartItemIntegrationTest() throws Exception {
        UserEntity user = userService.addUser(userTestData.CreateUserA());
        UserEntity user2 = userService.addUser(userTestData.CreateUserB());
        itemEntity item = itemService.addItems(itemTestData.CreateItem1(user2));

        mockMvc.perform(
                        post("/user/{userId}/cart/{itemId}",
                                        user.getUserId(), item.getItemId())
                                .param("quantity", "3")
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(user.getUserId()))
                .andExpect(jsonPath("$.itemId").value(item.getItemId()))
                .andExpect(jsonPath("$.quantity").value(3));
    }

    @Test
    public void testRemoveCartItemIntegrationTest() throws Exception {
        UserEntity user = userService.addUser(userTestData.CreateUserA());
        itemEntity item = itemService.addItems(itemTestData.CreateItem1(user));

        // Adding first
        mockMvc.perform(post("/user/{userId}/cart/{itemId}", user.getUserId(), item.getItemId())
                        .param("quantity", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        // Then delete it
        mockMvc.perform(delete("/user/{userId}/cart/{itemId}", user.getUserId(), item.getItemId()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testGetCartItemsIntegrationTest() throws Exception {
        UserEntity user = userService.addUser(userTestData.CreateUserA());
        itemEntity item1 = itemService.addItems(itemTestData.CreateItem1(user));
        itemEntity item2 = itemService.addItems(itemTestData.CreateItem2(user));

        mockMvc.perform(post("/user/{userId}/cart/{itemId}", user.getUserId(), item1.getItemId())
                        .param("quantity", "1"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/user/{userId}/cart/{itemId}", user.getUserId(), item2.getItemId())
                        .param("quantity", "2"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/user/{userId}/cart", user.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    public void testAddWishlistItemIntegrationTest() throws Exception {
        UserEntity user = userService.addUser(userTestData.CreateUserA());
        itemEntity item = itemService.addItems(itemTestData.CreateItem1(user));

        mockMvc.perform(post("/user/{userId}/wishlist/{itemId}", user.getUserId(), item.getItemId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(user.getUserId()))
                .andExpect(jsonPath("$.itemId").value(item.getItemId()));
    }

    @Test
    public void testRemoveWishlistItemIntegrationTest() throws Exception {
        UserEntity user = userService.addUser(userTestData.CreateUserA());
        itemEntity item = itemService.addItems(itemTestData.CreateItem1(user));

        // Adding first
        mockMvc.perform(post("/user/{userId}/wishlist/{itemId}", user.getUserId(), item.getItemId()))
                .andExpect(status().isCreated());

        // Then delete
        mockMvc.perform(delete("/user/{userId}/wishlist/{itemId}", user.getUserId(), item.getItemId()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testGetWishlistItemsIntegrationTest() throws Exception {
        UserEntity user = userService.addUser(userTestData.CreateUserA());
        itemEntity item1 = itemService.addItems(itemTestData.CreateItem1(user));
        itemEntity item2 = itemService.addItems(itemTestData.CreateItem2(user));

        mockMvc.perform(post("/user/{userId}/wishlist/{itemId}", user.getUserId(), item1.getItemId()))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/user/{userId}/wishlist/{itemId}", user.getUserId(), item2.getItemId()))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/user/{userId}/wishlist", user.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}

