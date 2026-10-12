"use client"

import Image from "next/image"
import { motion } from "framer-motion"
import { ShieldCheck, Sparkles, TrendingDown, TrendingUp } from "lucide-react"

import { Badge } from "@/components/ui/badge"
import { WishlistButton } from "@/components/marketplace/wishlist-button"
import type { ItemResponse } from "@/lib/types/api/item"
import { cn } from "@/lib/utils"

interface Props {
  item: ItemResponse
  /** Kept for parent compatibility; the shared wishlist cache owns live state. */
  isWishlisted?: boolean
  /** Kept for compatibility with older parent state; do not duplicate the API mutation here. */
  onWishlistToggle?: (id: number) => void
  className?: string
}

const formatINR = (value: number | null | undefined) =>
  value == null
    ? "Price unavailable"
    : new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency: "INR",
        maximumFractionDigits: 0,
      }).format(value)

function trustLabel(score: number): string {
  if (score >= 800) return "Highly trusted"
  if (score >= 650) return "Trusted trader"
  if (score >= 500) return "Established trader"
  return "Building trust"
}

export function MarketplaceItemCard({
  item,
  isWishlisted = false,
  className,
}: Props) {
  const seller = item.seller
  const trustScore = Math.round(Math.max(0, Math.min(1000, seller?.trustScore ?? 0)))
  const sellerName = seller?.name ?? "Unknown seller"
  const usedEstimate = item.generatedPrice

  const priceDiff =
    usedEstimate != null && usedEstimate > 0
      ? ((item.userPrice - usedEstimate) / usedEstimate) * 100
      : null

  const isGoodDeal = priceDiff != null && priceDiff < -5
  const isBadDeal = priceDiff != null && priceDiff > 10

  return (
    <motion.div
      whileHover={{ y: -4 }}
      whileTap={{ scale: 0.985 }}
      className={cn(
        "group relative cursor-pointer overflow-hidden rounded-xl marketplace-card glass",
        "transition-all duration-300 hover:border-primary/30 hover:glow-sm",
        className,
      )}
    >
      <div className="relative aspect-square overflow-hidden bg-muted/30">
        <Image
          src={item.images?.[0]?.secureUrl ?? "/placeholder.jpg"}
          alt={item.itemName}
          fill
          sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 25vw"
          className="object-cover transition-transform duration-500 group-hover:scale-105"
        />

        <div className="pointer-events-none absolute inset-0 bg-gradient-to-t from-background/75 via-transparent to-transparent opacity-80 transition-opacity duration-300 group-hover:opacity-100" />

        <div className="absolute left-3 top-3 z-10 flex flex-col items-start gap-2">
          {item.forTrade && item.forSale ? (
            <Badge className="border-none bg-gradient-to-r from-primary to-info text-primary-foreground">
              Trade / Sale
            </Badge>
          ) : item.forTrade ? (
            <Badge className="border-none bg-primary/90 text-primary-foreground">Trade only</Badge>
          ) : item.forSale ? (
            <Badge className="border-none bg-info/90 text-info-foreground">For sale</Badge>
          ) : (
            <Badge variant="secondary">Unavailable</Badge>
          )}
        </div>

        <WishlistButton
          itemId={item.itemId}
          initialSaved={isWishlisted}
          overlay
          className="h-10 w-10"
        />

        <div
          title={`Trust score: ${trustScore.toLocaleString("en-IN")} out of 1,000. This aggregate considers trade reliability, reviews, marketplace conduct, listing quality, and account age.`}
          className="absolute bottom-3 left-3 z-10 flex max-w-[calc(100%-1.5rem)] items-center gap-1.5 rounded-full border border-white/10 bg-background/70 px-2.5 py-1.5 text-foreground shadow-sm backdrop-blur-md"
        >
          <ShieldCheck
            aria-hidden="true"
            className={cn(
              "h-3.5 w-3.5 shrink-0",
              trustScore >= 800
                ? "text-emerald-400"
                : trustScore >= 500
                  ? "text-amber-400"
                  : "text-muted-foreground",
            )}
          />
          <span className="truncate text-[11px] font-semibold">{trustLabel(trustScore)}</span>
          <span className="shrink-0 text-[10px] tabular-nums text-muted-foreground">
            {trustScore.toLocaleString("en-IN")}/1,000
          </span>
        </div>
      </div>

      <div className="space-y-3.5 p-4">
        <div className="min-w-0">
          <h3 className="line-clamp-1 font-semibold text-foreground transition-colors group-hover:text-primary">
            {item.itemName}
          </h3>
          <div className="mt-2 flex items-center gap-2 text-muted-foreground">
            <Sparkles className="h-3.5 w-3.5 shrink-0" />
            <span className="text-xs font-medium">{item.condition.replace(/_/g, " ")}</span>
            <span aria-hidden="true" className="h-1 w-1 rounded-full bg-border" />
            <span className="truncate text-xs">{sellerName}</span>
          </div>
        </div>

        <div className="space-y-2 border-t border-border/50 pt-3">
          <div className="flex items-baseline justify-between gap-3">
            <span className="text-xs text-muted-foreground">Listed price</span>
            <span className="text-base font-bold tracking-tight text-foreground">{formatINR(item.userPrice)}</span>
          </div>

          <div className="flex items-center justify-between gap-3">
            <span className="flex items-center gap-1 text-xs text-muted-foreground">
              <Sparkles className="h-3 w-3 text-primary" /> Used estimate
            </span>
            {usedEstimate == null ? (
              <span className="text-xs text-muted-foreground">Not available</span>
            ) : (
              <div className="flex min-w-0 items-center gap-1.5">
                <span className="truncate text-sm font-medium text-muted-foreground">{formatINR(usedEstimate)}</span>
                {priceDiff != null && Math.abs(priceDiff) >= 5 && (
                  <span
                    className={cn(
                      "inline-flex shrink-0 items-center gap-0.5 text-[10px] font-semibold",
                      isGoodDeal ? "text-emerald-400" : isBadDeal ? "text-rose-400" : "text-muted-foreground",
                    )}
                    title="Difference between the listed price and the estimated used value"
                  >
                    {isGoodDeal ? (
                      <TrendingDown className="h-3 w-3" />
                    ) : isBadDeal ? (
                      <TrendingUp className="h-3 w-3" />
                    ) : null}
                    {Math.abs(priceDiff).toFixed(0)}%
                  </span>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </motion.div>
  )
}

export function MarketplaceItemCardSkeleton({ className }: { className?: string }) {
  return (
    <div className={cn("overflow-hidden rounded-xl marketplace-card glass", className)}>
      <div className="aspect-square animate-shimmer bg-muted/35" />
      <div className="space-y-3 p-4">
        <div className="h-5 w-3/4 animate-shimmer rounded bg-muted" />
        <div className="h-4 w-1/2 animate-shimmer rounded bg-muted" />
        <div className="space-y-2 border-t border-border/50 pt-3">
          <div className="h-6 w-full animate-shimmer rounded bg-muted" />
          <div className="h-4 w-2/3 animate-shimmer rounded bg-muted" />
        </div>
      </div>
    </div>
  )
}
