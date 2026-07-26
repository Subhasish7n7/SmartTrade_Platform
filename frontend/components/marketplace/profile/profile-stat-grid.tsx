// components/marketplace/profile/profile-stat-grid.tsx
"use client"

import { ProfileStats } from "@/app/profile/page"
import {
  Shield,
  CheckCircle2,
  Package,
  RefreshCw,
  Heart,
  Eye,
} from "lucide-react"

interface Props {
  stats: ProfileStats
}

const cards = (stats: ProfileStats) => [
  {
    title: "Trust Score",
    value: `${stats.trustScore}%`,
    icon: Shield,
    color: "text-[var(--trust-blue)]",
  },
  {
    title: "Completed Trades",
    value: stats.completedTrades,
    icon: CheckCircle2,
    color: "text-primary",
  },
  {
    title: "Active Listings",
    value: stats.activeListings,
    icon: Package,
    color: "text-[var(--premium-gold)]",
  },
  {
    title: "Trade Inventory",
    value: stats.tradeInventory,
    icon: RefreshCw,
    color: "text-[var(--negotiation-violet)]",
  },
  {
    title: "Wishlist",
    value: stats.wishlist,
    icon: Heart,
    color: "text-pink-400",
  },
  {
    title: "Profile Views",
    value: stats.profileViews.toLocaleString(),
    icon: Eye,
    color: "text-cyan-400",
  },
]

export function ProfileStatGrid({ stats }: Props) {
  return (
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">

      {cards(stats).map((card) => {

        const Icon = card.icon

        return (
          <div
            key={card.title}
            className="trade-card trade-card-hover"
          >
            <div className="trade-card-accent" />

            <div className="flex items-center justify-between p-6">

              <div>

                <p className="text-sm text-muted-foreground">
                  {card.title}
                </p>

                <h2 className="mt-2 text-3xl font-bold">
                  {card.value}
                </h2>

              </div>

              <div className="trade-icon">
                <Icon className={`h-6 w-6 ${card.color}`} />
              </div>

            </div>
          </div>
        )
      })}
    </div>
  )
}