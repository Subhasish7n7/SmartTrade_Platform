package smarttrade.backend.dto.admin;

import smarttrade.backend.entities.trade.DisputeReason;
import smarttrade.backend.entities.trade.DisputeResolution;
import smarttrade.backend.entities.trade.DisputeStatus;

import java.time.LocalDateTime;

public record AdminDisputeSummaryResponse(
        Long disputeId,
        Long tradeId,
        Long openedById,
        String openedByName,
        DisputeStatus status,
        DisputeReason reason,
        DisputeResolution resolution,
        Long resolvedById,
        String resolvedByName,
        Long faultUserId,
        LocalDateTime createdAt,
        LocalDateTime underReviewAt,
        LocalDateTime resolvedAt
) {}