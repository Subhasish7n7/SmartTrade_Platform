package smarttrade.backend.Mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import smarttrade.backend.dto.trade.*;
import smarttrade.backend.entities.TradeEntity;
import smarttrade.backend.entities.TradeOfferEntity;
import smarttrade.backend.entities.ItemEntity;
import smarttrade.backend.entities.UserEntity;
import smarttrade.backend.repository.TradeOfferRepo;
import smarttrade.backend.security.AuthenticatedUserService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TradeMapper {

    private final AuthenticatedUserService authenticatedUserService;
    private final TradeOfferRepo tradeOfferRepo;

    private TradeItemResponse mapItem(ItemEntity item) {

        return TradeItemResponse.builder()
                .itemId(item.getItemId())
                .name(item.getItemName())
                .imageUrl(item.getImageUrls().getFirst())
                .userPrice(item.getUserPrice())
                .systemGeneratedPrice(item.getGeneratedPrice())
                .condition(item.getCondition())
                .build();
    }
    public TradeOfferResponse mapFromEntity(TradeOfferEntity entity) {

        if (entity == null) {
            return null;
        }

        return TradeOfferResponse.builder()
                .tradeOfferId(entity.getId())
                .senderId(entity.getSender().getUserId())
                .receiverId(entity.getReceiver().getUserId())

                .senderItems(entity.getSenderItems()
                                .stream()
                                .map(this::mapItem)
                                .toList()
                )

                .receiverItems(entity.getReceiverItems()
                                .stream()
                                .map(this::mapItem)
                                .toList()
                )

                .cashAdjustment(entity.getCashAdjustment())
                .status(entity.getTrade().getStatus().name())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public TradeInboxResponse mapInboxTrade(TradeEntity trade, UserEntity currentUser, TradeOfferEntity offer) {

        UserEntity otherUser = trade.getInitiator().getUserId()
                        .equals(currentUser.getUserId()) ? trade.getReceiver() : trade.getInitiator();
        List<ItemEntity> yourItems=offer.getSender().getUserId()
                .equals(currentUser.getUserId())?offer.getSenderItems():offer.getReceiverItems();
        List<ItemEntity> theirItems=offer.getSender().getUserId()
                .equals(currentUser.getUserId())?offer.getReceiverItems():offer.getSenderItems();
        int yourValue = yourItems.stream()
                .mapToInt(ItemEntity::getUserPrice)
                .sum();

        int theirValue = theirItems.stream()
                .mapToInt(ItemEntity::getUserPrice)
                .sum();

        int valueDifference = yourValue - theirValue;

        return TradeInboxResponse.builder()
                .tradeId(trade.getTradeId())
                .otherUserId(otherUser.getUserId())
                .otherUserName(otherUser.getName())
                .status(trade.getStatus().name())
                .yourItemCount(yourItems.size())
                .theirItemCount(theirItems.size())
                .valueDifference(valueDifference)
                .updatedAt(offer.getCreatedAt())
                .latestOffer(mapFromEntity(offer))
                .build();
    }
    public TradeDetailsResponse mapTradeDetails(TradeEntity trade,
                            TradeOfferResponse offer, List<TradeHistoryResponse> history) {

        return TradeDetailsResponse.builder()
                .tradeId(trade.getTradeId())
                .initiatorId(trade.getInitiator().getUserId())
                .initiatorName(trade.getInitiator().getName())
                .receiverId(trade.getReceiver().getUserId())
                .receiverName(trade.getReceiver().getName())
                .status(trade.getStatus().name())
                .createdAt(trade.getCreatedAt())
                .offer(offer)
                .history(history)
                .build();
    }
}