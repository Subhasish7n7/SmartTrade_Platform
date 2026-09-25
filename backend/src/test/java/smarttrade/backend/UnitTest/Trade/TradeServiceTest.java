package smarttrade.backend.UnitTest.Trade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smarttrade.backend.Mappers.TradeMapper;
import smarttrade.backend.dto.trade.*;
import smarttrade.backend.entities.item.ItemCondition;
import smarttrade.backend.entities.item.ItemEntity;
import smarttrade.backend.entities.item.ItemImageEntity;
import smarttrade.backend.entities.item.ItemImageStatus;
import smarttrade.backend.entities.trade.TradeEntity;
import smarttrade.backend.entities.trade.TradeOfferEntity;
import smarttrade.backend.entities.trade.TradeStatus;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.repository.ItemRepo;
import smarttrade.backend.repository.TradeOfferRepo;
import smarttrade.backend.repository.TradeRepo;
import smarttrade.backend.repository.UserRepo;
import smarttrade.backend.security.AuthenticatedUserService;
import smarttrade.backend.service.ChatService;
import smarttrade.backend.service.NotificationService;
import smarttrade.backend.service.TradeService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TradeServiceTest {

    @Mock
    private TradeOfferRepo tradeOfferRepo;
    @Mock
    private TradeRepo tradeRepo;
    @Mock
    private ItemRepo itemRepo;
    @Mock
    private UserRepo userRepo;
    @Mock
    private AuthenticatedUserService authenticatedUserService;
    @Mock
    private TradeMapper tradeMapper;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ChatService chatService;

    @InjectMocks
    private TradeService tradeService;

    private UserEntity sender;
    private UserEntity receiver;
    private UserEntity outsider;

    @BeforeEach
    void setUp() {
        sender = user(1L, "sender@test.com", "Sender");
        receiver = user(2L, "receiver@test.com", "Receiver");
        outsider = user(3L, "outsider@test.com", "Outsider");
    }

    @Test
    void shouldCreateNewTradeOffer() {
        ItemEntity senderItem = item(11L, sender);
        ItemEntity receiverItem = item(22L, receiver);

        CreateTradeOfferRequest request = new CreateTradeOfferRequest();
        request.setReceiverId(2L);
        request.setSenderItemIds(List.of(11L));
        request.setReceiverItemIds(List.of(22L));
        request.setCashAdjustment(200.0);
        request.setInitialMessage(
                "Hi! I've sent you a trade offer. Let me know what you think."
        );

        when(authenticatedUserService.getCurrentUser()).thenReturn(sender);
        when(userRepo.findById(2L)).thenReturn(Optional.of(receiver));
        when(itemRepo.findAllById(List.of(11L))).thenReturn(List.of(senderItem));
        when(itemRepo.findAllById(List.of(22L))).thenReturn(List.of(receiverItem));
        when(tradeRepo.save(any(TradeEntity.class))).thenAnswer(invocation -> {
            TradeEntity trade = invocation.getArgument(0);
            trade.setTradeId(100L);
            return trade;
        });
        when(tradeOfferRepo.save(any(TradeOfferEntity.class))).thenAnswer(invocation -> {
            TradeOfferEntity offer = invocation.getArgument(0);
            offer.setId(200L);
            return offer;
        });
        when(tradeOfferRepo.countByTrade_TradeId(100L)).thenReturn(1L);

        TradeOfferEntity result = tradeService.createTradeOffer(request);

        assertEquals(200L, result.getId());
        assertSame(sender, result.getSender());
        assertSame(receiver, result.getReceiver());
        assertEquals(List.of(senderItem), result.getSenderItems());
        assertEquals(List.of(receiverItem), result.getReceiverItems());
        assertEquals(200.0, result.getCashAdjustment());
        assertEquals(TradeStatus.OPEN, result.getTrade().getStatus());

        verify(notificationService).notifyTradeOfferReceived(sender, receiver, 100L, false);
        verify(chatService).sendMessage(
                receiver.getUserId(),
                "Hi! I've sent you a trade offer. Let me know what you think."
        );
    }

    @Test
    void shouldCreateCounterOfferAndSetTradeToNegotiating() {
        ItemEntity senderItem = item(11L, receiver);
        ItemEntity receiverItem = item(22L, sender);
        TradeEntity trade = trade(100L, sender, receiver, TradeStatus.OPEN);

        CreateTradeOfferRequest request = new CreateTradeOfferRequest();
        request.setReceiverId(sender.getUserId());
        request.setSenderItemIds(List.of(11L));
        request.setReceiverItemIds(List.of(22L));
        request.setCashAdjustment(50.0);
        request.setTradeId(100L);
        request.setInitialMessage("I can offer this instead. What do you think?");

        when(authenticatedUserService.getCurrentUser()).thenReturn(receiver);
        when(userRepo.findById(1L)).thenReturn(Optional.of(sender));
        when(itemRepo.findAllById(List.of(11L))).thenReturn(List.of(senderItem));
        when(itemRepo.findAllById(List.of(22L))).thenReturn(List.of(receiverItem));
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(trade));
        when(tradeRepo.save(trade)).thenReturn(trade);
        when(tradeOfferRepo.save(any(TradeOfferEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tradeOfferRepo.countByTrade_TradeId(100L)).thenReturn(2L);

        TradeOfferEntity result = tradeService.createTradeOffer(request);

        assertSame(trade, result.getTrade());
        assertEquals(TradeStatus.NEGOTIATING, trade.getStatus());
        assertSame(receiver, result.getSender());
        assertSame(sender, result.getReceiver());
        verify(notificationService).notifyTradeOfferReceived(receiver, sender, 100L, true);
        verify(chatService).sendMessage(
                sender.getUserId(),
                "I can offer this instead. What do you think?"
        );
    }

    @Test
    void shouldRejectTradeWithYourself() {
        CreateTradeOfferRequest request = new CreateTradeOfferRequest();
        request.setReceiverId(1L);

        when(authenticatedUserService.getCurrentUser()).thenReturn(sender);
        when(userRepo.findById(1L)).thenReturn(Optional.of(sender));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tradeService.createTradeOffer(request)
        );

        assertEquals("Cannot trade with yourself", exception.getMessage());
        verifyNoInteractions(itemRepo, tradeRepo, tradeOfferRepo, notificationService);
    }

    @Test
    void shouldRejectTradeWhenReceiverDoesNotExist() {
        CreateTradeOfferRequest request = new CreateTradeOfferRequest();
        request.setReceiverId(999L);

        when(authenticatedUserService.getCurrentUser()).thenReturn(sender);
        when(userRepo.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tradeService.createTradeOffer(request)
        );

        assertEquals("Receiver not found", exception.getMessage());
    }

    @Test
    void shouldRejectTradeWhenSelectedItemDoesNotExist() {
        CreateTradeOfferRequest request = new CreateTradeOfferRequest();
        request.setReceiverId(2L);
        request.setSenderItemIds(List.of(11L));
        request.setReceiverItemIds(List.of());

        when(authenticatedUserService.getCurrentUser()).thenReturn(sender);
        when(userRepo.findById(2L)).thenReturn(Optional.of(receiver));
        when(itemRepo.findAllById(List.of(11L))).thenReturn(List.of());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tradeService.createTradeOffer(request)
        );

        assertEquals("One or more selected items do not exist.", exception.getMessage());
    }

    @Test
    void shouldRejectSenderItemOwnedByAnotherUser() {
        ItemEntity wrongOwnerItem = item(11L, receiver);
        CreateTradeOfferRequest request = new CreateTradeOfferRequest();
        request.setReceiverId(2L);
        request.setSenderItemIds(List.of(11L));
        request.setReceiverItemIds(List.of());

        when(authenticatedUserService.getCurrentUser()).thenReturn(sender);
        when(userRepo.findById(2L)).thenReturn(Optional.of(receiver));
        when(itemRepo.findAllById(List.of(11L))).thenReturn(List.of(wrongOwnerItem));
        when(itemRepo.findAllById(List.of())).thenReturn(List.of());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tradeService.createTradeOffer(request)
        );

        assertEquals("Sender does not own item.", exception.getMessage());
    }

    @Test
    void shouldRejectUnavailableReceiverItem() {
        ItemEntity receiverItem = item(22L, receiver);
        receiverItem.setAvailable(false);

        CreateTradeOfferRequest request = new CreateTradeOfferRequest();
        request.setReceiverId(2L);
        request.setSenderItemIds(List.of());
        request.setReceiverItemIds(List.of(22L));

        when(authenticatedUserService.getCurrentUser()).thenReturn(sender);
        when(userRepo.findById(2L)).thenReturn(Optional.of(receiver));
        when(itemRepo.findAllById(List.of())).thenReturn(List.of());
        when(itemRepo.findAllById(List.of(22L))).thenReturn(List.of(receiverItem));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> tradeService.createTradeOffer(request)
        );

        assertEquals("Receiver item unavailable.", exception.getMessage());
    }

    @Test
    void shouldRejectSoldSenderItem() {
        ItemEntity senderItem = item(11L, sender);
        senderItem.setSold(true);

        CreateTradeOfferRequest request = new CreateTradeOfferRequest();
        request.setReceiverId(2L);
        request.setSenderItemIds(List.of(11L));
        request.setReceiverItemIds(List.of());

        when(authenticatedUserService.getCurrentUser()).thenReturn(sender);
        when(userRepo.findById(2L)).thenReturn(Optional.of(receiver));
        when(itemRepo.findAllById(List.of(11L))).thenReturn(List.of(senderItem));
        when(itemRepo.findAllById(List.of())).thenReturn(List.of());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> tradeService.createTradeOffer(request)
        );

        assertEquals("Sender item already sold.", exception.getMessage());
    }

    @Test
    void shouldRejectAlreadyTradedSenderItem() {
        ItemEntity senderItem = item(11L, sender);
        senderItem.setTraded(true);

        CreateTradeOfferRequest request = new CreateTradeOfferRequest();
        request.setReceiverId(2L);
        request.setSenderItemIds(List.of(11L));
        request.setReceiverItemIds(List.of());

        when(authenticatedUserService.getCurrentUser()).thenReturn(sender);
        when(userRepo.findById(2L)).thenReturn(Optional.of(receiver));
        when(itemRepo.findAllById(List.of(11L))).thenReturn(List.of(senderItem));
        when(itemRepo.findAllById(List.of())).thenReturn(List.of());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> tradeService.createTradeOffer(request)
        );

        assertEquals("Sender item already traded.", exception.getMessage());
    }

    @Test
    void shouldRejectCounterOfferOnClosedTrade() {
        TradeEntity trade = trade(100L, sender, receiver, TradeStatus.ACCEPTED);
        CreateTradeOfferRequest request = new CreateTradeOfferRequest();
        request.setReceiverId(2L);
        request.setSenderItemIds(List.of());
        request.setReceiverItemIds(List.of());
        request.setTradeId(100L);

        when(authenticatedUserService.getCurrentUser()).thenReturn(sender);
        when(userRepo.findById(2L)).thenReturn(Optional.of(receiver));
        when(itemRepo.findAllById(List.of())).thenReturn(List.of());
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(trade));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> tradeService.createTradeOffer(request)
        );

        assertEquals("Trade can no longer receive offers.", exception.getMessage());
        verify(tradeRepo, never()).save(any());
        verify(tradeOfferRepo, never()).save(any());
    }

    @Test
    void shouldCreateBuyOffer() {
        ItemEntity target = item(50L, receiver);
        target.setForSale(true);

        CreateBuyRequest request = new CreateBuyRequest();
        request.setItemId(50L);
        request.setOfferedPrice(500.0);
        request.setInitialMessage("I'd like to buy this item. Is this price okay?");

        when(authenticatedUserService.getCurrentUser()).thenReturn(sender);
        when(itemRepo.findById(50L)).thenReturn(Optional.of(target));
        when(tradeRepo.save(any(TradeEntity.class))).thenAnswer(invocation -> {
            TradeEntity trade = invocation.getArgument(0);
            trade.setTradeId(300L);
            return trade;
        });
        when(tradeOfferRepo.save(any(TradeOfferEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TradeOfferEntity result = tradeService.createBuyOffer(request);

        assertSame(sender, result.getSender());
        assertSame(receiver, result.getReceiver());
        assertTrue(result.getSenderItems().isEmpty());
        assertEquals(List.of(target), result.getReceiverItems());
        assertEquals(500.0, result.getCashAdjustment());
        assertEquals(TradeStatus.OPEN, result.getTrade().getStatus());
        verify(notificationService).notifyBuyOffer(sender, receiver, 300L);
        verify(chatService).sendMessage(
                receiver.getUserId(),
                "I'd like to buy this item. Is this price okay?"
        );
    }

    @Test
    void shouldRejectBuyingOwnItem() {
        ItemEntity target = item(50L, sender);
        target.setForSale(true);

        CreateBuyRequest request = new CreateBuyRequest();
        request.setItemId(50L);

        when(authenticatedUserService.getCurrentUser()).thenReturn(sender);
        when(itemRepo.findById(50L)).thenReturn(Optional.of(target));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tradeService.createBuyOffer(request)
        );

        assertEquals("Cannot buy your own item", exception.getMessage());
        verify(tradeRepo, never()).save(any());
    }

    @Test
    void shouldRejectBuyWhenItemIsNotForSale() {
        ItemEntity target = item(50L, receiver);
        target.setForSale(false);

        CreateBuyRequest request = new CreateBuyRequest();
        request.setItemId(50L);

        when(authenticatedUserService.getCurrentUser()).thenReturn(sender);
        when(itemRepo.findById(50L)).thenReturn(Optional.of(target));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> tradeService.createBuyOffer(request)
        );

        assertEquals("Item is not for sale", exception.getMessage());
        verify(tradeRepo, never()).save(any());
    }

    @Test
    void shouldAcceptTradeAsLatestReceiver() {
        ItemEntity senderItem = item(11L, sender);
        ItemEntity receiverItem = item(22L, receiver);
        TradeEntity trade = trade(100L, sender, receiver, TradeStatus.OPEN);
        TradeOfferEntity offer = TradeOfferEntity.builder()
                .id(200L)
                .sender(sender)
                .receiver(receiver)
                .senderItems(List.of(senderItem))
                .receiverItems(List.of(receiverItem))
                .trade(trade)
                .createdAt(LocalDateTime.now())
                .build();

        when(authenticatedUserService.getCurrentUser()).thenReturn(receiver);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(trade));
        when(tradeOfferRepo.findTopByTrade_TradeIdOrderByCreatedAtDesc(100L)).thenReturn(Optional.of(offer));

        tradeService.acceptTrade(100L);

        assertEquals(TradeStatus.ACCEPTED, trade.getStatus());
        verify(tradeRepo).save(trade);
        verify(notificationService).notifyTradeAccepted(receiver, sender, 100L);
    }

    @Test
    void shouldRejectAcceptWhenTradeIsClosed() {
        TradeEntity trade = trade(100L, sender, receiver, TradeStatus.COMPLETED);

        when(authenticatedUserService.getCurrentUser()).thenReturn(receiver);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(trade));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> tradeService.acceptTrade(100L)
        );

        assertEquals("Trade cannot be accepted", exception.getMessage());
        verifyNoInteractions(tradeOfferRepo, notificationService);
    }

    @Test
    void shouldRejectAcceptWhenUserIsNotLatestReceiver() {
        TradeEntity trade = trade(100L, sender, receiver, TradeStatus.NEGOTIATING);
        TradeOfferEntity offer = TradeOfferEntity.builder()
                .sender(receiver)
                .receiver(sender)
                .senderItems(List.of())
                .receiverItems(List.of())
                .trade(trade)
                .createdAt(LocalDateTime.now())
                .build();

        when(authenticatedUserService.getCurrentUser()).thenReturn(receiver);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(trade));
        when(tradeOfferRepo.findTopByTrade_TradeIdOrderByCreatedAtDesc(100L)).thenReturn(Optional.of(offer));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> tradeService.acceptTrade(100L)
        );

        assertEquals("Only the latest receiver can accept the trade.", exception.getMessage());
        verify(tradeRepo, never()).save(any());
    }

    @Test
    void shouldRejectAcceptWhenLatestOfferHasInvalidItem() {
        ItemEntity unavailable = item(11L, sender);
        unavailable.setAvailable(false);
        TradeEntity trade = trade(100L, sender, receiver, TradeStatus.OPEN);
        TradeOfferEntity offer = TradeOfferEntity.builder()
                .sender(sender)
                .receiver(receiver)
                .senderItems(List.of(unavailable))
                .receiverItems(List.of())
                .trade(trade)
                .createdAt(LocalDateTime.now())
                .build();

        when(authenticatedUserService.getCurrentUser()).thenReturn(receiver);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(trade));
        when(tradeOfferRepo.findTopByTrade_TradeIdOrderByCreatedAtDesc(100L)).thenReturn(Optional.of(offer));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> tradeService.acceptTrade(100L)
        );

        assertEquals("Sender item unavailable.", exception.getMessage());
        verify(tradeRepo, never()).save(any());
    }

    @Test
    void shouldRequestCompletionAsInitiator() {
        TradeEntity trade = trade(100L, sender, receiver, TradeStatus.ACCEPTED);

        when(authenticatedUserService.getCurrentUser()).thenReturn(sender);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(trade));

        tradeService.requestCompletion(100L);

        assertTrue(trade.getInitiatorCompletionConfirmed());
        assertFalse(trade.getReceiverCompletionConfirmed());
        assertNotNull(trade.getCompletionRequestedAt());
        verify(tradeRepo).save(trade);
        verify(notificationService).notifyCompletionRequested(sender, receiver, 100L);
    }

    @Test
    void shouldRequestCompletionAsReceiver() {
        TradeEntity trade = trade(100L, sender, receiver, TradeStatus.ACCEPTED);

        when(authenticatedUserService.getCurrentUser()).thenReturn(receiver);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(trade));

        tradeService.requestCompletion(100L);

        assertFalse(trade.getInitiatorCompletionConfirmed());
        assertTrue(trade.getReceiverCompletionConfirmed());
        assertNotNull(trade.getCompletionRequestedAt());
        verify(notificationService).notifyCompletionRequested(receiver, sender, 100L);
    }

    @Test
    void shouldIgnoreRepeatedCompletionRequest() {
        LocalDateTime requestedAt = LocalDateTime.now().minusMinutes(5);
        TradeEntity trade = trade(100L, sender, receiver, TradeStatus.ACCEPTED);
        trade.setInitiatorCompletionConfirmed(true);
        trade.setCompletionRequestedAt(requestedAt);

        when(authenticatedUserService.getCurrentUser()).thenReturn(sender);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(trade));

        tradeService.requestCompletion(100L);

        assertEquals(requestedAt, trade.getCompletionRequestedAt());
        verify(tradeRepo, never()).save(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldRejectCompletionRequestFromNonParticipant() {
        TradeEntity trade = trade(100L, sender, receiver, TradeStatus.ACCEPTED);

        when(authenticatedUserService.getCurrentUser()).thenReturn(outsider);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(trade));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> tradeService.requestCompletion(100L)
        );

        assertEquals("User not part of trade", exception.getMessage());
    }

    @Test
    void shouldRejectCompletionRequestBeforeAcceptance() {
        TradeEntity trade = trade(100L, sender, receiver, TradeStatus.OPEN);

        when(authenticatedUserService.getCurrentUser()).thenReturn(sender);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(trade));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> tradeService.requestCompletion(100L)
        );

        assertEquals("Trade must be accepted first", exception.getMessage());
    }

    @Test
    void shouldConfirmCompletionWithoutCompletingUntilOtherParticipantConfirms() {
        TradeEntity trade = trade(100L, sender, receiver, TradeStatus.ACCEPTED);

        when(authenticatedUserService.getCurrentUser()).thenReturn(sender);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(trade));

        tradeService.confirmCompletion(100L);

        assertTrue(trade.getInitiatorCompletionConfirmed());
        assertFalse(trade.getReceiverCompletionConfirmed());
        assertEquals(TradeStatus.ACCEPTED, trade.getStatus());
        verify(tradeRepo).save(trade);
        verifyNoInteractions(tradeOfferRepo, notificationService);
    }

    @Test
    void shouldCompleteTradeAfterBothParticipantsConfirm() {
        ItemEntity senderItem = item(11L, sender);
        ItemEntity receiverItem = item(22L, receiver);
        TradeEntity trade = trade(100L, sender, receiver, TradeStatus.ACCEPTED);
        trade.setInitiatorCompletionConfirmed(true);

        TradeOfferEntity latestOffer = TradeOfferEntity.builder()
                .id(200L)
                .sender(sender)
                .receiver(receiver)
                .senderItems(List.of(senderItem))
                .receiverItems(List.of(receiverItem))
                .trade(trade)
                .createdAt(LocalDateTime.now())
                .build();

        when(authenticatedUserService.getCurrentUser()).thenReturn(receiver);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(trade));
        when(tradeOfferRepo.findTopByTrade_TradeIdOrderByCreatedAtDesc(100L)).thenReturn(Optional.of(latestOffer));

        tradeService.confirmCompletion(100L);

        assertEquals(TradeStatus.COMPLETED, trade.getStatus());
        assertFalse(trade.getInitiatorCompletionConfirmed());
        assertFalse(trade.getReceiverCompletionConfirmed());
        assertFalse(senderItem.isAvailable());
        assertTrue(senderItem.isTraded());
        assertNotNull(senderItem.getCompletedAt());
        assertFalse(receiverItem.isAvailable());
        assertTrue(receiverItem.isTraded());
        assertNotNull(receiverItem.getCompletedAt());

        verify(itemRepo).saveAll(List.of(senderItem));
        verify(itemRepo).saveAll(List.of(receiverItem));
        verify(tradeRepo).save(trade);
        verify(notificationService).notifyTradeCompleted(sender, receiver, 100L);
        verify(notificationService).notifyTradeCompleted(receiver, sender, 100L);
    }

    @Test
    void shouldCancelTrade() {
        TradeEntity trade = trade(100L, sender, receiver, TradeStatus.OPEN);

        when(authenticatedUserService.getCurrentUser()).thenReturn(sender);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(trade));

        tradeService.cancelTrade(100L);

        assertEquals(TradeStatus.CANCELLED, trade.getStatus());
        verify(tradeRepo).save(trade);
        verify(notificationService).notifyTradeCancelled(sender, receiver, 100L);
    }

    @Test
    void shouldRejectCancellationByNonParticipant() {
        TradeEntity trade = trade(100L, sender, receiver, TradeStatus.OPEN);

        when(authenticatedUserService.getCurrentUser()).thenReturn(outsider);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(trade));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> tradeService.cancelTrade(100L)
        );

        assertEquals("Only trade participants can cancel the trade.", exception.getMessage());
        verify(tradeRepo, never()).save(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldRejectCancellationOfCompletedTrade() {
        TradeEntity trade = trade(100L, sender, receiver, TradeStatus.COMPLETED);

        when(authenticatedUserService.getCurrentUser()).thenReturn(sender);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(trade));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> tradeService.cancelTrade(100L)
        );

        assertEquals("Completed trades cannot be cancelled.", exception.getMessage());
        verify(tradeRepo, never()).save(any());
    }

    @Test
    void shouldIgnoreRepeatedCancellation() {
        TradeEntity trade = trade(100L, sender, receiver, TradeStatus.CANCELLED);

        when(authenticatedUserService.getCurrentUser()).thenReturn(sender);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(trade));

        tradeService.cancelTrade(100L);

        assertEquals(TradeStatus.CANCELLED, trade.getStatus());
        verify(tradeRepo, never()).save(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldGetTradeInbox() {
        TradeEntity trade = trade(100L, sender, receiver, TradeStatus.OPEN);
        TradeOfferEntity offer = TradeOfferEntity.builder()
                .id(200L)
                .sender(sender)
                .receiver(receiver)
                .senderItems(List.of(item(11L, sender)))
                .receiverItems(List.of(item(22L, receiver)))
                .trade(trade)
                .createdAt(LocalDateTime.now())
                .build();
        TradeInboxResponse mapped = TradeInboxResponse.builder().tradeId(100L).build();

        when(authenticatedUserService.getCurrentUser()).thenReturn(sender);
        when(tradeRepo.findUserTrades(1L)).thenReturn(List.of(trade));
        when(tradeOfferRepo.findTopByTrade_TradeIdOrderByCreatedAtDesc(100L)).thenReturn(Optional.of(offer));
        when(tradeMapper.mapInboxTrade(trade, sender, offer)).thenReturn(mapped);

        assertEquals(List.of(mapped), tradeService.getTradeInbox());
        verify(tradeMapper).mapInboxTrade(trade, sender, offer);
    }

    @Test
    void shouldGetTradeOfferHistory() {
        TradeEntity trade = trade(100L, sender, receiver, TradeStatus.NEGOTIATING);
        TradeOfferEntity first = TradeOfferEntity.builder().id(201L).trade(trade).build();
        TradeOfferEntity second = TradeOfferEntity.builder().id(202L).trade(trade).build();
        TradeOfferResponse firstResponse = TradeOfferResponse.builder().tradeOfferId(201L).build();
        TradeOfferResponse secondResponse = TradeOfferResponse.builder().tradeOfferId(202L).build();

        when(tradeOfferRepo.findByTrade_TradeIdOrderByCreatedAtAsc(100L)).thenReturn(List.of(first, second));
        when(tradeMapper.mapFromEntity(first)).thenReturn(firstResponse);
        when(tradeMapper.mapFromEntity(second)).thenReturn(secondResponse);

        assertEquals(List.of(firstResponse, secondResponse), tradeService.getTradeOfferHistory(100L));
    }

    @Test
    void shouldGetTradeDetails() {
        TradeEntity trade = trade(100L, sender, receiver, TradeStatus.OPEN);
        TradeOfferEntity offerEntity = TradeOfferEntity.builder().id(200L).trade(trade).build();
        TradeOfferResponse offer = TradeOfferResponse.builder().tradeOfferId(200L).build();
        TradeHistoryResponse historyRow = TradeHistoryResponse.builder()
                .tradeOfferId(200L)
                .senderId(1L)
                .senderName("Sender")
                .build();
        TradeDetailsResponse details = TradeDetailsResponse.builder().tradeId(100L).build();

        when(tradeRepo.findById(100L)).thenReturn(Optional.of(trade));
        when(tradeOfferRepo.findById(200L)).thenReturn(Optional.of(offerEntity));
        when(tradeOfferRepo.findTradeHistoryByTradeId(100L)).thenReturn(List.of(historyRow));
        when(tradeMapper.mapFromEntity(offerEntity)).thenReturn(offer);
        when(tradeMapper.mapTradeDetails(trade, offer, List.of(historyRow))).thenReturn(details);

        assertSame(details, tradeService.getTrade(100L, 200L));
    }

    @Test
    void shouldRejectTradeDetailsWhenOfferBelongsToDifferentTrade() {
        TradeEntity trade = trade(100L, sender, receiver, TradeStatus.OPEN);
        TradeEntity otherTrade = trade(999L, sender, receiver, TradeStatus.OPEN);
        TradeOfferEntity offerEntity = TradeOfferEntity.builder().id(200L).trade(otherTrade).build();

        when(tradeRepo.findById(100L)).thenReturn(Optional.of(trade));
        when(tradeOfferRepo.findById(200L)).thenReturn(Optional.of(offerEntity));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tradeService.getTrade(100L, 200L)
        );

        assertEquals("trade offer doesn't belong to the trade", exception.getMessage());
        verify(tradeOfferRepo, never()).findTradeHistoryByTradeId(any());
    }

    @Test
    void shouldMapTradeableItemsUsingLowestActiveImage() {
        ItemImageEntity pending = ItemImageEntity.builder()
                .id(1L).secureUrl("pending.jpg").sortOrder(0).status(ItemImageStatus.PENDING_DELETION).build();
        ItemImageEntity activeLater = ItemImageEntity.builder()
                .id(2L).secureUrl("later.jpg").sortOrder(5).status(ItemImageStatus.ACTIVE).build();
        ItemImageEntity activeFirst = ItemImageEntity.builder()
                .id(3L).secureUrl("first.jpg").sortOrder(2).status(ItemImageStatus.ACTIVE).build();

        ItemEntity item = item(11L, sender);
        item.setItemName("Laptop");
        item.setUserPrice(50000);
        item.setGeneratedPrice(55000);
        item.setCondition(ItemCondition.GOOD);
        item.setImages(List.of(pending, activeLater, activeFirst));

        when(itemRepo.findTradeableItemsByUserId(1L)).thenReturn(List.of(item));

        List<TradeItemResponse> result = tradeService.getTradeableItems(1L);

        assertEquals(1, result.size());
        assertEquals(11L, result.get(0).getItemId());
        assertEquals("Laptop", result.get(0).getName());
        assertEquals("first.jpg", result.get(0).getImageUrl());
        assertEquals(50000, result.get(0).getUserPrice());
        assertEquals(55000, result.get(0).getSystemGeneratedPrice());
        assertEquals(ItemCondition.GOOD, result.get(0).getCondition());
    }

    private UserEntity user(Long id, String email, String name) {
        return UserEntity.builder()
                .userId(id).email(email).name(name).password("password").build();
    }

    private ItemEntity item(Long id, UserEntity owner) {
        return ItemEntity.builder()
                .itemId(id)
                .itemName("Test Item")
                .user(owner)
                .userPrice(1000)
                .generatedPrice(1200)
                .condition(ItemCondition.GOOD)
                .available(true)
                .sold(false)
                .traded(false)
                .images(List.of())
                .build();
    }

    private TradeEntity trade(Long id, UserEntity initiator, UserEntity receiver, TradeStatus status) {
        return TradeEntity.builder()
                .tradeId(id)
                .initiator(initiator)
                .receiver(receiver)
                .status(status)
                .initiatorCompletionConfirmed(false)
                .receiverCompletionConfirmed(false)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
