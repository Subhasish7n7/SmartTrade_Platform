package smarttrade.backend.TestDataUtil;

import smarttrade.backend.dto.TradeOfferDto;
import smarttrade.backend.entities.UserEntity;

import java.util.List;

public class tradeOfferTestData {
    public static TradeOfferDto createDummyTradeOfferDto(UserEntity sender, UserEntity receiver,
                                                         List<Long> senderItems, List<Long> receiverItems) {
        TradeOfferDto dto= new TradeOfferDto();
        dto.setSenderId(sender.getUserId());
        dto.setReceiverId(receiver.getUserId());
        dto.setSenderItemIds(senderItems);
        dto.setReceiverItemIds(receiverItems);
        return dto;
    }
}
