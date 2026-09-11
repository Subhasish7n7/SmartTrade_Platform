import { notFound } from "next/navigation"

import { getPublicProfile } from "@/lib/api/profile/server"
import { getItemsByUser } from "@/lib/api/items/server"
import { InventoryPage } from "@/components/marketplace/inventory/inventory-page"

interface Props {
  params: Promise<{ userId: string }>
}

export default async function PublicInventoryRoute({ params }: Props) {
  const { userId } = await params
  const id = Number(userId)

  if (!Number.isInteger(id) || id <= 0) notFound()

  try {
    const [profile, items] = await Promise.all([
      getPublicProfile(id),
      getItemsByUser(id),
    ])

    return <InventoryPage profile={profile} items={items} isOwner={false} />
  } catch {
    notFound()
  }
}
