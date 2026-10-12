"use client"

import Image from "next/image"
import Link from "next/link"
import * as React from "react"
import { motion } from "framer-motion"
import {
  ArrowUpRight,
  Eye,
  MapPin,
  PackageOpen,
  ShieldCheck,
  Sparkles,
} from "lucide-react"

import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { ItemResponse } from "@/lib/types/api/item"
import { cn } from "@/lib/utils"

interface Props {
  items: ItemResponse[]
  isOwner: boolean
}

type Filter = "all" | "sale" | "trade"

export function ProfileInventoryTab({ items, isOwner }: Props) {
  const [filter, setFilter] = React.useState<Filter>("all")

  const filteredItems = React.useMemo(() => {
    switch (filter) {
      case "sale":
        return items.filter((item) => item.forSale)
      case "trade":
        return items.filter((item) => item.forTrade)
      default:
        return items
    }
  }, [items, filter])

  const availableCount = items.filter((item) => item.available).length

  return (
    <section className="space-y-4">
      <div className="rounded-2xl border border-border/60 bg-card/50 p-4 backdrop-blur-xl sm:p-5">
        <div className="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
          <div>
            <div className="flex items-center gap-2 text-[11px] font-semibold uppercase tracking-[0.16em] text-muted-foreground">
              <PackageOpen className="h-3.5 w-3.5 text-[var(--trust-blue)]" />
              Marketplace inventory
            </div>
            <p className="mt-2 text-sm text-muted-foreground">
              {availableCount} active {availableCount === 1 ? "listing" : "listings"} · {items.length} total shown
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-2">
            <FilterButton active={filter === "all"} onClick={() => setFilter("all")} label="All" count={items.length} />
            <FilterButton
              active={filter === "sale"}
              onClick={() => setFilter("sale")}
              label="For Sale"
              count={items.filter((item) => item.forSale).length}
            />
            <FilterButton
              active={filter === "trade"}
              onClick={() => setFilter("trade")}
              label="For Trade"
              count={items.filter((item) => item.forTrade).length}
            />
          </div>
        </div>

        {isOwner && (
          <div className="mt-4 border-t border-border/45 pt-4">
            <Button asChild className="cta-primary rounded-xl">
              <Link href="/list-item">
                List an item
                <ArrowUpRight className="ml-2 h-4 w-4" />
              </Link>
            </Button>
          </div>
        )}
      </div>

      {filteredItems.length === 0 ? (
        <div className="rounded-2xl border border-border/60 bg-card/50 px-6 py-16 text-center backdrop-blur-xl">
          <PackageOpen className="mx-auto h-8 w-8 text-muted-foreground" />
          <h2 className="mt-4 text-lg font-semibold">Nothing matches this filter</h2>
          <p className="mx-auto mt-1 max-w-md text-sm leading-6 text-muted-foreground">
            Try another marketplace filter to see the listings available on this profile.
          </p>
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {filteredItems.map((item, index) => (
            <InventoryCard key={item.itemId} item={item} isOwner={isOwner} index={index} />
          ))}
        </div>
      )}
    </section>
  )
}

function FilterButton({
  active,
  onClick,
  label,
  count,
}: {
  active: boolean
  onClick(): void
  label: string
  count: number
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      aria-pressed={active}
      className={cn(
        "inline-flex h-9 items-center gap-2 rounded-xl border px-3 text-sm font-medium transition-colors",
        active
          ? "border-[var(--trust-blue)]/30 bg-[var(--trust-blue)]/8 text-foreground"
          : "border-border/60 bg-white/[0.02] text-muted-foreground hover:bg-white/[0.04] hover:text-foreground",
      )}
    >
      {label}
      <span className="rounded-full bg-white/[0.05] px-1.5 py-0.5 text-[10px] text-muted-foreground">{count}</span>
    </button>
  )
}

