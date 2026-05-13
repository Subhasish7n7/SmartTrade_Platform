package smarttrade.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smarttrade.backend.entities.TradeOfferEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface TradeOfferRepo extends JpaRepository<TradeOfferEntity, Long> {
    Optional<TradeOfferEntity> findTopByTrade_TradeIdOrderByCreatedAtDesc(Long tradeId);
    List<TradeOfferEntity> findByTrade_TradeId(Long tradeId);
}

