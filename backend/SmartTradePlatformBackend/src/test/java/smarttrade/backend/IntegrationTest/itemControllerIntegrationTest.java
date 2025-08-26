package smarttrade.backend.IntegrationTest;

import org.testcontainers.containers.PostgreSQLContainer;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.utility.DockerImageName;
import smarttrade.backend.Mappers.itemMapperImpl;
import smarttrade.backend.TestDataUtil.itemTestData;
import smarttrade.backend.TestDataUtil.userTestData;
import smarttrade.backend.dto.itemDto;
import smarttrade.backend.entities.itemEntity;
import smarttrade.backend.entities.userEntity;
import smarttrade.backend.service.itemService;
import smarttrade.backend.service.userService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class itemControllerIntegrationTest {
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:15-3.4")
                    .asCompatibleSubstituteFor("postgres")
    )
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Set Spring datasource properties from Testcontainer dynamically
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
    private final ObjectMapper objectMapper;
    private final itemService itemService;
    private final userService userService;
    private final itemMapperImpl itemMapper;
    @Autowired
    public itemControllerIntegrationTest(MockMvc mockMvc, itemService itemService,
                                         userService userService,itemMapperImpl itemMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = new ObjectMapper();
        this.itemService=itemService;
        this.userService=userService;
        this.itemMapper=itemMapper;
    }
    @Test
    public void TestCreateItems() throws Exception {
        userEntity userEntity= userTestData.CreateUser();
        userEntity savedUser= userService.addUser(userEntity);
        itemEntity itemEntity= itemTestData.CreateItem1(savedUser);
        itemService.addItems(itemEntity);
        String item_jason= objectMapper.writeValueAsString(List.of(itemEntity));
        mockMvc.perform(
                MockMvcRequestBuilders.get("/items/search")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].item_name").value("mouse"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].category").value("electronic"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].user.name").value("hari mishra"));;

    }
    @Test
    public void TestGetAllItems() throws Exception {
        userEntity user = userTestData.CreateUser();
        userEntity savedUser=userService.addUser(user);
        itemEntity item1 = itemTestData.CreateItem1(savedUser);
        itemEntity item2 = itemTestData.CreateItem2(savedUser);
        itemService.addItems(item1);
        itemService.addItems(item2);

        mockMvc.perform(MockMvcRequestBuilders.get("/items")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2));
    }

    @Test
    public void TestGetItemById() throws Exception {
        userEntity user = userTestData.CreateUser();
        user= userService.addUser(user);
        itemEntity item = itemTestData.CreateItem1(user);
        item = itemService.addItems(item);

        mockMvc.perform(MockMvcRequestBuilders.get("/items/{item_id}", item.getItem_id())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.item_name").value("mouse"));
    }

    @Test
    public void TestUpdateItem() throws Exception {
        userEntity user = userTestData.CreateUser();
        user=userService.addUser(user);
        itemEntity item = itemTestData.CreateItem1(user);
        item = itemService.addItems(item);
        item.setItem_name("keyboard");
        item.setCategory("electronic");
        String updateJson = objectMapper.writeValueAsString(item);

        mockMvc.perform(MockMvcRequestBuilders.patch("/items/{item_id}", item.getItem_id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.item_name").value("keyboard"));
    }

    @Test
    public void TestDeleteItem() throws Exception {
        userEntity user = userTestData.CreateUser();
        user=userService.addUser(user);
        itemEntity item = itemTestData.CreateItem1(user);
        item = itemService.addItems(item);

        mockMvc.perform(MockMvcRequestBuilders.delete("/items/{item_id}", item.getItem_id()))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        // Verify deleted by trying to fetch
        mockMvc.perform(MockMvcRequestBuilders.get("/items/{item_id}", item.getItem_id()))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
    @Test
    public void testSearchItems() throws Exception {
        // Arrange: Create and save user and item
        userEntity user = userTestData.CreateUser();
        user = userService.addUser(user);

        itemEntity item = itemTestData.CreateItem1(user);
        item.setCategory("electronics");
        item.setLabels(List.of("tech", "gadget"));
        item.setItem_name("mouse");
        item = itemService.addItems(item);

        // Act & Assert: Perform search request with parameters
        mockMvc.perform(MockMvcRequestBuilders.get("/items/search")
                        .param("category", "electronics")
                        .param("labels", "tech", "gadget") // Multiple labels
                        .param("name", "mouse")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].item_name").value("mouse"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].category").value("electronics"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].labels").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].labels").value(org.hamcrest.Matchers.containsInAnyOrder("tech", "gadget")));
    }
    @Test
    public void testGetNearbyItems() throws Exception {
        // Setup: Create user
        userEntity savedUser = userService.addUser(userTestData.CreateUser());

        // Setup: Create nearby item in NYC
        itemEntity nearbyItem = itemTestData.CreateItem1(savedUser);
        nearbyItem.setLocation(new GeometryFactory(new PrecisionModel(), 4326)
                .createPoint(new Coordinate(-74.0060, 40.7128))); // lng, lat order!
        itemService.addItems(nearbyItem);

        // Setup: Create far item in Los Angeles (~3940 km away)
        itemEntity farItem = itemTestData.CreateItem2(savedUser);
        farItem.setLocation(new GeometryFactory(new PrecisionModel(), 4326)
                .createPoint(new Coordinate(-118.2437, 34.0522)));
        itemService.addItems(farItem);

        double searchLat = 40.7128;
        double searchLng = -74.0060;
        double radiusKm = 50; // 50 km radius

        mockMvc.perform(MockMvcRequestBuilders.get("/items/nearby")
                        .param("lat", String.valueOf(searchLat))
                        .param("lng", String.valueOf(searchLng))
                        .param("radiusKm", String.valueOf(radiusKm))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                // Exactly one item returned (the nearby one)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                // Check returned item latitude and longitude match nearbyItem
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].latitude").value(40.7128))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].longitude").value(-74.0060))
                // Optional: check item name or other fields
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].item_name").value(nearbyItem.getItem_name()));
    }



}


