import { AnimatedPage, Section } from "@/components/marketplace/animations/section"
import { ProfileLayout } from "@/components/profile/profile-layout"
import { getCurrentUser } from "@/lib/api/auth/auth.server"
import { getMyInventory } from "@/lib/api/items/server"
import { getMyProfile } from "@/lib/api/profile/server"

function ProfileSignedOut() {
  return (
    <div className="content-surface mx-auto max-w-xl p-0 text-center">
      <div className="px-6 py-16">
        <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl border border-border/60 bg-white/[0.025]">
          <span className="text-xl">TV</span>
        </div>
        <h1 className="mt-5 text-2xl font-bold">Your trader profile</h1>
        <p className="mx-auto mt-2 max-w-md text-sm leading-6 text-muted-foreground">
          Sign in to open your profile, manage your marketplace presence, and see your inventory.
        </p>
        <a href="/login" className="cta-primary mt-6 inline-flex h-10 items-center justify-center rounded-xl px-5 text-sm font-semibold">
          Sign in
        </a>
      </div>
    </div>
  )
}
interface props{
  searchParams: Promise<{
    isSettings?:string
  }>
}

export default async function ProfilePage({searchParams}:props) {
  const {isSettings} = await searchParams
  let currentUser: Awaited<ReturnType<typeof getCurrentUser>> | null = null

  try {
    currentUser = await getCurrentUser()
  } catch {
    currentUser = null
  }

  const content = currentUser ? <ProfileData isSettings={isSettings} /> : <ProfileSignedOut />

  return (
    <div className="min-h-screen bg-background">
      <main className="px-4 pb-24 pt-6 sm:px-6 lg:px-8">
        <div className="mx-auto max-w-[1450px]">
          <AnimatedPage>
            <Section>{content}</Section>
          </AnimatedPage>
        </div>
      </main>
    </div>
  )
}

async function ProfileData({ isSettings }: { isSettings?: string }) {
  const [profile, items] = await Promise.all([getMyProfile(), getMyInventory()])
  return <ProfileLayout profile={profile} items={items} isOwner isSettings={isSettings} />
}
