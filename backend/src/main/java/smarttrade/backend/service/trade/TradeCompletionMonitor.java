package smarttrade.backend.service.trade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import smarttrade.backend.entities.trade.TradeEntity;
import smarttrade.backend.repository.trades.TradeRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeCompletionMonitor {

    private static final long COMPLETION_TIMEOUT_HOURS = 48;

    private static final Set<Long> MOCK_TRADE_IDS = Set.of(
            1L,
            2L,
            3L
    );

    private final TradeRepo tradeRepo;
    private final TradeDisputeService tradeDisputeService;

    @Scheduled(cron = "0 */5 * * * *")
    public void monitorCompletionDeadlines() {

        LocalDateTime cutoff =
                LocalDateTime.now()
                        .minusHours(COMPLETION_TIMEOUT_HOURS);

        List<TradeEntity> overdueTrades =
                tradeRepo.findAcceptedTradesWithExpiredCompletionDeadline(cutoff);

        for (TradeEntity trade : overdueTrades) {

            if (MOCK_TRADE_IDS.contains(trade.getTradeId())) {
                continue;
            }

            try {
                tradeDisputeService.autoOpenCompletionDispute(
                        trade.getTradeId()
                );
            } catch (Exception e) {
                log.error(
                        "Failed to process completion timeout for trade {}",
                        trade.getTradeId(),
                        e
                );
            }
        }
    }
}