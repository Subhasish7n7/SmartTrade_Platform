import { cn } from "@/lib/utils"

interface Props {
  userPrice: number
  aiEstimate: number
}

export function DealRating({ userPrice, aiEstimate }: Props) {
  if (!Number.isFinite(userPrice) || !Number.isFinite(aiEstimate) || aiEstimate <= 0) {
    return null
  }

  const diff = ((userPrice - aiEstimate) / aiEstimate) * 100
  const isGoodDeal = diff < -5
  const isAboveEstimate = diff > 5

  const label = isGoodDeal
    ? "Great Deal"
    : isAboveEstimate
      ? "Above Estimate"
      : "Fair Price"

  const tone = isGoodDeal
    ? "text-emerald-300"
    : isAboveEstimate
      ? "text-amber-300"
      : "text-[var(--trust-blue)]"

  const fillClass = isGoodDeal
    ? "bg-gradient-to-r from-emerald-400 to-[var(--trust-blue)]"
    : isAboveEstimate
      ? "bg-gradient-to-r from-amber-300 to-[var(--premium-gold)]"
      : "bg-gradient-to-r from-[var(--trust-blue)] to-[var(--negotiation-violet)]"

  const width = isGoodDeal
    ? Math.min(100, Math.max(20, 100 + diff))
    : isAboveEstimate
      ? Math.min(100, Math.max(20, 100 - diff))
      : Math.min(100, Math.max(35, 82 - Math.abs(diff)))

  const explanation = isGoodDeal
    ? `Your price is ${Math.abs(Math.round(diff))}% below the used estimate.`
    : isAboveEstimate
      ? `Your price is ${Math.round(diff)}% above the used estimate.`
      : "Your price sits close to the condition-adjusted estimate."

  return (
    <div className="rounded-xl border border-[var(--negotiation-violet)]/16 bg-gradient-to-br from-[var(--negotiation-violet)]/[0.055] via-white/[0.018] to-[var(--trust-blue)]/[0.035] px-3.5 py-3.5">
      <div className="flex items-center justify-between gap-4 text-sm">
        <span className="text-xs font-semibold uppercase tracking-[0.12em] text-muted-foreground">
          Deal rating
        </span>

        <span className={cn("font-semibold", tone)}>{label}</span>
      </div>

      <div className="mt-3 h-1.5 overflow-hidden rounded-full bg-white/[0.07]">
        <div
          className={cn("h-full rounded-full shadow-[0_0_14px_rgba(59,130,246,.14)]", fillClass)}
          style={{ width: `${width}%` }}
        />
      </div>

      <p className="mt-2 text-[11px] leading-5 text-muted-foreground">
        {explanation}
      </p>
    </div>
  )
}
