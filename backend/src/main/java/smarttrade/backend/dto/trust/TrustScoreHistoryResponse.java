package smarttrade.backend.dto.trust;

import smarttrade.backend.events.trust.TrustEventSourceType;
import smarttrade.backend.events.trust.TrustEventType;

import java.time.LocalDateTime;

public record TrustScoreHistoryResponse(
        Long historyId,
        Long userId,

        TrustEventType eventType,
        TrustEventSourceType sourceType,
        Long sourceId,
        Long relatedUserId,

        double scoreBefore,
        double scoreAfter,
        double scoreChange,

        double transactionComponent,
        double reviewComponent,
        double conductComponent,
        double verificationComponent,
        double listingQualityComponent,
        double maturityComponent,

        String reason,
        LocalDateTime createdAt
) {
}