import { ShieldCheck } from "lucide-react"

import { cn } from "@/lib/utils"
import { getTrustScoreBand, getTrustScoreExplanation, toTrustRating } from "@/components/trust-score"

interface TrustScoreBadgeProps {
  score: number | null | undefined
  className?: string
  appearance?: "default" | "overlay"
  size?: "sm" | "md"
  showLabel?: boolean
}

const toneClasses = {
  below: "border-amber-400/25 bg-amber-400/10 text-amber-200",
  neutral: "border-border/60 bg-muted/35 text-muted-foreground",
  positive: "border-teal-400/25 bg-teal-400/10 text-teal-200",
  strong: "border-sky-400/25 bg-sky-400/10 text-sky-200",
  excellent: "border-emerald-400/25 bg-emerald-400/10 text-emerald-200",
} as const

const iconToneClasses = {
  below: "text-amber-300",
  neutral: "text-muted-foreground",
  positive: "text-teal-300",
  strong: "text-sky-300",
  excellent: "text-emerald-300",
} as const

export function TrustScoreBadge({
  score,
  className,
  appearance = "default",
  size = "sm",
  showLabel = true,
}: TrustScoreBadgeProps) {
  const rating = toTrustRating(score)
  if (rating === null || score == null || !Number.isFinite(score)) return null

  const band = getTrustScoreBand(rating)
  const explanation = getTrustScoreExplanation(score, rating)
  const compact = size === "sm"

  return (
    <span
      title={explanation}
      aria-label={`${band.label}, trust rating ${rating} out of 100. ${explanation}`}
      tabIndex={0}
      className={cn(
        "inline-flex max-w-full items-center gap-1.5 rounded-full border font-medium leading-none outline-none focus-visible:ring-2 focus-visible:ring-ring/60",
        compact ? "px-2 py-1.5 text-[10px]" : "px-2.5 py-2 text-xs",
        appearance === "overlay"
          ? "border-white/15 bg-black/65 text-white backdrop-blur-md"
          : toneClasses[band.tone],
        className,
      )}
    >
      <ShieldCheck className={cn("shrink-0", compact ? "h-3.5 w-3.5" : "h-4 w-4", iconToneClasses[band.tone])} />
      {showLabel && <span className="truncate">{band.label}</span>}
      <span className="shrink-0 font-semibold tabular-nums">{rating}/100</span>
    </span>
  )
}
