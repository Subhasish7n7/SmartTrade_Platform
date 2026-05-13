package smarttrade.backend.Mappers;

import org.springframework.stereotype.Component;
import smarttrade.backend.dto.TradeOfferResponse;
import smarttrade.backend.entities.TradeOfferEntity;
import smarttrade.backend.entities.ItemEntity;

import java.util.stream.Collectors;

@Component
public class TradeMapper {

    public TradeOfferResponse mapFromEntity(TradeOfferEntity entity) {

        if (entity == null) {
            return null;
        }

        return TradeOfferResponse.builder()
                .tradeId(entity.getTrade().getTradeId())
                .senderId(entity.getSender().getUserId())
                .receiverId(entity.getReceiver().getUserId())
                .senderItemIds(
                        entity.getSenderItems()
                                .stream()
                                .map(ItemEntity::getItemId)
                                .collect(Collectors.toList())
                )
                .receiverItemIds(
                        entity.getReceiverItems()
                                .stream()
                                .map(ItemEntity::getItemId)
                                .collect(Collectors.toList())
                )
                .cashAdjustment(entity.getCashAdjustment())
                .status(entity.getTrade().getStatus().name())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}