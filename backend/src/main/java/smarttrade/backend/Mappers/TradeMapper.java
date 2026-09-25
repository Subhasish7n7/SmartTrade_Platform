package smarttrade.backend.Mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import smarttrade.backend.dto.trade.*;
import smarttrade.backend.entities.item.ItemImageEntity;
import smarttrade.backend.entities.item.ItemImageStatus;
import smarttrade.backend.entities.trade.TradeEntity;
import smarttrade.backend.entities.trade.TradeOfferEntity;
import smarttrade.backend.entities.item.ItemEntity;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.repository.TradeOfferRepo;
import smarttrade.backend.security.AuthenticatedUserService;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TradeMapper {

    private final AuthenticatedUserService authenticatedUserService;
    private final TradeOfferRepo tradeOfferRepo;

    private TradeItemResponse mapItem(ItemEntity item) {

        return TradeItemResponse.builder()
                .itemId(item.getItemId())
                .name(item.getItemName())
                .imageUrl(
                        item.getImages().stream()
                                .filter(image -> image.getStatus() == ItemImageStatus.ACTIVE)
                                .min(Comparator.comparing(ItemImageEntity::getSortOrder))
                                .map(ItemImageEntity::getSecureUrl)
                                .orElse(null)
                )
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


        return TradeInboxResponse.builder()
                .tradeId(trade.getTradeId())
                .otherUserId(otherUser.getUserId())
                .otherUserName(otherUser.getName())
                .status(trade.getStatus().name())
                .yourItemCount(yourItems.size())
                .theirItemCount(theirItems.size())
                .cashAdjustment(offer.getCashAdjustment())
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