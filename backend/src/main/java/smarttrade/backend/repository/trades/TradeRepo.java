package smarttrade.backend.repository.trades;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import smarttrade.backend.entities.trade.TradeEntity;
import smarttrade.backend.entities.trade.TradeStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TradeRepo extends JpaRepository<TradeEntity, Long> {

    @Query("""
            SELECT COUNT(t)
            FROM TradeEntity t
            WHERE
            (
                (t.initiator.userId = :user1
                    AND t.receiver.userId = :user2)
                OR
                (t.initiator.userId = :user2
                    AND t.receiver.userId = :user1)
            )
            AND t.status IN (
                smarttrade.backend.entities.trade.TradeStatus.OPEN,
                smarttrade.backend.entities.trade.TradeStatus.NEGOTIATING,
                smarttrade.backend.entities.trade.TradeStatus.ACCEPTED,
                smarttrade.backend.entities.trade.TradeStatus.DISPUTED
            )
            """)
    Integer countActiveTradesBetweenUsers(Long user1, Long user2);

    @Query("""
                SELECT t
                FROM TradeEntity t
                WHERE t.status = smarttrade.backend.entities.trade.TradeStatus.ACCEPTED
                  AND t.completionRequestedAt IS NOT NULL
                  AND t.completionRequestedAt <= :cutoff
            """)
    List<TradeEntity> findAcceptedTradesWithExpiredCompletionDeadline(
            @Param("cutoff") LocalDateTime cutoff
    );

    @Query("""
            SELECT t
            FROM TradeEntity t
            WHERE
                t.initiator.userId = :userId
                OR
                t.receiver.userId = :userId
            ORDER BY t.createdAt DESC
            """)
    List<TradeEntity> findUserTrades(Long userId);

    @Query("""
            SELECT DISTINCT t
            FROM TradeEntity t
            JOIN TradeOfferEntity o
            ON o.trade.tradeId = t.tradeId
            WHERE
            (
                t.status = smarttrade.backend.entities.trade.TradeStatus.OPEN
                OR
                t.status = smarttrade.backend.entities.trade.TradeStatus.NEGOTIATING
            )
            AND o.createdAt = (
            
                SELECT MAX(o2.createdAt)
            
                FROM TradeOfferEntity o2
            
                WHERE o2.trade.tradeId = t.tradeId
            
            )
            AND o.createdAt <= :cutoff
            """)
    List<TradeEntity> findExpiredNegotiations(LocalDateTime cutoff);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT t
            FROM TradeEntity t
            WHERE t.tradeId = :tradeId
            """)
    Optional<TradeEntity> findByIdForUpdate(
            @Param("tradeId") Long tradeId
    );

    @Query("""
            SELECT t
            FROM TradeEntity t
            WHERE t.status = smarttrade.backend.entities.trade.TradeStatus.ACCEPTED
              AND t.completionRequestedAt IS NOT NULL
              AND t.completionRequestedAt <= :cutoff
              AND (
                    (t.initiatorCompletionConfirmed = true
                     AND t.receiverCompletionConfirmed = false)
                    OR
                    (t.initiatorCompletionConfirmed = false
                     AND t.receiverCompletionConfirmed = true)
                  )
            """)
    List<TradeEntity> findAcceptedTradesWithPendingCompletionBefore(
            @Param("cutoff") LocalDateTime cutoff
    );

    @Query("""
            SELECT t
            FROM TradeEntity t
            WHERE
                (
                    LOWER(t.initiator.name) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(t.initiator.email) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(t.receiver.name) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(t.receiver.email) LIKE LOWER(CONCAT('%', :search, '%'))
                )
                AND (:status IS NULL OR t.status = :status)
            ORDER BY t.createdAt DESC
            """)
    Page<TradeEntity> searchAdminTrades(
            @Param("search") String search,
            @Param("status") TradeStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT t
            FROM TradeEntity t
            WHERE
                (:status IS NULL OR t.status = :status)
            ORDER BY t.createdAt DESC
            """)
    Page<TradeEntity> searchAdminTradesByStatus(
            @Param("status") TradeStatus status,
            Pageable pageable
    );

    Page<TradeEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(TradeStatus status);
}
