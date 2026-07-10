import {
  AlertCircle,
  ArrowLeftRight,
  Plus,
} from "lucide-react"

import { Badge } from "@/components/ui/badge"

import { CashAdjustment } from "./cash-adjustment"

interface Props {
  itemPrice: number
}

export function TradeOfferCard({
  itemPrice,
}: Props) {
  return (
    <div className="content-surface p-6 space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="font-semibold flex items-center gap-2">
          <ArrowLeftRight className="h-5 w-5 text-primary" />

          Trade Offer
        </h3>

        <Badge
          variant="outline"
          className="text-muted-foreground"
        >
          Optional
        </Badge>
      </div>

      {/* ITEMS */}
      <div>
        <p className="text-sm text-muted-foreground mb-2">
          Your Items (0 selected)
        </p>

        <button className="w-full h-24 rounded-xl border-2 border-dashed border-border hover:border-primary/50 transition-colors flex items-center justify-center gap-2 text-muted-foreground">
          <Plus className="h-5 w-5" />

          Add items from your inventory
        </button>
      </div>

      {/* CASH */}
      <CashAdjustment />

      {/* FAIRNESS */}
      <div className="p-4 rounded-xl bg-amber-500/10 border border-amber-500/20">
        <div className="flex items-center gap-3">
          <AlertCircle className="h-6 w-6 text-amber-400" />

          <div>
            <p className="font-medium">
              Add items to balance trade
            </p>

            <p className="text-sm text-muted-foreground">
              Estimated value: $0 vs $
              {itemPrice.toLocaleString()}
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}