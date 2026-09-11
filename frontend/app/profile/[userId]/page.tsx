import { AnimatedPage, Section } from "@/components/marketplace/animations/section"
import { Navbar } from "@/components/marketplace/navbar"
import { ProfileLayout } from "@/components/marketplace/profile/profile-layout"
import { getPublicProfile } from "@/lib/api/profile/server"
import { getItemsByUser } from "@/lib/api/items/server"

interface Props {
  params: Promise<{ userId: string }>
}

export default async function PublicProfilePage({ params }: Props) {
  const { userId: rawUserId } = await params
  const userId = Number(rawUserId)

  if (!Number.isInteger(userId) || userId <= 0) {
    return <ProfileNotFound />
  }

  try {
    const [profile, items] = await Promise.all([
      getPublicProfile(userId),
      getItemsByUser(userId),
    ])

    return (
      <div className="min-h-screen bg-background">
        <main className="px-4 pb-24 pt-20 sm:px-6 lg:px-8">
          <Navbar />
          <div className="mx-auto max-w-[1450px]">
            <AnimatedPage>
              <Section>
                <ProfileLayout profile={profile} items={items} isOwner={false} />
              </Section>
            </AnimatedPage>
          </div>
        </main>
      </div>
    )
  } catch {
    return <ProfileNotFound />
  }
}

function ProfileNotFound() {
  return (
    <div className="min-h-screen bg-background px-4 pb-24 pt-32 sm:px-6">
      <div className="mx-auto max-w-[1450px]">
        <div className="content-surface mx-auto max-w-lg overflow-hidden p-0 text-center">
          <div className="trade-card-accent" />
          <div className="px-6 py-16">
            <h1 className="text-2xl font-bold">Trader not found</h1>
            <p className="mt-2 text-sm leading-6 text-muted-foreground">
              This profile does not exist or is no longer available.
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
