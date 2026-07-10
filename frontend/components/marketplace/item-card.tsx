"use client"

import Image from "next/image"
import { motion } from "framer-motion"
import { Heart, ShieldCheck, Sparkles, TrendingUp, TrendingDown } from "lucide-react"
import { cn } from "@/lib/utils"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import {ItemResponse} from "@/lib/types/api/item"



interface Props {
  item: ItemResponse
  isWishlisted?: boolean
  onWishlistToggle?: (id: number) => void
  className?: string
}

export function MarketplaceItemCard({item, isWishlisted = false, onWishlistToggle, className,}: Props) {
  const seller = item.seller

  const trustScore = seller?.trustScore ?? 0
  const sellerName = seller?.name ?? "Unknown Seller"

  const userPrice = item.userPrice ?? 0
  const generatedPrice = item.generatedPrice ?? userPrice

  const priceDiff =
    generatedPrice > 0
      ? ((userPrice - generatedPrice) / generatedPrice) * 100
      : 0

  const isGoodDeal = priceDiff < -5
  const isBadDeal = priceDiff > 10

  return (
    <motion.div
      whileHover={{ y: -4 }}
      whileTap={{ scale: 0.98 }}
      className={cn(
        "group relative rounded-xl marketplace-card overflow-hidden glass cursor-pointer",
        "hover:border-primary/30 hover:glow-sm transition-all duration-300",
        className
      )}
    >
      {/* Image Container */}
      <div className="relative aspect-square overflow-hidden bg-muted/30">
        <Image
          src={item.imageUrls?.[0] ?? "/placeholder.jpg"}
          alt={item.itemName}
          fill
          className="object-cover transition-transform duration-500 group-hover:scale-110"
        />
        
        {/* Gradient Overlay */}
        <div className="absolute inset-0 bg-gradient-to-t 
        from-background/80 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />

        {/* Badges */}
        <div className="absolute top-3 left-3 flex flex-col gap-2">
          {item.forTrade && item.forSale ? (
            <Badge className="bg-gradient-to-r from-primary to-info text-primary-foreground border-none">
              Trade / Sale
            </Badge>
          ) : item.forTrade ? (
            <Badge className="bg-primary/90 text-primary-foreground border-none">
              Trade Only
            </Badge>
          ) : item.forSale ? (
            <Badge className="bg-info/90 text-info-foreground border-none">
              For Sale
            </Badge>
          ) : null}
        </div>

        {/* Wishlist Button */}
        <Button
          variant="ghost"
          size="icon"
          className={cn(
            "absolute top-3 right-3 h-8 w-8 rounded-full",
            "bg-background/60 backdrop-blur-sm hover:bg-background/80",
            isWishlisted && "text-red-500"
          )}
          onClick={(e) => {
            e.stopPropagation()
            onWishlistToggle?.(item.itemId)
          }}
        >
          <Heart className={cn("h-4 w-4", isWishlisted && "fill-current")} />
          <span className="sr-only">
            {isWishlisted ? "Remove from wishlist" : "Add to wishlist"}
          </span>
        </Button>

        {/* Trust Indicator */}
        <div className="absolute bottom-3 left-3 flex items-center gap-1.5 px-2 py-1 rounded-full bg-background/60 backdrop-blur-sm">
          <ShieldCheck className={cn(
            "h-3.5 w-3.5",
            trustScore >= 90 ? "text-emerald-400" :
            trustScore >= 70 ? "text-amber-400" : "text-red-400"
          )} />
          <span className="text-xs font-medium">{trustScore}%</span>
        </div>
      </div>

      {/* Content */}
      <div className="p-4 space-y-3">
        {/* Title */}
        <h3 className="font-semibold text-foreground line-clamp-1 group-hover:text-primary transition-colors">
          {item.itemName}
        </h3>

        {/* Condition */}
        <div className="flex items-center gap-2">
          <Sparkles className="h-3.5 w-3.5" />
          <span className="text-xs font-medium">
            {item.condition}
          </span>
        </div>

        {/* Pricing */}
        <div className="space-y-1.5">
          <div className="flex items-center justify-between">
            <span className="text-xs text-muted-foreground">Listed Price</span>
            <span className="text-lg font-bold text-foreground">
              ${userPrice.toLocaleString()}
            </span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-xs text-muted-foreground flex items-center gap-1">
              <Sparkles className="h-3 w-3 text-primary" />
              AI Estimate
            </span>
            <div className="flex items-center gap-1.5">
              <span className="text-sm text-muted-foreground">
                ${generatedPrice.toLocaleString()}
              </span>
              <span className={cn(
                "text-xs font-medium flex items-center gap-0.5",
                isGoodDeal ? "text-emerald-400" : isBadDeal ? "text-red-400" : "text-muted-foreground"
              )}>
                {isGoodDeal ? (
                  <TrendingDown className="h-3 w-3" />
                ) : isBadDeal ? (
                  <TrendingUp className="h-3 w-3" />
                ) : null}
                {Math.abs(priceDiff).toFixed(0)}%
              </span>
            </div>
          </div>
        </div>

        {/* Seller Info */}
        <div className="flex items-center gap-2 pt-2 border-t border-border/50">
          <div className="pt-2 border-t border-border/50">
            <span className="text-xs text-muted-foreground truncate block">
              {sellerName}
            </span>
          </div>
        </div>
      </div>
    </motion.div>
  )
}

// Skeleton loader for the card
export function MarketplaceItemCardSkeleton({ className }: { className?: string }) {
  return (
    <div className={cn("rounded-xl overflow-hidden glass", className)}>
      <div className="aspect-square animate-shimmer" />
      <div className="p-4 space-y-3">
        <div className="h-5 w-3/4 rounded bg-muted animate-shimmer" />
        <div className="h-4 w-1/2 rounded bg-muted animate-shimmer" />
        <div className="space-y-1.5">
          <div className="h-6 w-full rounded bg-muted animate-shimmer" />
          <div className="h-4 w-2/3 rounded bg-muted animate-shimmer" />
        </div>
        <div className="flex items-center gap-2 pt-2 border-t border-border/50">
          <div className="h-6 w-6 rounded-full bg-muted animate-shimmer" />
          <div className="h-4 w-20 rounded bg-muted animate-shimmer" />
        </div>
      </div>
    </div>
  )
}
