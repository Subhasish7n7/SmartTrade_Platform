package smarttrade.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeOfferDto {
    private Long senderId;
    private Long receiverId;
    private List<Long> senderItemIds;
    private List<Long> receiverItemIds;
    private LocalDateTime createdAt;
    private String status;
}

