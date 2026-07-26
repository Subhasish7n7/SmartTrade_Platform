// profile-trades-tab.tsx
"use client"

import Link from "next/link"

import { Button } from "@/components/ui/button"

import { ArrowRight, RefreshCcw } from "lucide-react"

import { ProfileTrade } from "@/app/profile/page"

interface Props {
  trades: ProfileTrade[]
}

export function ProfileTradesTab({
  trades,
}: Props) {

  if (trades.length === 0) {
    return (
      <div className="content-surface py-20 text-center">

        <RefreshCcw className="mx-auto h-12 w-12 text-muted-foreground" />

        <h2 className="mt-5 text-2xl font-semibold">
          No Active Trades
        </h2>

      </div>
    )
  }

  return (
    <div className="space-y-4">

      {trades.map((trade) => (

        <div
          key={trade.id}
          className="trade-card"
        >

          <div className="trade-card-accent" />

          <div className="p-6 flex flex-col gap-5 lg:flex-row lg:items-center lg:justify-between">

            <div>

              <h3 className="text-xl font-semibold">
                Trade #{trade.id}
              </h3>

              <p className="text-muted-foreground mt-1">
                Trading with {trade.otherUser}
              </p>

            </div>

            <div className="trade-stat">
              {trade.status}
            </div>

            <div className="text-sm text-muted-foreground">
              Updated {trade.updatedAt}
            </div>

            <Link
              href={`/trades/${trade.id}`}
            >
              <Button>

                View

                <ArrowRight className="ml-2 h-4 w-4" />

              </Button>
            </Link>

          </div>

        </div>

      ))}

    </div>
  )
}