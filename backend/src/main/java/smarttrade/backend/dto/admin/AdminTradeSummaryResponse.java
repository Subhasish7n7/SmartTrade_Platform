package smarttrade.backend.dto.admin;

import smarttrade.backend.entities.trade.TradeStatus;

import java.time.LocalDateTime;

public record AdminTradeSummaryResponse(
        Long tradeId,
        Long initiatorId,
        String initiatorName,
        String initiatorEmail,
        Long receiverId,
        String receiverName,
        String receiverEmail,
        TradeStatus status,
        boolean initiatorCompletionConfirmed,
        boolean receiverCompletionConfirmed,
        LocalDateTime createdAt,
        LocalDateTime acceptedAt,
        LocalDateTime completionRequestedAt,
        LocalDateTime completedAt,
        LocalDateTime cancelledAt,
        LocalDateTime disputedAt
) {}