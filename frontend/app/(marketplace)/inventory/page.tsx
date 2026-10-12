import { redirect } from "next/navigation"

import { getMyProfile } from "@/lib/api/profile/server"
import { getMyInventory } from "@/lib/api/items/server"
import type { ItemResponse } from "@/lib/types/api/item"
import { InventoryPage } from "@/components/marketplace/inventory/inventory-page"

function getHttpStatus(error: unknown): number | undefined {
  if (!error || typeof error !== "object" || !("response" in error)) return undefined
  const response = (error as { response?: { status?: unknown } }).response
  return typeof response?.status === "number" ? response.status : undefined
}

export default async function InventoryRoute() {
  let profile: Awaited<ReturnType<typeof getMyProfile>>
  let items: ItemResponse[]

  try {
    profile = await getMyProfile()
    items = await getMyInventory()
  } catch (error) {

    if (getHttpStatus(error) === 401) {
      redirect("/login?redirect=%2Finventory")
    }
    throw error
  }

  return <InventoryPage profile={profile} items={items} isOwner />
}
