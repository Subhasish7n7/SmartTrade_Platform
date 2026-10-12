package smarttrade.backend.dto.review;

import smarttrade.backend.entities.review.ReviewStatus;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long reviewId,
        Long tradeId,

        Long reviewerId,
        String reviewerName,
        String reviewerAvatarUrl,

        Long revieweeId,
        String revieweeName,
        String revieweeAvatarUrl,

        int rating,
        String comment,
        ReviewStatus status,
        LocalDateTime createdAt
) {
}