"use client"

import Image from "next/image"
import Link from "next/link"
import { useCallback, useEffect, useMemo, useState } from "react"
import { AlertCircle, ArrowUpRight, Heart, Loader2, Package, Search, SlidersHorizontal, Trash2 } from "lucide-react"

import { AnimatedPage } from "@/components/marketplace/animations/section"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { getItemById } from "@/lib/api/items/client"
import { getWishlist } from "@/lib/api/wishlist/client"
import { primeWishlistIds, setWishlistSaved } from "@/lib/api/wishlist/store"
import type { WishlistItemResponse } from "@/lib/types/api/wishlist"
import type { ItemResponse } from "@/lib/types/api/item"

interface WishlistEntryWithItem {
  saved: WishlistItemResponse
  item: ItemResponse | null
}

type WishlistFilter = "ALL" | "SALE" | "TRADE" | "BOTH" | "UNAVAILABLE"
type WishlistSort = "DATE_NEWEST" | "DATE_OLDEST" | "PRICE_LOW_TO_HIGH" | "PRICE_HIGH_TO_LOW"

const filterOptions: Array<{ value: WishlistFilter; label: string }> = [
  { value: "ALL", label: "All saved" },
  { value: "SALE", label: "For sale" },
  { value: "TRADE", label: "For trade" },
  { value: "BOTH", label: "Sale & trade" },
  { value: "UNAVAILABLE", label: "Unavailable" },
]

function money(value: number) {
  return new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 0 }).format(value)
}

function safeDate(value: string) {
  const timestamp = new Date(value).getTime()
  return Number.isFinite(timestamp) ? timestamp : 0
}

function errorMessage(error: unknown, fallback: string) {
  return error instanceof Error && error.message.trim() ? error.message : fallback
}

