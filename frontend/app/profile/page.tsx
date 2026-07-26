import { AnimatedPage, Section } from "@/components/marketplace/animations/section"
import { BackButton } from "@/components/marketplace/back-button"
import { Navbar } from "@/components/marketplace/navbar"

import { ProfileLayout } from "@/components/marketplace/profile/profile-layout"

// TODO
// import { getProfile } from "@/lib/api/profile/server"

export interface UserProfile {
  id: number
  name: string
  email: string
  phone?: string
  avatar?: string
  bio?: string
  joinedAt: string
  trustScore: number
  successfulTrades: number
  totalListings: number
}

export default async function ProfilePage() {

  // const profile = await getProfile()

  const profile: UserProfile = {
    id: 1,
    name: "Steven Dori",
    email: "steven@example.com",
    phone: "+91 9999999999",
    joinedAt: "January 2025",
    bio: "Electronics enthusiast and fair trader.",
    avatar: "",
    trustScore: 92,
    successfulTrades: 64,
    totalListings: 142,
  }

  return (
    <div className="h-screen overflow-y-auto">
    <main className="pt-20 pb-24">
      <Navbar/>
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">

        <AnimatedPage>

          <Section>
            <ProfileLayout profile={profile} />
          </Section>

        </AnimatedPage>

      </div>
    </main>
    </div>
  )
}