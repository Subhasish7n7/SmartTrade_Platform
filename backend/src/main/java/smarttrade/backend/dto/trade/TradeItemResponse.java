package smarttrade.backend.dto.trade;

import lombok.Builder;
import lombok.Data;
import smarttrade.backend.entities.item.ItemCondition;

// Used by trade offer response to view trade comparison on frontend.
@Data
@Builder
public class TradeItemResponse {

    private Long itemId;
    private String name;
    private String imageUrl;
    private Integer userPrice;
    private Integer systemGeneratedPrice;
    private Integer newPrice;
    private ItemCondition condition;
}