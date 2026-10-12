package smarttrade.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smarttrade.backend.entities.trust.TrustScoreHistoryEntity;

@Repository
public interface TrustScoreHistoryRepo extends JpaRepository<TrustScoreHistoryEntity, Long> {

    Page<TrustScoreHistoryEntity> findByUser_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}