export default function WishlistPage() {
  const [entries, setEntries] = useState<WishlistEntryWithItem[]>([])
  const [loading, setLoading] = useState(true)
  const [removingId, setRemovingId] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [detailWarning, setDetailWarning] = useState(false)
  const [query, setQuery] = useState("")
  const [filter, setFilter] = useState<WishlistFilter>("ALL")
  const [sort, setSort] = useState<WishlistSort>("DATE_NEWEST")

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    setDetailWarning(false)
    try {
      const savedItems = await getWishlist()
      primeWishlistIds(savedItems.map((entry) => entry.itemId))

      const detailed = await Promise.all(
        savedItems.map(async (saved): Promise<WishlistEntryWithItem> => {
          try {
            return { saved, item: await getItemById(saved.itemId) }
          } catch {
            // Keep the saved row visible even when the listing was removed or is no longer public.
            setDetailWarning(true)
            return { saved, item: null }
          }
        }),
      )
      setEntries(detailed)
    } catch (loadError) {
      setError(errorMessage(loadError, "Your wishlist could not be loaded. Please try again."))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void load()
  }, [load])

  const counts = useMemo(() => {
    return {
      ALL: entries.length,
      SALE: entries.filter(({ item }) => Boolean(item?.available && item.forSale && !item.forTrade)).length,
      TRADE: entries.filter(({ item }) => Boolean(item?.available && item.forTrade && !item.forSale)).length,
      BOTH: entries.filter(({ item }) => Boolean(item?.available && item.forSale && item.forTrade)).length,
      UNAVAILABLE: entries.filter(({ item }) => !item || !item.available).length,
    }
  }, [entries])

  const visibleEntries = useMemo(() => {
    const needle = query.trim().toLocaleLowerCase()
    return entries
      .filter(({ item }) => {
        if (filter === "SALE") return Boolean(item?.available && item.forSale && !item.forTrade)
        if (filter === "TRADE") return Boolean(item?.available && item.forTrade && !item.forSale)
        if (filter === "BOTH") return Boolean(item?.available && item.forSale && item.forTrade)
        if (filter === "UNAVAILABLE") return !item || !item.available
        return true
      })
      .filter(({ item, saved }) => {
        if (!needle) return true
        const searchable = [item?.itemName, item?.category, item?.customCategory, item?.city, item?.locality, ...(item?.labels ?? []), String(saved.itemId)]
          .filter(Boolean)
          .join(" ")
          .toLocaleLowerCase()
        return searchable.includes(needle)
      })
      .slice()
      .sort((a, b) => {
        if (sort === "DATE_NEWEST") return safeDate(b.saved.dateAdded) - safeDate(a.saved.dateAdded)
        if (sort === "DATE_OLDEST") return safeDate(a.saved.dateAdded) - safeDate(b.saved.dateAdded)
        const aPrice = a.item?.userPrice
        const bPrice = b.item?.userPrice
        if (aPrice == null && bPrice == null) return 0
        if (aPrice == null) return 1
        if (bPrice == null) return -1
        return sort === "PRICE_LOW_TO_HIGH" ? aPrice - bPrice : bPrice - aPrice
      })
  }, [entries, filter, query, sort])

  async function removeItem(itemId: number) {
    setRemovingId(itemId)
    setError(null)
    try {
      await setWishlistSaved(itemId, false)
      setEntries((current) => current.filter(({ saved }) => saved.itemId !== itemId))
    } catch (removeError) {
      setError(errorMessage(removeError, "This item could not be removed from your wishlist."))
    } finally {
      setRemovingId(null)
    }
  }

  function clearFilters() {
    setQuery("")
    setFilter("ALL")
    setSort("DATE_NEWEST")
  }

  const hasFilters = Boolean(query.trim()) || filter !== "ALL" || sort !== "DATE_NEWEST"

  return (
    <main className="min-h-screen px-4 pb-24 pt-20 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-7xl">
        <AnimatedPage>
          <div className="space-y-5">
            <header className="content-surface !p-5 sm:!p-6">
              <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div className="flex items-start gap-3">
                  <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl border border-rose-400/20 bg-rose-400/[0.07] text-rose-300">
                    <Heart className="h-5 w-5" />
                  </span>
                  <div>
                    <p className="text-[11px] font-semibold uppercase tracking-[0.16em] text-muted-foreground">Your saved items</p>
                    <h1 className="mt-1 text-2xl font-bold tracking-tight sm:text-3xl">Wishlist</h1>
                    <p className="mt-1 text-sm text-muted-foreground">Keep track of items you may want to buy or trade.</p>
                  </div>
                </div>
                <div className="flex items-center gap-2 self-start rounded-xl border border-border/60 bg-background/30 px-3 py-2 text-sm text-muted-foreground sm:self-auto">
                  <Package className="h-4 w-4" />
                  <span><strong className="text-foreground">{entries.length}</strong> saved {entries.length === 1 ? "item" : "items"}</span>
                </div>
              </div>
            </header>

            <section className="content-surface !p-4 sm:!p-5">
              <div className="grid gap-3 md:grid-cols-[minmax(0,1fr)_220px_auto] md:items-center">
                <label className="relative block">
                  <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                  <Input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search saved items" aria-label="Search saved items" className="h-10 rounded-xl pl-9" />
                </label>
                <label className="relative block">
                  <SlidersHorizontal className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                  <select value={sort} onChange={(event) => setSort(event.target.value as WishlistSort)} aria-label="Sort wishlist" className="h-10 w-full appearance-none rounded-xl border border-input bg-background pl-9 pr-3 text-sm outline-none focus:ring-2 focus:ring-ring">
                    <option value="DATE_NEWEST">Recently saved</option>
                    <option value="DATE_OLDEST">Oldest saved</option>
                    <option value="PRICE_LOW_TO_HIGH">Price: low to high</option>
                    <option value="PRICE_HIGH_TO_LOW">Price: high to low</option>
                  </select>
                </label>
                {hasFilters && <Button type="button" variant="surface" className="h-10 rounded-xl" onClick={clearFilters}>Reset</Button>}
              </div>
              <div className="mt-4 flex gap-2 overflow-x-auto pb-1">
                {filterOptions.map((option) => (
                  <button key={option.value} type="button" onClick={() => setFilter(option.value)} aria-pressed={filter === option.value} className={`inline-flex h-9 shrink-0 items-center gap-2 rounded-xl border px-3 text-xs font-semibold transition ${filter === option.value ? "border-primary/35 bg-primary/10 text-foreground" : "border-border/60 bg-background/30 text-muted-foreground hover:text-foreground"}`}>
                    {option.label}<span className="rounded-full bg-background/60 px-1.5 py-0.5 text-[10px] tabular-nums">{counts[option.value]}</span>
                  </button>
                ))}
              </div>
            </section>

            {error && <div role="alert" className="flex items-start gap-2 rounded-xl border border-destructive/30 bg-destructive/5 p-3 text-sm text-destructive"><AlertCircle className="mt-0.5 h-4 w-4 shrink-0" />{error}<button type="button" className="ml-auto underline" onClick={() => void load()}>Retry</button></div>}
            {detailWarning && !error && <p role="status" className="rounded-xl border border-amber-400/20 bg-amber-400/[0.05] px-3 py-2 text-sm text-muted-foreground">Some saved listings are no longer available to view. You can still remove them from your wishlist.</p>}

            {loading ? (
              <div className="content-surface flex min-h-56 items-center justify-center gap-2 text-sm text-muted-foreground"><Loader2 className="h-4 w-4 animate-spin" />Loading your wishlist…</div>
            ) : visibleEntries.length > 0 ? (
              <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
                {visibleEntries.map(({ saved, item }) => (
                  <article key={saved.id} className="group overflow-hidden rounded-2xl border border-border/60 bg-card/50 transition-colors hover:border-primary/25">
                    {item ? (
                      <Link href={`/item/${item.itemId}`} className="block">
                        <div className="relative aspect-[4/3] bg-muted/30">
                          <Image src={item.images?.[0]?.secureUrl ?? "/placeholder.jpg"} alt={item.itemName} fill sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 25vw" className="object-cover transition-transform duration-300 group-hover:scale-[1.025]" />
                          <div className="absolute left-3 top-3 flex flex-wrap gap-1.5">
                            {!item.available && <span className="rounded-full border border-amber-300/25 bg-background/90 px-2 py-1 text-[10px] font-semibold text-amber-300">Unavailable</span>}
                            {item.forSale && item.forTrade && <span className="rounded-full bg-background/90 px-2 py-1 text-[10px] font-semibold">Sale + trade</span>}
                          </div>
                        </div>
                        <div className="p-4">
                          <p className="line-clamp-2 min-h-10 text-sm font-semibold leading-5">{item.itemName}</p>
                          <p className="mt-1 text-xs text-muted-foreground">{item.category.replaceAll("_", " ")}{item.condition ? ` · ${item.condition.replaceAll("_", " ")}` : ""}</p>
                          <div className="mt-3 flex items-end justify-between gap-2">
                            <div>
                              <p className="text-base font-bold">{money(item.userPrice)}</p>
                              <p className="mt-0.5 text-[11px] text-muted-foreground">Seller's price</p>
                            </div>
                            <ArrowUpRight className="h-4 w-4 text-muted-foreground transition-colors group-hover:text-foreground" />
                          </div>
                        </div>
                      </Link>
                    ) : (
                      <div className="flex min-h-[260px] flex-col p-4">
                        <span className="flex h-10 w-10 items-center justify-center rounded-xl border border-border/60 bg-background/30 text-muted-foreground"><Package className="h-5 w-5" /></span>
                        <h2 className="mt-4 font-semibold">Listing unavailable</h2>
                        <p className="mt-1 text-sm leading-5 text-muted-foreground">This item may have been removed or hidden by the seller's visibility settings.</p>
                        <p className="mt-auto pt-4 text-xs text-muted-foreground">Item #{saved.itemId}</p>
                      </div>
                    )}
                    <div className="flex items-center justify-between gap-2 border-t border-border/50 px-3 py-2.5">
                      <span className="text-[11px] text-muted-foreground">Saved {new Intl.DateTimeFormat("en-IN", { dateStyle: "medium" }).format(new Date(saved.dateAdded))}</span>
                      <Button type="button" variant="ghost" size="sm" disabled={removingId === saved.itemId} onClick={() => void removeItem(saved.itemId)} className="h-8 rounded-lg px-2.5 text-muted-foreground hover:text-destructive">
                        {removingId === saved.itemId ? <Loader2 className="mr-1.5 h-3.5 w-3.5 animate-spin" /> : <Trash2 className="mr-1.5 h-3.5 w-3.5" />}
                        Remove
                      </Button>
                    </div>
                  </article>
                ))}
              </section>
            ) : (
              <div className="content-surface flex min-h-64 flex-col items-center justify-center px-6 py-12 text-center">
                <span className="flex h-12 w-12 items-center justify-center rounded-2xl border border-border/60 bg-background/30 text-muted-foreground"><Heart className="h-5 w-5" /></span>
                <h2 className="mt-4 text-lg font-semibold">{error && entries.length === 0 ? "Couldn’t load your wishlist" : hasFilters ? "No matching saved items" : "Your wishlist is empty"}</h2>
                <p className="mt-1 max-w-md text-sm leading-6 text-muted-foreground">{error && entries.length === 0 ? "Check your connection or sign-in status, then try again." : hasFilters ? "Try another search or reset your filters." : "Save interesting listings from the marketplace and they will appear here."}</p>
                {error && entries.length === 0 ? <Button type="button" variant="surface" className="mt-4 rounded-xl" onClick={() => void load()}>Try again</Button> : hasFilters ? <Button type="button" variant="surface" className="mt-4 rounded-xl" onClick={clearFilters}>Reset filters</Button> : <Button asChild className="cta-primary mt-4 rounded-xl"><Link href="/marketplace">Browse marketplace</Link></Button>}
              </div>
            )}
          </div>
        </AnimatedPage>
      </div>
    </main>
  )
}
