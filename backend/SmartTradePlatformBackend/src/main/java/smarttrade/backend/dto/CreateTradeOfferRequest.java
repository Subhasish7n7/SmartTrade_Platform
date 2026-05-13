package smarttrade.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateTradeOfferRequest {

    private Long receiverId;

    private List<Long> senderItemIds;

    private List<Long> receiverItemIds;

    private Double cashAdjustment;

    private Long tradeId;
}