package smarttrade.backend.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import smarttrade.backend.entities.TradeOfferEntity;
import smarttrade.backend.repository.TradeOfferRepo;
import smarttrade.backend.repository.itemRepo;
import smarttrade.backend.repository.userRepo;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeOfferRepo tradeRepo;

    public TradeOfferEntity createTradeOffer(TradeOfferEntity entity) {
        entity.setStatus("PENDING");
        entity.setCreatedAt(LocalDateTime.now());
        return tradeRepo.save(entity);
    }

    public void updateTradeStatus(Long tradeId, String status) {
        TradeOfferEntity offer = tradeRepo.findById(tradeId).orElseThrow();
        offer.setStatus(status);
        tradeRepo.save(offer);
    }
}

