package smarttrade.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smarttrade.backend.entities.TradeEntity;

@Repository
public interface TradeRepo extends JpaRepository<TradeEntity, Long> {
}
