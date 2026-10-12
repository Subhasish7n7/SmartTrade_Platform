"use client"

import { Scale } from "lucide-react"

interface Props {
  yourValue: number
  theirValue: number
  usedYourValue?: number
  usedTheirValue?: number
  newYourValue?: number | null
  newTheirValue?: number | null
  cashAdjustment?: number
  yourItemCount?: number
  theirItemCount?: number
  compact?: boolean
}

const formatPrice = (value: number | null | undefined) =>
  value == null ? "N/A" : `₹${value.toLocaleString("en-IN")}`

export function TradeFairnessCard({
  yourValue,
  theirValue,
  usedYourValue,
  usedTheirValue,
  newYourValue,
  newTheirValue,
  cashAdjustment = 0,
  yourItemCount,
  theirItemCount,
  compact = false,
}: Props) {
  const balance =
    yourValue > 0 && theirValue > 0
      ? Math.round((Math.min(yourValue, theirValue) / Math.max(yourValue, theirValue)) * 100)
      : 0

  const listedDifference = Math.abs(yourValue - theirValue)

  const tone =
    balance >= 95
      ? {
          label: "Very close",
          border: "border-[var(--trust-blue)]/25",
          bg: "bg-[linear-gradient(145deg,rgba(59,130,246,.095),rgba(168,85,247,.055))]",
          accent: "text-[var(--trust-blue)]",
          glow: "shadow-[0_18px_45px_rgba(59,130,246,.08)]",
        }
      : balance >= 85
        ? {
            label: "Moderate gap",
            border: "border-[var(--negotiation-violet)]/25",
            bg: "bg-[linear-gradient(145deg,rgba(168,85,247,.10),rgba(59,130,246,.045))]",
            accent: "text-[var(--negotiation-violet)]",
            glow: "shadow-[0_18px_45px_rgba(168,85,247,.08)]",
          }
        : balance >= 70
          ? {
              label: "Worth adjusting",
              border: "border-[var(--premium-gold)]/25",
              bg: "bg-[linear-gradient(145deg,rgba(250,204,21,.085),rgba(168,85,247,.04))]",
              accent: "text-[var(--premium-gold)]",
              glow: "shadow-[0_18px_45px_rgba(250,204,21,.06)]",
            }
          : {
              label: "Large value gap",
              border: "border-[var(--trade-danger)]/30",
              bg: "bg-[linear-gradient(145deg,rgba(239,68,68,.085),rgba(250,204,21,.035))]",
              accent: "text-[var(--trade-danger)]",
              glow: "shadow-[0_18px_45px_rgba(239,68,68,.055)]",
            }

  return (
    <section
      className={`trade-decision-card rounded-[22px] border ${tone.border} ${tone.bg} ${tone.glow} ${compact ? "p-4" : "p-5"}`}
    >
      <div className="flex items-start justify-between gap-3">
        <div>
          <div className="flex items-center gap-2">
            <span
              className={`inline-flex h-8 w-8 items-center justify-center rounded-xl border border-white/[0.07] bg-white/[0.03] ${tone.accent}`}
            >
              <Scale className="h-4 w-4" />
            </span>
            <div>
              <p className="text-[10px] font-semibold uppercase tracking-[0.16em] text-muted-foreground">
                Decision support
              </p>
              <h2 className="mt-0.5 text-sm font-semibold">{tone.label}</h2>
            </div>
          </div>
        </div>

        <div className="text-right">
          <p className="text-[10px] text-muted-foreground">Listed balance</p>
          <p className={`mt-0.5 text-base font-bold tabular-nums ${tone.accent}`}>{balance}%</p>
        </div>
      </div>

      <div className="mt-3">
        <div className="h-1.5 overflow-hidden rounded-full bg-black/20">
          <div
            className="h-full rounded-full bg-[linear-gradient(90deg,var(--trust-blue),var(--negotiation-violet),var(--premium-gold))] transition-[width] duration-300"
            style={{ width: `${Math.max(0, Math.min(100, balance))}%` }}
          />
        </div>
        <p className="mt-2 text-[11px] leading-5 text-muted-foreground">
          {listedDifference === 0
            ? "The listed values are exactly even."
            : yourValue > theirValue
              ? `You are giving ${formatPrice(listedDifference)} more at listed value.`
              : `You are receiving ${formatPrice(listedDifference)} more at listed value.`}
        </p>
      </div>

      <div className="mt-4 overflow-hidden rounded-xl border border-white/[0.07] bg-black/10">
        <div className="grid grid-cols-[minmax(0,1fr)_6.25rem_6.25rem] border-b border-white/[0.06] bg-white/[0.018] px-3 py-2 text-[9px] font-semibold uppercase tracking-[0.08em] text-muted-foreground">
          <span>Value lens</span>
          <span className="text-right">You give</span>
          <span className="text-right">You receive</span>
        </div>

        <DecisionRow label="Listed" yourValue={yourValue} theirValue={theirValue} />
        <DecisionRow label="Used estimate" yourValue={usedYourValue} theirValue={usedTheirValue} />
        <DecisionRow label="New market" yourValue={newYourValue} theirValue={newTheirValue} last />
      </div>

      {(yourItemCount != null || theirItemCount != null) && (
        <div className="mt-3 grid gap-2 sm:grid-cols-2">
          {yourItemCount != null && <MiniStat label="You give" value={`${yourItemCount} ${yourItemCount === 1 ? "item" : "items"}`} />}
          {theirItemCount != null && <MiniStat label="You receive" value={`${theirItemCount} ${theirItemCount === 1 ? "item" : "items"}`} />}
        </div>
      )}

      {cashAdjustment > 0 && (
        <div className="mt-2 rounded-xl border border-[var(--premium-gold)]/12 bg-[var(--premium-gold)]/[0.035] px-3 py-2 text-[11px] text-muted-foreground">
          Cash adjustment included: <span className="font-semibold text-foreground">{formatPrice(cashAdjustment)}</span>
        </div>
      )}

      <div className="mt-3 rounded-xl border border-white/[0.06] bg-white/[0.022] px-3 py-2.5 text-[11px] leading-5 text-muted-foreground">
        {getRecommendation({
          yourValue,
          theirValue,
          usedYourValue,
          usedTheirValue,
          newYourValue,
          newTheirValue,
        })}
      </div>
    </section>
  )
}

