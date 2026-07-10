package smarttrade.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import smarttrade.backend.Mappers.TradeMapper;
import smarttrade.backend.dto.trade.*;
import smarttrade.backend.entities.TradeEntity;
import smarttrade.backend.entities.TradeOfferEntity;
import smarttrade.backend.entities.TradeStatus;
import smarttrade.backend.entities.ItemEntity;
import smarttrade.backend.repository.TradeOfferRepo;
import smarttrade.backend.repository.TradeRepo;
import smarttrade.backend.repository.ItemRepo;
import lombok.extern.slf4j.Slf4j;
import smarttrade.backend.entities.UserEntity;
import smarttrade.backend.repository.UserRepo;
import smarttrade.backend.security.AuthenticatedUserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeService {

    private final TradeOfferRepo tradeOfferRepo;
    private final TradeRepo tradeRepo;
    private final ItemRepo itemRepo;
    private final UserRepo userRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final TradeMapper tradeMapper;

    @Transactional
    public TradeOfferEntity createTradeOffer(CreateTradeOfferRequest request) {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        UserEntity receiver = userRepo.findById(request.getReceiverId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Receiver not found"));

        if (currentUser.getUserId().equals(receiver.getUserId())) {
            throw new IllegalArgumentException("Cannot trade with yourself");
        }

        List<ItemEntity> senderItems = itemRepo.findAllById(request.getSenderItemIds());
        List<ItemEntity> receiverItems = itemRepo.findAllById(request.getReceiverItemIds());

        for (ItemEntity item : senderItems) {
            if (!item.getUser().getUserId().equals(currentUser.getUserId())) {
                throw new IllegalArgumentException("Sender does not own item");
            }
            if (!item.isAvailable()) {
                throw new IllegalStateException("Sender item unavailable");
            }
            if (item.isLocked()) {throw new IllegalStateException("Sender item already locked");
            }
        }

        for (ItemEntity item : receiverItems) {
            if (!item.getUser().getUserId().equals(receiver.getUserId())) {
                throw new IllegalArgumentException("Receiver does not own requested item");
            }

            if (!item.isAvailable()) {
                throw new IllegalStateException("Receiver item unavailable");
            }
        }

        TradeEntity trade;

        if (request.getTradeId() == null) {

            trade = TradeEntity.builder()
                    .initiator(currentUser)
                    .receiver(receiver)
                    .status(TradeStatus.OPEN)
                    .createdAt(LocalDateTime.now())
                    .build();

            trade = tradeRepo.save(trade);

        } else {
            trade = tradeRepo.findById(request.getTradeId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Trade not found"));
        }

        lockItems(senderItems, trade.getTradeId());

        TradeOfferEntity offer = TradeOfferEntity.builder()
                .sender(currentUser)
                .receiver(receiver)
                .senderItems(senderItems)
                .receiverItems(receiverItems)
                .trade(trade)
                .createdBy(currentUser)
                .cashAdjustment(request.getCashAdjustment())
                .createdAt(LocalDateTime.now())
                .build();

        log.info(
                "User {} created trade offer for trade {}",
                currentUser.getEmail(),
                trade.getTradeId()
        );

        return tradeOfferRepo.save(offer);
    }
    @Transactional
    public TradeOfferEntity createBuyOffer(CreateBuyRequest request) {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        ItemEntity targetItem = itemRepo.findById(request.getItemId())
                        .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        if (!targetItem.isForSale()) {
            throw new IllegalStateException("Item is not for sale");
        }

        if (!targetItem.isAvailable()) {
            throw new IllegalStateException("Item unavailable");
        }

        UserEntity seller = targetItem.getUser();

        if (seller.getUserId().equals(currentUser.getUserId())) {
            throw new IllegalArgumentException("Cannot buy your own item");
        }

        TradeEntity trade = TradeEntity.builder()
                .initiator(currentUser)
                .receiver(seller)
                .status(TradeStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        trade = tradeRepo.save(trade);

        TradeOfferEntity offer = TradeOfferEntity.builder()
                        .sender(currentUser)
                        .receiver(seller)
                        .senderItems(List.of())
                        .receiverItems(List.of(targetItem))
                        .trade(trade)
                        .createdBy(currentUser)
                        .cashAdjustment(request.getOfferedPrice())
                        .createdAt(LocalDateTime.now())
                        .build();

        log.info(
                "User {} created buy offer for item {}",
                currentUser.getEmail(),
                targetItem.getItemId()
        );

        return tradeOfferRepo.save(offer);
    }

    @Transactional
    public void acceptTrade(Long tradeId) {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        TradeEntity trade = tradeRepo.findById(tradeId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Trade not found"));

        if (trade.getStatus() != TradeStatus.OPEN
                && trade.getStatus() != TradeStatus.NEGOTIATING){
            throw new IllegalStateException("Trade cannot be accepted");
        }

        TradeOfferEntity latestOffer = tradeOfferRepo
                .findTopByTrade_TradeIdOrderByCreatedAtDesc(tradeId)
                        .orElseThrow(() ->
                                new IllegalStateException("No offers found"));

    /*
        Only latest receiver can accept.
    */
        if (!latestOffer.getReceiver().getUserId().equals(currentUser.getUserId())) {
            throw new IllegalStateException("Only latest receiver can accept trade");
        }

        trade.setStatus(TradeStatus.ACCEPTED);
        tradeRepo.save(trade);

        log.info(
                "User {} accepted trade {}",
                currentUser.getEmail(),
                tradeId
        );
    }
    @Transactional
    public void completeTrade(Long tradeId) {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        TradeEntity trade = tradeRepo.findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found"));

        if (trade.getStatus() != TradeStatus.ACCEPTED) {
            throw new IllegalStateException("Trade must be accepted first");
        }

        boolean isParticipant = trade.getInitiator().getUserId()
                        .equals(currentUser.getUserId())
                        ||
                        trade.getReceiver().getUserId()
                                .equals(currentUser.getUserId());

        if (!isParticipant) {
            throw new IllegalStateException("User not part of trade");
        }

        TradeOfferEntity latestOffer = tradeOfferRepo
                        .findTopByTrade_TradeIdOrderByCreatedAtDesc(tradeId)
                        .orElseThrow(() ->
                                new IllegalStateException("No offers found"));

        latestOffer.getSenderItems().forEach(i -> {
            i.setAvailable(false);
            i.setLocked(false);
            i.setTraded(true);
            i.setCompletedAt(LocalDateTime.now());
        });

        latestOffer.getReceiverItems().forEach(i -> {
            i.setAvailable(false);
            i.setLocked(false);
            i.setTraded(true);
            i.setCompletedAt(LocalDateTime.now());
        });

        itemRepo.saveAll(latestOffer.getSenderItems());
        itemRepo.saveAll(latestOffer.getReceiverItems());
        trade.setStatus(TradeStatus.COMPLETED);
        tradeRepo.save(trade);
    }
    @Transactional
    public void cancelTrade(Long tradeId) {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        TradeEntity trade = tradeRepo.findById(tradeId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Trade not found"));

        boolean isParticipant = trade.getInitiator().getUserId()
                        .equals(currentUser.getUserId())
                        ||
                        trade.getReceiver().getUserId()
                                .equals(currentUser.getUserId());

        if (!isParticipant) {
            throw new IllegalStateException("User not part of trade");
        }

        TradeOfferEntity latestOffer = tradeOfferRepo
                        .findTopByTrade_TradeIdOrderByCreatedAtDesc(tradeId)
                        .orElseThrow(() ->
                                new IllegalStateException("No offers found"));

        latestOffer.getSenderItems().forEach(i -> {
            i.setLocked(false);
            i.setLockedByTradeId(null);
        });

        itemRepo.saveAll(latestOffer.getSenderItems());
        trade.setStatus(TradeStatus.CANCELLED);
        tradeRepo.save(trade);
    }

    public List<TradeInboxResponse> getTradeInbox() {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        return tradeRepo.findUserTrades(currentUser.getUserId())
                .stream()
                .map(trade -> {
                    TradeOfferEntity offer = tradeOfferRepo
                            .findTopByTrade_TradeIdOrderByCreatedAtDesc(trade.getTradeId())
                            .orElseThrow(()->new IllegalArgumentException("Trade offer not found"));
                    return tradeMapper.mapInboxTrade(trade, currentUser, offer);
                })
                .toList();
    }
    public List<TradeOfferResponse> getTradeOfferHistory(Long tradeId) {

        return tradeOfferRepo
                .findByTrade_TradeIdOrderByCreatedAtAsc(tradeId)
                .stream()
                .map(tradeMapper::mapFromEntity)
                .toList();
    }

    public TradeDetailsResponse getTrade(Long tradeId, Long tradeOfferId) {

        TradeEntity trade = tradeRepo.findById(tradeId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Trade not found"));

        TradeOfferEntity offerEntity = tradeOfferRepo.findById(tradeOfferId)
                .orElseThrow(()->
                        new IllegalArgumentException("trade offer not found"));

        if (!offerEntity.getTrade().getTradeId().equals(tradeId)) {
            throw new IllegalArgumentException("trade offer doesn't belong to the trade");
        }

        List<TradeHistoryResponse> history = tradeOfferRepo.findTradeHistoryByTradeId(tradeId);

        TradeOfferResponse offer= tradeMapper.mapFromEntity(offerEntity);

        return tradeMapper.mapTradeDetails(trade, offer, history);
    }
    public List<TradeItemResponse> getTradeableItems(Long userId) {

        List<ItemEntity> items = itemRepo.findTradeableItemsByUserId(userId);

        return items.stream()
                .map(item -> TradeItemResponse.builder()
                        .itemId(item.getItemId())
                        .name(item.getItemName())
                        .imageUrl(
                                item.getImageUrls().isEmpty()
                                        ? null
                                        : item.getImageUrls().getFirst()
                        )
                        .userPrice(item.getUserPrice())
                        .systemGeneratedPrice(item.getGeneratedPrice())
                        .condition(item.getCondition())
                        .build())
                .toList();
    }

    private void lockItems(List<ItemEntity> items, Long tradeId) {
        for (ItemEntity item : items) {
            if (item.isLocked()) {
                throw new IllegalStateException("Item already locked");
            }
            item.setLocked(true);
            item.setLockedByTradeId(tradeId);
        }
        itemRepo.saveAll(items);
    }
}