import Link from "next/link"
import { LockKeyhole, ArrowLeft } from "lucide-react"

interface Props {
  kind: "profile" | "inventory"
  compact?: boolean
}

export function PrivateAccessNotice({ kind, compact = false }: Props) {
  const isProfile = kind === "profile"
  const title = isProfile ? "This profile is private" : "This inventory is private"
  const description = isProfile
    ? "This trader has chosen not to share their profile publicly. You can still browse other listings on SmartTrade."
    : "This trader has chosen not to show their inventory publicly. Their listings are not available to other users."

  if (compact) {
    return (
      <div role="status" className="flex items-start gap-3 rounded-xl border border-border/60 bg-card/40 p-4">
        <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg border border-border/60 bg-background/40 text-muted-foreground"><LockKeyhole className="h-4 w-4" /></span>
        <div className="min-w-0">
          <p className="text-sm font-semibold">{title}</p>
          <p className="mt-1 text-sm leading-5 text-muted-foreground">{description}</p>
        </div>
      </div>
    )
  }

  return (
    <main className="min-h-[55vh] px-4 pb-20 pt-24 sm:px-6 lg:px-8">
      <section className="content-surface mx-auto flex max-w-xl flex-col items-center px-6 py-12 text-center sm:px-10">
        <span className="flex h-14 w-14 items-center justify-center rounded-2xl border border-border/60 bg-background/40 text-muted-foreground"><LockKeyhole className="h-6 w-6" /></span>
        <p className="mt-5 text-[11px] font-semibold uppercase tracking-[0.16em] text-muted-foreground">Privacy settings</p>
        <h1 className="mt-2 text-2xl font-bold tracking-tight">{title}</h1>
        <p className="mt-2 max-w-md text-sm leading-6 text-muted-foreground">{description}</p>
        <Link href="/marketplace" className="cta-primary mt-6 inline-flex h-10 items-center justify-center rounded-xl px-4 text-sm font-semibold"><ArrowLeft className="mr-2 h-4 w-4" />Back to marketplace</Link>
      </section>
    </main>
  )
}
