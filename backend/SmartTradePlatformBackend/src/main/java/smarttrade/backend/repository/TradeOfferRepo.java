package smarttrade.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smarttrade.backend.entities.TradeOfferEntity;

import java.util.List;

@Repository
public interface TradeOfferRepo extends JpaRepository<TradeOfferEntity, Long> {
    List<TradeOfferEntity> findByTrade_TradeId(Long tradeId);
}

