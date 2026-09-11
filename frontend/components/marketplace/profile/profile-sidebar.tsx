"use client"

import {
  ArrowLeftRight,
  History,
  Package,
  RefreshCw,
  Settings,
  Shield,
  Star,
  User,
} from "lucide-react"

import { cn } from "@/lib/utils"

export type ProfileTab =
  | "overview"
  | "inventory"
  | "trade-inventory"
  | "trades"
  | "reviews"
  | "activity"
  | "security"
  | "settings"

const profileItems = [
  { title: "Overview", value: "overview", icon: User },
  { title: "Inventory", value: "inventory", icon: Package },
  { title: "Trade Inventory", value: "trade-inventory", icon: RefreshCw },
  { title: "Trades", value: "trades", icon: ArrowLeftRight },
  { title: "Reviews", value: "reviews", icon: Star },
  { title: "Activity", value: "activity", icon: History },
] as const

const accountItems = [
  { title: "Security", value: "security", icon: Shield },
  { title: "Settings", value: "settings", icon: Settings },
] as const

interface Props {
  tab: ProfileTab
  onChange(tab: ProfileTab): void
}

export function ProfileSidebar({ tab, onChange }: Props) {
  return (
    <aside className="content-surface h-fit p-2.5 lg:sticky lg:top-24">
      <ProfileNavGroup label="Profile" items={profileItems} tab={tab} onChange={onChange} />

      <div className="my-3 h-px bg-gradient-to-r from-transparent via-border/70 to-transparent" />

      <ProfileNavGroup label="Account" items={accountItems} tab={tab} onChange={onChange} />
    </aside>
  )
}

function ProfileNavGroup({
  label,
  items,
  tab,
  onChange,
}: {
  label: string
  items: ReadonlyArray<{ title: string; value: ProfileTab; icon: typeof User }>
  tab: ProfileTab
  onChange(tab: ProfileTab): void
}) {
  return (
    <div>
      <p className="px-3 py-2 text-[10px] font-semibold uppercase tracking-[0.18em] text-muted-foreground/80">
        {label}
      </p>

      <div className="space-y-1">
        {items.map((item) => {
          const Icon = item.icon
          const active = tab === item.value

          return (
            <button
              key={item.value}
              type="button"
              onClick={() => onChange(item.value)}
              className={cn(
                "group relative flex w-full items-center gap-3 overflow-hidden rounded-xl px-3.5 py-3 text-left text-sm transition-all duration-200",
                active
                  ? "border border-[var(--trust-blue)]/25 bg-gradient-to-r from-[var(--trust-blue)]/12 via-[var(--negotiation-violet)]/8 to-transparent text-foreground shadow-[0_0_24px_rgba(59,130,246,.08)]"
                  : "border border-transparent text-muted-foreground hover:border-border/50 hover:bg-white/[0.035] hover:text-foreground",
              )}
            >
              {active && (
                <span className="absolute inset-y-2 left-0 w-0.5 rounded-full bg-gradient-to-b from-[var(--trust-blue)] via-[var(--negotiation-violet)] to-[var(--premium-gold)]" />
              )}

              <span
                className={cn(
                  "flex h-8 w-8 shrink-0 items-center justify-center rounded-lg border transition-all",
                  active
                    ? "border-[var(--trust-blue)]/20 bg-[var(--trust-blue)]/10 text-[var(--trust-blue)]"
                    : "border-border/40 bg-white/[0.025] text-muted-foreground group-hover:border-border/70 group-hover:text-foreground",
                )}
              >
                <Icon className="h-4 w-4" />
              </span>

              <span className="font-medium">{item.title}</span>
            </button>
          )
        })}
      </div>
    </div>
  )
}
