package smarttrade.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smarttrade.backend.dto.review.CreateReviewRequest;
import smarttrade.backend.dto.review.ReviewEligibilityResponse;
import smarttrade.backend.dto.review.ReviewResponse;
import smarttrade.backend.service.review.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/trades/{tradeId}")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable Long tradeId,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reviewService.createReview(tradeId, request));
    }

    @GetMapping("/trades/{tradeId}")
    public ResponseEntity<List<ReviewResponse>> getTradeReviews(@PathVariable Long tradeId) {
        return ResponseEntity.ok(reviewService.getTradeReviews(tradeId));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<ReviewResponse>> getUserReviews(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.getUserReviews(userId));
    }

    @GetMapping("/trades/{tradeId}/eligibility")
    public ResponseEntity<ReviewEligibilityResponse> getEligibility(@PathVariable Long tradeId) {
        return ResponseEntity.ok(reviewService.getEligibility(tradeId));
    }
}