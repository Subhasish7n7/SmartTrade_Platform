package smarttrade.backend.dto.trade;

import lombok.Data;

@Data
public class CreateBuyRequest {
    private Long itemId;
    private Double offeredPrice;
}