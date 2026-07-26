// components/marketplace/profile/profile-activity-tab.tsx
"use client"

import {
  Package,
  RefreshCcw,
  Star,
  User,
} from "lucide-react"

import { ActivityItem } from "@/app/profile/page"

interface Props {
  activity: ActivityItem[]
}

const icons = {
  listing: Package,
  trade: RefreshCcw,
  review: Star,
  account: User,
}

export function ProfileActivityTab({
  activity,
}: Props) {

  if (!activity.length) {
    return (
      <div className="content-surface py-20 text-center">

        <User className="mx-auto h-12 w-12 text-muted-foreground" />

        <h2 className="mt-5 text-2xl font-semibold">
          No Activity
        </h2>

      </div>
    )
  }

  return (
    <div className="relative">

      <div className="absolute left-5 top-0 bottom-0 w-px bg-border" />

      <div className="space-y-8">

        {activity.map((item) => {

          const Icon = icons[item.icon]

          return (
            <div
              key={item.id}
              className="relative flex gap-5"
            >

              <div className="trade-icon relative z-10">

                <Icon className="h-5 w-5" />

              </div>

              <div className="content-surface flex-1">

                <h3 className="font-semibold">
                  {item.title}
                </h3>

                <p className="mt-2 text-muted-foreground">
                  {item.subtitle}
                </p>

                <p className="mt-4 text-xs text-muted-foreground">
                  {item.createdAt}
                </p>

              </div>

            </div>
          )

        })}

      </div>

    </div>
  )
}