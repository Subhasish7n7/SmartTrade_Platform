package smarttrade.backend.dto.trade;

import lombok.Builder;
import lombok.Data;
//used by trade offer response to view trade comparision on front end
@Data
@Builder
public class TradeItemResponse {
    private Long itemId;
    private String name;
    private String imageUrl;
    private Integer userPrice;
    private Integer systemGeneratedPrice;
    private String condition;
}