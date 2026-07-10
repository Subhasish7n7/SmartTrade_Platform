package smarttrade.backend.dto.trade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

//not used delete later
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeOfferDto {
    private Long senderId;
    private Long receiverId;
    private List<Long> senderItemIds;
    private List<Long> receiverItemIds;
    private Double cashAdjustment;
    private LocalDateTime createdAt;
    private String status;
}

