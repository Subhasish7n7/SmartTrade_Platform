package smarttrade.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import smarttrade.backend.entities.TradeEntity;
import smarttrade.backend.entities.TradeOfferEntity;
import smarttrade.backend.entities.TradeStatus;
import smarttrade.backend.entities.itemEntity;
import smarttrade.backend.repository.TradeOfferRepo;
import smarttrade.backend.repository.TradeRepo;
import smarttrade.backend.repository.itemRepo;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeOfferRepo tradeOfferRepo;
    private final TradeRepo tradeRepo;
    private final itemRepo itemRepo;

    @Transactional
    public TradeOfferEntity createTradeOffer(TradeOfferEntity offer) {

        TradeEntity trade;

        if (offer.getTrade() == null) {
            trade = TradeEntity.builder()
                    .initiator(offer.getCreatedBy())
                    .receiver(offer.getReceiver())
                    .status(TradeStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();

            trade = tradeRepo.save(trade);
            offer.setTrade(trade);
        } else {
            trade = tradeRepo.findById(offer.getTrade().getTradeId()).orElseThrow();
        }

        // 🔒 LOCK ITEMS
        lockItems(offer.getSenderItems(), trade.getTradeId());

        offer.setCreatedAt(LocalDateTime.now());

        return tradeOfferRepo.save(offer);
    }

    @Transactional
    public void acceptTrade(Long tradeId) {

        TradeEntity trade = tradeRepo.findById(tradeId).orElseThrow();

        if (trade.getStatus() != TradeStatus.PENDING) {
            throw new IllegalStateException("Invalid state");
        }

        trade.setStatus(TradeStatus.ACCEPTED);

        List<TradeOfferEntity> offers = tradeOfferRepo.findByTrade_TradeId(tradeId);
        TradeOfferEntity latestOffer = offers.get(offers.size() - 1);

        // mark items unavailable
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
    }

    private void lockItems(List<itemEntity> items, Long tradeId) {
        for (itemEntity item : items) {
            if (item.isLocked()) {
                throw new IllegalStateException("Item already locked");
            }
            item.setLocked(true);
            item.setLockedByTradeId(tradeId);
        }
        itemRepo.saveAll(items);
    }
}