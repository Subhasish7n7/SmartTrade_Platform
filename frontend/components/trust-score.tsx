export type TrustScoreBand = {
  label: string
  tone: "below" | "neutral" | "positive" | "strong" | "excellent"
}

/**
 * The backend stores a 0–1000 aggregate score. The UI displays the same linear
 * scale divided by ten so users can read it more easily, without calling it a
 * percentage. 500/1000 therefore becomes the neutral 50/100 baseline.
 */
export function toTrustRating(score: number | null | undefined): number | null {
  if (score == null || !Number.isFinite(score)) return null

  const boundedScore = Math.min(1000, Math.max(0, score))
  return Math.round(boundedScore / 10)
}

export function getTrustScoreBand(rating: number): TrustScoreBand {
  if (rating < 40) return { label: "Below neutral", tone: "below" }
  if (rating < 60) return { label: "Around neutral", tone: "neutral" }
  if (rating < 80) return { label: "Positive reputation", tone: "positive" }
  if (rating < 90) return { label: "Strong reputation", tone: "strong" }
  return { label: "Highly trusted", tone: "excellent" }
}

export function getTrustScoreExplanation(score: number, rating: number): string {
  return [
    `Trust rating: ${rating}/100 (underlying score ${Math.round(score)}/1000).`,
    "The 0–100 display is a scaled rating, not a percentage.",
    "It aggregates trade reliability, active reviews, account conduct, listing quality and account age; repeat reviewers have diminishing influence.",
    "Verification is reserved in the scoring model but is not currently wired into the backend score.",
    "50/100 (500/1000) is the neutral baseline. A trust rating is a signal, not a guarantee.",
  ].join(" ")
}
