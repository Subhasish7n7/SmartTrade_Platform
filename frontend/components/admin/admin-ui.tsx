import Link from "next/link"
import {
  ArrowRight,
  CheckCircle2,
  Clock3,
  EyeOff,
  FileClock,
  ShieldAlert,
} from "lucide-react"
import type { ReactNode } from "react"

import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"

export function AdminPageHeader({
  eyebrow,
  title,
  description,
  action,
}: {
  eyebrow: string
  title: string
  description: string
  action?: ReactNode
}) {
  return (
    <div className="mb-5 flex flex-col gap-3 border-b border-white/[0.06] pb-5 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <p className="text-[10px] font-semibold uppercase tracking-[0.18em] text-[var(--trust-blue)]">{eyebrow}</p>
        <h1 className="mt-1 text-xl font-bold tracking-tight sm:text-2xl">{title}</h1>
        <p className="mt-1 max-w-3xl text-sm text-muted-foreground">{description}</p>
      </div>
      {action}
    </div>
  )
}

export function AdminStatCard({
  label,
  value,
  detail,
  icon,
  tone = "blue",
}: {
  label: string
  value: string | number
  detail: string
  icon: ReactNode
  tone?: "blue" | "violet" | "gold" | "success" | "warning"
}) {
  const tones = {
    blue: "border-[var(--trust-blue)]/15 bg-[var(--trust-blue)]/[0.045] text-[var(--trust-blue)]",
    violet: "border-[var(--negotiation-violet)]/15 bg-[var(--negotiation-violet)]/[0.045] text-[var(--negotiation-violet)]",
    gold: "border-[var(--premium-gold)]/15 bg-[var(--premium-gold)]/[0.045] text-[var(--premium-gold)]",
    success: "border-emerald-400/15 bg-emerald-400/[0.045] text-emerald-300",
    warning: "border-amber-400/15 bg-amber-400/[0.045] text-amber-300",
  }

  return (
    <div className="relative overflow-hidden rounded-2xl border border-white/[0.07] bg-[rgba(10,16,25,.72)] p-4 shadow-[0_12px_32px_rgba(0,0,0,.1)] backdrop-blur-xl">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-[10px] font-semibold uppercase tracking-[0.14em] text-muted-foreground">{label}</p>
          <p className="mt-2 text-2xl font-bold tracking-tight">{value}</p>
          <p className="mt-1 text-xs text-muted-foreground">{detail}</p>
        </div>
        <div className={`flex h-9 w-9 items-center justify-center rounded-xl border ${tones[tone]}`}>
          {icon}
        </div>
      </div>
    </div>
  )
}

export function AdminPanel({
  title,
  description,
  children,
  action,
}: {
  title: string
  description?: string
  children: ReactNode
  action?: ReactNode
}) {
  return (
    <section className="overflow-hidden rounded-2xl border border-white/[0.07] bg-[rgba(9,15,24,.72)] shadow-[0_16px_40px_rgba(0,0,0,.11)] backdrop-blur-xl">
      <div className="flex flex-col gap-2 border-b border-white/[0.06] px-4 py-3.5 sm:flex-row sm:items-center sm:justify-between sm:px-5">
        <div>
          <h2 className="text-sm font-semibold">{title}</h2>
          {description && <p className="mt-0.5 text-xs text-muted-foreground">{description}</p>}
        </div>
        {action}
      </div>
      <div>{children}</div>
    </section>
  )
}

export function AdminStatusBadge({ status }: { status: string | null | undefined }) {
  const value = (status || "UNKNOWN").replaceAll("_", " ")
  const upper = (status || "UNKNOWN").toUpperCase()

  const className =
    upper === "ACTIVE" || upper === "COMPLETED" || upper === "RESOLVED" || upper === "VISIBLE" || upper === "READY"
      ? "border-emerald-400/15 bg-emerald-400/[0.07] text-emerald-300"
      : upper === "SUSPENDED" || upper === "HIDDEN" || upper === "CANCELLED" || upper === "EXPIRED" || upper === "FAILED"
        ? "border-rose-400/15 bg-rose-400/[0.07] text-rose-300"
        : upper === "UNDER_REVIEW" || upper === "NEGOTIATING" || upper === "IN_PROGRESS" || upper === "PENDING"
          ? "border-amber-400/15 bg-amber-400/[0.07] text-amber-300"
          : "border-[var(--trust-blue)]/15 bg-[var(--trust-blue)]/[0.06] text-[var(--trust-blue)]"

  return <Badge variant="outline" className={`border ${className}`}>{value}</Badge>
}

