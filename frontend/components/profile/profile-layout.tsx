"use client"

import { AnimatePresence, motion } from "framer-motion"
import { ChevronRight, CircleUserRound } from "lucide-react"
import * as React from "react"

import { ItemResponse } from "@/lib/types/api/item"
import { MyUserProfile, PublicUserProfile } from "@/lib/types/api/profile"

import { ProfileHeader } from "./profile-header"
import { ProfileInventoryTab } from "./profile-inventory-tab"
import { ProfileSidebar, ProfileTab } from "./profile-sidebar"
import { ProfileOverview } from "./sections/profile-overview"
import { ProfileReviewsTab } from "./sections/profile-reviews-tab"
import { ProfileSecurityTab } from "./sections/profile-security-tab"
import { ProfileSettingsTab } from "./sections/profile-settings-tab"

interface Props {
  profile: MyUserProfile | PublicUserProfile
  items: ItemResponse[]
  isOwner: boolean
  isSettings?: string
}

export function ProfileLayout({ profile, items, isOwner, isSettings }: Props) {
  const [tab, setTab] = React.useState<ProfileTab>(isSettings === "true" ? "settings" : "overview")
  const changeTab = React.useCallback(
    (nextTab: ProfileTab) => {
      if (nextTab === tab) return

      window.scrollTo({ top: 0, left: 0, behavior: "auto" })
      setTab(nextTab)
    },
    [tab],
  )

  return (
    <div className="relative block">
      <ProfileSidebar tab={tab} onChange={changeTab} isOwner={isOwner} />

      <main className="min-w-0 lg:ml-[275px] xl:ml-[290px]">
        <AnimatePresence mode="wait" initial={false}>
          <motion.div
            key={tab}
            initial={{ opacity: 0, y: 7 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -5 }}
            transition={{ duration: 0.18, ease: "easeOut" }}
            className="space-y-5"
          >
            {tab === "overview" ? (
              <>
                <ProfileHeader profile={profile} isOwner={isOwner} onEdit={isOwner ? () => changeTab("settings") : undefined} />
                <ProfileOverview profile={profile} items={items} isOwner={isOwner} onNavigate={changeTab} />
              </>
            ) : (
              <ProfileTabHeader tab={tab} isOwner={isOwner} onEditProfile={tab === "settings" ? undefined : () => changeTab("settings")} />
            )}

            {tab === "inventory" && <ProfileInventoryTab items={items} isOwner={isOwner} />}

            {tab === "reviews" && <ProfileReviewsTab userId={profile.id} isOwner={isOwner} />}

            {tab === "security" && isOwner && <ProfileSecurityTab />}

            {tab === "settings" && isOwner && <ProfileSettingsTab />}
          </motion.div>
        </AnimatePresence>
      </main>
    </div>
  )
}

const pageMeta: Record<Exclude<ProfileTab, "overview">, { title: string; description: string }> = {
  inventory: {
    title: "Inventory",
    description: "Browse the items currently listed by this trader on SmartTrade.",
  },
  reviews: {
    title: "Reviews",
    description: "Transaction feedback from people who have completed marketplace activity with this trader.",
  },
  security: {
    title: "Security",
    description: "Manage authentication, account protection, and active sessions.",
  },
  settings: {
    title: "Settings",
    description: "Manage marketplace preferences, privacy, and notifications.",
  },
}

function ProfileTabHeader({
  tab,
  isOwner,
  onEditProfile,
}: {
  tab: Exclude<ProfileTab, "overview">
  isOwner: boolean
  onEditProfile?: () => void
}) {
  const meta = pageMeta[tab]

  return (
    <section className="rounded-2xl border border-border/60 bg-card/50 px-5 py-4 backdrop-blur-xl sm:px-6 sm:py-5">
      <div className="flex items-center justify-between gap-5">
        <div className="min-w-0">
          <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-[0.16em] text-muted-foreground">
            <CircleUserRound className="h-3.5 w-3.5 text-[var(--trust-blue)]" />
            Profile
            <ChevronRight className="h-3.5 w-3.5 opacity-50" />
            <span className="text-foreground/80">{meta.title}</span>
          </div>
          <h1 className="mt-2 text-2xl font-bold tracking-tight sm:text-[28px]">{meta.title}</h1>
          <p className="mt-1 max-w-2xl text-sm leading-6 text-muted-foreground">{meta.description}</p>
        </div>

        {isOwner && tab === "settings" && onEditProfile && (
          <button
            type="button"
            onClick={onEditProfile}
            className="inline-flex h-10 shrink-0 items-center justify-center rounded-xl border border-border/70 bg-white/[0.035] px-4 text-sm font-semibold transition hover:border-[var(--trust-blue)]/35 hover:bg-white/[0.055]"
          >
            Edit profile
          </button>
        )}
      </div>
    </section>
  )
}
