package smarttrade.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smarttrade.backend.entities.review.ReviewEntity;
import smarttrade.backend.entities.review.ReviewStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepo extends JpaRepository<ReviewEntity, Long> {

    boolean existsByTrade_TradeIdAndReviewer_UserId(
            Long tradeId,
            Long reviewerUserId
    );

    Optional<ReviewEntity> findByTrade_TradeIdAndReviewer_UserId(
            Long tradeId,
            Long reviewerUserId
    );

    List<ReviewEntity> findByTrade_TradeIdOrderByCreatedAtAscReviewIdAsc(
            Long tradeId
    );

    List<ReviewEntity> findByReviewee_UserIdAndStatusOrderByCreatedAtAscReviewIdAsc(
            Long revieweeId,
            ReviewStatus status
    );

    List<ReviewEntity> findByReviewee_UserIdOrderByCreatedAtDesc(
            Long revieweeId
    );

    long countByReviewee_UserIdAndStatus(
            Long revieweeId,
            ReviewStatus status
    );
}