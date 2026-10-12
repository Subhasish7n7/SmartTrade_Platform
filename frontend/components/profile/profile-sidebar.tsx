"use client"

import {
  Package,
  Shield,
  Settings,
  Star,
  UserRound,
} from "lucide-react"
import type { LucideIcon } from "lucide-react"

import { cn } from "@/lib/utils"

export type ProfileTab = "overview" | "inventory" | "reviews" | "security" | "settings"

type NavItem = {
  title: string
  value: ProfileTab
  icon: LucideIcon
}

const profileItems: NavItem[] = [
  { title: "Overview", value: "overview", icon: UserRound },
  { title: "Inventory", value: "inventory", icon: Package },
  { title: "Reviews", value: "reviews", icon: Star },
]

const accountItems: NavItem[] = [
  { title: "Security", value: "security", icon: Shield },
  { title: "Settings", value: "settings", icon: Settings },
]

interface Props {
  tab: ProfileTab
  isOwner: boolean
  onChange(tab: ProfileTab): void
}

export function ProfileSidebar({ tab, isOwner, onChange }: Props) {
  return (
    <aside
      className={cn(
        "rounded-2xl border border-border/60 bg-card/45 backdrop-blur-xl",
        "lg:fixed lg:top-6 lg:bottom-6 lg:z-30 lg:w-[255px] xl:w-[270px]",
        "lg:left-[max(2rem,calc((100vw-1450px)/2))] lg:overflow-y-auto lg:overscroll-contain",
        "[scrollbar-width:thin] [scrollbar-color:var(--border)_transparent]",
      )}
    >
      <div className="p-2.5 lg:p-3">
        <div className="hidden px-2.5 pb-2 pt-1 text-[10px] font-semibold uppercase tracking-[0.18em] text-muted-foreground/75 lg:block">
          Profile
        </div>

        <div className="flex gap-1 overflow-x-auto pb-0.5 lg:block lg:space-y-0.5 lg:overflow-visible">
          {profileItems.map((item) => (
            <ProfileNavItem key={item.value} item={item} active={tab === item.value} onChange={onChange} />
          ))}
        </div>

        {isOwner && (
          <>
            <div className="my-3 hidden h-px bg-border/60 lg:block" />
            <div className="hidden px-2.5 pb-2 pt-0.5 text-[10px] font-semibold uppercase tracking-[0.18em] text-muted-foreground/75 lg:block">
              Account
            </div>
            <div className="flex gap-1 overflow-x-auto pb-0.5 lg:block lg:space-y-0.5 lg:overflow-visible">
              {accountItems.map((item) => (
                <ProfileNavItem key={item.value} item={item} active={tab === item.value} onChange={onChange} />
              ))}
            </div>
          </>
        )}
      </div>
    </aside>
  )
}

function ProfileNavItem({
  item,
  active,
  onChange,
}: {
  item: NavItem
  active: boolean
  onChange(tab: ProfileTab): void
}) {
  const Icon = item.icon

  return (
    <button
      type="button"
      onClick={() => onChange(item.value)}
      aria-current={active ? "page" : undefined}
      className={cn(
        "group relative flex h-11 shrink-0 items-center gap-3 rounded-xl px-3 text-left text-sm transition-colors",
        "lg:w-full",
        active
          ? "bg-[var(--trust-blue)]/[0.075] text-foreground"
          : "text-muted-foreground hover:bg-white/[0.035] hover:text-foreground",
      )}
    >
      {active && <span className="absolute inset-y-2 left-0.5 w-0.5 rounded-full bg-[var(--trust-blue)]" />}

      <span
        className={cn(
          "flex h-8 w-8 shrink-0 items-center justify-center rounded-lg border transition-colors",
          active
            ? "border-[var(--trust-blue)]/20 bg-[var(--trust-blue)]/10 text-[var(--trust-blue)]"
            : "border-border/50 bg-white/[0.02] text-muted-foreground group-hover:border-border/80 group-hover:text-foreground",
        )}
      >
        <Icon className="h-4 w-4" />
      </span>

      <span className="font-medium">{item.title}</span>
    </button>
  )
}
