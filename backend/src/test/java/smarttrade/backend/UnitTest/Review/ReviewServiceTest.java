package smarttrade.backend.UnitTest.Review;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
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
import smarttrade.backend.events.trust.TrustEventType;
import smarttrade.backend.exceptions.ForbiddenOperationException;
import smarttrade.backend.repository.ReviewRepo;
import smarttrade.backend.repository.trades.TradeRepo;
import smarttrade.backend.security.AuthenticatedUserService;
import smarttrade.backend.service.review.ReviewService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ReviewRepo reviewRepo;

    @Mock
    private TradeRepo tradeRepo;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ReviewService reviewService;

    private UserEntity initiator;
    private UserEntity receiver;
    private UserEntity outsider;
    private TradeEntity completedTrade;

    @BeforeEach
    void setUp() {
        initiator = user(1L, "alice@test.com", "Alice");
        receiver = user(2L, "bob@test.com", "Bob");
        outsider = user(3L, "outsider@test.com", "Outsider");

        completedTrade = TradeEntity.builder()
                .tradeId(100L)
                .initiator(initiator)
                .receiver(receiver)
                .status(TradeStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldCreateReviewAndPublishTrustEventForReviewee() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(initiator);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(completedTrade));
        when(reviewRepo.existsByTrade_TradeIdAndReviewer_UserId(100L, 1L))
                .thenReturn(false);

        LocalDateTime createdAt = LocalDateTime.now();
        ReviewEntity saved = ReviewEntity.builder()
                .reviewId(50L)
                .trade(completedTrade)
                .reviewer(initiator)
                .reviewee(receiver)
                .rating(5)
                .comment("Great trader")
                .status(ReviewStatus.ACTIVE)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        when(reviewRepo.saveAndFlush(any(ReviewEntity.class))).thenReturn(saved);
        when(reviewMapper.mapFromEntity(saved)).thenReturn(
                new ReviewResponse(
                        50L,
                        100L,
                        1L,
                        "Alice",
                        null,
                        2L,
                        "Bob",
                        null,
                        5,
                        "Great trader",
                        ReviewStatus.ACTIVE,
                        createdAt
                )
        );

        ReviewResponse result = reviewService.createReview(
                100L,
                new CreateReviewRequest(5, "  Great trader  ")
        );

        assertEquals(50L, result.reviewId());
        assertEquals(2L, result.revieweeId());
        assertEquals("Great trader", result.comment());

        ArgumentCaptor<ReviewEntity> reviewCaptor =
                ArgumentCaptor.forClass(ReviewEntity.class);
        verify(reviewRepo).saveAndFlush(reviewCaptor.capture());

        ReviewEntity requestReview = reviewCaptor.getValue();
        assertSame(initiator, requestReview.getReviewer());
        assertSame(receiver, requestReview.getReviewee());
        assertSame(completedTrade, requestReview.getTrade());
        assertEquals(5, requestReview.getRating());
        assertEquals("Great trader", requestReview.getComment());
        assertEquals(ReviewStatus.ACTIVE, requestReview.getStatus());

        ArgumentCaptor<TrustEvent> eventCaptor =
                ArgumentCaptor.forClass(TrustEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

        TrustEvent event = eventCaptor.getValue();
        assertEquals(receiver.getUserId(), event.userId());
        assertEquals(TrustEventType.REVIEW_RECEIVED, event.type());
        assertEquals(50L, event.sourceId());
        assertEquals(initiator.getUserId(), event.relatedUserId());
    }

    @Test
    void receiverCanReviewInitiator() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(receiver);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(completedTrade));
        when(reviewRepo.existsByTrade_TradeIdAndReviewer_UserId(100L, 2L))
                .thenReturn(false);

        ReviewEntity saved = ReviewEntity.builder()
                .reviewId(51L)
                .trade(completedTrade)
                .reviewer(receiver)
                .reviewee(initiator)
                .rating(4)
                .status(ReviewStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(reviewRepo.saveAndFlush(any(ReviewEntity.class))).thenReturn(saved);
        when(reviewMapper.mapFromEntity(saved)).thenReturn(
                new ReviewResponse(
                        51L, 100L, 2L, "Bob", null,
                        1L, "Alice", null, 4, null,
                        ReviewStatus.ACTIVE, saved.getCreatedAt()
                )
        );

        ReviewResponse result = reviewService.createReview(
                100L,
                new CreateReviewRequest(4, null)
        );

        assertEquals(1L, result.revieweeId());
        verify(reviewRepo).saveAndFlush(any(ReviewEntity.class));
    }

    @Test
    void shouldRejectNonParticipant() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(outsider);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(completedTrade));

        assertThrows(
                ForbiddenOperationException.class,
                () -> reviewService.createReview(
                        100L,
                        new CreateReviewRequest(5, "test")
                )
        );

        verify(reviewRepo, never()).saveAndFlush(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldRejectIncompleteTrade() {
        completedTrade.setStatus(TradeStatus.ACCEPTED);
        when(authenticatedUserService.getCurrentUser()).thenReturn(initiator);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(completedTrade));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> reviewService.createReview(
                        100L,
                        new CreateReviewRequest(5, "test")
                )
        );

        assertEquals(
                "Reviews are only allowed for completed trades.",
                exception.getMessage()
        );
        verify(reviewRepo, never()).saveAndFlush(any());
    }

    @Test
    void shouldRejectCancelledTrade() {
        completedTrade.setStatus(TradeStatus.CANCELLED);
        when(authenticatedUserService.getCurrentUser()).thenReturn(initiator);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(completedTrade));

        assertThrows(
                IllegalStateException.class,
                () -> reviewService.createReview(
                        100L,
                        new CreateReviewRequest(5, "test")
                )
        );

        verify(reviewRepo, never()).saveAndFlush(any());
    }

    @Test
    void shouldRejectDisputedTrade() {
        completedTrade.setStatus(TradeStatus.DISPUTED);
        when(authenticatedUserService.getCurrentUser()).thenReturn(initiator);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(completedTrade));

        assertThrows(
                IllegalStateException.class,
                () -> reviewService.createReview(
                        100L,
                        new CreateReviewRequest(5, "test")
                )
        );

        verify(reviewRepo, never()).saveAndFlush(any());
    }

    @Test
    void shouldRejectDuplicateReview() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(initiator);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(completedTrade));
        when(reviewRepo.existsByTrade_TradeIdAndReviewer_UserId(100L, 1L))
                .thenReturn(true);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> reviewService.createReview(
                        100L,
                        new CreateReviewRequest(5, "test")
                )
        );

        assertEquals(
                "You have already reviewed this trade.",
                exception.getMessage()
        );
        verify(reviewRepo, never()).saveAndFlush(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldTranslateDatabaseDuplicateIntoBusinessException() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(initiator);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(completedTrade));
        when(reviewRepo.existsByTrade_TradeIdAndReviewer_UserId(100L, 1L))
                .thenReturn(false);
        when(reviewRepo.saveAndFlush(any(ReviewEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> reviewService.createReview(
                        100L,
                        new CreateReviewRequest(5, "test")
                )
        );

        assertEquals(
                "You have already reviewed this trade.",
                exception.getMessage()
        );
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldNormalizeNullAndBlankComments() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(initiator);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(completedTrade));
        when(reviewRepo.existsByTrade_TradeIdAndReviewer_UserId(100L, 1L))
                .thenReturn(false);

        ReviewEntity saved = ReviewEntity.builder()
                .reviewId(52L)
                .trade(completedTrade)
                .reviewer(initiator)
                .reviewee(receiver)
                .rating(3)
                .status(ReviewStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(reviewRepo.saveAndFlush(any(ReviewEntity.class))).thenReturn(saved);
        when(reviewMapper.mapFromEntity(saved)).thenReturn(
                new ReviewResponse(
                        52L, 100L, 1L, "Alice", null,
                        2L, "Bob", null, 3, null,
                        ReviewStatus.ACTIVE, saved.getCreatedAt()
                )
        );

        reviewService.createReview(100L, new CreateReviewRequest(3, "   "));

        ArgumentCaptor<ReviewEntity> captor =
                ArgumentCaptor.forClass(ReviewEntity.class);
        verify(reviewRepo).saveAndFlush(captor.capture());
        assertNull(captor.getValue().getComment());
    }

    @Test
    void shouldReturnEligibleForCompletedTradeBeforeReview() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(initiator);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(completedTrade));
        when(reviewRepo.existsByTrade_TradeIdAndReviewer_UserId(100L, 1L))
                .thenReturn(false);

        ReviewEligibilityResponse result =
                reviewService.getEligibility(100L);

        assertTrue(result.eligible());
        assertFalse(result.alreadyReviewed());
        assertEquals(2L, result.revieweeId());
        assertEquals("Bob", result.revieweeName());
        assertNull(result.reason());
    }

    @Test
    void shouldReturnIneligibleWhenTradeIsNotCompleted() {
        completedTrade.setStatus(TradeStatus.DISPUTED);
        when(authenticatedUserService.getCurrentUser()).thenReturn(initiator);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(completedTrade));
        when(reviewRepo.existsByTrade_TradeIdAndReviewer_UserId(100L, 1L))
                .thenReturn(false);

        ReviewEligibilityResponse result =
                reviewService.getEligibility(100L);

        assertFalse(result.eligible());
        assertFalse(result.alreadyReviewed());
        assertEquals("Trade is not completed.", result.reason());
    }

    @Test
    void shouldReturnAlreadyReviewedEligibilityState() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(initiator);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(completedTrade));
        when(reviewRepo.existsByTrade_TradeIdAndReviewer_UserId(100L, 1L))
                .thenReturn(true);

        ReviewEligibilityResponse result =
                reviewService.getEligibility(100L);

        assertFalse(result.eligible());
        assertTrue(result.alreadyReviewed());
        assertEquals("You have already reviewed this trade.", result.reason());
    }

    @Test
    void shouldRejectEligibilityForNonParticipant() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(outsider);
        when(tradeRepo.findById(100L)).thenReturn(Optional.of(completedTrade));

        assertThrows(
                ForbiddenOperationException.class,
                () -> reviewService.getEligibility(100L)
        );
    }

    @Test
    void shouldReturnTradeAndUserReviews() {
        ReviewEntity review = ReviewEntity.builder()
                .reviewId(10L)
                .trade(completedTrade)
                .reviewer(initiator)
                .reviewee(receiver)
                .rating(5)
                .status(ReviewStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ReviewResponse response = new ReviewResponse(
                10L, 100L, 1L, "Alice", null,
                2L, "Bob", null, 5, null,
                ReviewStatus.ACTIVE, review.getCreatedAt()
        );

        when(tradeRepo.existsById(100L)).thenReturn(true);
        when(reviewRepo.findByTrade_TradeIdOrderByCreatedAtAscReviewIdAsc(100L))
                .thenReturn(List.of(review));
        when(reviewMapper.mapFromEntity(review)).thenReturn(response);

        List<ReviewResponse> result = reviewService.getTradeReviews(100L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).reviewId());

        when(reviewRepo.findByReviewee_UserIdOrderByCreatedAtDesc(2L))
                .thenReturn(List.of(review));

        result = reviewService.getUserReviews(2L);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).revieweeId());
    }

    private UserEntity user(Long id, String email, String name) {
        return UserEntity.builder()
                .userId(id)
                .email(email)
                .password("password")
                .name(name)
                .trustScore(500.0)
                .build();
    }
}
