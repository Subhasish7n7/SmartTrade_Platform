"use client"

import { useState } from "react"

import { UserProfile } from "@/app/profile/page"

import { ProfileHeader } from "./profile-header"
import { ProfileSidebar } from "./profile-sidebar"

import { ProfileOverview } from "./sections/profile-overview"

interface Props {
  profile: UserProfile
}

export type ProfileTab =
  | "overview"
  | "inventory"
  | "trade-inventory"
  | "trades"
  | "reviews"
  | "activity"
  | "security"
  | "settings"

export function ProfileLayout({
  profile,
}: Props) {

  const [tab, setTab] =
    useState<ProfileTab>("overview")

  return (

    <div className="space-y-2">
      <ProfileHeader profile={profile} />

      <div className="grid gap-3 lg:grid-cols-[290px_1fr]">

        <ProfileSidebar
          tab={tab}
          onChange={setTab}
        />

        <div className="content-surface min-h-[700px] p-8">

          {tab === "overview" && (
            <ProfileOverview
              profile={profile}
            />
          )}

          {tab !== "overview" && (
            <div className="py-24 text-center text-muted-foreground">

              {tab} page

            </div>
          )}

        </div>

      </div>

    </div>

  )
}