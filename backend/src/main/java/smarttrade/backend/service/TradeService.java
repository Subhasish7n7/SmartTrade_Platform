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
    private final NotificationService notificationService;

    @Transactional
    public TradeOfferEntity createTradeOffer(CreateTradeOfferRequest request) {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        UserEntity receiver = userRepo.findById(request.getReceiverId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Receiver not found"));

        if (currentUser.getUserId().equals(receiver.getUserId())) {
            throw new IllegalArgumentException("Cannot trade with yourself");
        }

        List<ItemEntity> senderItems = loadItems(request.getSenderItemIds());

        List<ItemEntity> receiverItems = loadItems(request.getReceiverItemIds());

        validateTradeItems(senderItems, receiverItems, currentUser, receiver);

        TradeEntity trade;

        if (request.getTradeId() == null) {

            trade = TradeEntity.builder()
                    .initiator(currentUser)
                    .receiver(receiver)
                    .status(TradeStatus.OPEN)
                    .createdAt(LocalDateTime.now())
                    .build();

        } else {

            trade = tradeRepo.findById(request.getTradeId())
                    .orElseThrow(() -> new IllegalArgumentException("Trade not found"));

            if (trade.getStatus() != TradeStatus.OPEN && trade.getStatus() != TradeStatus.NEGOTIATING) {

                throw new IllegalStateException("Trade can no longer receive offers.");
            }

            trade.setStatus(TradeStatus.NEGOTIATING);

        }
        trade = tradeRepo.save(trade);

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

        log.info("User {} created trade offer {}", currentUser.getEmail(), trade.getTradeId());

        TradeOfferEntity savedOffer = tradeOfferRepo.save(offer);

        boolean counter = tradeOfferRepo.countByTrade_TradeId(trade.getTradeId()) > 1;

        notificationService.notifyTradeOfferReceived(
                currentUser,
                receiver,
                trade.getTradeId(),
                counter
        );

        return savedOffer;
    }
    @Transactional
    public TradeOfferEntity createBuyOffer(CreateBuyRequest request) {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        ItemEntity targetItem = itemRepo.findById(request.getItemId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Item not found"));

        UserEntity seller = targetItem.getUser();

        if (seller.getUserId().equals(currentUser.getUserId())) {
            throw new IllegalArgumentException(
                    "Cannot buy your own item");
        }

        if (!targetItem.isForSale()) {
            throw new IllegalStateException(
                    "Item is not for sale");
        }

        validateTradeItems(List.of(), List.of(targetItem), currentUser, seller);

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

        TradeOfferEntity savedOffer = tradeOfferRepo.save(offer);

        notificationService.notifyBuyOffer(
                currentUser,
                seller,
                trade.getTradeId()
        );

        log.info(
                "User {} created buy offer {}",
                currentUser.getEmail(),
                trade.getTradeId()
        );

        return savedOffer;
    }

    @Transactional
    public void acceptTrade(Long tradeId) {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        TradeEntity trade = tradeRepo.findById(tradeId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Trade not found"));

        if (trade.getStatus() != TradeStatus.OPEN && trade.getStatus() != TradeStatus.NEGOTIATING) {

            throw new IllegalStateException("Trade cannot be accepted");
        }

        TradeOfferEntity latestOffer = tradeOfferRepo.findTopByTrade_TradeIdOrderByCreatedAtDesc(tradeId)
                .orElseThrow(() -> new IllegalStateException("No offers found"));

        if (!latestOffer.getReceiver().getUserId().equals(currentUser.getUserId())) {

            throw new IllegalStateException("Only the latest receiver can accept the trade.");
        }

        /*
         * Revalidate latest offer.
         *
         * Items may have changed ownership or become unavailable
         * while negotiation was happening.
         */
        validateTradeItems(
                latestOffer.getSenderItems(),
                latestOffer.getReceiverItems(),
                latestOffer.getSender(),
                latestOffer.getReceiver()
        );

        trade.setStatus(TradeStatus.ACCEPTED);

        tradeRepo.save(trade);

        notificationService.notifyTradeAccepted(
                currentUser,
                latestOffer.getSender(),
                tradeId
        );

        log.info("User {} accepted trade {}", currentUser.getEmail(), tradeId);
    }
    private void completeAcceptedTrade(TradeEntity trade) {

        TradeOfferEntity latestOffer = tradeOfferRepo
                .findTopByTrade_TradeIdOrderByCreatedAtDesc(trade.getTradeId())
                .orElseThrow(() ->
                        new IllegalStateException("No offers found"));

        LocalDateTime completedAt = LocalDateTime.now();

        latestOffer.getSenderItems().forEach(item -> {
            item.setAvailable(false);
            item.setTraded(true);
            item.setCompletedAt(completedAt);
        });

        latestOffer.getReceiverItems().forEach(item -> {
            item.setAvailable(false);
            item.setTraded(true);
            item.setCompletedAt(completedAt);
        });

        itemRepo.saveAll(latestOffer.getSenderItems());
        itemRepo.saveAll(latestOffer.getReceiverItems());

        trade.setStatus(TradeStatus.COMPLETED);
        
        trade.setInitiatorCompletionConfirmed(false);
        trade.setReceiverCompletionConfirmed(false);

        tradeRepo.save(trade);

        notificationService.notifyTradeCompleted(
                trade.getInitiator(),
                trade.getReceiver(),
                trade.getTradeId()
        );

        notificationService.notifyTradeCompleted(
                trade.getReceiver(),
                trade.getInitiator(),
                trade.getTradeId()
        );

        log.info("Trade {} completed successfully", trade.getTradeId());
    }
    private void validateParticipant(TradeEntity trade, UserEntity user) {

        boolean participant = trade.getInitiator().getUserId().equals(user.getUserId())
                                      ||
                              trade.getReceiver().getUserId().equals(user.getUserId());

        if (!participant) {
            throw new IllegalStateException("User not part of trade");
        }
    }
    private List<ItemEntity> loadItems(List<Long> itemIds) {

        List<ItemEntity> items = itemRepo.findAllById(itemIds);

        if (items.size() != itemIds.size()) {
            throw new IllegalArgumentException("One or more selected items do not exist.");
        }

        return items;
    }
    private void validateTradeItems(List<ItemEntity> senderItems, List<ItemEntity> receiverItems, UserEntity sender, UserEntity receiver) {

        for (ItemEntity item : senderItems) {

            if (!item.getUser().getUserId().equals(sender.getUserId())) {
                throw new IllegalArgumentException("Sender does not own item.");
            }

            if (!item.isAvailable()) {
                throw new IllegalStateException("Sender item unavailable.");
            }

            if (item.isSold()) {
                throw new IllegalStateException("Sender item already sold.");
            }

            if (item.isTraded()) {
                throw new IllegalStateException("Sender item already traded.");
            }
        }

        for (ItemEntity item : receiverItems) {

            if (!item.getUser().getUserId().equals(receiver.getUserId())) {
                throw new IllegalArgumentException("Receiver does not own requested item.");
            }

            if (!item.isAvailable()) {
                throw new IllegalStateException("Receiver item unavailable.");
            }

            if (item.isSold()) {
                throw new IllegalStateException("Receiver item already sold.");
            }

            if (item.isTraded()) {
                throw new IllegalStateException("Receiver item already traded.");
            }
        }
    }

    @Transactional
    public void requestCompletion(Long tradeId) {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        TradeEntity trade = tradeRepo.findById(tradeId)
                        .orElseThrow(() -> new IllegalArgumentException("Trade not found"));

        validateParticipant(trade, currentUser);
        if (trade.getStatus() != TradeStatus.ACCEPTED) {

            throw new IllegalStateException(
                    "Trade must be accepted first");
        }

        if (hasUserConfirmed(trade, currentUser)) {

            return;
        }

        setUserConfirmed(trade, currentUser);

        if (trade.getCompletionRequestedAt() == null) {
            trade.setCompletionRequestedAt(LocalDateTime.now());
        }

        tradeRepo.save(trade);

        UserEntity otherUser =
                trade.getInitiator().getUserId()
                        .equals(currentUser.getUserId())
                        ? trade.getReceiver()
                        : trade.getInitiator();

        notificationService.notifyCompletionRequested(
                currentUser,
                otherUser,
                tradeId
        );
    }

    @Transactional
    public void confirmCompletion(Long tradeId) {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        TradeEntity trade = tradeRepo.findById(tradeId)
                        .orElseThrow(() -> new IllegalArgumentException("Trade not found"));

        validateParticipant(trade, currentUser);

        if (trade.getStatus() != TradeStatus.ACCEPTED) {

            throw new IllegalStateException(
                    "Trade must be accepted first");
        }

        if (hasUserConfirmed(trade, currentUser)) {

            return;
        }

        setUserConfirmed(trade, currentUser);

        if (!trade.getInitiatorCompletionConfirmed()
                ||
                !trade.getReceiverCompletionConfirmed()) {

            tradeRepo.save(trade);

            return;
        }

        completeAcceptedTrade(trade);
    }

    @Transactional
    public void cancelTrade(Long tradeId) {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        TradeEntity trade = tradeRepo.findById(tradeId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Trade not found"));

        if (trade.getStatus() == TradeStatus.COMPLETED) {
            throw new IllegalStateException("Completed trades cannot be cancelled.");
        }

        if (trade.getStatus() == TradeStatus.CANCELLED) {
            return;
        }

        if (!trade.getInitiator().getUserId().equals(currentUser.getUserId())
                && !trade.getReceiver().getUserId().equals(currentUser.getUserId())) {

            throw new IllegalStateException("Only trade participants can cancel the trade.");
        }

        trade.setStatus(TradeStatus.CANCELLED);

        tradeRepo.save(trade);

        UserEntity otherUser = trade.getInitiator().getUserId().equals(currentUser.getUserId())
                        ? trade.getReceiver()
                        : trade.getInitiator();

        notificationService.notifyTradeCancelled(
                currentUser,
                otherUser,
                tradeId
        );

        log.info("User {} cancelled trade {}", currentUser.getEmail(), tradeId);
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
    private boolean hasUserConfirmed(TradeEntity trade, UserEntity user) {

        if (trade.getInitiator().getUserId().equals(user.getUserId())) {

            return trade.getInitiatorCompletionConfirmed();
        }

        return trade.getReceiverCompletionConfirmed();
    }
    private void setUserConfirmed(TradeEntity trade, UserEntity user) {

        if (trade.getInitiator().getUserId().equals(user.getUserId())) {

            trade.setInitiatorCompletionConfirmed(true);

        } else {
            trade.setReceiverCompletionConfirmed(true);
        }
    }
}