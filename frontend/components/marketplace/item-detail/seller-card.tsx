import { Star,} from "lucide-react"

import {
  Avatar,
  AvatarFallback,
} from "@/components/ui/avatar"

import { SellerUI } from "@/lib/types/api/item"

interface Props {
  seller: SellerUI
}

export function SellerCard({ seller,}: Props) {
  return (
    <div className="content-surface p-6">
      <h3 className="text-lg font-semibold mb-4">
        Seller
      </h3>

      <div className="flex items-start gap-4">
        <Avatar className="h-16 w-16">
          <AvatarFallback>
            {seller.name[0]}
          </AvatarFallback>
        </Avatar>

        <div className="flex-1">
          <div className="flex items-center gap-2 mb-1">
            <span className="font-semibold">
              {seller.name}
            </span>
          </div>

          <div className="flex items-center gap-4 text-sm text-muted-foreground mb-3">
            <span className="flex items-center gap-1">
              <Star className="h-3.5 w-3.5 fill-yellow-400 text-yellow-400" />

              {seller.trustScore}% Trust
            </span>

            <span>
              {seller.successfulTrades}/
              {seller.totalListings} trades
            </span>
          </div>
        </div>
      </div>
    </div>
  )
}