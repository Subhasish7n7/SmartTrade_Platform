package smarttrade.backend.UnitTest.Trade;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smarttrade.backend.Mappers.TradeMapper;
import smarttrade.backend.dto.trade.TradeDetailsResponse;
import smarttrade.backend.dto.trade.TradeHistoryResponse;
import smarttrade.backend.dto.trade.TradeInboxResponse;
import smarttrade.backend.dto.trade.TradeOfferResponse;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.entities.item.ItemCondition;
import smarttrade.backend.entities.item.ItemEntity;
import smarttrade.backend.entities.item.ItemImageEntity;
import smarttrade.backend.entities.item.ItemImageStatus;
import smarttrade.backend.entities.trade.TradeEntity;
import smarttrade.backend.entities.trade.TradeOfferEntity;
import smarttrade.backend.entities.trade.TradeStatus;
import smarttrade.backend.repository.trades.TradeOfferRepo;
import smarttrade.backend.security.AuthenticatedUserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TradeMapperTest {

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @Mock
    private TradeOfferRepo tradeOfferRepo;

    @InjectMocks
    private TradeMapper tradeMapper;

    @Test
    void shouldReturnNullForNullTradeOffer() {
        assertNull(tradeMapper.mapFromEntity(null));
    }

    @Test
    void shouldMapTradeOfferAndChooseLowestActiveImage() {
        UserEntity sender = user(1L, "sender@test.com", "Sender");
        UserEntity receiver = user(2L, "receiver@test.com", "Receiver");

        ItemImageEntity pending = image(1L, "pending.jpg", 0, ItemImageStatus.PENDING_DELETION);
        ItemImageEntity activeHigher = image(2L, "higher.jpg", 5, ItemImageStatus.ACTIVE);
        ItemImageEntity activeLowest = image(3L, "lowest.jpg", 2, ItemImageStatus.ACTIVE);

        ItemEntity senderItem = item(10L, "Laptop", sender, 50000, 55000, ItemCondition.GOOD);
        senderItem.setImages(List.of(pending, activeHigher, activeLowest));

        ItemEntity receiverItem = item(20L, "Phone", receiver, 30000, 32000, ItemCondition.NEW);

        LocalDateTime createdAt = LocalDateTime.now();
        TradeEntity trade = TradeEntity.builder()
                .tradeId(100L)
                .initiator(sender)
                .receiver(receiver)
                .status(TradeStatus.NEGOTIATING)
                .createdAt(createdAt.minusMinutes(5))
                .build();

        TradeOfferEntity offer = TradeOfferEntity.builder()
                .id(200L)
                .sender(sender)
                .receiver(receiver)
                .senderItems(List.of(senderItem))
                .receiverItems(List.of(receiverItem))
                .trade(trade)
                .cashAdjustment(750.0)
                .createdAt(createdAt)
                .build();

        TradeOfferResponse response = tradeMapper.mapFromEntity(offer);

        assertEquals(200L, response.getTradeOfferId());
        assertEquals(1L, response.getSenderId());
        assertEquals(2L, response.getReceiverId());
        assertEquals(750.0, response.getCashAdjustment());
        assertEquals("NEGOTIATING", response.getStatus());
        assertEquals(createdAt, response.getCreatedAt());

        assertEquals(1, response.getSenderItems().size());
        assertEquals("lowest.jpg", response.getSenderItems().get(0).getImageUrl());
        assertEquals(50000, response.getSenderItems().get(0).getUserPrice());
        assertEquals(55000, response.getSenderItems().get(0).getSystemGeneratedPrice());
        assertEquals(ItemCondition.GOOD, response.getSenderItems().get(0).getCondition());

        assertEquals(1, response.getReceiverItems().size());
        assertNull(response.getReceiverItems().get(0).getImageUrl());
    }

    @Test
    void shouldMapInboxForInitiatorPerspective() {
        UserEntity currentUser = user(1L, "sender@test.com", "Sender");
        UserEntity otherUser = user(2L, "receiver@test.com", "Receiver");

        ItemEntity yourItem = item(10L, "Laptop", currentUser, 50000, 55000, ItemCondition.GOOD);
        ItemEntity theirItem = item(20L, "Phone", otherUser, 30000, 32000, ItemCondition.NEW);

        TradeEntity trade = TradeEntity.builder()
                .tradeId(100L)
                .initiator(currentUser)
                .receiver(otherUser)
                .status(TradeStatus.OPEN)
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .build();

        LocalDateTime offerTime = LocalDateTime.now();
        TradeOfferEntity offer = TradeOfferEntity.builder()
                .id(200L)
                .sender(currentUser)
                .receiver(otherUser)
                .senderItems(List.of(yourItem))
                .receiverItems(List.of(theirItem))
                .trade(trade)
                .cashAdjustment(500.0)
                .createdAt(offerTime)
                .build();

        TradeOfferResponse latest = tradeMapper.mapFromEntity(offer);
        TradeInboxResponse response = tradeMapper.mapInboxTrade(trade, currentUser, offer);

        assertEquals(100L, response.getTradeId());
        assertEquals(2L, response.getOtherUserId());
        assertEquals("Receiver", response.getOtherUserName());
        assertEquals("OPEN", response.getStatus());
        assertEquals(1, response.getYourItemCount());
        assertEquals(1, response.getTheirItemCount());
        assertEquals(500.0, response.getCashAdjustment());
        assertEquals(offerTime, response.getUpdatedAt());
        assertEquals(latest, response.getLatestOffer());
    }

    @Test
    void shouldMapInboxForReceiverPerspective() {
        UserEntity initiator = user(1L, "sender@test.com", "Sender");
        UserEntity currentUser = user(2L, "receiver@test.com", "Receiver");

        ItemEntity senderItem = item(10L, "Laptop", initiator, 50000, 55000, ItemCondition.GOOD);
        ItemEntity receiverItem = item(20L, "Phone", currentUser, 30000, 32000, ItemCondition.NEW);

        TradeEntity trade = TradeEntity.builder()
                .tradeId(100L)
                .initiator(initiator)
                .receiver(currentUser)
                .status(TradeStatus.NEGOTIATING)
                .createdAt(LocalDateTime.now())
                .build();

        TradeOfferEntity offer = TradeOfferEntity.builder()
                .id(200L)
                .sender(initiator)
                .receiver(currentUser)
                .senderItems(List.of(senderItem))
                .receiverItems(List.of(receiverItem))
                .trade(trade)
                .cashAdjustment(100.0)
                .createdAt(LocalDateTime.now())
                .build();

        TradeInboxResponse response = tradeMapper.mapInboxTrade(trade, currentUser, offer);

        assertEquals(1L, response.getOtherUserId());
        assertEquals("Sender", response.getOtherUserName());
        assertEquals(1, response.getYourItemCount());
        assertEquals(1, response.getTheirItemCount());
        assertEquals(100.0, response.getCashAdjustment());
    }

    @Test
    void shouldMapTradeDetails() {
        UserEntity initiator = user(1L, "sender@test.com", "Sender");
        UserEntity receiver = user(2L, "receiver@test.com", "Receiver");
        LocalDateTime createdAt = LocalDateTime.now();

        TradeEntity trade = TradeEntity.builder()
                .tradeId(100L)
                .initiator(initiator)
                .receiver(receiver)
                .status(TradeStatus.ACCEPTED)
                .createdAt(createdAt)
                .build();

        TradeOfferResponse offer = TradeOfferResponse.builder().tradeOfferId(200L).build();
        List<TradeHistoryResponse> history = List.of(
                TradeHistoryResponse.builder()
                        .tradeOfferId(200L)
                        .senderId(1L)
                        .senderName("Sender")
                        .createdAt(createdAt)
                        .build()
        );

        TradeDetailsResponse response = tradeMapper.mapTradeDetails(trade, offer, history);

        assertEquals(100L, response.getTradeId());
        assertEquals(1L, response.getInitiatorId());
        assertEquals("Sender", response.getInitiatorName());
        assertEquals(2L, response.getReceiverId());
        assertEquals("Receiver", response.getReceiverName());
        assertEquals("ACCEPTED", response.getStatus());
        assertEquals(createdAt, response.getCreatedAt());
        assertSame(offer, response.getOffer());
        assertSame(history, response.getHistory());
    }

    private UserEntity user(Long id, String email, String name) {
        return UserEntity.builder()
                .userId(id)
                .email(email)
                .name(name)
                .password("password")
                .build();
    }

    private ItemEntity item(
            Long id,
            String name,
            UserEntity owner,
            Integer userPrice,
            Integer generatedPrice,
            ItemCondition condition
    ) {
        return ItemEntity.builder()
                .itemId(id)
                .itemName(name)
                .user(owner)
                .userPrice(userPrice)
                .generatedPrice(generatedPrice)
                .condition(condition)
                .images(List.of())
                .build();
    }

    private ItemImageEntity image(
            Long id,
            String url,
            int sortOrder,
            ItemImageStatus status
    ) {
        return ItemImageEntity.builder()
                .id(id)
                .secureUrl(url)
                .sortOrder(sortOrder)
                .status(status)
                .build();
    }
}
