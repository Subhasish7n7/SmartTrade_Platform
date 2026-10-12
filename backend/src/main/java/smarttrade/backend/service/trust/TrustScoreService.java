package smarttrade.backend.service.trust;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import smarttrade.backend.dto.trust.TrustScoreHistoryResponse;
import smarttrade.backend.entities.item.ItemEntity;
import smarttrade.backend.entities.item.ItemImageStatus;
import smarttrade.backend.entities.review.ReviewEntity;
import smarttrade.backend.entities.review.ReviewStatus;
import smarttrade.backend.entities.trust.TrustScoreHistoryEntity;
import smarttrade.backend.entities.user.AccountStatus;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.events.trust.TrustEvent;
import smarttrade.backend.repository.*;
import smarttrade.backend.repository.item.ItemRepo;
import smarttrade.backend.repository.trades.TradeDisputeRepo;
import smarttrade.backend.repository.user.UserRepo;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrustScoreService {

    private static final double BASE_SCORE = 500.0;

    private static final double TRANSACTION_RANGE = 250.0;
    private static final double REVIEW_RANGE = 150.0;
    private static final double CONDUCT_RANGE = 150.0;
    private static final double VERIFICATION_RANGE = 100.0;
    private static final double LISTING_QUALITY_RANGE = 75.0;
    private static final double MATURITY_RANGE = 25.0;

    private final UserRepo userRepo;
    private final TradeDisputeRepo tradeDisputeRepo;
    private final ReviewRepo reviewRepo;
    private final ItemRepo itemRepo;
    private final TrustScoreHistoryRepo trustScoreHistoryRepo;

    @Transactional
    public void process(TrustEvent event) {

        if (event == null || event.userId() == null) {
            throw new IllegalArgumentException(
                    "Trust event and userId are required."
            );
        }

        /*
         * Serialize score mutations for the same user.
         *
         * This protects the score/history pair when two trust events are
         * processed concurrently.
         */
        UserEntity user = userRepo.findByIdForUpdate(event.userId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Trust user not found: " + event.userId())
                );

        TrustScoreBreakdown breakdown = calculateBreakdown(user);

        double before = round2(user.getTrustScore());

        double after = round2(
                clamp(
                        BASE_SCORE
                                + breakdown.transactionComponent()
                                + breakdown.reviewComponent()
                                + breakdown.conductComponent()
                                + breakdown.verificationComponent()
                                + breakdown.listingQualityComponent()
                                + breakdown.maturityComponent(),
                        0.0,
                        1000.0
                )
        );

        double change = round2(after - before);

        /*
         * The score is always synchronized with the current authoritative
         * facts, even when a trigger does not change the score.
         */
        user.setTrustScore(after);
        userRepo.save(user);

        /*
         * History is for actual score changes rather than every trigger.
         * This keeps LISTING_CHANGED and similar events from producing a
         * useless row every time the score remains identical.
         */
        if (Math.abs(change) > 0.0001) {

            TrustScoreHistoryEntity history =
                    TrustScoreHistoryEntity.builder()
                            .user(user)
                            .eventType(event.type())
                            .sourceType(event.sourceType())
                            .sourceId(event.sourceId())
                            .relatedUserId(event.relatedUserId())
                            .scoreBefore(before)
                            .scoreAfter(after)
                            .scoreChange(change)
                            .transactionComponent(
                                    round2(breakdown.transactionComponent())
                            )
                            .reviewComponent(
                                    round2(breakdown.reviewComponent())
                            )
                            .conductComponent(
                                    round2(breakdown.conductComponent())
                            )
                            .verificationComponent(
                                    round2(breakdown.verificationComponent())
                            )
                            .listingQualityComponent(
                                    round2(breakdown.listingQualityComponent())
                            )
                            .maturityComponent(
                                    round2(breakdown.maturityComponent())
                            )
                            .reason(event.reason())
                            .createdAt(
                                    event.occurredAt() == null
                                            ? LocalDateTime.now()
                                            : event.occurredAt()
                            )
                            .build();

            trustScoreHistoryRepo.save(history);
        }

        log.info(
                "Trust score recalculated for user {}: {} -> {} (event={})",
                user.getUserId(),
                before,
                after,
                event.type()
        );
    }

    @Transactional
    public Page<TrustScoreHistoryResponse> getHistory(
            Long userId,
            Pageable pageable
    ) {
        if (!userRepo.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }

        return trustScoreHistoryRepo
                .findByUser_UserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapHistory);
    }

    /*
     * Recalculates one user without requiring a business event.
     *
     * This is useful for a one-time migration/backfill of existing users.
     */
    @Transactional
    public void recalculateUser(Long userId) {

        UserEntity user =
                userRepo.findByIdForUpdate(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("User not found")
                        );

        TrustScoreBreakdown breakdown =
                calculateBreakdown(user);

        user.setTrustScore(
                round2(
                        clamp(
                                BASE_SCORE
                                        + breakdown.transactionComponent()
                                        + breakdown.reviewComponent()
                                        + breakdown.conductComponent()
                                        + breakdown.verificationComponent()
                                        + breakdown.listingQualityComponent()
                                        + breakdown.maturityComponent(),
                                0.0,
                                1000.0
                        )
                )
        );

        userRepo.save(user);
    }

    @Transactional
    public void recalculateAllUsers() {

        userRepo.findAll().forEach(user -> recalculateUser(user.getUserId()));
    }

    private TrustScoreBreakdown calculateBreakdown(UserEntity user) {

        double transactionComponent = calculateTransactionReliability(user);

        double reviewComponent = calculateReviewReputation(user);

        double conductComponent = calculateMarketplaceConduct(user);

        double verificationComponent = calculateVerification(user);

        double listingQualityComponent = calculateListingQuality(user);

        double maturityComponent = calculateMaturity(user);

        return new TrustScoreBreakdown(
                transactionComponent,
                reviewComponent,
                conductComponent,
                verificationComponent,
                listingQualityComponent,
                maturityComponent
        );
    }

    private double calculateTransactionReliability(UserEntity user) {

        long successfulTrades = Math.max(0, user.getSuccessfulTrades());

        long faultCount = tradeDisputeRepo
                .countByFaultUser_UserIdAndResolution(
                        user.getUserId(),
                        smarttrade.backend.entities.trade.DisputeResolution.CANCEL_WITH_FAULT
                );

        /*
         * Neutral Beta(1,1)-style prior:
         *
         * no history -> 0 contribution
         * one success -> modest positive contribution
         * one fault   -> modest negative contribution
         *
         * The asymptotes are -250 and +250.
         */
        double smoothedSuccessRate = (successfulTrades + 1.0) / (successfulTrades + faultCount + 2.0);
        return clamp(
                (smoothedSuccessRate - 0.5) * 500.0,
                -TRANSACTION_RANGE,
                TRANSACTION_RANGE
        );
    }

    private double calculateReviewReputation(UserEntity user) {

        List<ReviewEntity> reviews = reviewRepo
                .findByReviewee_UserIdAndStatusOrderByCreatedAtAscReviewIdAsc(
                        user.getUserId(),
                        ReviewStatus.ACTIVE
                );

        if (reviews.isEmpty()) {
            return 0.0;
        }

        Map<Long, Integer> reviewCountByReviewer = new HashMap<>();

        double weightedSignalSum = 0.0;
        double totalWeight = 0.0;

        for (ReviewEntity review : reviews) {

            Long reviewerId = review.getReviewer().getUserId();

            int occurrence = reviewCountByReviewer.merge(reviewerId, 1, Integer::sum);

            double weight = 1.0 / occurrence;

            double signal = (review.getRating() - 3.0) / 2.0;

            weightedSignalSum += signal * weight;
            totalWeight += weight;
        }

        /*
         * Neutral pseudo-review prevents one 5-star or 1-star review from
         * immediately reaching the full +/-150 boundary.
         */
        double weightedAverage = weightedSignalSum / (totalWeight + 1.0);

        return clamp(
                weightedAverage * REVIEW_RANGE,
                -REVIEW_RANGE,
                REVIEW_RANGE
        );
    }

    private double calculateMarketplaceConduct(UserEntity user) {

        /*
         * The supplied project does not yet expose a separate moderation
         * conduct score/ledger.
         *
         * Until that exists, suspension is the current authoritative
         * conduct signal.
         */
        return user.getAccountStatus() == AccountStatus.SUSPENDED
                ? -CONDUCT_RANGE
                : 0.0;
    }

    private double calculateVerification(UserEntity user) {
        /*
         * No verification field/subsystem exists in the supplied model.
         * Keep this explicit hook returning zero until such a subsystem
         * is introduced.
         */
        return 0.0;
    }

    private double calculateListingQuality(UserEntity user) {

        List<ItemEntity> listings =
                itemRepo.findByUser_UserIdAndModerationStatus(
                        user.getUserId(),
                        smarttrade.backend.entities.item.ItemModerationStatus.ACTIVE
                );

        if (listings.isEmpty()) {
            return 0.0;
        }

        double totalQuality = 0.0;

        for (ItemEntity item : listings) {

            int earned = 0;

            if (item.getDescription() != null && !item.getDescription().isBlank()) {
                earned++;
            }

            boolean hasActiveImage =
                    item.getImages() != null
                            && item.getImages()
                            .stream()
                            .anyMatch(
                                    image ->
                                            image.getStatus()
                                                    == ItemImageStatus.ACTIVE
                            );

            if (hasActiveImage) {
                earned++;
            }

            boolean hasLocation =
                    item.getLocation() != null
                            || (
                            item.getCity() != null
                                    && !item.getCity().isBlank()
                                    && item.getState() != null
                                    && !item.getState().isBlank()
                    );

            if (hasLocation) {
                earned++;
            }

            if (item.getLabels() != null
                    && !item.getLabels().isEmpty()) {
                earned++;
            }

            totalQuality += earned / 4.0;
        }

        double averageQuality =
                totalQuality / listings.size();

        return clamp(
                averageQuality * LISTING_QUALITY_RANGE,
                0.0,
                LISTING_QUALITY_RANGE
        );
    }

    private double calculateMaturity(UserEntity user) {

        if (user.getCreatedAt() == null) {
            return 0.0;
        }

        long days = Math.max(0, ChronoUnit.DAYS.between(user.getCreatedAt(), LocalDateTime.now()));

        return Math.min(MATURITY_RANGE, (days / 365.0) * MATURITY_RANGE);
    }

    private TrustScoreHistoryResponse mapHistory(TrustScoreHistoryEntity history) {
        return new TrustScoreHistoryResponse(
                history.getHistoryId(),
                history.getUser().getUserId(),
                history.getEventType(),
                history.getSourceType(),
                history.getSourceId(),
                history.getRelatedUserId(),
                history.getScoreBefore(),
                history.getScoreAfter(),
                history.getScoreChange(),
                history.getTransactionComponent(),
                history.getReviewComponent(),
                history.getConductComponent(),
                history.getVerificationComponent(),
                history.getListingQualityComponent(),
                history.getMaturityComponent(),
                history.getReason(),
                history.getCreatedAt()
        );
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record TrustScoreBreakdown(
            double transactionComponent,
            double reviewComponent,
            double conductComponent,
            double verificationComponent,
            double listingQualityComponent,
            double maturityComponent
    ) {
    }
}