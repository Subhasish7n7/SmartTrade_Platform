package smarttrade.backend.UnitTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smarttrade.backend.dto.trade.CreateBuyRequest;
import smarttrade.backend.dto.trade.CreateTradeOfferRequest;
import smarttrade.backend.entities.*;
import smarttrade.backend.repository.ItemRepo;
import smarttrade.backend.repository.TradeOfferRepo;
import smarttrade.backend.repository.TradeRepo;
import smarttrade.backend.repository.UserRepo;
import smarttrade.backend.security.AuthenticatedUserService;
import smarttrade.backend.service.TradeService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    @InjectMocks
    private TradeService tradeService;

    private UserEntity sender;
    private UserEntity receiver;

    @BeforeEach
    void setup() {

        sender = UserEntity.builder()
                .userId(1L)
                .email("sender@test.com")
                .build();

        receiver = UserEntity.builder()
                .userId(2L)
                .email("receiver@test.com")
                .build();
    }

    @Test
    void shouldCreateTradeOffer() {

        ItemEntity senderItem = ItemEntity.builder()
                .itemId(1L)
                .user(sender)
                .available(true)
                .locked(false)
                .build();

        ItemEntity receiverItem = ItemEntity.builder()
                .itemId(2L)
                .user(receiver)
                .available(true)
                .build();

        CreateTradeOfferRequest request =
                new CreateTradeOfferRequest();

        request.setReceiverId(2L);
        request.setSenderItemIds(List.of(1L));
        request.setReceiverItemIds(List.of(2L));
        request.setCashAdjustment(200.0);

        TradeEntity trade = TradeEntity.builder()
                .tradeId(10L)
                .status(TradeStatus.OPEN)
                .build();

        when(authenticatedUserService.getCurrentUser())
                .thenReturn(sender);

        when(userRepo.findById(2L))
                .thenReturn(Optional.of(receiver));

        when(itemRepo.findAllById(List.of(1L)))
                .thenReturn(List.of(senderItem));

        when(itemRepo.findAllById(List.of(2L)))
                .thenReturn(List.of(receiverItem));

        when(tradeRepo.save(any()))
                .thenReturn(trade);

        when(tradeOfferRepo.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        TradeOfferEntity result =
                tradeService.createTradeOffer(request);

        assertNotNull(result);

        assertEquals(sender, result.getSender());

        verify(itemRepo, times(1))
                .saveAll(any());
    }

    @Test
    void shouldRejectTradeWithYourself() {

        CreateTradeOfferRequest request =
                new CreateTradeOfferRequest();

        request.setReceiverId(1L);

        when(authenticatedUserService.getCurrentUser())
                .thenReturn(sender);

        when(userRepo.findById(1L))
                .thenReturn(Optional.of(sender));

        assertThrows(
                IllegalArgumentException.class,
                () -> tradeService.createTradeOffer(request)
        );
    }

    @Test
    void shouldRejectLockedSenderItem() {

        ItemEntity lockedItem = ItemEntity.builder()
                .itemId(1L)
                .user(sender)
                .available(true)
                .locked(true)
                .build();

        CreateTradeOfferRequest request =
                new CreateTradeOfferRequest();

        request.setReceiverId(2L);
        request.setSenderItemIds(List.of(1L));
        request.setReceiverItemIds(List.of());

        when(authenticatedUserService.getCurrentUser())
                .thenReturn(sender);

        when(userRepo.findById(2L))
                .thenReturn(Optional.of(receiver));

        when(itemRepo.findAllById(List.of(1L)))
                .thenReturn(List.of(lockedItem));

        when(itemRepo.findAllById(List.of()))
                .thenReturn(List.of());

        assertThrows(
                IllegalStateException.class,
                () -> tradeService.createTradeOffer(request)
        );
    }

    @Test
    void shouldCreateBuyOffer() {

        ItemEntity item = ItemEntity.builder()
                .itemId(1L)
                .user(receiver)
                .forSale(true)
                .available(true)
                .build();

        CreateBuyRequest request =
                new CreateBuyRequest();

        request.setItemId(1L);
        request.setOfferedPrice(500.0);

        TradeEntity trade = TradeEntity.builder()
                .tradeId(99L)
                .status(TradeStatus.OPEN)
                .build();

        when(authenticatedUserService.getCurrentUser())
                .thenReturn(sender);

        when(itemRepo.findById(1L))
                .thenReturn(Optional.of(item));

        when(tradeRepo.save(any()))
                .thenReturn(trade);

        when(tradeOfferRepo.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        TradeOfferEntity result =
                tradeService.createBuyOffer(request);

        assertEquals(500.0, result.getCashAdjustment());
    }

    @Test
    void shouldRejectBuyingOwnItem() {

        ItemEntity item = ItemEntity.builder()
                .itemId(1L)
                .user(sender)
                .forSale(true)
                .available(true)
                .build();

        CreateBuyRequest request =
                new CreateBuyRequest();

        request.setItemId(1L);

        when(authenticatedUserService.getCurrentUser())
                .thenReturn(sender);

        when(itemRepo.findById(1L))
                .thenReturn(Optional.of(item));

        assertThrows(
                IllegalArgumentException.class,
                () -> tradeService.createBuyOffer(request)
        );
    }

    @Test
    void shouldAcceptTrade() {

        TradeEntity trade = TradeEntity.builder()
                .tradeId(1L)
                .status(TradeStatus.OPEN)
                .build();

        TradeOfferEntity offer =
                TradeOfferEntity.builder()
                        .receiver(receiver)
                        .createdAt(LocalDateTime.now())
                        .build();

        when(authenticatedUserService.getCurrentUser())
                .thenReturn(receiver);

        when(tradeRepo.findById(1L))
                .thenReturn(Optional.of(trade));

        when(tradeOfferRepo
                .findTopByTrade_TradeIdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(offer));

        tradeService.acceptTrade(1L);

        assertEquals(TradeStatus.ACCEPTED,
                trade.getStatus());

        verify(tradeRepo).save(trade);
    }

    @Test
    void shouldCompleteTrade() {

        ItemEntity senderItem = ItemEntity.builder()
                .available(true)
                .locked(true)
                .build();

        TradeEntity trade = TradeEntity.builder()
                .tradeId(1L)
                .initiator(sender)
                .receiver(receiver)
                .status(TradeStatus.ACCEPTED)
                .build();

        TradeOfferEntity offer =
                TradeOfferEntity.builder()
                        .senderItems(List.of(senderItem))
                        .receiverItems(List.of())
                        .build();

        when(authenticatedUserService.getCurrentUser())
                .thenReturn(sender);

        when(tradeRepo.findById(1L))
                .thenReturn(Optional.of(trade));

        when(tradeOfferRepo
                .findTopByTrade_TradeIdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(offer));

        tradeService.completeTrade(1L);

        assertFalse(senderItem.isAvailable());
        assertFalse(senderItem.isLocked());
        assertTrue(senderItem.isTraded());

        assertEquals(TradeStatus.COMPLETED,
                trade.getStatus());
    }

    @Test
    void shouldCancelTrade() {

        ItemEntity senderItem = ItemEntity.builder()
                .locked(true)
                .lockedByTradeId(1L)
                .build();

        TradeEntity trade = TradeEntity.builder()
                .tradeId(1L)
                .initiator(sender)
                .receiver(receiver)
                .status(TradeStatus.OPEN)
                .build();

        TradeOfferEntity offer =
                TradeOfferEntity.builder()
                        .senderItems(List.of(senderItem))
                        .build();

        when(authenticatedUserService.getCurrentUser())
                .thenReturn(sender);

        when(tradeRepo.findById(1L))
                .thenReturn(Optional.of(trade));

        when(tradeOfferRepo
                .findTopByTrade_TradeIdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(offer));

        tradeService.cancelTrade(1L);

        assertFalse(senderItem.isLocked());

        assertEquals(TradeStatus.CANCELLED,
                trade.getStatus());
    }
}