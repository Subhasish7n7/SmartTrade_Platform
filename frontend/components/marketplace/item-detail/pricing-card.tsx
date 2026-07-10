import { TrendingDown } from "lucide-react"
import { ItemResponse } from "@/lib/types/api/item"
import { AiEstimateBox } from "./ai-estimate-box"
import { DealRating } from "./deal-rating"

interface Props {
  item: ItemResponse
}

export function PricingCard({ item }: Props) {
  const userPrice = item.userPrice ?? 0;
  const newPrice = item.newPrice ?? 0;
  const generatedPrice = item.generatedPrice ?? 0;

  const savings = newPrice - userPrice;
  const savingsPercent =
    newPrice > 0 ? Math.round((savings / newPrice) * 100) : 0;

  const formatPrice = (price: number | null | undefined) =>
  price == null ? "N/A" : `$${price.toLocaleString()}`;

  return (
    <div className="content-surface holo-edge rounded-3xl p-6 space-y-5">
      {/* PRICE */}
      <div className="flex items-start justify-between">
        <div>
          <p className="eyebrow mb-2">Listed Price</p>
          <p className="price-figure text-4xl font-bold leading-none">
            {formatPrice(item.userPrice)}
          </p>
          {item.newPrice != null && item.userPrice != null && (
            <p className="text-sm text-emerald-400 mt-2 font-medium">
              Save ${savings.toLocaleString()} · {savingsPercent}% off
            </p>
          )}
        </div>

        <div className="px-3 py-1.5 rounded-full text-xs font-semibold flex items-center gap-1 bg-emerald-500/15 text-emerald-400 border border-emerald-500/20">
          <TrendingDown className="h-3.5 w-3.5" />
          10% below market
        </div>
      </div>

      <AiEstimateBox aiEstimate={generatedPrice} />

      {/* ORIGINAL */}
      <div className="flex items-center justify-between text-sm py-3 border-y border-border/40">
        <span className="text-muted-foreground">Original / New Price</span>
        <span className="price-figure line-through text-muted-foreground">
          {formatPrice(item.newPrice)}
        </span>
      </div>

      <DealRating userPrice={userPrice} aiEstimate={generatedPrice} />
    </div>
  )
}