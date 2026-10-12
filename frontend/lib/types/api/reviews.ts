export type ReviewStatus = "ACTIVE" | "HIDDEN" | "REMOVED" | "PENDING";

export interface ReviewResponse {
  reviewId: number;
  tradeId: number;
  reviewerId: number;
  reviewerName: string;
  reviewerAvatarUrl: string | null;
  revieweeId: number;
  revieweeName: string;
  revieweeAvatarUrl: string | null;
  rating: number;
  comment: string | null;
  status: ReviewStatus;
  createdAt: string;
}

export interface ReviewEligibilityResponse {
  tradeId: number;
  eligible: boolean;
  alreadyReviewed: boolean;
  revieweeId: number;
  revieweeName: string;
  reason: string | null;
}

export interface CreateReviewRequest {
  rating: number;
  comment?: string;
}