"use client"

import { Check, RefreshCw, X } from "lucide-react"
import { Button } from "@/components/ui/button"
import { acceptTrade, cancelTrade } from "@/lib/api/trades/client"
import Link from "next/link"

interface Props {
  tradeId: number
  otherUserId: number
}

export function TradeDetailClient({ tradeId, otherUserId }: Props) {
  const handleAccept = async () => {
    await acceptTrade(tradeId)
  }

  const handleReject = async () => {
    await cancelTrade(tradeId)
  }

  return (
    <div className="trade-card">
      <div className="trade-card-accent" />

      <div className="p-5">
        <h3 className="font-semibold mb-1">Trade Actions</h3>

        <p className="text-sm text-muted-foreground mb-5">
          Respond to the current proposal
        </p>

        <div className="space-y-3">
          <Button onClick={handleAccept} className="w-full cta-primary">
            <Check className="h-4 w-4 mr-2" />
            Accept Trade
          </Button>

          <Link href={`/trades/create/${otherUserId}?tradeId=${tradeId}`}>
            <Button variant="surface" className="w-full">
              <RefreshCw className="h-4 w-4 mr-2" />
              Counter Offer
            </Button>
          </Link>

          <Button
            variant="destructive"
            className="w-full hover:bg-"
            onClick={handleReject}
          >
            <X className="h-4 w-4 mr-2" />
            Cancel Trade
          </Button>
        </div>
      </div>
    </div>
  )
}