package smarttrade.backend.dto.trade;

import smarttrade.backend.entities.trade.DisputeReason;
import smarttrade.backend.entities.trade.DisputeResolution;
import smarttrade.backend.entities.trade.DisputeStatus;

import java.time.LocalDateTime;

public record TradeDisputeResponse(
        Long disputeId,
        Long tradeId,
        DisputeStatus status,
        DisputeReason reason,
        String description,
        DisputeResolution resolution,
        String resolutionNotes,
        LocalDateTime createdAt,
        LocalDateTime underReviewAt,
        LocalDateTime resolvedAt
) {
}