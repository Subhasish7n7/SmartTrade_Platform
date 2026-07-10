package smarttrade.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import smarttrade.backend.dto.trade.TradeHistoryResponse;
import smarttrade.backend.entities.TradeOfferEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface TradeOfferRepo extends JpaRepository<TradeOfferEntity, Long> {
    Optional<TradeOfferEntity> findTopByTrade_TradeIdOrderByCreatedAtDesc(Long tradeId);
    List<TradeOfferEntity> findByTrade_TradeId(Long tradeId);
    List<TradeOfferEntity> findByTrade_TradeIdOrderByCreatedAtAsc(Long tradeId);
    @Query("""
    SELECT new smarttrade.backend.dto.trade.TradeHistoryResponse(
        t.id,
        s.userId,
        s.name,
        t.createdAt
    )
    FROM TradeOfferEntity t
    JOIN t.sender s
    WHERE t.trade.tradeId = :tradeId
    ORDER BY t.createdAt DESC
""")
    List<TradeHistoryResponse> findTradeHistoryByTradeId(Long tradeId);
}

