"use client"

import Link from "next/link"

import { Button } from "@/components/ui/button"

interface Props {
  tradeId: number
}

export function PinnedTradeCard({
  tradeId,
}: Props) {
  return (
    <div
      className="
        mx-5
        mt-5
        trade-card
        rounded-2xl
        p-4
      "
    >
      <div className="flex items-center justify-between">
        <div>
          <p className="text-xs text-muted-foreground">
            Pinned Trade
          </p>

          <p className="font-semibold mt-1">
            Trade #{tradeId}
          </p>
        </div>

        <Link
          href={`/trades/${tradeId}`}
        >
          <Button size="sm">
            View Trade
          </Button>
        </Link>
      </div>
    </div>
  )
}