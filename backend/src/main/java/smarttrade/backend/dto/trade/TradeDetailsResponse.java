package smarttrade.backend.dto.trade;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

// Used for viewing trade detail page.
@Data
@Builder
public class TradeDetailsResponse {

    private Long tradeId;

    private Long initiatorId;
    private String initiatorName;
    private Double initiatorTrustScore;

    private Long receiverId;
    private String receiverName;
    private Double receiverTrustScore;

    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;

    private Boolean initiatorCompletionConfirmed;
    private Boolean receiverCompletionConfirmed;
    private LocalDateTime completionRequestedAt;

    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime disputedAt;

    private TradeOfferResponse offer;
    private List<TradeHistoryResponse> history;
}