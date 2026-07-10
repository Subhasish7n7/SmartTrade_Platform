package smarttrade.backend.dto.trade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class TradeHistoryResponse {
    private Long tradeOfferId;
    private Long senderId;
    private String senderName;
    private LocalDateTime createdAt;
}
