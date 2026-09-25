"use client"

import { useMemo, useState } from "react"
import { AnimatePresence, motion } from "framer-motion"
import {
  ArrowLeftRight,
  Boxes,
  CheckCircle2,
  Filter,
  Package,
  Plus,
  Search,
  Tag,
  X,
} from "lucide-react"
import Link from "next/link"

import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { ItemResponse } from "@/lib/types/api/item"
import { MyUserProfile, PublicUserProfile } from "@/lib/types/api/profile"
import { InventoryItemCard } from "./inventory-item-card"

interface Props {
  profile: MyUserProfile | PublicUserProfile
  items: ItemResponse[]
  isOwner: boolean
}

type Filter = "ALL" | "AVAILABLE" | "TRADE" | "SALE" | "UNAVAILABLE"

export function InventoryPage({ profile, items: initialItems, isOwner }: Props) {
  const [items, setItems] = useState(() =>
    isOwner ? initialItems : initialItems.filter((item) => item.available),
  )
  const [query, setQuery] = useState("")
  const [filter, setFilter] = useState<Filter>("ALL")
  const [showFilters, setShowFilters] = useState(false)

  const counts = useMemo(
    () => ({
      all: items.length,
      available: items.filter((item) => item.available).length,
      trade: items.filter((item) => item.forTrade && item.available).length,
      sale: items.filter((item) => item.forSale && item.available).length,
      unavailable: items.filter((item) => !item.available).length,
    }),
    [items],
  )

  const visibleItems = useMemo(() => {
    const normalized = query.trim().toLowerCase()

    return items.filter((item) => {
      const searchable = [
        item.itemName,
        item.category,
        item.customCategory ?? "",
        ...(item.labels ?? []),
      ]
        .join(" ")
        .toLowerCase()

      const matchesQuery = !normalized || searchable.includes(normalized)

      const matchesFilter =
        filter === "ALL" ||
        (filter === "AVAILABLE" && item.available) ||
        (filter === "TRADE" && item.forTrade && item.available) ||
        (filter === "SALE" && item.forSale && item.available) ||
        (filter === "UNAVAILABLE" && !item.available)

      return matchesQuery && matchesFilter
    })
  }, [filter, items, query])

  function updateAvailability(itemId: number, available: boolean) {
    setItems((current) =>
      current.map((currentItem) =>
        currentItem.itemId === itemId ? { ...currentItem, available } : currentItem,
      ),
    )
  }

  function removeItem(itemId: number) {
    setItems((current) => current.filter((item) => item.itemId !== itemId))
  }

  function clearFilters() {
    setQuery("")
    setFilter("ALL")
  }

  const isFiltered = Boolean(query || filter !== "ALL")

  return (
    <main className="relative min-h-screen overflow-hidden px-4 pb-24 pt-20 sm:px-6 lg:px-8">
      <div className="pointer-events-none absolute inset-x-0 top-0 h-[560px] bg-[radial-gradient(circle_at_12%_4%,rgba(59,130,246,.13),transparent_31%),radial-gradient(circle_at_88%_10%,rgba(168,85,247,.11),transparent_29%),radial-gradient(circle_at_50%_0%,rgba(16,185,129,.06),transparent_36%)]" />
      <div className="pointer-events-none absolute left-1/2 top-20 h-px w-[70%] -translate-x-1/2 bg-gradient-to-r from-transparent via-primary/20 to-transparent" />

      <div className="relative mx-auto max-w-[1500px]">
        <motion.section
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          className="trade-card overflow-hidden rounded-[30px] border-white/[0.08]"
        >
          <div className="trade-card-accent" />

          <div className="relative overflow-hidden p-5 sm:p-7">
            <div className="pointer-events-none absolute -right-20 -top-24 h-80 w-80 rounded-full bg-[var(--trust-blue)]/10 blur-3xl" />
            <div className="pointer-events-none absolute bottom-[-150px] left-[28%] h-80 w-80 rounded-full bg-[var(--negotiation-violet)]/9 blur-3xl" />
            <div className="pointer-events-none absolute right-[28%] top-1/2 h-40 w-40 -translate-y-1/2 rounded-full bg-[var(--premium-gold)]/5 blur-3xl" />

            <div className="relative flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
              <div className="min-w-0">
                <div className="flex flex-wrap items-center gap-2">
                  <Badge
                    variant="outline"
                    className="border-primary/20 bg-primary/[0.07] text-primary shadow-[0_0_24px_rgba(45,212,191,.06)]"
                  >
                    <Boxes className="mr-1.5 h-3.5 w-3.5" />
                    {isOwner ? "Your marketplace inventory" : `${profile.name}'s inventory`}
                  </Badge>

                  {counts.trade > 0 && (
                    <Badge
                      variant="outline"
                      className="border-[var(--negotiation-violet)]/25 bg-[var(--negotiation-violet)]/[0.07] text-[var(--negotiation-violet)]"
                    >
                      <ArrowLeftRight className="mr-1.5 h-3.5 w-3.5" />
                      {counts.trade} trade-ready
                    </Badge>
                  )}

                  {counts.sale > 0 && (
                    <Badge
                      variant="outline"
                      className="border-[var(--premium-gold)]/20 bg-[var(--premium-gold)]/[0.06] text-[var(--premium-gold)]"
                    >
                      <Tag className="mr-1.5 h-3.5 w-3.5" />
                      {counts.sale} for sale
                    </Badge>
                  )}
                </div>

                <div className="mt-4 max-w-3xl">
                  <h1 className="text-3xl font-bold tracking-[-0.025em] sm:text-4xl">
                    {isOwner ? "Manage your inventory" : `${profile.name}'s inventory`}
                  </h1>
                  <p className="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground sm:text-base">
                    {isOwner
                      ? "Keep listings polished, control availability, and make the right items easy to trade."
                      : "Explore this trader's available listings and find something worth making an offer on."}
                  </p>
                </div>
              </div>

              {isOwner && (
                <Button
                  asChild
                  className="cta-primary h-11 rounded-xl px-5 shadow-[0_12px_32px_rgba(45,212,191,.10)]"
                >
                  <Link href="/list-item">
                    <Plus className="mr-2 h-4 w-4" />
                    List an item
                  </Link>
                </Button>
              )}
            </div>

            <div className="relative mt-7 grid grid-cols-2 overflow-hidden rounded-2xl border border-white/[0.07] bg-black/10 sm:grid-cols-4">
              <InventoryMetric
                label="Total"
                value={counts.all}
                icon={<Package className="h-4 w-4" />}
              />
              <InventoryMetric
                label="Available"
                value={counts.available}
                icon={<CheckCircle2 className="h-4 w-4" />}
                tone="success"
              />
              <InventoryMetric
                label="For Trade"
                value={counts.trade}
                icon={<ArrowLeftRight className="h-4 w-4" />}
                tone="violet"
              />
              <InventoryMetric
                label="For Sale"
                value={counts.sale}
                icon={<Tag className="h-4 w-4" />}
                tone="gold"
              />
            </div>
          </div>
        </motion.section>

        <section className="mt-5 rounded-[26px] border border-white/[0.07] bg-card/35 p-3 shadow-[0_18px_50px_rgba(0,0,0,.08)] backdrop-blur-xl sm:p-4">
          <div className="flex flex-col gap-3 lg:flex-row lg:items-center">
            <div className="relative flex-1">
              <Search className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />

              <Input
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                placeholder={isOwner ? "Search your inventory…" : "Search listings…"}
                className="h-11 rounded-xl border-white/[0.07] bg-background/30 pl-10 pr-10 shadow-inner"
              />

              {query && (
                <button
                  type="button"
                  onClick={() => setQuery("")}
                  className="absolute right-3 top-1/2 -translate-y-1/2 rounded-full p-1 text-muted-foreground transition hover:bg-white/[0.05] hover:text-foreground"
                  aria-label="Clear search"
                >
                  <X className="h-4 w-4" />
                </button>
              )}
            </div>

            <Button
              type="button"
              variant={showFilters ? "default" : "surface"}
              className="h-11 rounded-xl px-4"
              onClick={() => setShowFilters((value) => !value)}
            >
              <Filter className="mr-2 h-4 w-4" />
              Filters
              {filter !== "ALL" && (
                <span className="ml-2 inline-flex h-5 min-w-5 items-center justify-center rounded-full bg-background/20 px-1.5 text-[10px] font-bold">
                  1
                </span>
              )}
            </Button>
          </div>

          <AnimatePresence initial={false}>
            {(showFilters || filter !== "ALL") && (
              <motion.div
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: "auto" }}
                exit={{ opacity: 0, height: 0 }}
                className="overflow-hidden"
              >
                <div className="mt-3 flex flex-wrap gap-2 border-t border-white/[0.06] pt-3">
                  {(["ALL", "AVAILABLE", "TRADE", "SALE", "UNAVAILABLE"] as Filter[]).map(
                    (value) => (
                      <button
                        key={value}
                        type="button"
                        onClick={() => setFilter(value)}
                        className={`trade-filter-btn !w-auto !rounded-xl !px-3 !py-2 ${
                          filter === value ? "trade-filter-active" : ""
                        }`}
                      >
                        <span>
                          {value === "ALL"
                            ? "All"
                            : value === "AVAILABLE"
                              ? "Available"
                              : value === "TRADE"
                                ? "Trade"
                                : value === "SALE"
                                  ? "Sale"
                                  : "Unavailable"}
                        </span>
                        <span>
                          {value === "ALL"
                            ? counts.all
                            : value === "AVAILABLE"
                              ? counts.available
                              : value === "TRADE"
                                ? counts.trade
                                : value === "SALE"
                                  ? counts.sale
                                  : counts.unavailable}
                        </span>
                      </button>
                    ),
                  )}
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        </section>

        <div className="mt-5 flex items-center justify-between gap-4 px-1">
          <p className="text-sm text-muted-foreground">
            Showing{" "}
            <span className="font-semibold text-foreground">{visibleItems.length}</span>{" "}
            {visibleItems.length === 1 ? "listing" : "listings"}
          </p>

          {isFiltered ? (
            <button
              type="button"
              onClick={clearFilters}
              className="text-xs font-semibold text-primary transition hover:text-primary/80 hover:underline"
            >
              Clear filters
            </button>
          ) : null}
        </div>

        {visibleItems.length > 0 ? (
          <motion.div layout className="mt-4 grid gap-4 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4">
            {visibleItems.map((item) => (
              <InventoryItemCard
                key={item.itemId}
                item={item}
                isOwner={isOwner}
                onChanged={(available) => updateAvailability(item.itemId, available)}
                onRemoved={removeItem}
              />
            ))}
          </motion.div>
        ) : (
          <InventoryEmpty
            isOwner={isOwner}
            filtered={isFiltered}
            onClear={clearFilters}
          />
        )}
      </div>
    </main>
  )
}

