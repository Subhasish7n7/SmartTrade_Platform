package smarttrade.backend.service.review;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import smarttrade.backend.Mappers.ReviewMapper;
import smarttrade.backend.dto.review.CreateReviewRequest;
import smarttrade.backend.dto.review.ReviewEligibilityResponse;
import smarttrade.backend.dto.review.ReviewResponse;
import smarttrade.backend.entities.review.ReviewEntity;
import smarttrade.backend.entities.review.ReviewStatus;
import smarttrade.backend.entities.trade.TradeEntity;
import smarttrade.backend.entities.trade.TradeStatus;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.events.trust.TrustEvent;
import smarttrade.backend.events.trust.TrustEventSourceType;
import smarttrade.backend.events.trust.TrustEventType;
import smarttrade.backend.exceptions.ForbiddenOperationException;
import smarttrade.backend.repository.ReviewRepo;
import smarttrade.backend.repository.trades.TradeRepo;
import smarttrade.backend.security.AuthenticatedUserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepo reviewRepo;
    private final TradeRepo tradeRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final ReviewMapper reviewMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public ReviewResponse createReview(Long tradeId, CreateReviewRequest request) {
        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        TradeEntity trade = tradeRepo.findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found"));

        if (trade.getStatus() != TradeStatus.COMPLETED) {
            throw new IllegalStateException("Reviews are only allowed for completed trades.");
        }

        UserEntity reviewee = findOtherParticipant(trade, currentUser);

        if (reviewee == null) {
            throw new ForbiddenOperationException(
                    "Only trade participants can review each other."
            );
        }

        if (reviewee.getUserId().equals(currentUser.getUserId())) {
            throw new IllegalStateException(
                    "Users cannot review themselves."
            );
        }

        if (reviewRepo.existsByTrade_TradeIdAndReviewer_UserId(tradeId, currentUser.getUserId())) {
            throw new IllegalStateException("You have already reviewed this trade.");
        }

        String comment = normalizeComment(request.comment());

        ReviewEntity review = ReviewEntity.builder()
                .trade(trade)
                .reviewer(currentUser)
                .reviewee(reviewee)
                .rating(request.rating())
                .comment(comment)
                .status(ReviewStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        try {
            /*
             * saveAndFlush makes the database uniqueness constraint visible
             * inside this request, protecting against concurrent duplicate
             * review submissions.
             */
            review = reviewRepo.saveAndFlush(review);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("You have already reviewed this trade.");
        }

        /*
         * Review is the authoritative source event.
         *
         * Only the reviewee's score is recalculated.
         */
        applicationEventPublisher.publishEvent(
                TrustEvent.now(
                        reviewee.getUserId(),
                        TrustEventType.REVIEW_RECEIVED,
                        TrustEventSourceType.REVIEW,
                        review.getReviewId(),
                        currentUser.getUserId(),
                        "Received review for completed trade " + tradeId
                )
        );

        return reviewMapper.mapFromEntity(review);
    }

    @Transactional
    public List<ReviewResponse> getTradeReviews(Long tradeId) {

        if (!tradeRepo.existsById(tradeId)) {
            throw new IllegalArgumentException("Trade not found");
        }

        return reviewRepo
                .findByTrade_TradeIdOrderByCreatedAtAscReviewIdAsc(tradeId)
                .stream()
                .filter(review -> review.getStatus() == ReviewStatus.ACTIVE)
                .map(reviewMapper::mapFromEntity)
                .toList();
    }

    @Transactional
    public List<ReviewResponse> getUserReviews(Long userId) {

        return reviewRepo
                .findByReviewee_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(review -> review.getStatus() == ReviewStatus.ACTIVE)
                .map(reviewMapper::mapFromEntity)
                .toList();
    }

    @Transactional
    public ReviewEligibilityResponse getEligibility(Long tradeId) {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        TradeEntity trade = tradeRepo.findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found"));

        UserEntity reviewee = findOtherParticipant(trade, currentUser);

        if (reviewee == null) {
            throw new ForbiddenOperationException(
                    "Only trade participants can access review eligibility."
            );
        }

        boolean alreadyReviewed = reviewRepo.existsByTrade_TradeIdAndReviewer_UserId(
                tradeId,
                currentUser.getUserId()
        );

        if (trade.getStatus() != TradeStatus.COMPLETED) {
            return new ReviewEligibilityResponse(
                    tradeId,
                    false,
                    alreadyReviewed,
                    reviewee.getUserId(),
                    reviewee.getName(),
                    "Trade is not completed."
            );
        }

        if (alreadyReviewed) {
            return new ReviewEligibilityResponse(
                    tradeId,
                    false,
                    true,
                    reviewee.getUserId(),
                    reviewee.getName(),
                    "You have already reviewed this trade."
            );
        }

        return new ReviewEligibilityResponse(
                tradeId,
                true,
                false,
                reviewee.getUserId(),
                reviewee.getName(),
                null
        );
    }

    private UserEntity findOtherParticipant(TradeEntity trade, UserEntity currentUser) {

        Long currentUserId = currentUser.getUserId();

        if (Objects.equals(trade.getInitiator().getUserId(), currentUserId)) {
            return trade.getReceiver();
        }

        if (Objects.equals(trade.getReceiver().getUserId(), currentUserId)) {
            return trade.getInitiator();
        }

        return null;
    }

    private String normalizeComment(String comment) {

        if (comment == null) {
            return null;
        }

        String normalized = comment.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}