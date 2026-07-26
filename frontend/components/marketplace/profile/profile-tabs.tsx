// components/marketplace/profile/profile-tabs.tsx
"use client"

import * as React from "react"

import { UserProfile } from "@/app/profile/page"

import { Button } from "@/components/ui/button"
import { ProfileInventoryTab } from "./profile-inventory-tab"
import { ProfileTradeInventoryTab } from "./profile-trade-inventory-tab"
import { ProfileTradesTab } from "./profile-trades-tab"
import { ProfileReviewsTab } from "./profile-reviews-tab"
import { ProfileActivityTab } from "./profile-activity-tab"

const tabs = ["Overview", "Inventory", "Trade Inventory", "Trades", "Reviews", "Activity"]

interface Props {
  profile: UserProfile
}

export function ProfileTabs({ profile }: Props) {
  const [tab, setTab] = React.useState("Overview")

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap gap-2">
        {tabs.map((item) => (
          <Button
            key={item}
            onClick={() => setTab(item)}
            variant={tab === item ? "default" : "surface"}
            className={tab === item ? "cta-primary" : ""}
          >
            {item}
          </Button>
        ))}
      </div>

      {tab === "Inventory" && <ProfileInventoryTab items={profile.inventory} />}

      {tab === "Trade Inventory" && <ProfileTradeInventoryTab items={profile.tradeInventory} />}

      {tab === "Trades" && <ProfileTradesTab trades={profile.activeTrades} />}

      {tab === "Reviews" && <ProfileReviewsTab reviews={profile.reviews} />}

      {tab === "Activity" && <ProfileActivityTab activity={profile.activity} />}

      {tab !== "Overview" && (
        <div className="content-surface h-[500px] flex items-center justify-center">
          <div className="text-center">
            <h2 className="text-2xl font-semibold">{tab}</h2>

            <p className="mt-2 text-muted-foreground">Implementation coming next.</p>
          </div>
        </div>
      )}
    </div>
  )
}

function Info({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div>
      <p className="text-sm text-muted-foreground">{label}</p>

      <p className="mt-1 font-medium">{value}</p>
    </div>
  )
}

function MiniStat({ title, value }: { title: string; value: React.ReactNode }) {
  return (
    <div className="trade-stat">
      <p className="text-xs text-muted-foreground">{title}</p>

      <p className="mt-2 text-xl font-bold">{value}</p>
    </div>
  )
}
