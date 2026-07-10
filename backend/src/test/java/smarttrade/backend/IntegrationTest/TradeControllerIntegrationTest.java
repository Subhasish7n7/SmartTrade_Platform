package smarttrade.backend.IntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;
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
import smarttrade.backend.dto.trade.CreateBuyRequest;
import smarttrade.backend.dto.trade.CreateTradeOfferRequest;
import smarttrade.backend.entities.*;
import smarttrade.backend.repository.ItemRepo;
import smarttrade.backend.repository.TradeOfferRepo;
import smarttrade.backend.repository.TradeRepo;
import smarttrade.backend.repository.UserRepo;
import smarttrade.backend.security.JwtService;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class TradeControllerIntegrationTest {

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

        registry.add("jwt.secret",
                () -> "thisisaverylongsecretkeyfortestingjwt123456");

        registry.add("frontend.url",
                () -> "http://localhost:3000");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ItemRepo itemRepo;

    @Autowired
    private TradeRepo tradeRepo;

    @Autowired
    private TradeOfferRepo tradeOfferRepo;

    @Autowired
    private JwtService jwtService;

    private UserEntity sender;
    private UserEntity receiver;

    private String senderToken;

    @BeforeEach
    void setup() {

        tradeOfferRepo.deleteAll();
        tradeRepo.deleteAll();
        itemRepo.deleteAll();
        userRepo.deleteAll();

        sender = userRepo.save(
                UserEntity.builder()
                        .email("sender@test.com")
                        .password("pass")
                        .name("Sender")
                        .build()
        );

        receiver = userRepo.save(
                UserEntity.builder()
                        .email("receiver@test.com")
                        .password("pass")
                        .name("Receiver")
                        .build()
        );

        senderToken =
                jwtService.generateToken(sender.getEmail());
    }

    @Test
    void shouldCreateTradeOffer() throws Exception {

        ItemEntity senderItem =
                itemRepo.save(createItem(sender));

        ItemEntity receiverItem =
                itemRepo.save(createItem(receiver));

        CreateTradeOfferRequest request =
                new CreateTradeOfferRequest();

        request.setReceiverId(receiver.getUserId());

        request.setSenderItemIds(
                List.of(senderItem.getItemId()));

        request.setReceiverItemIds(
                List.of(receiverItem.getItemId()));

        request.setCashAdjustment(100.0);

        mockMvc.perform(
                        post("/trade/offer")
                                .header("Authorization",
                                        "Bearer " + senderToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.senderId")
                        .value(sender.getUserId()));
    }

    @Test
    void shouldRejectUnauthenticatedTradeOffer()
            throws Exception {

        CreateTradeOfferRequest request =
                new CreateTradeOfferRequest();

        mockMvc.perform(
                        post("/trade/offer")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldCreateBuyOffer() throws Exception {

        ItemEntity item =
                createItem(receiver);

        item.setForSale(true);

        item = itemRepo.save(item);

        CreateBuyRequest request =
                new CreateBuyRequest();

        request.setItemId(item.getItemId());

        request.setOfferedPrice(500.0);

        mockMvc.perform(
                        post("/trade/buy")
                                .header("Authorization",
                                        "Bearer " + senderToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cashAdjustment")
                        .value(500.0));
    }

    @Test
    void shouldAcceptTrade() throws Exception {

        TradeEntity trade =
                tradeRepo.save(
                        TradeEntity.builder()
                                .initiator(sender)
                                .receiver(receiver)
                                .status(TradeStatus.OPEN)
                                .build()
                );

        tradeOfferRepo.save(
                TradeOfferEntity.builder()
                        .trade(trade)
                        .sender(sender)
                        .receiver(receiver)
                        .senderItems(List.of())
                        .receiverItems(List.of())
                        .build()
        );

        String receiverToken =
                jwtService.generateToken(receiver.getEmail());

        mockMvc.perform(
                        patch("/trade/" + trade.getTradeId() + "/accept")
                                .header("Authorization",
                                        "Bearer " + receiverToken)
                )
                .andExpect(status().isOk());
    }

    private ItemEntity createItem(UserEntity user) {

        GeometryFactory geometryFactory =
                new GeometryFactory(
                        new PrecisionModel(),
                        4326
                );

        Point point =
                geometryFactory.createPoint(
                        new Coordinate(85.8245, 20.2961)
                );

        return ItemEntity.builder()
                .itemName("Laptop")
                .user(user)
                .available(true)
                .locked(false)
                .forSale(true)
                .location(point)
                .build();
    }
}