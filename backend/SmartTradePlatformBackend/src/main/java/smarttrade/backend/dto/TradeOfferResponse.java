package smarttrade.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TradeOfferResponse {

    private Long tradeId;

    private Long senderId;

    private Long receiverId;

    private List<Long> senderItemIds;

    private List<Long> receiverItemIds;

    private Double cashAdjustment;

    private String status;

    private LocalDateTime createdAt;
}