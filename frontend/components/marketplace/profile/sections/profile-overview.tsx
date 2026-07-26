"use client"

import { UserProfile } from "@/app/profile/page"

interface Props {
  profile: UserProfile
}

export function ProfileOverview({
  profile,
}: Props) {
  return (
    <div className="space-y-10">

      {/* Personal */}

      <Section
        title="Personal Information"
        description="Basic account information."
      >

        <Field
          label="Full Name"
          value={profile.name}
        />

        <Field
          label="Email"
          value={profile.email}
        />

        <Field
          label="Phone"
          value={profile.phone ?? "-"}
        />

        <Field
          label="Joined"
          value={profile.joinedAt}
        />

      </Section>

      {/* Trading */}

      <Section
        title="Trading Information"
        description="Your marketplace statistics."
      >

        <Field
          label="Trust Score"
          value={`${profile.trustScore}%`}
        />

        <Field
          label="Successful Trades"
          value={profile.successfulTrades}
        />

        <Field
          label="Listings"
          value={profile.totalListings}
        />

      </Section>

      {/* About */}

      <Section
        title="About"
        description="Public profile description."
      >

        <div className="rounded-xl border bg-background p-5 text-muted-foreground leading-relaxed">

          {profile.bio}

        </div>

      </Section>

    </div>
  )
}

function Section({
  title,
  description,
  children,
}: React.PropsWithChildren<{
  title: string
  description: string
}>) {
  return (
    <section>

      <div className="mb-5">

        <h2 className="text-2xl font-semibold">
          {title}
        </h2>

        <p className="text-muted-foreground mt-1">
          {description}
        </p>

      </div>

      <div className="overflow-hidden rounded-xl border">

        {children}

      </div>

    </section>
  )
}

function Field({
  label,
  value,
}: {
  label: string
  value: React.ReactNode
}) {
  return (
    <div className="grid grid-cols-[220px_1fr] border-b last:border-b-0">

      <div className="bg-muted/30 px-6 py-5 text-sm font-medium">

        {label}

      </div>

      <div className="px-6 py-5">

        {value}

      </div>

    </div>
  )
}