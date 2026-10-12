package smarttrade.backend.Mappers;

import org.springframework.stereotype.Component;
import smarttrade.backend.dto.review.ReviewResponse;
import smarttrade.backend.entities.review.ReviewEntity;
import smarttrade.backend.entities.user.UserEntity;

@Component
public class ReviewMapper {

    public ReviewResponse mapFromEntity(ReviewEntity review) {

        UserEntity reviewer = review.getReviewer();
        UserEntity reviewee = review.getReviewee();

        return new ReviewResponse(
                review.getReviewId(),
                review.getTrade().getTradeId(),

                reviewer.getUserId(),
                reviewer.getName(),
                reviewer.getAvatar() != null
                        ? reviewer.getAvatar().getSecureUrl()
                        : null,

                reviewee.getUserId(),
                reviewee.getName(),
                reviewee.getAvatar() != null
                        ? reviewee.getAvatar().getSecureUrl()
                        : null,

                review.getRating(),
                review.getComment(),
                review.getStatus(),
                review.getCreatedAt()
        );
    }
}