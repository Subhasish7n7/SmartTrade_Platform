package smarttrade.backend.IntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import smarttrade.backend.Mappers.TradeMapper;
import smarttrade.backend.TestDataUtil.itemTestData;
import smarttrade.backend.TestDataUtil.tradeOfferTestData;
import smarttrade.backend.TestDataUtil.userTestData;
import smarttrade.backend.dto.TradeOfferDto;
import smarttrade.backend.entities.TradeOfferEntity;
import smarttrade.backend.entities.UserEntity;
import smarttrade.backend.entities.itemEntity;
import smarttrade.backend.service.TradeService;
import smarttrade.backend.service.itemService;
import smarttrade.backend.service.userService;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class TradeControllerIntegrationTest {
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
    @Autowired
    private TradeMapper tradeMapper;
    @Autowired
    private itemService itemService;
    @Autowired
    private userService userService;
    @Autowired
    private TradeService tradeService;
    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper= new ObjectMapper();

    @Test
    public void createTradeOffer() throws Exception{
        UserEntity sender= userTestData.CreateUserA();
        UserEntity receiver= userTestData.CreateUserB();
        userService.addUser(sender);
        userService.addUser(receiver);
        itemEntity item1= itemService.addItems(itemTestData.CreateItem1(sender));
        itemEntity item2= itemService.addItems(itemTestData.CreateItem2(sender));
        itemEntity item3= itemService.addItems(itemTestData.CreateItem1(receiver));
        List<Long> senderItems=List.of(item1.getItemId(),item2.getItemId());
        List<Long> receiverItems= List.of(item3.getItemId());

        TradeOfferDto dto= tradeOfferTestData.createDummyTradeOfferDto(sender,receiver,
                senderItems,receiverItems);
        String tradeJson= objectMapper.writeValueAsString(dto);
        mockMvc.perform(
                MockMvcRequestBuilders.post("/trade/offer")
                        .contentType(MediaType.APPLICATION_JSON).
                        content(tradeJson)
        ).andExpect(status().isCreated()
                ).andExpect(jsonPath("$.senderId").value(sender.getUserId()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.receiverId").value(receiver.getUserId())).andDo(print());
    }
    @Test
    public void testUpdateTradeStatus() throws Exception {

        UserEntity sender= userTestData.CreateUserA();
        UserEntity receiver= userTestData.CreateUserB();
        userService.addUser(sender);
        userService.addUser(receiver);
        itemEntity item1= itemService.addItems(itemTestData.CreateItem1(sender));
        itemEntity item2= itemService.addItems(itemTestData.CreateItem2(sender));
        itemEntity item3= itemService.addItems(itemTestData.CreateItem1(receiver));
        List<Long> senderItems=List.of(item1.getItemId(),item2.getItemId());
        List<Long> receiverItems= List.of(item3.getItemId());

        TradeOfferDto dto= tradeOfferTestData.createDummyTradeOfferDto(sender,receiver,
                senderItems,receiverItems);
        TradeOfferEntity tradeOffer=tradeService.createTradeOffer(tradeMapper.mapToEntity(dto));
        mockMvc.perform(
                MockMvcRequestBuilders.patch("/trade/{tradeId}/status",tradeOffer.getTradeId())
                        .param("status","ACCEPTED")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

    }

}
