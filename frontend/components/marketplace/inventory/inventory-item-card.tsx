"use client"

import Image from "next/image"
import Link from "next/link"
import { motion } from "framer-motion"
import {
  ArrowLeftRight,
  Eye,
  MapPin,
  MoreHorizontal,
  PackageCheck,
  PackageX,
  Pencil,
  ShieldCheck,
  Sparkles,
  Trash2,
} from "lucide-react"
import { useState } from "react"

import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { ItemResponse } from "@/lib/types/api/item"
import { deleteItem, updateItemAvailability } from "@/lib/api/items/client"
import { cn } from "@/lib/utils"

interface Props {
  item: ItemResponse
  isOwner: boolean
  onChanged?: (available: boolean) => void
  onRemoved?: (itemId: number) => void
}

export function InventoryItemCard({ item, isOwner, onChanged, onRemoved }: Props) {
  const [updating, setUpdating] = useState(false)
  const [removing, setRemoving] = useState(false)

  const generatedPrice = item.generatedPrice ?? item.userPrice
  const priceDiff = generatedPrice > 0 ? ((item.userPrice - generatedPrice) / generatedPrice) * 100 : 0
  const image = item.imageUrls?.[0] ?? "/placeholder.jpg"
  const location = [item.locality, item.city].filter(Boolean).join(", ")

  async function toggleAvailability() {
    try {
      setUpdating(true)
      await updateItemAvailability(item.itemId, !item.available)
      onChanged?.(!item.available)
    } finally {
      setUpdating(false)
    }
  }

  async function handleDelete() {
    if (!window.confirm(`Delete “${item.itemName}”? This cannot be undone.`)) return

    try {
      setRemoving(true)
      await deleteItem(item.itemId)
      onRemoved?.(item.itemId)
    } finally {
      setRemoving(false)
    }
  }

  return (
    <motion.article
      layout
      whileHover={{ y: -4 }}
      transition={{ duration: 0.18 }}
      className={cn(
        "group relative overflow-hidden rounded-[26px] border border-border/55 bg-card/55 backdrop-blur-xl transition-all duration-300",
        "shadow-[0_0_30px_rgba(59,130,246,.05)] hover:border-[var(--trust-blue)]/25 hover:shadow-[0_0_36px_rgba(59,130,246,.10)]",
        !item.available && "opacity-80"
      )}
    >
      <div className="h-1 bg-gradient-to-r from-[var(--trust-blue)] via-[var(--negotiation-violet)] to-[var(--premium-gold)]" />

      <div className="relative aspect-[1.1] overflow-hidden bg-muted/20">
        <Image src={image} alt={item.itemName} fill className="object-cover transition-transform duration-500 group-hover:scale-[1.045]" />
        <div className="absolute inset-0 bg-gradient-to-t from-background/75 via-transparent to-transparent" />

        <div className="absolute left-3 top-3 flex flex-wrap gap-1.5">
          {item.forTrade && item.forSale ? (
            <Badge className="border-none bg-gradient-to-r from-primary to-info text-primary-foreground">Trade / Sale</Badge>
          ) : item.forTrade ? (
            <Badge className="border-none bg-primary/90 text-primary-foreground">Trade</Badge>
          ) : item.forSale ? (
            <Badge className="border-none bg-info/90 text-info-foreground">Sale</Badge>
          ) : null}

          <Badge variant="outline" className="border-white/10 bg-background/55 backdrop-blur-md">
            {item.condition.replaceAll("_", " ")}
          </Badge>
        </div>

        <div className="absolute right-3 top-3">
          {isOwner ? (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button size="icon" variant="secondary" className="h-8 w-8 rounded-full bg-background/65 backdrop-blur-md">
                  <MoreHorizontal className="h-4 w-4" />
                  <span className="sr-only">Manage {item.itemName}</span>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-48">
                <DropdownMenuItem asChild>
                  <Link href={`/item/${item.itemId}`}>
                    <Eye className="mr-2 h-4 w-4" />
                    View listing
                  </Link>
                </DropdownMenuItem>
                <DropdownMenuItem onClick={toggleAvailability} disabled={updating}>
                  {item.available ? <PackageX className="mr-2 h-4 w-4" /> : <PackageCheck className="mr-2 h-4 w-4" />}
                  {item.available ? "Mark unavailable" : "Mark available"}
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={handleDelete} disabled={removing} className="text-destructive focus:text-destructive">
                  <Trash2 className="mr-2 h-4 w-4" />
                  {removing ? "Deleting…" : "Delete listing"}
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          ) : (
            <Button asChild size="icon" variant="secondary" className="h-8 w-8 rounded-full bg-background/65 backdrop-blur-md">
              <Link href={`/item/${item.itemId}`}>
                <Eye className="h-4 w-4" />
                <span className="sr-only">View {item.itemName}</span>
              </Link>
            </Button>
          )}
        </div>

        <div className="absolute bottom-3 left-3 flex items-center gap-1.5 rounded-full border border-white/10 bg-background/60 px-2.5 py-1.5 backdrop-blur-md">
          <ShieldCheck className="h-3.5 w-3.5 text-[var(--trust-blue)]" />
          <span className="text-xs font-semibold">{Math.round(item.seller?.trustScore ?? 0)}%</span>
          <span className="text-[10px] text-muted-foreground">trust</span>
        </div>

        {!item.available && (
          <div className="absolute bottom-3 right-3 rounded-full border border-amber-400/20 bg-background/75 px-2.5 py-1.5 text-[10px] font-semibold text-amber-300 backdrop-blur-md">
            Unavailable
          </div>
        )}
      </div>

      <div className="p-4.5">
        <div className="flex items-start justify-between gap-3">
          <div className="min-w-0">
            <h3 className="line-clamp-1 text-[17px] font-semibold tracking-tight transition-colors group-hover:text-primary">{item.itemName}</h3>
            <div className="mt-1.5 flex flex-wrap items-center gap-x-3 gap-y-1.5 text-xs text-muted-foreground">
              <span className="inline-flex items-center gap-1"><Sparkles className="h-3 w-3" />{item.condition.replaceAll("_", " ")}</span>
              <span>{item.customCategory || item.category.replaceAll("_", " ")}</span>
            </div>
          </div>

          <div className="shrink-0 text-right">
            <p className="text-lg font-bold">₹{item.userPrice.toLocaleString()}</p>
            {generatedPrice > 0 && (
              <p className={cn("mt-0.5 text-[10px] font-medium", priceDiff < -5 ? "text-emerald-400" : "text-muted-foreground")}>
                AI ₹{generatedPrice.toLocaleString()}
              </p>
            )}
          </div>
        </div>

        <div className="mt-4 flex items-center justify-between border-t border-border/45 pt-3">
          <div className="min-w-0 text-xs text-muted-foreground">
            {location ? (
              <span className="inline-flex max-w-[190px] items-center gap-1.5 truncate">
                <MapPin className="h-3.5 w-3.5 shrink-0 text-[var(--chat-cyan)]" />
                {location}
              </span>
            ) : (
              <span>Location not specified</span>
            )}
          </div>

          {!isOwner && item.forTrade && item.available ? (
            <Button asChild size="sm" className="h-8 rounded-lg cta-primary">
              <Link href={`/trades/create/${item.itemId}`}>
                <ArrowLeftRight className="mr-1.5 h-3.5 w-3.5" />
                Trade
              </Link>
            </Button>
          ) : (
            <Button asChild size="sm" variant="surface" className="h-8 rounded-lg">
              <Link href={`/item/${item.itemId}`}>
                <Eye className="mr-1.5 h-3.5 w-3.5" />
                View
              </Link>
            </Button>
          )}
        </div>
      </div>

      {isOwner && (
        <button
          type="button"
          onClick={toggleAvailability}
          disabled={updating}
          className="flex w-full items-center justify-center gap-2 border-t border-border/35 bg-white/[0.015] px-4 py-2.5 text-[11px] font-medium text-muted-foreground transition hover:bg-white/[0.035] hover:text-foreground disabled:opacity-50"
        >
          {item.available ? <PackageCheck className="h-3.5 w-3.5 text-emerald-400" /> : <PackageX className="h-3.5 w-3.5 text-amber-300" />}
          {updating ? "Updating availability…" : item.available ? "Listed and available" : "Currently unavailable"}
          <Pencil className="ml-auto h-3 w-3 opacity-40" />
        </button>
      )}
    </motion.article>
  )
}
