import { Progress } from "@/components/ui/progress"

import { Minus, Scale, TrendingDown, TrendingUp } from "lucide-react"

interface Props {
  yourValue: number
  theirValue: number
}

export function TradeFairnessCard({ yourValue, theirValue }: Props) {
  const fairness = yourValue && theirValue ? Math.round((Math.min(yourValue, theirValue) / Math.max(yourValue, theirValue)) * 100) : 0
  const difference = Math.abs(yourValue - theirValue)
  const excellent = fairness >= 95
  const fair = fairness >= 85
  const uneven = fairness >= 70
  const status = excellent
    ? {
        label: "Excellent Match",
        color: "text-[var(--premium-gold)]",
        bg: "bg-[var(--premium-gold)]/10",
        icon: Minus,
        recommendation: "Very balanced trade. Both sides contribute nearly equal value.",
      }
    : fair
      ? {
          label: "Fair Trade",
          color: "text-[var(--trust-blue)]",
          bg: "bg-[var(--trust-blue)]/10",
          icon: Minus,
          recommendation: "Good balance. Small value differences are unlikely to affect acceptance.",
        }
      : uneven
        ? {
            label: "Uneven Trade",
            color: "text-warning",
            bg: "bg-warning/10",
            icon: yourValue > theirValue ? TrendingDown : TrendingUp,
            recommendation: "Consider adjusting value or adding cash before sending.",
          }
        : {
            label: "Poor Match",
            color: "text-destructive",
            bg: "bg-destructive/10",
            icon: yourValue > theirValue ? TrendingDown : TrendingUp,
            recommendation: "Large value gap detected. This offer may be rejected.",
          }

  const Icon = status.icon

  return (
    <div className="rounded-xl border border-border/50 bg-card/40 p-3">
      <div className="flex items-center justify-between">
        <div>
          <div className="flex items-center gap-2">
            <Scale className="h-4 w-4 text-primary" />

            <span className="font-medium text-sm">{status.label}</span>
          </div>

          <p className="text-xs text-muted-foreground mt-1">{fairness}% balanced</p>
        </div>

        <div className={`text-lg font-bold ${status.color}`}>{fairness}%</div>
      </div>

      <Progress value={fairness} className="h-1 mt-3 mb-3" />

      <div className={`rounded-lg p-2 text-xs leading-relaxed ${status.bg}`}>
        {difference === 0 && "Excellent balance. This trade is likely to feel fair to both parties."}

        {yourValue > theirValue && (
          <>
            <p>• You're offering ₹{difference.toLocaleString()} more.</p>
            <p>• Consider asking for cash or another item.</p>
            <p>• This offer currently favors the seller.</p>
          </>
        )}

        {theirValue > yourValue && (
          <>
            <p>• You're receiving ₹{difference.toLocaleString()} more.</p>
            <p>• The seller may expect additional value.</p>
            <p>• Consider adding cash to improve acceptance.</p>
          </>
        )}
      </div>
    </div>
  )
}
