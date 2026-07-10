import { MarketplaceItem } from "@/lib/types/api/item"

export interface TradeOffer {
  tradeId: string

  status:
    | "OPEN"
    | "NEGOTIATING"
    | "ACCEPTED"
    | "COMPLETED"
    | "CANCELLED"
    | "EXPIRED"

  yourItems: MarketplaceItem[]
  theirItems: MarketplaceItem[]
  cashAdjustment: number
  createdAt: string
}
