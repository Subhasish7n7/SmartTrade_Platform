"use client"

import Link from "next/link"
import { usePathname } from "next/navigation"
import {
  ArrowLeftRight,
  ClipboardList,
  FileClock,
  LayoutDashboard,
  Package,
  Scale,
  ShieldCheck,
  ShoppingBag,
  Users,
} from "lucide-react"
import * as React from "react"

import { cn } from "@/lib/utils"

const links = [
  { href: "/admin", label: "Overview", icon: LayoutDashboard, exact: true },
  { href: "/admin/users", label: "Users", icon: Users },
  { href: "/admin/items", label: "Items", icon: Package },
  { href: "/admin/trades", label: "Trades", icon: ArrowLeftRight },
  { href: "/admin/disputes", label: "Disputes", icon: Scale },
  { href: "/admin/audit-logs", label: "Audit Logs", icon: FileClock },
]

export function AdminSidebar() {
  const pathname = usePathname()
  const [open, setOpen] = React.useState(false)

  const active = (href: string, exact = false) =>
    exact ? pathname === href : pathname.startsWith(href)

  return (
    <>
      <button
        type="button"
        aria-label="Open admin navigation"
        onClick={() => setOpen(true)}
        className="fixed left-3 top-3 z-[70] inline-flex h-10 w-10 items-center justify-center rounded-xl border border-white/[0.09] bg-[rgba(9,15,24,.86)] text-muted-foreground shadow-lg backdrop-blur-xl lg:hidden"
      >
        <ClipboardList className="h-5 w-5" />
      </button>

      {open && (
        <button
          type="button"
          aria-label="Close admin navigation"
          onClick={() => setOpen(false)}
          className="fixed inset-0 z-[60] bg-black/50 backdrop-blur-[2px] lg:hidden"
        />
      )}

      <aside
        className={cn(
          "fixed inset-y-0 left-0 z-[65] flex w-[258px] flex-col border-r border-white/[0.07] bg-[rgba(7,12,20,.94)] px-3 py-4 shadow-2xl backdrop-blur-2xl transition-transform duration-200 lg:translate-x-0",
          open ? "translate-x-0" : "-translate-x-full",
        )}
      >
        <div className="px-2 pb-5">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-[var(--trust-blue)] via-[var(--negotiation-violet)] to-[var(--premium-gold)] text-background shadow-[0_0_28px_rgba(59,130,246,.12)]">
              <ShieldCheck className="h-5 w-5" />
            </div>
            <div>
              <p className="text-sm font-bold tracking-tight">SmartTrade</p>
              <p className="text-[10px] font-semibold uppercase tracking-[0.18em] text-muted-foreground">Admin Console</p>
            </div>
          </div>
        </div>

        <div className="mb-2 px-2 text-[10px] font-semibold uppercase tracking-[0.18em] text-muted-foreground/70">
          Operations
        </div>

        <nav className="space-y-1">
          {links.map((item) => {
            const Icon = item.icon
            const isCurrent = active(item.href, item.exact)

            return (
              <Link
                key={item.href}
                href={item.href}
                onClick={() => setOpen(false)}
                className={cn(
                  "group flex items-center gap-3 rounded-xl border px-3 py-2.5 text-sm font-medium transition-all",
                  isCurrent
                    ? "border-[var(--trust-blue)]/18 bg-[var(--trust-blue)]/[0.085] text-foreground shadow-[inset_0_1px_rgba(255,255,255,.03)]"
                    : "border-transparent text-muted-foreground hover:border-white/[0.06] hover:bg-white/[0.025] hover:text-foreground",
                )}
              >
                <span
                  className={cn(
                    "flex h-8 w-8 items-center justify-center rounded-lg border transition-colors",
                    isCurrent
                      ? "border-[var(--trust-blue)]/20 bg-[var(--trust-blue)]/[0.08] text-[var(--trust-blue)]"
                      : "border-white/[0.05] bg-white/[0.018] text-muted-foreground group-hover:text-foreground",
                  )}
                >
                  <Icon className="h-4 w-4" />
                </span>
                {item.label}
              </Link>
            )
          })}
        </nav>

        <div className="mt-auto space-y-1 border-t border-white/[0.06] pt-3">
          <Link
            href="/marketplace"
            className="flex items-center gap-3 rounded-xl border border-transparent px-3 py-2.5 text-sm font-medium text-muted-foreground transition hover:border-white/[0.06] hover:bg-white/[0.025] hover:text-foreground"
          >
            <span className="flex h-8 w-8 items-center justify-center rounded-lg border border-white/[0.05] bg-white/[0.018]">
              <ShoppingBag className="h-4 w-4" />
            </span>
            Marketplace
          </Link>
        </div>
      </aside>
    </>
  )
}