export function EmptyAdminState({ title, description }: { title: string; description: string }) {
  return (
    <div className="flex min-h-48 flex-col items-center justify-center px-6 py-8 text-center">
      <div className="flex h-10 w-10 items-center justify-center rounded-xl border border-white/[0.07] bg-white/[0.02] text-muted-foreground">
        <ShieldAlert className="h-4.5 w-4.5" />
      </div>
      <p className="mt-3 text-sm font-semibold">{title}</p>
      <p className="mt-1 max-w-md text-xs leading-5 text-muted-foreground">{description}</p>
    </div>
  )
}

export function AdminPagination({
  page,
  totalPages,
  searchParams,
}: {
  page: number
  totalPages: number
  searchParams: Record<string, string | undefined>
}) {
  if (totalPages <= 1) return null

  const makeHref = (nextPage: number) => {
    const params = new URLSearchParams()
    for (const [key, value] of Object.entries(searchParams)) {
      if (key !== "page" && value) params.set(key, value)
    }
    params.set("page", String(nextPage))
    return `?${params.toString()}`
  }

  const start = Math.max(0, Math.min(page - 2, totalPages - 5))
  const end = Math.min(totalPages, start + 5)
  const pages = Array.from({ length: end - start }, (_, index) => start + index)

  return (
    <div className="flex items-center justify-between gap-3 border-t border-white/[0.06] px-4 py-3 sm:px-5">
      <p className="text-xs text-muted-foreground">
        Page <span className="font-semibold text-foreground">{page + 1}</span> of {totalPages}
      </p>
      <div className="flex items-center gap-1">
        <Button asChild variant="ghost" size="sm" disabled={page <= 0} className="rounded-lg">
          <Link href={makeHref(Math.max(0, page - 1))}>Prev</Link>
        </Button>
        {pages.map((number) => (
          <Link
            key={number}
            href={makeHref(number)}
            className={`hidden h-8 min-w-8 items-center justify-center rounded-lg border px-2 text-xs font-medium transition sm:inline-flex ${
              number === page
                ? "border-[var(--trust-blue)]/18 bg-[var(--trust-blue)]/[0.08] text-foreground"
                : "border-transparent text-muted-foreground hover:border-white/[0.06] hover:bg-white/[0.025] hover:text-foreground"
            }`}
          >
            {number + 1}
          </Link>
        ))}
        <Button asChild variant="ghost" size="sm" disabled={page >= totalPages - 1} className="rounded-lg">
          <Link href={makeHref(Math.min(totalPages - 1, page + 1))}>Next</Link>
        </Button>
      </div>
    </div>
  )
}

export function ActionLink({ href, children }: { href: string; children: ReactNode }) {
  return (
    <Button asChild variant="ghost" size="sm" className="rounded-lg text-xs">
      <Link href={href}>
        {children}
        <ArrowRight className="ml-1.5 h-3.5 w-3.5" />
      </Link>
    </Button>
  )
}

export function AdminDate({ value, empty = "—" }: { value: string | null | undefined; empty?: string }) {
  if (!value) return <span>{empty}</span>

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return <span>{value}</span>

  return (
    <span title={date.toLocaleString("en-IN")}>
      {new Intl.DateTimeFormat("en-IN", { day: "2-digit", month: "short", year: "numeric" }).format(date)}
    </span>
  )
}

export function AdminIconHint({ type }: { type: "review" | "hidden" | "resolved" | "audit" }) {
  const icon = type === "review" ? <Clock3 className="h-3.5 w-3.5" /> : type === "hidden" ? <EyeOff className="h-3.5 w-3.5" /> : type === "resolved" ? <CheckCircle2 className="h-3.5 w-3.5" /> : <FileClock className="h-3.5 w-3.5" />
  return <span className="text-muted-foreground">{icon}</span>
}