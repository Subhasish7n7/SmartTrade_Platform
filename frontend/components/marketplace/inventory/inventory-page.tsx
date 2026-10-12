"use client"

import { useMemo, useState, type ReactNode } from "react"
import { AnimatePresence, motion } from "framer-motion"
import {
  ArrowLeftRight,
  Boxes,
  CheckCircle2,
  Filter,
  Package,
  Plus,
  Search,
  SlidersHorizontal,
  Tag,
  X,
} from "lucide-react"
import Link from "next/link"

import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import type { ItemResponse } from "@/lib/types/api/item"
import type { MyUserProfile, PublicUserProfile } from "@/lib/types/api/profile"
import { InventoryItemCard } from "./inventory-item-card"

interface Props {
  profile: MyUserProfile | PublicUserProfile
  items: ItemResponse[]
  isOwner: boolean
}

type InventoryFilter = "ALL" | "AVAILABLE" | "TRADE" | "SALE" | "BOTH" | "UNAVAILABLE"
type InventorySort = "NEWEST" | "OLDEST" | "PRICE_LOW" | "PRICE_HIGH"

const sortOptions: { value: InventorySort; label: string }[] = [
  { value: "NEWEST", label: "Newest first" },
  { value: "OLDEST", label: "Oldest first" },
  { value: "PRICE_LOW", label: "Price: low to high" },
  { value: "PRICE_HIGH", label: "Price: high to low" },
]

