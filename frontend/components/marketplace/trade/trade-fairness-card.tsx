import { Progress } from "@/components/ui/progress";

import { Scale, TrendingUp, TrendingDown, Minus } from "lucide-react";

interface Props {
  yourValue: number;
  theirValue: number;
}

export function TradeFairnessCard({ yourValue, theirValue }: Props) {
  const fairness =
    yourValue && theirValue
      ? Math.round(
          (Math.min(yourValue, theirValue) / Math.max(yourValue, theirValue)) *
            100,
        )
      : 0;
  const difference = Math.abs(yourValue - theirValue);
  const excellent = fairness >= 95;
  const fair = fairness >= 85;
  const uneven = fairness >= 70;
  const status = excellent
    ? {
        label: "Excellent Match",
        color: "text-[var(--premium-gold)]",
        bg: "bg-[var(--premium-gold)]/10",
        icon: Minus,
        recommendation:
          "Very balanced trade. Both sides contribute nearly equal value.",
      }
    : fair
      ? {
          label: "Fair Trade",
          color: "text-[var(--trust-blue)]",
          bg: "bg-[var(--trust-blue)]/10",
          icon: Minus,
          recommendation:
            "Good balance. Small value differences are unlikely to affect acceptance.",
        }
      : uneven
        ? {
            label: "Uneven Trade",
            color: "text-warning",
            bg: "bg-warning/10",
            icon: yourValue > theirValue ? TrendingDown : TrendingUp,
            recommendation:
              "Consider adjusting value or adding cash before sending.",
          }
        : {
            label: "Poor Match",
            color: "text-destructive",
            bg: "bg-destructive/10",
            icon: yourValue > theirValue ? TrendingDown : TrendingUp,
            recommendation:
              "Large value gap detected. This offer may be rejected.",
          };

  const Icon = status.icon;

  return (
    <div className="trade-card">
      <div className="trade-card-accent" />

      <div className="p-5">
        <div className="flex items-center gap-3 mb-5">
          <div className="trade-icon">
            <Scale className="h-5 w-5 text-primary" />
          </div>

          <div>
            <h3 className="font-semibold">Trade Analysis</h3>

            <p className="text-sm text-muted-foreground">
              Value balance overview
            </p>
          </div>
        </div>

        <div className="text-center py-2">
          <div className="text-4xl font-bold leading-none">{fairness}%</div>

          <div
            className={`
              inline-flex
              items-center
              gap-2
              px-3
              py-1
              rounded-full
              mt-3
              text-sm
              font-medium
              ${status.bg}
              ${status.color}
            `}
          >
            <Icon className="h-4 w-4" />
            {status.label}
          </div>
        </div>

        <Progress value={fairness} className="h-3 mb-5" />
        <div className="grid grid-cols-2 gap-3 mb-5">

          <div className="trade-stat text-center">
            <p className="text-xs text-muted-foreground">
              Your Value
            </p>

            <p className="font-semibold mt-1">
              ₹{yourValue.toLocaleString()}
            </p>
          </div>

          <div className="trade-stat text-center">
            <p className="text-xs text-muted-foreground">
              Their Value
            </p>

            <p className="font-semibold mt-1">
              ₹{theirValue.toLocaleString()}
            </p>
          </div>

        </div>

        <div
          className={`
            rounded-2xl
            p-4
            text-sm
            leading-relaxed
            ${status.bg}
          `}
        >
          <div className="mb-1 text-center">

            <p className="text-sm text-muted-foreground">
              {theirValue > yourValue
                ? `You receive ₹${difference.toLocaleString()} more value`
                : yourValue > theirValue
                ? `You offer ₹${difference.toLocaleString()} more value`
                : "Perfectly balanced exchange"}
            </p>

          </div>
          {status.recommendation}
        </div>
      </div>
    </div>
  );
}
