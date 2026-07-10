package smarttrade.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import smarttrade.backend.entities.TradeEntity;

import java.util.List;

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
    smarttrade.backend.entities.TradeStatus.OPEN,
    smarttrade.backend.entities.TradeStatus.NEGOTIATING,
    smarttrade.backend.entities.TradeStatus.ACCEPTED
)
""")
    Integer countActiveTradesBetweenUsers(Long user1, Long user2);

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
}
