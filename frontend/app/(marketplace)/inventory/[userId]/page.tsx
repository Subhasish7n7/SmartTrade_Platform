import { notFound } from "next/navigation"

import { PrivateAccessNotice } from "@/components/marketplace/private-access-notice"
import { getPublicProfile } from "@/lib/api/profile/server"
import { getItemsByUser } from "@/lib/api/items/server"
import { InventoryPage } from "@/components/marketplace/inventory/inventory-page"
import type { ItemResponse } from "@/lib/types/api/item"
import type { PublicUserProfile } from "@/lib/types/api/profile"

interface Props {
  params: Promise<{ userId: string }>
}

function getHttpStatus(error: unknown): number | undefined {
  if (!error || typeof error !== "object" || !("response" in error)) return undefined
  const response = (error as { response?: { status?: unknown } }).response
  return typeof response?.status === "number" ? response.status : undefined
}

export default async function PublicInventoryRoute({ params }: Props) {
  const { userId } = await params
  const id = Number(userId)
  if (!Number.isSafeInteger(id) || id <= 0) notFound()

  let profile: PublicUserProfile | null = null
  let privateProfile = false
  try {
    profile = await getPublicProfile(id)
  } catch (error) {
    const status = getHttpStatus(error)
    if (status === 403) privateProfile = true
    else if (status === 404) notFound()
    else throw error
  }
  if (privateProfile || !profile) return <PrivateAccessNotice kind="profile" />

  let items: ItemResponse[] = []
  let privateInventory = false
  try {
    items = await getItemsByUser(id)
  } catch (error) {
    const status = getHttpStatus(error)
    if (status === 403) privateInventory = true
    else if (status === 404) notFound()
    else throw error
  }
  if (privateInventory) return <PrivateAccessNotice kind="inventory" />

  return <InventoryPage profile={profile} items={items} isOwner={false} />
}
