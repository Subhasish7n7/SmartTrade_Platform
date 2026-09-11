import { redirect } from "next/navigation"

import { getMyProfile } from "@/lib/api/profile/server"
import { getItemsByUser } from "@/lib/api/items/server"
import { InventoryPage } from "@/components/marketplace/inventory/inventory-page"

export default async function InventoryRoute() {
  try {
    const profile = await getMyProfile()
    const items = await getItemsByUser(profile.id)

    return <InventoryPage profile={profile} items={items} isOwner />
  } catch {
    redirect("/login?redirect=%2Finventory")
  }
}