export function InventoryPage({ profile, items: initialItems, isOwner }: Props) {
  const [items, setItems] = useState<ItemResponse[]>(initialItems)
  const [query, setQuery] = useState("")
  const [filter, setFilter] = useState<InventoryFilter>("ALL")
  const [sort, setSort] = useState<InventorySort>("NEWEST")
  const [showFilters, setShowFilters] = useState(true)

  const counts = useMemo(() => {
    const availableItems = items.filter((item) => item.available)
    return {
      all: items.length,
      available: availableItems.length,
      trade: availableItems.filter((item) => item.forTrade).length,
      sale: availableItems.filter((item) => item.forSale).length,
      both: availableItems.filter((item) => item.forTrade && item.forSale).length,
      unavailable: items.filter((item) => !item.available).length,
    }
  }, [items])

  const visibleItems = useMemo(() => {
    const normalizedQuery = query.trim().toLocaleLowerCase()

    const filteredItems = items.filter((item) => {
      const searchableText = [
        item.itemName,
        item.description,
        item.category,
        item.customCategory ?? "",
        item.condition,
        item.locality ?? "",
        item.city ?? "",
        item.state ?? "",
        ...(item.labels ?? []),
      ]
        .join(" ")
        .toLocaleLowerCase()

      const matchesQuery = !normalizedQuery || searchableText.includes(normalizedQuery)
      const matchesFilter =
        filter === "ALL" ||
        (filter === "AVAILABLE" && item.available) ||
        (filter === "TRADE" && item.available && item.forTrade) ||
        (filter === "SALE" && item.available && item.forSale) ||
        (filter === "BOTH" && item.available && item.forTrade && item.forSale) ||
        (filter === "UNAVAILABLE" && !item.available)

      return matchesQuery && matchesFilter
    })

    return [...filteredItems].sort((a, b) => {
      if (sort === "PRICE_LOW") return a.userPrice - b.userPrice || b.itemId - a.itemId
      if (sort === "PRICE_HIGH") return b.userPrice - a.userPrice || b.itemId - a.itemId

      const aDate = Date.parse(a.createdAt ?? "") || 0
      const bDate = Date.parse(b.createdAt ?? "") || 0
      return sort === "OLDEST" ? aDate - bDate || a.itemId - b.itemId : bDate - aDate || b.itemId - a.itemId
    })
  }, [filter, items, query, sort])

  function updateAvailability(itemId: number, available: boolean) {
    setItems((current) =>
      current.map((item) => (item.itemId === itemId ? { ...item, available } : item)),
    )
  }

  function removeItem(itemId: number) {
    setItems((current) => current.filter((item) => item.itemId !== itemId))
  }

  function clearFilters() {
    setQuery("")
    setFilter("ALL")
    setSort("NEWEST")
  }

  const isFiltered = Boolean(query.trim() || filter !== "ALL" || sort !== "NEWEST")
  const filterOptions: { value: InventoryFilter; label: string; count: number }[] = [
    { value: "ALL", label: "All", count: counts.all },
    { value: "AVAILABLE", label: "Available", count: counts.available },
    { value: "TRADE", label: "Trade", count: counts.trade },
    { value: "SALE", label: "Sale", count: counts.sale },
    { value: "BOTH", label: "Sale + trade", count: counts.both },
    { value: "UNAVAILABLE", label: "Unavailable", count: counts.unavailable },
  ]

  return (
    <main className="mx-auto min-h-screen w-full max-w-[1440px] px-4 pb-16 pt-20 sm:px-6 lg:px-8">
      <div className="space-y-4">
        <header className="content-surface !p-4 sm:!p-5">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div className="min-w-0">
              <div className="mb-2 flex flex-wrap items-center gap-2">
                <Badge variant="outline" className="border-primary/20 bg-primary/[0.06] text-primary">
                  <Boxes className="mr-1.5 h-3.5 w-3.5" />
                  {isOwner ? "Your inventory" : `${profile.name}'s inventory`}
                </Badge>
                <span className="text-xs text-muted-foreground">
                  {counts.all} {counts.all === 1 ? "listing" : "listings"}
                </span>
              </div>
              <h1 className="text-2xl font-semibold tracking-tight">
                {isOwner ? "Manage your inventory" : `${profile.name}'s listings`}
              </h1>
              <p className="mt-1 text-sm text-muted-foreground">
                {isOwner
                  ? "Update availability, edit listings, and keep your items current."
                  : "Browse the listings this trader has chosen to make visible."}
              </p>
            </div>

            {isOwner && (
              <Button asChild className="cta-primary h-10 shrink-0 rounded-xl px-4">
                <Link href="/list-item">
                  <Plus className="mr-2 h-4 w-4" />
                  List an item
                </Link>
              </Button>
            )}
          </div>

          <div className="mt-4 grid grid-cols-2 gap-2 sm:grid-cols-4">
            <InventoryMetric label="Available" value={counts.available} icon={<CheckCircle2 className="h-4 w-4" />} />
            <InventoryMetric label="For trade" value={counts.trade} icon={<ArrowLeftRight className="h-4 w-4" />} />
            <InventoryMetric label="For sale" value={counts.sale} icon={<Tag className="h-4 w-4" />} />
            <InventoryMetric label="Unavailable" value={counts.unavailable} icon={<Package className="h-4 w-4" />} />
          </div>
        </header>

        <section className="content-surface !p-3 sm:!p-4" aria-label="Inventory search and filters">
          <div className="flex flex-col gap-2.5 lg:flex-row lg:items-center">
            <div className="relative min-w-0 flex-1">
              <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                placeholder={isOwner ? "Search name, category, description, or location…" : "Search listings…"}
                aria-label="Search inventory"
                className="h-10 rounded-xl pl-9 pr-9"
              />
              {query && (
                <button
                  type="button"
                  onClick={() => setQuery("")}
                  aria-label="Clear search"
                  className="absolute right-2.5 top-1/2 -translate-y-1/2 rounded-md p-1 text-muted-foreground hover:bg-muted hover:text-foreground"
                >
                  <X className="h-4 w-4" />
                </button>
              )}
            </div>

            <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
              <label className="flex h-10 items-center gap-2 rounded-xl border border-border/60 bg-background/30 px-3 text-xs text-muted-foreground">
                <SlidersHorizontal className="h-3.5 w-3.5" />
                <span className="sr-only">Sort inventory</span>
                <select
                  value={sort}
                  onChange={(event) => setSort(event.target.value as InventorySort)}
                  aria-label="Sort inventory"
                  className="max-w-full bg-transparent text-sm text-foreground outline-none"
                >
                  {sortOptions.map((option) => (
                    <option key={option.value} value={option.value} className="bg-background text-foreground">
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>
              <Button
                type="button"
                variant="surface"
                className="h-10 rounded-xl px-3"
                onClick={() => setShowFilters((open) => !open)}
                aria-expanded={showFilters}
              >
                <Filter className="mr-2 h-4 w-4" />
                Filters
                {filter !== "ALL" && <span className="ml-2 rounded-full bg-primary/15 px-1.5 py-0.5 text-[10px] text-primary">1</span>}
              </Button>
            </div>
          </div>

          <AnimatePresence initial={false}>
            {(showFilters || filter !== "ALL") && (
              <motion.div
                initial={{ height: 0, opacity: 0 }}
                animate={{ height: "auto", opacity: 1 }}
                exit={{ height: 0, opacity: 0 }}
                className="overflow-hidden"
              >
                <div className="mt-3 flex flex-wrap gap-2 border-t border-border/50 pt-3">
                  {filterOptions.map((option) => {
                    const active = filter === option.value
                    return (
                      <button
                        key={option.value}
                        type="button"
                        aria-pressed={active}
                        onClick={() => setFilter(option.value)}
                        className={
                          active
                            ? "inline-flex items-center gap-2 rounded-lg border border-primary/35 bg-primary/10 px-3 py-2 text-xs font-semibold text-primary"
                            : "inline-flex items-center gap-2 rounded-lg border border-border/60 bg-background/20 px-3 py-2 text-xs text-muted-foreground transition hover:border-border hover:text-foreground"
                        }
                      >
                        {option.label}
                        <span className={active ? "tabular-nums" : "tabular-nums opacity-70"}>{option.count}</span>
                      </button>
                    )
                  })}
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        </section>

        <div className="flex items-center justify-between gap-3 px-1">
          <p className="text-sm text-muted-foreground" aria-live="polite">
            Showing <span className="font-semibold text-foreground">{visibleItems.length}</span>{" "}
            {visibleItems.length === 1 ? "listing" : "listings"}
          </p>
          {isFiltered && (
            <button type="button" onClick={clearFilters} className="text-xs font-medium text-primary hover:underline">
              Reset search, filters and sorting
            </button>
          )}
        </div>

        {visibleItems.length > 0 ? (
          <motion.div layout className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4">
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
          <InventoryEmpty isOwner={isOwner} filtered={isFiltered} onClear={clearFilters} />
        )}
      </div>
    </main>
  )
}

function InventoryMetric({ label, value, icon }: { label: string; value: number; icon: ReactNode }) {
  return (
    <div className="flex min-w-0 items-center gap-3 rounded-xl border border-border/55 bg-background/20 px-3 py-3">
      <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-muted/45 text-muted-foreground">{icon}</span>
      <div className="min-w-0">
        <p className="truncate text-[11px] text-muted-foreground">{label}</p>
        <p className="mt-0.5 text-lg font-semibold leading-5 tabular-nums">{value}</p>
      </div>
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
    <section className="content-surface flex min-h-[250px] items-center justify-center !p-6 text-center">
      <div className="max-w-md">
        <span className="mx-auto flex h-12 w-12 items-center justify-center rounded-xl border border-border/60 bg-muted/30 text-muted-foreground">
          <Boxes className="h-5 w-5" />
        </span>
        <h2 className="mt-4 text-lg font-semibold">
          {filtered ? "No listings match" : isOwner ? "Your inventory is empty" : "No listings to show"}
        </h2>
        <p className="mt-1.5 text-sm leading-6 text-muted-foreground">
          {filtered
            ? "Try a different search or reset the current filters."
            : isOwner
              ? "List an item to start selling or trading on SmartTrade."
              : "This trader has no listings currently visible to you."}
        </p>
        {filtered ? (
          <Button variant="surface" className="mt-4 rounded-xl" onClick={onClear}>Reset filters</Button>
        ) : isOwner ? (
          <Button asChild className="cta-primary mt-4 rounded-xl">
            <Link href="/list-item"><Plus className="mr-2 h-4 w-4" />List an item</Link>
          </Button>
        ) : null}
      </div>
    </section>
  )
}
