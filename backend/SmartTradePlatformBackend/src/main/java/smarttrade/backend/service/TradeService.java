package smarttrade.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import smarttrade.backend.dto.CreateTradeOfferRequest;
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

    @Transactional
    public TradeOfferEntity createTradeOffer(
            CreateTradeOfferRequest request
    ) {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        UserEntity receiver = userRepo.findById(request.getReceiverId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Receiver not found"));

        if (currentUser.getUserId().equals(receiver.getUserId())) {
            throw new IllegalArgumentException(
                    "Cannot trade with yourself"
            );
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

            if (item.isLocked()) {
                throw new IllegalStateException("Sender item already locked");
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
                    .status(TradeStatus.PENDING)
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
    public void acceptTrade(Long tradeId) {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        TradeEntity trade = tradeRepo.findById(tradeId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Trade not found"));

        if (trade.getStatus() != TradeStatus.PENDING) {
            throw new IllegalStateException("Invalid trade state");
        }

        TradeOfferEntity latestOffer =
                tradeOfferRepo.findTopByTrade_TradeIdOrderByCreatedAtDesc(tradeId)
                        .orElseThrow(() ->
                                new IllegalStateException("No offers found"));

    /*
        Only latest receiver can accept.
    */
        if (!latestOffer.getReceiver().getUserId().equals(currentUser.getUserId())) {
            throw new IllegalStateException(
                    "Only latest receiver can accept trade"
            );
        }

        trade.setStatus(TradeStatus.ACCEPTED);

        latestOffer.getSenderItems().forEach(i -> {
            i.setAvailable(false);
            i.setLocked(false);
        });
        latestOffer.getReceiverItems().forEach(i -> {
            i.setAvailable(false);
            i.setLocked(false);
        });

        itemRepo.saveAll(latestOffer.getSenderItems());
        itemRepo.saveAll(latestOffer.getReceiverItems());

        tradeRepo.save(trade);

        log.info(
                "User {} accepted trade {}",
                currentUser.getEmail(),
                tradeId
        );
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