function DecisionRow({
  label,
  yourValue,
  theirValue,
  last,
}: {
  label: string
  yourValue?: number | null
  theirValue?: number | null
  last?: boolean
}) {
  return (
    <div
      className={`grid grid-cols-[minmax(0,1fr)_6.25rem_6.25rem] items-center gap-2 px-3 py-2.5 ${
        last ? "" : "border-b border-white/[0.05]"
      }`}
    >
      <span className="text-[11px] text-muted-foreground">{label}</span>
      <span className="text-right text-[11px] font-semibold tabular-nums">{formatPrice(yourValue)}</span>
      <span className="text-right text-[11px] font-semibold tabular-nums">{formatPrice(theirValue)}</span>
    </div>
  )
}

function MiniStat({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-xl border border-white/[0.055] bg-white/[0.018] px-2.5 py-2">
      <p className="text-[9px] font-semibold uppercase tracking-[0.08em] text-muted-foreground">{label}</p>
      <p className="mt-0.5 text-[11px] font-bold tabular-nums">{value}</p>
    </div>
  )
}

function getRecommendation({
  yourValue,
  theirValue,
  usedYourValue,
  usedTheirValue,
  newYourValue,
  newTheirValue,
}: {
  yourValue: number
  theirValue: number
  usedYourValue?: number
  usedTheirValue?: number
  newYourValue?: number | null
  newTheirValue?: number | null
}) {
  if (yourValue === 0 || theirValue === 0) {
    return "Select at least one item on both sides to see a meaningful trade comparison."
  }

  const listedGap = Math.abs(yourValue - theirValue)

  if (
    usedYourValue != null &&
    usedTheirValue != null &&
    Math.abs(usedYourValue - usedTheirValue) > Math.max(listedGap * 2, 500)
  ) {
    return "The listed values are relatively close, but the used estimates tell a different story. Compare the item-level estimates before accepting the offer."
  }

  if (
    newYourValue != null &&
    newTheirValue != null &&
    Math.abs(newYourValue - newTheirValue) > Math.max(listedGap * 3, 1000)
  ) {
    return "New-market references show a larger gap than the listed prices. Use item condition and used estimates to judge whether that difference is meaningful."
  }

  if (listedGap === 0) {
    return "The listed values are even. The next useful checks are item condition, valuation references, and trader context."
  }

  return yourValue > theirValue
    ? `You are giving ${formatPrice(listedGap)} more at listed value. A cash adjustment or another item could bring the proposal closer.`
    : `You are receiving ${formatPrice(listedGap)} more at listed value. You may have room to offer less or keep the current proposal depending on the other valuation references.`
}