function InventoryCard({
  item,
  isOwner,
  index,
}: {
  item: ItemResponse
  isOwner: boolean
  index: number
}) {
  const image = item.images?.[0]?.secureUrl ?? "/placeholder.jpg"
  const listedPrice = item.userPrice ?? 0
  const generatedPrice = item.generatedPrice ?? listedPrice
  const hasEstimate = generatedPrice > 0
  const dealDiff = hasEstimate && generatedPrice > 0 ? ((listedPrice - generatedPrice) / generatedPrice) * 100 : 0
  const location = [item.locality, item.city].filter(Boolean).join(", ")
  const listingLabel = item.forTrade && item.forSale ? "Trade / Sale" : item.forTrade ? "Trade Only" : "For Sale"

  return (
    <motion.article
      initial={{ opacity: 0, y: 6 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: index * 0.025, duration: 0.18 }}
      className="group overflow-hidden rounded-2xl border border-border/55 bg-card/45 backdrop-blur-xl transition hover:-translate-y-0.5 hover:border-[var(--trust-blue)]/25"
    >
      <div className="relative aspect-[4/3] overflow-hidden bg-muted/30">
        <Image src={image} alt={item.itemName} fill className="object-cover transition-transform duration-500 group-hover:scale-[1.035]" />
        <div className="absolute inset-0 bg-gradient-to-t from-background/85 via-background/5 to-transparent" />

        <div className="absolute left-3 top-3 flex flex-wrap gap-1.5">
          <Badge className="border-none bg-background/78 text-foreground shadow-sm backdrop-blur-md">{listingLabel}</Badge>
          {!item.available && <Badge className="border-none bg-destructive/85 text-destructive-foreground">Unavailable</Badge>}
        </div>

        <div className="absolute bottom-3 left-3 flex items-center gap-1.5 rounded-full border border-white/10 bg-background/72 px-2.5 py-1 backdrop-blur-md">
          <ShieldCheck className={cn("h-3.5 w-3.5", trustColor(item.seller?.trustScore ?? 0))} />
          <span className="text-xs font-medium">{Math.round(item.seller?.trustScore ?? 0)}%</span>
        </div>
      </div>

      <div className="p-4">
        <h3 className="line-clamp-1 text-[15px] font-semibold transition-colors group-hover:text-primary">{item.itemName}</h3>
        <div className="mt-1 flex items-center gap-1.5 text-xs text-muted-foreground">
          <Sparkles className="h-3 w-3 text-primary" />
          {formatEnum(item.condition)} <span>•</span> {formatEnum(item.category)}
        </div>

        <div className="mt-4 flex items-end justify-between gap-3">
          <div>
            <p className="text-[10px] uppercase tracking-wider text-muted-foreground">Listed value</p>
            <p className="mt-0.5 text-xl font-bold">₹{listedPrice.toLocaleString("en-IN")}</p>
          </div>

          {hasEstimate && (
            <div className="text-right">
              <p className="flex items-center justify-end gap-1 text-[10px] uppercase tracking-wider text-muted-foreground">
                <Sparkles className="h-3 w-3" />
                Estimate
              </p>
              <div className="mt-0.5 flex items-center gap-1.5">
                <span className="text-sm text-muted-foreground">₹{generatedPrice.toLocaleString("en-IN")}</span>
                {Math.abs(dealDiff) >= 5 && (
                  <span className={cn("text-[10px] font-semibold", dealDiff < 0 ? "text-emerald-400" : "text-amber-400")}>
                    {dealDiff > 0 ? "+" : ""}{Math.round(dealDiff)}%
                  </span>
                )}
              </div>
            </div>
          )}
        </div>

        {location && (
          <div className="mt-3 flex items-center gap-1.5 text-xs text-muted-foreground">
            <MapPin className="h-3.5 w-3.5 shrink-0 text-[var(--chat-cyan)]" />
            <span className="truncate">{location}</span>
          </div>
        )}

        <div className="mt-4 grid grid-cols-2 gap-2">
          <Button asChild variant="surface" className="rounded-xl">
            <Link href={`/item/${item.itemId}`}>
              <Eye className="mr-1.5 h-4 w-4" />
              View
            </Link>
          </Button>

          {isOwner ? (
            <Button asChild variant="secondary" className="rounded-xl">
              <Link href={`/item/${item.itemId}`}>
                Manage
                <ArrowUpRight className="ml-1.5 h-4 w-4" />
              </Link>
            </Button>
          ) : item.forTrade && item.available ? (
            <Button asChild className="cta-primary rounded-xl">
              <Link href={`/trades/create/${item.itemId}`}>
                Trade
                <ArrowUpRight className="ml-1.5 h-4 w-4" />
              </Link>
            </Button>
          ) : (
            <Button disabled variant="secondary" className="rounded-xl">
              Unavailable
            </Button>
          )}
        </div>
      </div>
    </motion.article>
  )
}

function formatEnum(value: string) {
  return value
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ")
}

function trustColor(score: number) {
  if (score >= 90) return "text-emerald-400"
  if (score >= 70) return "text-amber-400"
  return "text-muted-foreground"
}
