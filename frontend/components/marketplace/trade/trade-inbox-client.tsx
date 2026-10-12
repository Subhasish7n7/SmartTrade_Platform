"use client"

import { AnimatePresence, motion } from "framer-motion"
import {
  AlertTriangle,
  CheckCircle2,
  Clock3,
  Filter,
  Handshake,
  Inbox,
  Search,
  ShoppingBag,
  SlidersHorizontal,
  XCircle,
} from "lucide-react"
import Link from "next/link"
import * as React from "react"

import { AnimatedPage } from "@/components/marketplace/animations/section"
import { TradeInboxCard } from "@/components/marketplace/trade/trade-inbox-card"
import type { TradeInboxResponse, TradeStatus } from "@/lib/types/api/trade"

interface Props {
  trades: TradeInboxResponse[]
  currentUserId: number
}

type TradeFilter = "ALL" | TradeStatus
type SortOrder = "RECENT" | "OLDEST"

const filterOrder: Array<{
  value: TradeFilter
  label: string
  icon: React.ComponentType<{ className?: string }>
}> = [
  { value: "ALL", label: "All", icon: Inbox },
  { value: "OPEN", label: "Open", icon: Clock3 },
  { value: "NEGOTIATING", label: "Negotiating", icon: Handshake },
  { value: "ACCEPTED", label: "Accepted", icon: CheckCircle2 },
  { value: "DISPUTED", label: "Disputed", icon: AlertTriangle },
  { value: "COMPLETED", label: "Completed", icon: CheckCircle2 },
  { value: "CANCELLED", label: "Cancelled", icon: XCircle },
  { value: "EXPIRED", label: "Expired", icon: Clock3 },
]

