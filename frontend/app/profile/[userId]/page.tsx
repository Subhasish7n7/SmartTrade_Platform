import { notFound } from "next/navigation"

import { AnimatedPage, Section } from "@/components/marketplace/animations/section"
import { Navbar } from "@/components/marketplace/navbar"
import { PrivateAccessNotice } from "@/components/marketplace/private-access-notice"
import { ProfileLayout } from "@/components/profile/profile-layout"
import { getPublicProfile } from "@/lib/api/profile/server"
import { getItemsByUser } from "@/lib/api/items/server"
import type { PublicUserProfile } from "@/lib/types/api/profile"
import type { ItemResponse } from "@/lib/types/api/item"

interface Props {
  params: Promise<{ userId: string }>
}

function getHttpStatus(error: unknown): number | undefined {
  if (!error || typeof error !== "object" || !("response" in error)) return undefined
  const response = (error as { response?: { status?: unknown } }).response
  return typeof response?.status === "number" ? response.status : undefined
}

export default async function PublicProfilePage({ params }: Props) {
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

  if (privateProfile || !profile) {
    return (
      <div className="min-h-screen bg-background">
        <Navbar />
        <AnimatedPage>
          <Section>
            <PrivateAccessNotice kind="profile" />
          </Section>
        </AnimatedPage>
      </div>
    )
  }

  let items: ItemResponse[] = []
  let inventoryPrivate = false
  try {
    items = await getItemsByUser(id)
  } catch (error) {
    const status = getHttpStatus(error)
    if (status === 403) inventoryPrivate = true
    else if (status === 404) notFound()
    else throw error
  }

  return (
    <div className="min-h-screen bg-background">
      <main className="px-4 pb-24 pt-20 sm:px-6 lg:px-8">
        <Navbar />
        <div className="mx-auto max-w-[1450px]">
          <AnimatedPage>
            <Section>
              <div className="space-y-4">
                {inventoryPrivate && <PrivateAccessNotice kind="inventory" compact />}
                <ProfileLayout profile={profile} items={items} isOwner={false} />
              </div>
            </Section>
          </AnimatedPage>
        </div>
      </main>
    </div>
  )
}
