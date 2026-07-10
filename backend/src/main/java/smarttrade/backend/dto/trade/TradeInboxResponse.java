package smarttrade.backend.dto.trade;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
//used for showing trade inbox page
@Data
@Builder
public class TradeInboxResponse {
    private Long tradeId;
    private Long otherUserId;
    private String otherUserName;
    private String otherUserProfilePicture;
    private String status;
    private Integer yourItemCount;
    private Integer theirItemCount;
    private Integer valueDifference;
    private LocalDateTime updatedAt;
    private TradeOfferResponse latestOffer;
}