package smarttrade.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smarttrade.backend.entities.trade.TradeEntity;
import smarttrade.backend.entities.trade.TradeStatus;
import smarttrade.backend.repository.TradeRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeExpirationScheduler {

    private static final int EXPIRATION_DAYS = 14;

    /*
     * These are the current mock/UI trades.
     * They must remain active for visual/testing purposes.
     */
    private static final Set<Long> MOCK_TRADE_IDS = Set.of(
            1L,
            2L,
            3L
    );

    private final TradeRepo tradeRepo;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void expireInactiveTrades() {

        LocalDateTime cutoff =
                LocalDateTime.now().minusDays(EXPIRATION_DAYS);

        List<TradeEntity> expired =
                tradeRepo.findExpiredNegotiations(cutoff);

        if (expired.isEmpty()) {
            return;
        }

        List<TradeEntity> tradesToExpire = expired.stream()
                .filter(trade ->
                        !MOCK_TRADE_IDS.contains(trade.getTradeId())
                )
                .toList();

        if (tradesToExpire.isEmpty()) {
            return;
        }

        for (TradeEntity trade : tradesToExpire) {

            trade.setStatus(TradeStatus.EXPIRED);

            notificationService.notifyTradeExpired(
                    trade.getInitiator(),
                    null,
                    trade.getTradeId()
            );

            notificationService.notifyTradeExpired(
                    trade.getReceiver(),
                    null,
                    trade.getTradeId()
            );

            log.info(
                    "Trade {} expired automatically.",
                    trade.getTradeId()
            );
        }

        tradeRepo.saveAll(tradesToExpire);
    }
}