package smarttrade.backend.dto.review;

public record ReviewEligibilityResponse(
        Long tradeId,
        boolean eligible,
        boolean alreadyReviewed,
        Long revieweeId,
        String revieweeName,
        String reason
) {
}