export function TradeInboxClient({ trades, currentUserId }: Props) {
  const [filter, setFilter] = React.useState<TradeFilter>("ALL")
  const [query, setQuery] = React.useState("")
  const [sortOrder, setSortOrder] = React.useState<SortOrder>("RECENT")

  const counts = React.useMemo(() => {
    const map = new Map<TradeStatus, number>()

    for (const trade of trades) {
      map.set(trade.status, (map.get(trade.status) ?? 0) + 1)
    }

    return map
  }, [trades])

  const filteredTrades = React.useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase()

    return trades
      .filter((trade) => filter === "ALL" || trade.status === filter)
      .filter((trade) => {
        if (!normalizedQuery) return true
        return trade.otherUserName.toLowerCase().includes(normalizedQuery)
      })
      .slice()
      .sort((a, b) => {
        const aTime = new Date(a.updatedAt).getTime()
        const bTime = new Date(b.updatedAt).getTime()
        return sortOrder === "RECENT" ? bTime - aTime : aTime - bTime
      })
  }, [filter, query, sortOrder, trades])

  const hasFilters = filter !== "ALL" || query.trim().length > 0 || sortOrder !== "RECENT"

  function clearFilters() {
    setFilter("ALL")
    setQuery("")
    setSortOrder("RECENT")
  }

  return (
    <main className="relative z-10 min-h-screen overflow-hidden pb-24 pt-20 lg:pt-20">
      <div
        aria-hidden="true"
        className="pointer-events-none absolute inset-0 -z-10"
        style={{
          background:
            "radial-gradient(circle at 8% 12%, rgba(59,130,246,.075), transparent 28%), radial-gradient(circle at 92% 16%, rgba(168,85,247,.06), transparent 28%), radial-gradient(circle at 50% 82%, rgba(16,185,129,.025), transparent 30%)",
        }}
      />

      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <AnimatedPage>
          <div className="space-y-5">
            <section className="trade-inbox-hero relative overflow-hidden rounded-[28px] border border-[var(--trust-blue)]/18 px-5 py-5 shadow-[0_16px_44px_rgba(4,12,24,.2)] sm:px-6 sm:py-5">
              <div className="pointer-events-none absolute inset-y-0 right-0 w-[42%] bg-[radial-gradient(circle_at_70%_28%,rgba(168,85,247,.10),transparent_58%)]" />
              <div className="relative flex items-center justify-between gap-5">
                <div className="min-w-0 max-w-3xl">
                  <div className="flex items-center gap-2 text-[11px] font-semibold uppercase tracking-[0.18em] text-muted-foreground">
                    <span className="inline-flex h-7 w-7 items-center justify-center rounded-lg border border-[var(--trust-blue)]/25 bg-[var(--trust-blue)]/[0.07] text-[var(--trust-blue)] shadow-[0_0_18px_rgba(59,130,246,.06)]">
                      <Handshake className="h-3.5 w-3.5" />
                    </span>
                    Trade center
                  </div>

                  <h1 className="mt-2.5 bg-gradient-to-r from-[var(--trust-blue)] via-[var(--negotiation-violet)] to-[var(--premium-gold)] bg-clip-text text-2xl font-bold tracking-tight text-transparent sm:text-3xl">
                    Trade inbox
                  </h1>
                  <p className="mt-1 max-w-2xl text-sm leading-6 text-muted-foreground">
                    Open and manage your marketplace trades, negotiations, and trade history from one place.
                  </p>
                </div>

                <div className="hidden shrink-0 items-center gap-2 sm:flex">
                  <span className="rounded-xl border border-white/[0.09] bg-black/10 px-3 py-2 text-xs text-muted-foreground">
                    <span className="font-semibold text-foreground">{trades.length}</span> {trades.length === 1 ? "trade" : "trades"}
                  </span>
                  <Link
                    href="/marketplace"
                    className="inline-flex h-10 items-center justify-center gap-2 rounded-xl border border-[var(--trust-blue)]/24 bg-[var(--trust-blue)]/[0.07] px-3.5 text-sm font-semibold text-foreground transition hover:border-[var(--trust-blue)]/45 hover:bg-[var(--trust-blue)]/[0.12]"
                  >
                    <ShoppingBag className="h-4 w-4 text-[var(--trust-blue)]" />
                    Browse marketplace
                  </Link>
                </div>
              </div>
            </section>

            <section className="rounded-[24px] border border-white/[0.09] bg-[rgba(9,15,24,.72)] p-4 shadow-[0_12px_34px_rgba(0,0,0,.12)] sm:p-5">
              <div className="flex flex-col gap-3 xl:flex-row xl:items-center xl:justify-between">
                <div className="flex min-w-0 items-center gap-2.5">
                  <span className="inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-lg border border-white/[0.10] bg-white/[0.025] text-muted-foreground">
                    <Filter className="h-3.5 w-3.5" />
                  </span>
                  <div className="min-w-0">
                    <p className="text-sm font-semibold">Filter trades</p>
                    <p className="text-xs text-muted-foreground">Choose a state, search a trader, or change the order.</p>
                  </div>
                </div>

                <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
                  <label className="relative block sm:w-64">
                    <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                    <input
                      value={query}
                      onChange={(event) => setQuery(event.target.value)}
                      placeholder="Search traders"
                      aria-label="Search trades by trader name"
                      className="h-10 w-full rounded-xl border border-white/[0.12] bg-background/55 pl-9 pr-3 text-sm outline-none transition placeholder:text-muted-foreground/65 hover:border-white/[0.18] focus:border-[var(--trust-blue)]/55 focus:bg-background/75 focus:ring-1 focus:ring-[var(--trust-blue)]/20"
                    />
                  </label>

                  <div className="relative">
                    <SlidersHorizontal className="pointer-events-none absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-muted-foreground" />
                    <select
                      value={sortOrder}
                      onChange={(event) => setSortOrder(event.target.value as SortOrder)}
                      aria-label="Sort trades"
                      className="h-10 w-full appearance-none rounded-xl border border-white/[0.12] bg-background/55 pl-9 pr-9 text-sm font-medium text-foreground outline-none transition hover:border-white/[0.18] focus:border-[var(--trust-blue)]/55 focus:ring-1 focus:ring-[var(--trust-blue)]/20 sm:w-40"
                    >
                      <option value="RECENT">Newest first</option>
                      <option value="OLDEST">Oldest first</option>
                    </select>
                  </div>

                  {hasFilters && (
                    <button
                      type="button"
                      onClick={clearFilters}
                      className="h-10 rounded-xl border border-white/[0.11] bg-white/[0.02] px-3 text-sm font-medium text-muted-foreground transition hover:border-white/[0.18] hover:bg-white/[0.05] hover:text-foreground"
                    >
                      Clear
                    </button>
                  )}
                </div>
              </div>

              <div className="mt-4 flex gap-2 overflow-x-auto pb-1 [scrollbar-width:thin]">
                {filterOrder.map((item) => {
                  const Icon = item.icon
                  const active = filter === item.value
                  const count = item.value === "ALL" ? trades.length : (counts.get(item.value) ?? 0)

                  return (
                    <button
                      key={item.value}
                      type="button"
                      onClick={() => setFilter(item.value)}
                      aria-pressed={active}
                      className={[
                        "inline-flex h-9 shrink-0 items-center gap-2 rounded-xl border px-3 text-xs font-semibold transition",
                        active
                          ? filterActiveClass(item.value)
                          : "border-white/[0.10] bg-background/50 text-muted-foreground hover:border-white/[0.18] hover:bg-white/[0.035] hover:text-foreground",
                      ].join(" ")}
                    >
                      <Icon className={`h-3.5 w-3.5 ${active ? filterIconClass(item.value) : ""}`} />
                      {item.label}
                      <span className="rounded-full border border-white/[0.07] bg-black/20 px-1.5 py-0.5 text-[10px] tabular-nums">
                        {count}
                      </span>
                    </button>
                  )
                })}
              </div>
            </section>

            <section>
              <div className="mb-4 flex items-end justify-between gap-4 px-1">
                <div>
                  <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-muted-foreground">Trade activity</p>
                  <h2 className="mt-1.5 text-xl font-semibold tracking-tight">Your trades</h2>
                </div>

                <span className="text-xs font-medium text-muted-foreground">
                  {filteredTrades.length} {filteredTrades.length === 1 ? "result" : "results"}
                </span>
              </div>

              {filteredTrades.length === 0 ? (
                <EmptyTrades filter={filter} query={query} onClear={clearFilters} />
              ) : (
                <div className="space-y-4">
                  <AnimatePresence initial={false} mode="popLayout">
                    {filteredTrades.map((trade, index) => (
                      <motion.div
                        key={trade.tradeId}
                        layout
                        initial={{ opacity: 0, y: 8 }}
                        animate={{ opacity: 1, y: 0 }}
                        exit={{ opacity: 0, y: -8 }}
                        transition={{ duration: 0.18, delay: Math.min(index, 6) * 0.02 }}
                      >
                        <TradeInboxCard trade={trade} currentUserId={currentUserId} />
                      </motion.div>
                    ))}
                  </AnimatePresence>
                </div>
              )}
            </section>
          </div>
        </AnimatedPage>
      </div>
    </main>
  )
}

