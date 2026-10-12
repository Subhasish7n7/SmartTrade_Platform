"use client"

import { useState } from "react"
import { motion } from "framer-motion"
import {
  ArrowLeftRight,
  Eye,
  MapPin,
  MoreHorizontal,
  PackageCheck,
  PackageX,
  Pencil,
  Trash2,
} from "lucide-react"
import Image from "next/image"
import Link from "next/link"

import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { TrustScoreBadge } from "@/components/trust-score-badge"
import { deleteItem, updateItemAvailability } from "@/lib/api/items/client"
import type { ItemResponse } from "@/lib/types/api/item"
import { cn } from "@/lib/utils"

interface Props {
  item: ItemResponse
  isOwner: boolean
  onChanged?: (available: boolean) => void
  onRemoved?: (itemId: number) => void
}

const INR = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  maximumFractionDigits: 0,
})

function formatPrice(value: number | null | undefined) {
  return value == null || !Number.isFinite(value) ? null : INR.format(value)
}

function readableLabel(value: string) {
  return value.toLowerCase().replaceAll("_", " ").replace(/\b\w/g, (letter) => letter.toUpperCase())
}

function getApiErrorMessage(error: unknown, fallback: string) {
  if (error && typeof error === "object") {
    const candidate = error as {
      message?: unknown
      response?: { data?: { message?: unknown; error?: unknown; detail?: unknown } }
    }
    const data = candidate.response?.data
    for (const value of [data?.message, data?.detail, data?.error, candidate.message]) {
      if (typeof value === "string" && value.trim()) return value.trim()
    }
  }
  return fallback
}

