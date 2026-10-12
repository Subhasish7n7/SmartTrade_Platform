package smarttrade.backend.events.trust;

import java.time.LocalDateTime;

public record TrustEvent(
        Long userId,
        TrustEventType type,
        TrustEventSourceType sourceType,
        Long sourceId,
        Long relatedUserId,
        LocalDateTime occurredAt,
        String reason
) {

    public static TrustEvent now(
            Long userId,
            TrustEventType type,
            TrustEventSourceType sourceType,
            Long sourceId,
            Long relatedUserId,
            String reason
    ) {
        return new TrustEvent(
                userId,
                type,
                sourceType,
                sourceId,
                relatedUserId,
                LocalDateTime.now(),
                reason
        );
    }
}