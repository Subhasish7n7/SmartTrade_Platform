package smarttrade.backend.dto.trade;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
//used for viewing trade detail page
@Data
@Builder
public class TradeDetailsResponse {
    private Long tradeId;
    private Long initiatorId;
    private Long receiverId;
    private String initiatorName;
    private String receiverName;
    private String status;
    private LocalDateTime createdAt;
    private TradeOfferResponse offer;
    private List<TradeHistoryResponse> history;
}