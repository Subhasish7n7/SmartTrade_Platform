"use client";

import { useEffect, useState, type FormEvent } from "react";
import { Star } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import {
  createTradeReview,
  getTradeReviewEligibility,
} from "@/lib/api/reviews/client";
import type { ReviewEligibilityResponse } from "@/lib/types/api/reviews";

export function TradeReviewForm({
  tradeId,
  status,
}: {
  tradeId: number;
  status: string;
}) {
  const [eligibility, setEligibility] =
    useState<ReviewEligibilityResponse | null>(null);
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState("");
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [done, setDone] = useState(false);

  useEffect(() => {
    let cancelled = false;
    if (status !== "COMPLETED") {
      setLoading(false);
      return;
    }
    getTradeReviewEligibility(tradeId)
      .then((value) => {
        if (!cancelled) setEligibility(value);
      })
      .catch((err) => {
        if (!cancelled)
          setError(
            err instanceof Error
              ? err.message
              : "Review eligibility could not be checked.",
          );
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [status, tradeId]);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    if (!eligibility?.eligible) return;
    setSubmitting(true);
    try {
      await createTradeReview(tradeId, {
        rating,
        comment: comment.trim() || undefined,
      });
      setDone(true);
      setEligibility({
        ...eligibility,
        eligible: false,
        alreadyReviewed: true,
        reason: "You have already reviewed this trade.",
      });
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Review could not be submitted.",
      );
    } finally {
      setSubmitting(false);
    }
  }

  if (status !== "COMPLETED") return null;
  if (loading)
    return (
      <div className="trade-card rounded-2xl p-5 text-sm text-muted-foreground">
        Checking review eligibility…
      </div>
    );
  if (error && !eligibility)
    return (
      <div
        role="alert"
        className="trade-card rounded-2xl p-5 text-sm text-destructive"
      >
        {error}
      </div>
    );
  if (!eligibility?.eligible)
    return (
      <div className="trade-card rounded-2xl p-5">
        <h3 className="font-semibold">
          {done || eligibility?.alreadyReviewed
            ? "Review submitted"
            : "Review unavailable"}
        </h3>
        <p className="mt-1 text-sm text-muted-foreground">
          {done
            ? "Thanks for sharing your experience."
            : (eligibility?.reason ??
              "This trade is not eligible for a review.")}
        </p>
      </div>
    );

  return (
    <form
      onSubmit={submit}
      className="trade-card space-y-4 rounded-2xl p-5 sm:p-6"
    >
      <div>
        <h3 className="font-semibold">Review {eligibility.revieweeName}</h3>
        <p className="mt-1 text-sm text-muted-foreground">
          Your review must reflect this completed trade.
        </p>
      </div>
      <div role="radiogroup" aria-label="Rating" className="flex gap-2">
        {Array.from({ length: 5 }, (_, i) => {
          const value = i + 1;
          return (
            <button
              key={value}
              type="button"
              role="radio"
              aria-checked={rating === value}
              aria-label={`${value} star${value > 1 ? "s" : ""}`}
              onClick={() => setRating(value)}
              className={`rounded-lg p-1 transition ${value <= rating ? "text-[var(--premium-gold)]" : "text-muted-foreground/40"}`}
            >
              <Star
                className={`h-7 w-7 ${value <= rating ? "fill-current" : ""}`}
              />
            </button>
          );
        })}
      </div>
      <label className="block space-y-2">
        <span className="text-sm font-medium">Comment (optional)</span>
        <Textarea
          value={comment}
          onChange={(event) => setComment(event.target.value)}
          rows={4}
          maxLength={2000}
          placeholder="How did the trade go?"
          disabled={submitting}
        />
      </label>
      {error && (
        <p role="alert" className="text-sm text-destructive">
          {error}
        </p>
      )}
      <Button type="submit" className="cta-primary" disabled={submitting}>
        {submitting ? "Submitting…" : "Submit review"}
      </Button>
    </form>
  );
}