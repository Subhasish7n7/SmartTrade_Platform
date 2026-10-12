package smarttrade.backend.UnitTest.Trust;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smarttrade.backend.entities.item.ItemCategory;
import smarttrade.backend.entities.item.ItemCondition;
import smarttrade.backend.entities.item.ItemEntity;
import smarttrade.backend.entities.item.ItemImageEntity;
import smarttrade.backend.entities.item.ItemImageStatus;
import smarttrade.backend.entities.item.ItemModerationStatus;
import smarttrade.backend.entities.review.ReviewEntity;
import smarttrade.backend.entities.review.ReviewStatus;
import smarttrade.backend.entities.trade.DisputeResolution;
import smarttrade.backend.entities.trust.TrustScoreHistoryEntity;
import smarttrade.backend.entities.user.AccountStatus;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.events.trust.TrustEvent;
import smarttrade.backend.events.trust.TrustEventSourceType;
import smarttrade.backend.events.trust.TrustEventType;
import smarttrade.backend.repository.item.ItemRepo;
import smarttrade.backend.repository.ReviewRepo;
import smarttrade.backend.repository.trades.TradeDisputeRepo;
import smarttrade.backend.repository.TrustScoreHistoryRepo;
import smarttrade.backend.repository.user.UserRepo;
import smarttrade.backend.service.trust.TrustScoreService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrustScoreServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private TradeDisputeRepo tradeDisputeRepo;

    @Mock
    private ReviewRepo reviewRepo;

    @Mock
    private ItemRepo itemRepo;

    @Mock
    private TrustScoreHistoryRepo trustScoreHistoryRepo;

    @InjectMocks
    private TrustScoreService trustScoreService;

    private UserEntity user;
    private UserEntity reviewer;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .userId(1L)
                .email("user@test.com")
                .password("password")
                .name("User")
                .trustScore(500.0)
                .successfulTrades(0)
                .totalListings(0)
                .createdAt(LocalDateTime.now())
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        reviewer = UserEntity.builder()
                .userId(2L)
                .email("reviewer@test.com")
                .password("password")
                .name("Reviewer")
                .trustScore(500.0)
                .createdAt(LocalDateTime.now())
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        when(tradeDisputeRepo.countByFaultUser_UserIdAndResolution(
                anyLong(), any(DisputeResolution.class)))
                .thenReturn(0L);
        when(reviewRepo.findByReviewee_UserIdAndStatusOrderByCreatedAtAscReviewIdAsc(
                anyLong(), any(ReviewStatus.class)))
                .thenReturn(List.of());
        when(itemRepo.findByUser_UserIdAndModerationStatus(
                anyLong(), any(ItemModerationStatus.class)))
                .thenReturn(List.of());
    }

    @Test
    void newUserWithNoEvidenceRemainsAtNeutral500() {
        when(userRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(user));

        trustScoreService.process(event(TrustEventType.TRADE_COMPLETED, 10L));

        assertEquals(500.0, user.getTrustScore());
        verify(trustScoreHistoryRepo, never()).save(any());
    }

    @Test
    void oneSuccessfulTradeProducesPositiveTransactionComponent() {
        user.setSuccessfulTrades(1);
        when(userRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(user));

        trustScoreService.process(event(TrustEventType.TRADE_COMPLETED, 10L));

        assertEquals(583.33, user.getTrustScore(), 0.01);

        ArgumentCaptor<TrustScoreHistoryEntity> captor =
                ArgumentCaptor.forClass(TrustScoreHistoryEntity.class);
        verify(trustScoreHistoryRepo).save(captor.capture());

        TrustScoreHistoryEntity history = captor.getValue();
        assertEquals(500.0, history.getScoreBefore(), 0.01);
        assertEquals(583.33, history.getScoreAfter(), 0.01);
        assertEquals(83.33, history.getScoreChange(), 0.01);
        assertEquals(83.33, history.getTransactionComponent(), 0.01);
        assertEquals(TrustEventType.TRADE_COMPLETED, history.getEventType());
    }

    @Test
    void oneFaultProducesNegativeTransactionComponent() {
        when(tradeDisputeRepo.countByFaultUser_UserIdAndResolution(
                1L,
                DisputeResolution.CANCEL_WITH_FAULT
        )).thenReturn(1L);
        when(userRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(user));

        trustScoreService.process(event(TrustEventType.TRADE_FAULT_ASSIGNED, 20L));

        assertEquals(416.67, user.getTrustScore(), 0.01);
    }

    @Test
    void fiveStarReviewProducesPositiveReviewComponent() {
        ReviewEntity review = review(10L, reviewer, user, 5, ReviewStatus.ACTIVE);

        when(reviewRepo.findByReviewee_UserIdAndStatusOrderByCreatedAtAscReviewIdAsc(
                1L, ReviewStatus.ACTIVE))
                .thenReturn(List.of(review));
        when(userRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(user));

        trustScoreService.process(event(TrustEventType.REVIEW_RECEIVED, 10L));

        assertEquals(575.0, user.getTrustScore(), 0.01);

        ArgumentCaptor<TrustScoreHistoryEntity> captor =
                ArgumentCaptor.forClass(TrustScoreHistoryEntity.class);
        verify(trustScoreHistoryRepo).save(captor.capture());
        assertEquals(75.0, captor.getValue().getReviewComponent(), 0.01);
    }

    @Test
    void oneStarReviewProducesNegativeReviewComponent() {
        ReviewEntity review = review(11L, reviewer, user, 1, ReviewStatus.ACTIVE);

        when(reviewRepo.findByReviewee_UserIdAndStatusOrderByCreatedAtAscReviewIdAsc(
                1L, ReviewStatus.ACTIVE))
                .thenReturn(List.of(review));
        when(userRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(user));

        trustScoreService.process(event(TrustEventType.REVIEW_RECEIVED, 11L));

        assertEquals(425.0, user.getTrustScore(), 0.01);
    }

    @Test
    void threeStarReviewIsNeutral() {
        ReviewEntity review = review(12L, reviewer, user, 3, ReviewStatus.ACTIVE);

        when(reviewRepo.findByReviewee_UserIdAndStatusOrderByCreatedAtAscReviewIdAsc(
                1L, ReviewStatus.ACTIVE))
                .thenReturn(List.of(review));
        when(userRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(user));

        trustScoreService.process(event(TrustEventType.REVIEW_RECEIVED, 12L));

        assertEquals(500.0, user.getTrustScore(), 0.01);
        verify(trustScoreHistoryRepo, never()).save(any());
    }

    @Test
    void repeatedReviewsFromSameReviewerHaveDiminishingInfluence() {
        List<ReviewEntity> twoFromSameReviewer = List.of(
                review(20L, reviewer, user, 5, ReviewStatus.ACTIVE),
                review(21L, reviewer, user, 5, ReviewStatus.ACTIVE)
        );

        when(reviewRepo.findByReviewee_UserIdAndStatusOrderByCreatedAtAscReviewIdAsc(
                1L, ReviewStatus.ACTIVE))
                .thenReturn(twoFromSameReviewer);
        when(userRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(user));

        trustScoreService.process(event(TrustEventType.REVIEW_RECEIVED, 20L));

        assertEquals(590.0, user.getTrustScore(), 0.01);

        reset(reviewRepo, userRepo, trustScoreHistoryRepo);
        user.setTrustScore(500.0);

        UserEntity reviewer2 = UserEntity.builder()
                .userId(3L)
                .email("reviewer2@test.com")
                .name("Reviewer 2")
                .createdAt(LocalDateTime.now())
                .build();

        List<ReviewEntity> fromDifferentReviewers = List.of(
                review(22L, reviewer, user, 5, ReviewStatus.ACTIVE),
                review(23L, reviewer2, user, 5, ReviewStatus.ACTIVE)
        );

        when(tradeDisputeRepo.countByFaultUser_UserIdAndResolution(
                anyLong(), any(DisputeResolution.class)))
                .thenReturn(0L);
        when(itemRepo.findByUser_UserIdAndModerationStatus(
                anyLong(), any(ItemModerationStatus.class)))
                .thenReturn(List.of());
        when(reviewRepo.findByReviewee_UserIdAndStatusOrderByCreatedAtAscReviewIdAsc(
                1L, ReviewStatus.ACTIVE))
                .thenReturn(fromDifferentReviewers);
        when(userRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(user));

        trustScoreService.process(event(TrustEventType.REVIEW_RECEIVED, 22L));

        assertEquals(600.0, user.getTrustScore(), 0.01);
    }

    @Test
    void hiddenAndRemovedReviewsDoNotContributeBecauseOnlyActiveReviewsAreQueried() {
        when(reviewRepo.findByReviewee_UserIdAndStatusOrderByCreatedAtAscReviewIdAsc(
                1L, ReviewStatus.ACTIVE))
                .thenReturn(List.of());
        when(userRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(user));

        trustScoreService.process(event(TrustEventType.REVIEW_REMOVED, 30L));

        assertEquals(500.0, user.getTrustScore());
        verify(trustScoreHistoryRepo, never()).save(any());
    }

    @Test
    void suspendedUserReceivesConductPenalty() {
        user.setAccountStatus(AccountStatus.SUSPENDED);
        when(userRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(user));

        trustScoreService.process(event(TrustEventType.USER_SUSPENDED, 31L));

        assertEquals(350.0, user.getTrustScore(), 0.01);
    }

    @Test
    void completeListingQualityCanContributeUpTo75() {
        ItemImageEntity activeImage = ItemImageEntity.builder()
                .status(ItemImageStatus.ACTIVE)
                .build();

        ItemEntity item = ItemEntity.builder()
                .itemId(100L)
                .itemName("Laptop")
                .userPrice(50000)
                .category(ItemCategory.COMPUTERS)
                .condition(ItemCondition.GOOD)
                .description("Detailed description")
                .labels(List.of("gaming", "laptop"))
                .city("Bhubaneswar")
                .state("Odisha")
                .moderationStatus(ItemModerationStatus.ACTIVE)
                .images(new ArrayList<>(List.of(activeImage)))
                .build();

        when(itemRepo.findByUser_UserIdAndModerationStatus(
                1L, ItemModerationStatus.ACTIVE))
                .thenReturn(List.of(item));
        when(userRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(user));

        trustScoreService.process(event(TrustEventType.LISTING_CHANGED, 100L));

        assertEquals(575.0, user.getTrustScore(), 0.01);
    }

    @Test
    void oneYearMaturityContributes25() {
        user.setCreatedAt(LocalDateTime.now().minusDays(365));
        when(userRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(user));

        trustScoreService.process(event(TrustEventType.TRADE_COMPLETED, 40L));

        assertEquals(525.0, user.getTrustScore(), 0.01);
    }

    @Test
    void processingSameEventTwiceDoesNotDriftScoreOrCreateSecondHistoryRow() {
        user.setSuccessfulTrades(1);
        when(userRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(user));

        trustScoreService.process(event(TrustEventType.TRADE_COMPLETED, 50L));
        trustScoreService.process(event(TrustEventType.TRADE_COMPLETED, 50L));

        assertEquals(583.33, user.getTrustScore(), 0.01);
        verify(trustScoreHistoryRepo, times(1)).save(any(TrustScoreHistoryEntity.class));
        verify(userRepo, times(2)).save(user);
    }

    @Test
    void scoreAlwaysRemainsBetweenZeroAnd1000() {
        user.setAccountStatus(AccountStatus.SUSPENDED);

        List<ReviewEntity> reviews = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            reviews.add(review(
                    1000L + i,
                    reviewer,
                    user,
                    1,
                    ReviewStatus.ACTIVE
            ));
        }

        when(tradeDisputeRepo.countByFaultUser_UserIdAndResolution(
                1L,
                DisputeResolution.CANCEL_WITH_FAULT
        )).thenReturn(100L);
        when(reviewRepo.findByReviewee_UserIdAndStatusOrderByCreatedAtAscReviewIdAsc(
                1L, ReviewStatus.ACTIVE))
                .thenReturn(reviews);
        when(userRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(user));

        trustScoreService.process(event(TrustEventType.TRADE_FAULT_ASSIGNED, 60L));

        assertTrue(user.getTrustScore() >= 0.0);
        assertTrue(user.getTrustScore() <= 1000.0);
    }

    private TrustEvent event(TrustEventType type, Long sourceId) {
        return TrustEvent.now(
                1L,
                type,
                type == TrustEventType.REVIEW_RECEIVED
                        ? TrustEventSourceType.REVIEW
                        : TrustEventSourceType.TRADE,
                sourceId,
                reviewer.getUserId(),
                "unit test"
        );
    }

    private ReviewEntity review(
            Long id,
            UserEntity reviewAuthor,
            UserEntity reviewee,
            int rating,
            ReviewStatus status
    ) {
        return ReviewEntity.builder()
                .reviewId(id)
                .reviewer(reviewAuthor)
                .reviewee(reviewee)
                .rating(rating)
                .status(status)
                .createdAt(LocalDateTime.now().minusSeconds(id))
                .updatedAt(LocalDateTime.now().minusSeconds(id))
                .build();
    }
}
