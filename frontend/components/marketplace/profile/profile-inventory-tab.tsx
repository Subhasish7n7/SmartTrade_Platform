"use client"

import Image from "next/image"
import Link from "next/link"
import { motion } from "framer-motion"
import {
  ArrowUpRight,
  CheckCircle2,
  Eye,
  MapPin,
  PackageOpen,
  RefreshCw,
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
  title?: string
  description?: string
  emptyTitle?: string
  emptyDescription?: string
}

export function ProfileInventoryTab({
  items,
  isOwner,
  title = "Inventory",
  description = "Items listed by this trader on TradeVault.",
  emptyTitle = "No listings yet",
  emptyDescription,
}: Props) {
  const resolvedEmptyDescription =
    emptyDescription ??
    (isOwner
      ? "Your marketplace inventory is empty. List your first item to get started."
      : "This trader has not published any marketplace listings yet.")

  return (
    <section className="space-y-4">
      <div className="content-surface overflow-hidden p-0">
        <div className="trade-card-accent" />
        <div className="flex flex-col gap-4 p-5 sm:flex-row sm:items-end sm:justify-between sm:p-6">
          <div>
            <div className="flex items-center gap-2">
              <PackageOpen className="h-4 w-4 text-[var(--trust-blue)]" />
              <span className="text-xs font-semibold uppercase tracking-[0.16em] text-muted-foreground">Marketplace inventory</span>
            </div>
            <h2 className="mt-2 text-2xl font-bold tracking-tight">{title}</h2>
            <p className="mt-1 text-sm text-muted-foreground">{description}</p>
          </div>

          <div className="flex items-center gap-2 rounded-xl border border-border/50 bg-white/[0.025] px-3 py-2 text-sm">
            <span className="font-semibold">{items.length}</span>
            <span className="text-muted-foreground">{items.length === 1 ? "listing" : "listings"}</span>
          </div>
        </div>
      </div>

      {items.length === 0 ? (
        <EmptyInventory
          isOwner={isOwner}
          title={emptyTitle}
          description={resolvedEmptyDescription}
        />
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {items.map((item, index) => (
            <InventoryCard key={item.itemId} item={item} isOwner={isOwner} index={index} />
          ))}
        </div>
      )}
    </section>
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
  const dealDiff = hasEstimate && generatedPrice > 0
    ? ((listedPrice - generatedPrice) / generatedPrice) * 100
    : 0

  const location = [item.locality, item.city].filter(Boolean).join(", ")
  const listingLabel = item.forTrade && item.forSale ? "Trade / Sale" : item.forTrade ? "Trade Only" : "For Sale"

  return (
    <motion.article
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: index * 0.035, duration: 0.22 }}
      whileHover={{ y: -4 }}
      className="group relative overflow-hidden rounded-2xl border border-border/55 bg-card/55 shadow-[0_0_26px_rgba(59,130,246,.045)] backdrop-blur-xl transition-shadow duration-300 hover:border-[var(--trust-blue)]/25 hover:shadow-[0_0_34px_rgba(59,130,246,.10)]"
    >
      <div className="relative aspect-[4/3] overflow-hidden bg-muted/30">
        <Image
          src={image}
          alt={item.itemName}
          fill
          className="object-cover transition-transform duration-500 group-hover:scale-105"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-background/85 via-background/5 to-transparent" />

        <div className="absolute left-3 top-3 flex flex-wrap gap-1.5">
          <Badge className="border-none bg-background/75 text-foreground shadow-sm backdrop-blur-md">
            {listingLabel}
          </Badge>
          {!item.available && (
            <Badge className="border-none bg-destructive/85 text-destructive-foreground">
              Unavailable
            </Badge>
          )}
        </div>

        <div className="absolute bottom-3 left-3 flex items-center gap-1.5 rounded-full border border-white/10 bg-background/70 px-2.5 py-1 backdrop-blur-md">
          <ShieldCheck className={cn("h-3.5 w-3.5", trustColor(item.seller?.trustScore ?? 0))} />
          <span className="text-xs font-medium">{Math.round(item.seller?.trustScore ?? 0)}%</span>
        </div>
      </div>

      <div className="p-4">
        <div className="flex items-start justify-between gap-3">
          <div className="min-w-0">
            <h3 className="line-clamp-1 text-[15px] font-semibold transition-colors group-hover:text-primary">
              {item.itemName}
            </h3>
            <div className="mt-1 flex items-center gap-1.5 text-xs text-muted-foreground">
              <Sparkles className="h-3 w-3 text-primary" />
              {formatEnum(item.condition)}
              <span>•</span>
              {formatEnum(item.category)}
            </div>
          </div>
        </div>

        <div className="mt-4 flex items-end justify-between gap-3">
          <div>
            <p className="text-[10px] uppercase tracking-wider text-muted-foreground">Listed value</p>
            <p className="mt-0.5 text-xl font-bold">₹{listedPrice.toLocaleString()}</p>
          </div>

          {hasEstimate && (
            <div className="text-right">
              <p className="flex items-center justify-end gap-1 text-[10px] uppercase tracking-wider text-muted-foreground">
                <Sparkles className="h-3 w-3" />
                Estimate
              </p>
              <div className="mt-0.5 flex items-center gap-1.5">
                <span className="text-sm text-muted-foreground">₹{generatedPrice.toLocaleString()}</span>
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
            <Button asChild className="rounded-xl" variant="secondary">
              <Link href={`/item/${item.itemId}`}>
                <ArrowUpRight className="mr-1.5 h-4 w-4" />
                View listing
              </Link>
            </Button>
          ) : item.forTrade && item.available ? (
            <Button asChild className="cta-primary rounded-xl">
              <Link href={`/trades/create/${item.itemId}`}>
                <ArrowUpRight className="mr-1.5 h-4 w-4" />
                Trade
              </Link>
            </Button>
          ) : (
            <Button disabled variant="secondary" className="rounded-xl">
              <CheckCircle2 className="mr-1.5 h-4 w-4" />
              Unavailable
            </Button>
          )}
        </div>
      </div>
    </motion.article>
  )
}

function EmptyInventory({
  isOwner,
  title,
  description,
}: {
  isOwner: boolean
  title: string
  description: string
}) {
  return (
    <div className="content-surface overflow-hidden p-0">
      <div className="trade-card-accent" />
      <div className="flex min-h-[330px] items-center justify-center px-6 py-14 text-center">
        <div className="max-w-md">
          <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl border border-border/60 bg-white/[0.025] shadow-[0_0_30px_rgba(59,130,246,.08)]">
            <PackageOpen className="h-6 w-6 text-[var(--trust-blue)]" />
          </div>
          <h3 className="mt-5 text-xl font-semibold">{title}</h3>
          <p className="mt-2 text-sm leading-6 text-muted-foreground">{description}</p>
          {isOwner && (
            <Button asChild className="cta-primary mt-5 rounded-xl">
              <Link href="/list-item">
                List an item
                <ArrowUpRight className="ml-2 h-4 w-4" />
              </Link>
            </Button>
          )}
        </div>
      </div>
    </div>
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
  return "text-red-400"
}
