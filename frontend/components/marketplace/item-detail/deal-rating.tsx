import { cn } from "@/lib/utils"

interface Props {
  userPrice: number
  aiEstimate: number
}

export function DealRating({ userPrice, aiEstimate,}: Props) {
  const diff =
    ((userPrice - aiEstimate) /
      aiEstimate) *
    100

  const isGoodDeal = diff < -5

  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between text-sm">
        <span className="text-muted-foreground">
          Deal Rating
        </span>

        <span
          className={cn(
            "font-medium",
            isGoodDeal
              ? "text-emerald-400"
              : "text-amber-400"
          )}
        >
          {isGoodDeal
            ? "Great Deal"
            : "Fair Price"}
        </span>
      </div>

      <div className="h-2 rounded-full bg-muted overflow-hidden">
        <div
          className={cn(
            "h-full rounded-full",
            isGoodDeal
              ? "bg-emerald-500"
              : "bg-amber-500"
          )}
          style={{
            width: `${Math.min(
              100,
              Math.max(
                15,
                100 - diff
              )
            )}%`,
          }}
        />
      </div>
    </div>
  )
}