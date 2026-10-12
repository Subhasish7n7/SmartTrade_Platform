package smarttrade.backend.repository.trades;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smarttrade.backend.entities.trade.DisputeResolution;
import smarttrade.backend.entities.trade.DisputeStatus;
import smarttrade.backend.entities.trade.TradeDisputeEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TradeDisputeRepo extends JpaRepository<TradeDisputeEntity, Long> {
    long countByFaultUser_UserIdAndResolution(Long userId, DisputeResolution resolution);

    boolean existsByTrade_TradeIdAndStatusIn(Long tradeId, Collection<DisputeStatus> statuses);

    Optional<TradeDisputeEntity> findFirstByTrade_TradeIdOrderByCreatedAtDesc(Long tradeId);

    List<TradeDisputeEntity> findByTrade_TradeIdOrderByCreatedAtDesc(Long tradeId);

    Page<TradeDisputeEntity> findByStatus(DisputeStatus status, Pageable pageable);

    Page<TradeDisputeEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<TradeDisputeEntity> findTop5ByStatusInOrderByCreatedAtDesc(Collection<DisputeStatus> statuses);

    long countByStatus(DisputeStatus status);
}