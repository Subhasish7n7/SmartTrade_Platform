package smarttrade.backend.dto.trade;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

//used for viewing a specific trade
@Data
@Builder
public class TradeOfferResponse {
    private Long tradeOfferId;
    private Long senderId;
    private Long receiverId;
    private List<TradeItemResponse> senderItems;
    private List<TradeItemResponse> receiverItems;
    private Double cashAdjustment;
    private String status;
    private LocalDateTime createdAt;
}