function filterActiveClass(value: TradeFilter) {
  switch (value) {
    case "OPEN":
      return "border-[var(--trust-blue)]/32 bg-[var(--trust-blue)]/[0.11] text-foreground shadow-[0_0_22px_rgba(59,130,246,.08)]"
    case "NEGOTIATING":
      return "border-[var(--negotiation-violet)]/34 bg-[var(--negotiation-violet)]/[0.11] text-foreground shadow-[0_0_22px_rgba(168,85,247,.08)]"
    case "ACCEPTED":
    case "COMPLETED":
      return "border-emerald-400/28 bg-emerald-400/[0.09] text-foreground shadow-[0_0_22px_rgba(16,185,129,.07)]"
    case "CANCELLED":
      return "border-red-400/25 bg-red-400/[0.08] text-foreground"
    case "EXPIRED":
      return "border-[var(--premium-gold)]/28 bg-[var(--premium-gold)]/[0.09] text-foreground"
    default:
      return "border-[var(--chat-cyan)]/30 bg-[var(--chat-cyan)]/[0.09] text-foreground shadow-[0_0_22px_rgba(34,211,238,.07)]"
  }
}

function filterIconClass(value: TradeFilter) {
  switch (value) {
    case "OPEN":
      return "text-[var(--trust-blue)]"
    case "NEGOTIATING":
      return "text-[var(--negotiation-violet)]"
    case "ACCEPTED":
    case "COMPLETED":
      return "text-emerald-400"
    case "CANCELLED":
      return "text-red-300"
    case "DISPUTED":
      return "border-amber-300/25 bg-amber-300/[0.08] text-foreground shadow-[0_0_22px_rgba(251,191,36,.06)]"
    case "EXPIRED":
      return "text-[var(--premium-gold)]"
    default:
      return "text-[var(--chat-cyan)]"
  }
}

function EmptyTrades({ filter, query, onClear }: { filter: TradeFilter; query: string; onClear(): void }) {
  const hasSearch = query.trim().length > 0
  const label = filter === "ALL" ? "trades" : `${filter.toLowerCase()} trades`

  return (
    <div className="content-surface rounded-[24px] p-8 text-center sm:p-12">
      <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-2xl border border-border/60 bg-white/[0.025] text-muted-foreground">
        <Inbox className="h-5 w-5" />
      </div>
      <h3 className="mt-4 text-lg font-semibold">{hasSearch ? "No matching trades" : `No ${label} yet`}</h3>
      <p className="mx-auto mt-2 max-w-md text-sm leading-6 text-muted-foreground">
        {hasSearch
          ? "Try a different trader name or clear the current filters."
          : filter === "ALL"
            ? "Trades and negotiations you enter will appear here."
            : "There are no trades in this state right now."}
      </p>

      {(hasSearch || filter !== "ALL") && (
        <button
          type="button"
          onClick={onClear}
          className="mt-5 inline-flex h-10 items-center justify-center rounded-xl border border-border/60 bg-white/[0.02] px-4 text-sm font-semibold transition hover:bg-white/[0.05]"
        >
          Clear filters
        </button>
      )}
    </div>
  )
}
