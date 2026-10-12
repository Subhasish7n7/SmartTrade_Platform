"use client"

import { Star } from "lucide-react"

interface Props {
  score: number
  compact?: boolean
}

function getTrustLevel(score: number) {
  if (score >= 800) {
    return {
      label: "Highly trusted",
      stars: 5,
      className: "text-emerald-300",
    }
  }

  if (score >= 600) {
    return {
      label: "Trusted trader",
      stars: 4,
      className: "text-[var(--premium-gold)]",
    }
  }

  if (score >= 400) {
    return {
      label: "Established trader",
      stars: 3,
      className: "text-[var(--trust-blue)]",
    }
  }

  if (score >= 200) {
    return {
      label: "Use some caution",
      stars: 2,
      className: "text-amber-300",
    }
  }

  return {
    label: "High caution",
    stars: 1,
    className: "text-red-300",
  }
}

export function TradeTrustLevel({ score, compact = false }: Props) {
  const trust = getTrustLevel(Math.max(0, Math.min(1000, score)))

  return (
    <div
      className="group inline-flex items-center gap-2"
      title="Trust level is based on transaction reliability, reviews, marketplace conduct, verification, listing quality, and account maturity. It is not a review-only rating."
    >
      <span className={`inline-flex items-center gap-0.5 ${trust.className}`} aria-label={`${trust.label}, ${trust.stars} out of 5 trust stars`}>
        {Array.from({ length: 5 }).map((_, index) => (
          <Star
            key={index}
            className={`${compact ? "h-3 w-3" : "h-3.5 w-3.5"} ${
              index < trust.stars ? "fill-current" : "opacity-25"
            }`}
          />
        ))}
      </span>

      <span className={`${compact ? "text-[11px]" : "text-xs"} font-medium text-foreground/85`}>
        {trust.label}
      </span>
    </div>
  )
}