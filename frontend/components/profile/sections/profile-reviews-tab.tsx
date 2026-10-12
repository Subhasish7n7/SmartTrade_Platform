"use client";

import { useEffect, useMemo, useState } from "react";
import { Star } from "lucide-react";

import { getUserReviews } from "@/lib/api/reviews/client";
import type { ReviewResponse } from "@/lib/types/api/reviews";

interface Props {
  userId: number | null | undefined;
  isOwner: boolean;
}

export function ProfileReviewsTab({ userId }: Props) {
  const [reviews, setReviews] = useState<ReviewResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    setLoading(true);
    setError(null);
    setReviews([]);

    if (
      typeof userId !== "number" ||
      !Number.isSafeInteger(userId) ||
      userId <= 0
    ) {
      setError(
        "Reviews cannot be loaded because this profile has no valid user ID.",
      );
      setLoading(false);

      return () => {
        cancelled = true;
      };
    }

    getUserReviews(userId)
      .then((response) => {
        if (!cancelled) {
          setReviews(response.filter((review) => review.status === "ACTIVE"));
        }
      })
      .catch((loadError: unknown) => {
        if (!cancelled) {
          setError(
            loadError instanceof Error
              ? loadError.message
              : "Reviews could not be loaded.",
          );
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [userId]);

  const average = useMemo(
    () =>
      reviews.length
        ? reviews.reduce((sum, review) => sum + review.rating, 0) /
          reviews.length
        : 0,
    [reviews],
  );

  return (
    <section className="space-y-5">
      <div className="trade-card rounded-2xl p-5 sm:p-6">
        <p className="text-sm text-muted-foreground">Public reviews</p>

        <div className="mt-2 flex flex-wrap items-end gap-3">
          <span className="text-4xl font-bold tabular-nums">
            {reviews.length ? average.toFixed(1) : "—"}
          </span>

          <span className="pb-1 text-sm text-muted-foreground">
            out of 5 · {reviews.length}{" "}
            {reviews.length === 1 ? "review" : "reviews"}
          </span>
        </div>

        {!!reviews.length && (
          <div
            className="mt-2 flex gap-1 text-[var(--premium-gold)]"
            aria-label={`${average.toFixed(1)} out of 5 stars`}
          >
            {Array.from({ length: 5 }, (_, index) => (
              <Star
                key={index}
                className={`h-4 w-4 ${
                  index < Math.round(average) ? "fill-current" : "opacity-30"
                }`}
              />
            ))}
          </div>
        )}
      </div>

      {loading && (
        <div className="trade-card rounded-2xl p-6 text-sm text-muted-foreground">
          Loading reviews…
        </div>
      )}

      {!loading && error && (
        <div
          role="alert"
          className="trade-card rounded-2xl p-5 text-sm text-destructive"
        >
          {error}
        </div>
      )}

      {!loading && !error && reviews.length === 0 && (
        <div className="trade-card rounded-2xl p-8 text-center">
          <Star className="mx-auto h-7 w-7 text-muted-foreground/60" />

          <h3 className="mt-3 font-semibold">No public reviews yet</h3>

          <p className="mt-1 text-sm text-muted-foreground">
            Completed trades can lead to reviews from the other participant.
          </p>
        </div>
      )}

      {!loading &&
        !error &&
        reviews.map((review) => (
          <article
            key={review.reviewId}
            className="trade-card rounded-2xl p-5"
          >
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div>
                <p className="font-semibold">{review.reviewerName}</p>

                <p className="mt-1 text-xs text-muted-foreground">
                  {new Intl.DateTimeFormat("en-IN", {
                    dateStyle: "medium",
                  }).format(new Date(review.createdAt))}
                </p>
              </div>

              <div
                className="flex items-center gap-1 text-sm font-semibold text-[var(--premium-gold)]"
                aria-label={`${review.rating} out of 5 stars`}
              >
                <Star className="h-4 w-4 fill-current" />
                {review.rating}/5
              </div>
            </div>

            {review.comment ? (
              <p className="mt-4 whitespace-pre-wrap text-sm leading-6 text-muted-foreground">
                {review.comment}
              </p>
            ) : (
              <p className="mt-4 text-sm italic text-muted-foreground">
                No written comment.
              </p>
            )}
          </article>
        ))}
    </section>
  );
}