export function InventoryItemCard({ item, isOwner, onChanged, onRemoved }: Props) {
  const [updating, setUpdating] = useState(false)
  const [removing, setRemoving] = useState(false)
  const [actionError, setActionError] = useState<string | null>(null)

  const image = item.images?.[0]?.secureUrl ?? "/placeholder.jpg"
  const location = [item.locality, item.city, item.state].filter(Boolean).join(", ")
  const listedPrice = formatPrice(item.userPrice)
  const usedEstimate = item.generatedPrice != null && item.generatedPrice > 0
    ? formatPrice(item.generatedPrice)
    : null
  const category = item.customCategory?.trim() || readableLabel(item.category)
  const condition = readableLabel(item.condition)
  const busy = updating || removing

  async function toggleAvailability() {
    if (!isOwner || busy) return

    const nextAvailable = !item.available
    setActionError(null)
    setUpdating(true)
    try {
      await updateItemAvailability(item.itemId, nextAvailable)
      // Update the parent only after the API confirms success.
      onChanged?.(nextAvailable)
    } catch (error) {
      setActionError(getApiErrorMessage(error, "Availability could not be updated. Please try again."))
    } finally {
      setUpdating(false)
    }
  }

  async function handleDelete() {
    if (!isOwner || busy) return
    if (!window.confirm(`Delete “${item.itemName}”? This cannot be undone.`)) return

    setActionError(null)
    setRemoving(true)
    try {
      await deleteItem(item.itemId)
      onRemoved?.(item.itemId)
    } catch (error) {
      setActionError(getApiErrorMessage(error, "The listing could not be deleted. Please try again."))
    } finally {
      setRemoving(false)
    }
  }

  return (
    <motion.article
      layout
      transition={{ duration: 0.16 }}
      aria-busy={busy}
      className={cn(
        "group min-w-0 overflow-hidden rounded-2xl border border-border/60 bg-card/45 transition-colors",
        "hover:border-primary/25",
        !item.available && "opacity-[0.88]",
      )}
    >
      <div className="relative aspect-[4/3] overflow-hidden bg-muted/25">
        <Image
          src={image}
          alt={item.itemName}
          fill
          sizes="(min-width: 1536px) 24vw, (min-width: 1280px) 32vw, (min-width: 640px) 48vw, 100vw"
          className="object-cover transition-transform duration-300 group-hover:scale-[1.025]"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-black/75 via-black/5 to-black/15" />

        <div className="absolute left-3 top-3 flex max-w-[calc(100%-3.75rem)] flex-wrap gap-1.5">
          {item.forTrade && item.forSale ? (
            <Badge className="border border-white/15 bg-primary/90 text-primary-foreground">Sale + trade</Badge>
          ) : item.forTrade ? (
            <Badge className="border border-white/15 bg-primary/90 text-primary-foreground">Trade</Badge>
          ) : item.forSale ? (
            <Badge variant="secondary" className="border border-white/15 bg-background/85">For sale</Badge>
          ) : null}
          <Badge variant="outline" className="border-white/20 bg-black/55 text-white backdrop-blur-sm">
            {condition}
          </Badge>
        </div>

        <div className="absolute right-3 top-3">
          {isOwner ? (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button
                  size="icon"
                  variant="secondary"
                  className="h-8 w-8 rounded-full border border-white/15 bg-background/85"
                  aria-label={`Manage ${item.itemName}`}
                >
                  <MoreHorizontal className="h-4 w-4" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-48">
                <DropdownMenuItem asChild>
                  <Link href={`/item/${item.itemId}`}><Eye className="mr-2 h-4 w-4" />View listing</Link>
                </DropdownMenuItem>
                <DropdownMenuItem asChild>
                  <Link href={`/list-item?editId=${item.itemId}`}><Pencil className="mr-2 h-4 w-4" />Edit listing</Link>
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem
                  onClick={() => void handleDelete()}
                  disabled={busy}
                  className="text-destructive focus:text-destructive"
                >
                  <Trash2 className="mr-2 h-4 w-4" />
                  {removing ? "Deleting…" : "Delete listing"}
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          ) : (
            <Button asChild size="icon" variant="secondary" className="h-8 w-8 rounded-full border border-white/15 bg-background/85">
              <Link href={`/item/${item.itemId}`} aria-label={`View ${item.itemName}`}>
                <Eye className="h-4 w-4" />
              </Link>
            </Button>
          )}
        </div>

        <div className="absolute bottom-3 left-3 right-3 flex items-end justify-between gap-2">
          <TrustScoreBadge score={item.seller?.trustScore} appearance="overlay" size="sm" />
          <span
            className={cn(
              "shrink-0 rounded-full border px-2.5 py-1.5 text-[10px] font-semibold backdrop-blur-md",
              item.available
                ? "border-emerald-300/20 bg-black/55 text-emerald-200"
                : "border-amber-300/25 bg-black/60 text-amber-200",
            )}
          >
            {item.available ? "Available" : "Unavailable"}
          </span>
        </div>
      </div>

      <div className="p-3.5">
        {actionError && (
          <div role="alert" className="mb-3 rounded-lg border border-destructive/25 bg-destructive/5 px-3 py-2 text-xs leading-5 text-destructive">
            {actionError}
          </div>
        )}

        <div className="flex items-start justify-between gap-3">
          <div className="min-w-0 flex-1">
            <h3 className="line-clamp-1 text-sm font-semibold tracking-tight group-hover:text-primary">
              {item.itemName}
            </h3>
            <p className="mt-1 text-xs text-muted-foreground">{category} <span aria-hidden="true">·</span> {condition}</p>
          </div>
          <div className="shrink-0 text-right">
            <p className="text-base font-semibold leading-5 tabular-nums">{listedPrice ?? "Price not set"}</p>
            {usedEstimate && (
              <p className="mt-1 text-[10px] leading-4 text-muted-foreground">
                Used estimate {usedEstimate}
              </p>
            )}
          </div>
        </div>

        {item.description?.trim() && (
          <p className="mt-2 line-clamp-2 text-xs leading-5 text-muted-foreground">{item.description}</p>
        )}

        <div className="mt-3 flex min-h-8 items-center justify-between gap-2 border-t border-border/45 pt-3">
          <div className="min-w-0 text-xs text-muted-foreground">
            {location ? (
              <span className="inline-flex max-w-full items-center gap-1.5">
                <MapPin className="h-3.5 w-3.5 shrink-0" />
                <span className="truncate">{location}</span>
              </span>
            ) : (
              <span>Location not specified</span>
            )}
          </div>

          {!isOwner && item.available && item.forTrade ? (
            <Button asChild size="sm" className="cta-primary h-8 shrink-0 rounded-lg px-3">
              <Link href={`/trades/create/${item.itemId}`}><ArrowLeftRight className="mr-1.5 h-3.5 w-3.5" />Trade</Link>
            </Button>
          ) : !isOwner ? (
            <Button asChild size="sm" variant="surface" className="h-8 shrink-0 rounded-lg px-3">
              <Link href={`/item/${item.itemId}`}><Eye className="mr-1.5 h-3.5 w-3.5" />View</Link>
            </Button>
          ) : null}
        </div>
      </div>

      {isOwner && (
        <div className="flex items-center justify-between gap-2 border-t border-border/45 bg-background/20 px-3 py-2">
          <button
            type="button"
            onClick={() => void toggleAvailability()}
            disabled={busy}
            className="inline-flex min-w-0 items-center gap-2 rounded-lg px-1.5 py-1.5 text-xs text-muted-foreground transition hover:text-foreground disabled:cursor-not-allowed disabled:opacity-50"
          >
            {item.available ? <PackageX className="h-3.5 w-3.5 text-amber-300" /> : <PackageCheck className="h-3.5 w-3.5 text-emerald-300" />}
            <span className="truncate">
              {updating ? "Updating…" : item.available ? "Mark unavailable" : "Mark available"}
            </span>
          </button>
          <Button asChild size="sm" variant="surface" className="h-8 shrink-0 rounded-lg px-3">
            <Link href={`/list-item?editId=${item.itemId}`}><Pencil className="mr-1.5 h-3.5 w-3.5" />Edit</Link>
          </Button>
        </div>
      )}
    </motion.article>
  )
}
