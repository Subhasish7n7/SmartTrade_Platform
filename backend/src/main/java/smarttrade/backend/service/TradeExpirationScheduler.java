package smarttrade.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smarttrade.backend.entities.TradeEntity;
import smarttrade.backend.entities.TradeStatus;
import smarttrade.backend.repository.TradeRepo;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeExpirationScheduler {

    private static final int EXPIRATION_DAYS = 14;

    private final TradeRepo tradeRepo;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void expireInactiveTrades() {

        LocalDateTime cutoff = LocalDateTime.now().minusDays(EXPIRATION_DAYS);

        List<TradeEntity> expired = tradeRepo.findExpiredNegotiations(cutoff);

        if (expired.isEmpty()) {
            return;
        }

        for (TradeEntity trade : expired) {

            trade.setStatus(TradeStatus.EXPIRED);

            notificationService.notifyTradeExpired(
                    trade.getInitiator(),
                    null,
                    trade.getTradeId());

            notificationService.notifyTradeExpired(
                    trade.getReceiver(),
                    null,
                    trade.getTradeId());

            log.info("Trade {} expired automatically.", trade.getTradeId());
        }

        tradeRepo.saveAll(expired);
    }
}