"use client"

import * as React from "react"
import { AnimatePresence, motion } from "framer-motion"

import { ItemResponse } from "@/lib/types/api/item"
import { MyUserProfile, PublicUserProfile } from "@/lib/types/api/profile"

import { ProfileHeader } from "./profile-header"
import { ProfileSidebar, ProfileTab } from "./profile-sidebar"
import { ProfileInventoryTab } from "./profile-inventory-tab"
import { ProfileOverview } from "./sections/profile-overview"

interface Props {
  profile: MyUserProfile | PublicUserProfile
  items: ItemResponse[]
  isOwner: boolean
}

export function ProfileLayout({ profile, items, isOwner }: Props) {
  const [tab, setTab] = React.useState<ProfileTab>("overview")

  return (
    <div className="space-y-5">
      <ProfileHeader profile={profile} isOwner={isOwner} onEdit={isOwner ? () => setTab("settings") : undefined} />

      <div className="grid gap-5 lg:grid-cols-[250px_minmax(0,1fr)] xl:grid-cols-[270px_minmax(0,1fr)]">
        <ProfileSidebar tab={tab} onChange={setTab} />

        <main className="min-w-0">
          <AnimatePresence mode="wait" initial={false}>
            <motion.div
              key={tab}
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -6 }}
              transition={{ duration: 0.18 }}
            >
              {tab === "overview" && (
                <ProfileOverview profile={profile} items={items} isOwner={isOwner} />
              )}

              {tab === "inventory" && (
                <ProfileInventoryTab items={items} isOwner={isOwner} />
              )}

              {tab === "trade-inventory" && (
                <ProfileInventoryTab
                  items={items.filter((item) => item.forTrade && item.available)}
                  isOwner={isOwner}
                  title="Trade Inventory"
                  description="Items currently available for trade proposals."
                  emptyTitle="No trade inventory"
                  emptyDescription={
                    isOwner
                      ? "List an item for trade to start building your trade inventory."
                      : "This trader has no items currently available for trade."
                  }
                />
              )}

              {tab !== "overview" && tab !== "inventory" && tab !== "trade-inventory" && (
                <ProfileComingSoon title={tabLabels[tab]} />
              )}
            </motion.div>
          </AnimatePresence>
        </main>
      </div>
    </div>
  )
}

const tabLabels: Record<ProfileTab, string> = {
  overview: "Overview",
  inventory: "Inventory",
  "trade-inventory": "Trade Inventory",
  trades: "Trades",
  reviews: "Reviews",
  activity: "Activity",
  security: "Security",
  settings: "Settings",
}

function ProfileComingSoon({ title }: { title: string }) {
  return (
    <section className="content-surface overflow-hidden p-0">
      <div className="trade-card-accent" />
      <div className="flex min-h-[460px] items-center justify-center px-6 py-16 text-center">
        <div className="max-w-md">
          <div className="mx-auto mb-5 h-12 w-12 rounded-2xl border border-border/60 bg-card/60 shadow-[0_0_30px_rgba(168,85,247,.08)]" />
          <h2 className="text-xl font-semibold">{title}</h2>
          <p className="mt-2 text-sm leading-6 text-muted-foreground">
            This profile area is reserved for the next feature pass. The profile shell and marketplace data layer are already live.
          </p>
        </div>
      </div>
    </section>
  )
}
