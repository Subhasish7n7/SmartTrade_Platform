package smarttrade.backend.service.trade;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import smarttrade.backend.entities.item.ItemEntity;
import smarttrade.backend.entities.trade.TradeEntity;
import smarttrade.backend.entities.trade.TradeOfferEntity;
import smarttrade.backend.entities.trade.TradeStatus;
import smarttrade.backend.events.trust.TrustEvent;
import smarttrade.backend.events.trust.TrustEventSourceType;
import smarttrade.backend.events.trust.TrustEventType;
import smarttrade.backend.repository.item.ItemRepo;
import smarttrade.backend.repository.trades.TradeOfferRepo;
import smarttrade.backend.repository.trades.TradeRepo;
import smarttrade.backend.repository.user.UserRepo;
import smarttrade.backend.service.NotificationService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeFinalizationService {

    private final TradeOfferRepo tradeOfferRepo;
    private final TradeRepo tradeRepo;
    private final ItemRepo itemRepo;
    private final UserRepo userRepo;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher applicationEventPublisher;


    /**
     * Finalizes a trade as COMPLETED.
     * <p>
     * This is the single business path responsible for turning an
     * ACCEPTED or administratively resolved DISPUTED trade into COMPLETED.
     * <p>
     * successfulTrades is also updated here so every successful
     * completion path uses the same logic.
     */
    @Transactional
    public void completeTrade(Long tradeId) {

        TradeEntity trade = tradeRepo.findByIdForUpdate(tradeId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Trade not found")
                );

        /*
         * Idempotency:
         *
         * A scheduler, user confirmation, or admin action must never
         * increment successfulTrades twice for the same trade.
         */
        if (trade.getStatus() == TradeStatus.COMPLETED) {
            log.info(
                    "Trade {} is already completed. Skipping finalization.",
                    tradeId
            );
            return;
        }

        if (trade.getStatus() != TradeStatus.ACCEPTED
                && trade.getStatus() != TradeStatus.DISPUTED) {

            throw new IllegalStateException(
                    "Only accepted or disputed trades can be completed."
            );
        }

        TradeOfferEntity latestOffer =
                tradeOfferRepo
                        .findTopByTrade_TradeIdOrderByCreatedAtDesc(tradeId)
                        .orElseThrow(() ->
                                new IllegalStateException("No offers found")
                        );

        LocalDateTime completedAt = LocalDateTime.now();

        latestOffer.getSenderItems().forEach(item -> finalizeItem(item, completedAt));

        latestOffer.getReceiverItems().forEach(item -> finalizeItem(item, completedAt));

        itemRepo.saveAll(latestOffer.getSenderItems());
        itemRepo.saveAll(latestOffer.getReceiverItems());

        trade.setStatus(TradeStatus.COMPLETED);
        trade.setInitiatorCompletionConfirmed(true);
        trade.setReceiverCompletionConfirmed(true);
        trade.setCompletedAt(completedAt);

        tradeRepo.save(trade);

        incrementSuccessfulTrades(trade);
        applicationEventPublisher.publishEvent(
                TrustEvent.now(
                        trade.getInitiator().getUserId(),
                        TrustEventType.TRADE_COMPLETED,
                        TrustEventSourceType.TRADE,
                        tradeId,
                        trade.getReceiver().getUserId(),
                        "Completed trade " + tradeId
                )
        );
        applicationEventPublisher.publishEvent(
                TrustEvent.now(
                        trade.getReceiver().getUserId(),
                        TrustEventType.TRADE_COMPLETED,
                        TrustEventSourceType.TRADE,
                        tradeId,
                        trade.getInitiator().getUserId(),
                        "Completed trade " + tradeId
                )
        );

        log.info(
                "Trade {} finalized as COMPLETED",
                tradeId
        );

        notificationService.notifyTradeCompleted(
                trade.getInitiator(),
                trade.getReceiver(),
                tradeId
        );

        notificationService.notifyTradeCompleted(
                trade.getReceiver(),
                trade.getInitiator(),
                tradeId
        );
    }

    private void incrementSuccessfulTrades(TradeEntity trade) {

        trade.getInitiator().setSuccessfulTrades(
                trade.getInitiator().getSuccessfulTrades() + 1
        );

        trade.getReceiver().setSuccessfulTrades(
                trade.getReceiver().getSuccessfulTrades() + 1
        );

        /*
         * These UserEntity instances are managed because the trade and
         * its relationships are loaded inside the same transaction.
         *
         * saveAll is not technically required for JPA dirty checking,
         * but it makes the intent explicit.
         */
        userRepo.saveAll(
                java.util.List.of(
                        trade.getInitiator(),
                        trade.getReceiver()
                )
        );
    }

    private void finalizeItem(ItemEntity item, LocalDateTime completedAt) {
        item.setAvailable(false);
        item.setTraded(true);
        item.setCompletedAt(completedAt);
    }
}