function InventoryMetric({
  label,
  value,
  icon,
  tone = "primary",
}: {
  label: string
  value: number
  icon: React.ReactNode
  tone?: "primary" | "success" | "violet" | "gold"
}) {
  const colors = {
    primary: "text-primary",
    success: "text-emerald-400",
    violet: "text-[var(--negotiation-violet)]",
    gold: "text-[var(--premium-gold)]",
  }

  return (
    <div className="border-b border-white/[0.055] px-4 py-4 last:border-0 sm:border-b-0 sm:border-r">
      <div className="flex items-center gap-2 text-xs text-muted-foreground">
        <span className={colors[tone]}>{icon}</span>
        {label}
      </div>
      <div className="mt-1.5 text-xl font-bold tracking-tight">{value}</div>
    </div>
  )
}

function InventoryEmpty({
  isOwner,
  filtered,
  onClear,
}: {
  isOwner: boolean
  filtered: boolean
  onClear: () => void
}) {
  return (
    <section className="content-surface mt-5 flex min-h-[420px] items-center justify-center overflow-hidden p-0">
      <div className="relative max-w-md px-6 py-16 text-center">
        <div className="pointer-events-none absolute left-1/2 top-1/2 h-52 w-52 -translate-x-1/2 -translate-y-1/2 rounded-full bg-[var(--chat-cyan)]/7 blur-3xl" />
        <div className="pointer-events-none absolute left-1/2 top-[38%] h-20 w-20 -translate-x-1/2 rounded-full border border-primary/10 bg-primary/[0.02] blur-sm" />

        <div className="relative mx-auto flex h-16 w-16 items-center justify-center rounded-2xl border border-white/[0.08] bg-black/20 shadow-[0_0_34px_rgba(103,232,249,.08)]">
          <Boxes className="h-7 w-7 text-[var(--chat-cyan)]" />
        </div>

        <h2 className="relative mt-5 text-xl font-semibold tracking-tight">
          {filtered
            ? "No listings match"
            : isOwner
              ? "Your inventory is empty"
              : "No listings yet"}
        </h2>

        <p className="relative mt-2 text-sm leading-6 text-muted-foreground">
          {filtered
            ? "Try a different search or clear the current filters."
            : isOwner
              ? "List your first item to start selling or negotiating trades."
              : "This trader does not currently have any available listings to show."}
        </p>

        {filtered ? (
          <Button variant="surface" className="relative mt-5 rounded-xl" onClick={onClear}>
            Clear filters
          </Button>
        ) : isOwner ? (
          <Button asChild className="relative mt-5 rounded-xl cta-primary">
            <Link href="/list-item">
              <Plus className="mr-2 h-4 w-4" />
              List your first item
            </Link>
          </Button>
        ) : null}
      </div>
    </section>
  )
}