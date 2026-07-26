// components/marketplace/profile/profile-trade-inventory-tab.tsx
"use client"

import { ProfileInventoryTab } from "./profile-inventory-tab"

import { ProfileItem } from "@/app/profile/page"

interface Props {
  items: ProfileItem[]
}

export function ProfileTradeInventoryTab({
  items,
}: Props) {
  return (
    <ProfileInventoryTab
      items={items.filter((x) => x.forTrade)}
    />
  )
}