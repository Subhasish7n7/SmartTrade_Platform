import { clientApi } from "@/lib/api/configs/client";
import type {
  CreateReviewRequest,
  ReviewEligibilityResponse,
  ReviewResponse,
} from "@/lib/types/api/reviews";

export async function getUserReviews(
  userId: number,
): Promise<ReviewResponse[]> {
  if (!Number.isSafeInteger(userId) || userId <= 0) {
    throw new Error("A valid user ID is required to load reviews.");
  }

  const { data } = await clientApi.get<ReviewResponse[]>(
    `/reviews/users/${userId}`,
  );

  return data;
}

export async function getTradeReviewEligibility(
  tradeId: number,
): Promise<ReviewEligibilityResponse> {
  const { data } = await clientApi.get<ReviewEligibilityResponse>(
    `/reviews/trades/${tradeId}/eligibility`,
  );

  return data;
}

export async function createTradeReview(
  tradeId: number,
  payload: CreateReviewRequest,
): Promise<ReviewResponse> {
  const { data } = await clientApi.post<ReviewResponse>(
    `/reviews/trades/${tradeId}`,
    payload,
  );

  return data;
}