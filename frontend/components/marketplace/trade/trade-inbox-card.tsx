"use client"

import { useState, type ReactNode } from "react"
import Link from "next/link"
import { motion } from "framer-motion"
import {
  ArrowLeftRight,
  ArrowRight,
  Clock3,
  MessageCircle,
  Package,
  Send,
} from "lucide-react"

import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import { TradeReviewForm } from "./trade-review-form"
import type { TradeInboxResponse } from "@/lib/types/api/trade"
import { TradeStatusBadge } from "./trade-status-badge"


interface Props {
  trade: TradeInboxResponse
  currentUserId: number
}

export function TradeInboxCard({ trade, currentUserId }: Props) {
  const [showReviewForm, setShowReviewForm] = useState(false)
  const latestOfferIsMine = trade.latestOffer.senderId === currentUserId
  const lineClass = latestOfferIsMine
    ? "bg-[var(--trust-blue)]"
    : "bg-[var(--negotiation-violet)]"
  const latestOfferTone = latestOfferIsMine
    ? "text-[var(--trust-blue)]"
    : "text-[var(--negotiation-violet)]"
  const latestOfferLabel = latestOfferIsMine ? "Your latest offer" : "Their latest offer"
  const latestOfferDate = formatTradeDate(trade.latestOffer.createdAt)
  const updatedDate = formatTradeDate(trade.updatedAt)
  const totalCurrentOfferItems =
    trade.latestOffer.senderItems.length + trade.latestOffer.receiverItems.length

  return (
    <motion.article
      whileHover={{ y: -2 }}
      transition={{ duration: 0.18 }}
      className="group relative overflow-hidden rounded-[28px] border border-white/[0.10] shadow-[0_18px_44px_rgba(0,0,0,.16)] backdrop-blur-xl transition-all duration-300 hover:border-[var(--trust-blue)]/30 hover:shadow-[0_22px_52px_rgba(18,45,82,.2)]"
      style={{
        background:
          "radial-gradient(circle at 5% 0%, rgba(59,130,246,.09), transparent 24%), radial-gradient(circle at 95% 0%, rgba(168,85,247,.075), transparent 24%), linear-gradient(135deg, rgba(8,15,24,.96), rgba(13,10,19,.96))",
      }}
    >
      <div className={`absolute inset-y-5 left-0 w-0.5 rounded-r-full ${lineClass}`} />
      <div className="pointer-events-none absolute -right-28 -top-28 h-56 w-56 rounded-full bg-[var(--negotiation-violet)]/[0.035] blur-3xl opacity-90 transition-opacity group-hover:opacity-100" />
      <div className="pointer-events-none absolute -bottom-28 left-1/3 h-48 w-48 rounded-full bg-[var(--trust-blue)]/[0.025] blur-3xl" />

      <div className="relative p-5 sm:p-6">
        <div className="flex items-start gap-4">
          <Link
            href={`/profile/${trade.otherUserId}`}
            aria-label={`View ${trade.otherUserName}'s profile`}
            className="group/avatar shrink-0"
          >
            <Avatar className="profile-avatar h-14 w-14 rounded-2xl transition-transform duration-200 group-hover/avatar:scale-[1.025] sm:h-16 sm:w-16">
              <AvatarImage src={trade.otherUserProfilePicture} alt={trade.otherUserName} />
              <AvatarFallback className="bg-[var(--trust-blue)]/[0.07] text-sm font-semibold text-[var(--trust-blue)]">
                {trade.otherUserName.slice(0, 2).toUpperCase()}
              </AvatarFallback>
            </Avatar>
          </Link>

          <div className="min-w-0 flex-1">
            <div className="flex flex-wrap items-center gap-2">
              <h3 className="truncate text-lg font-semibold tracking-tight">{trade.otherUserName}</h3>
              <TradeStatusBadge status={trade.status} />
            </div>

            <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-1.5 text-xs text-muted-foreground">
              <span className="inline-flex items-center gap-1.5">
                <Clock3 className="h-3.5 w-3.5" />
                Updated {updatedDate}
              </span>
              <span className={`inline-flex items-center gap-1.5 font-medium ${latestOfferTone}`}>
                <Send className="h-3.5 w-3.5" />
                {latestOfferLabel}
              </span>
              <span className={`inline-flex items-center gap-1.5 font-medium ${latestOfferTone}`}>
                {latestOfferDate}
              </span>
            </div>
          </div>
        </div>

        <div className="mt-4 grid gap-2 sm:grid-cols-3">
          <TradeSummaryTile
            label="Your items"
            value={`${trade.yourItemCount} ${trade.yourItemCount === 1 ? "item" : "items"}`}
            icon={<Package className="h-4 w-4" />}
            tone="blue"
          />
          <TradeSummaryTile
            label="Their items"
            value={`${trade.theirItemCount} ${trade.theirItemCount === 1 ? "item" : "items"}`}
            icon={<Package className="h-4 w-4" />}
            tone="violet"
          />
          <TradeSummaryTile
            label="Cash adjustment"
            value={`₹${(trade.cashAdjustment ?? 0).toLocaleString("en-IN")}`}
            icon={<ArrowLeftRight className="h-4 w-4" />}
            tone={trade.cashAdjustment === 0 ? "cyan" : "gold"}
          />
        </div>

        <div className="mt-4 flex flex-col gap-3 border-t border-white/[0.08] pt-4 sm:flex-row sm:items-center sm:justify-between">
          <div className="min-w-0">
            <div className="flex items-center gap-2 text-[10px] font-semibold uppercase tracking-[0.14em] text-muted-foreground">
              <span>Current proposal</span>
              <span className="h-1 w-1 rounded-full bg-muted-foreground/45" />
              <span>#{trade.latestOffer.tradeOfferId}</span>
            </div>
            <p className="mt-1 truncate text-xs text-foreground/65">
              {totalCurrentOfferItems} {totalCurrentOfferItems === 1 ? "item" : "items"} in the latest proposal
            </p>
          </div>

          <div className="flex flex-wrap gap-2 sm:justify-end">
            <Button asChild className="cta-primary rounded-xl">
              <Link href={`/trades/${trade.tradeId}?tradeOfferId=${trade.latestOffer.tradeOfferId}`}>
                <ArrowRight className="mr-2 h-4 w-4" />
                Open trade
              </Link>
            </Button>

            <Button asChild variant="surface" className="rounded-xl">
              <Link href={`/chat/${trade.otherUserId}`}>
                <MessageCircle className="mr-2 h-4 w-4" />
                Chat
              </Link>
            </Button>
          </div>
        </div>
      </div>

      {trade.status === "COMPLETED" && (
        <div className="border-t border-white/[0.08] px-4 py-4 sm:px-6">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <p className="text-sm font-semibold">Share your trade experience</p>
              <p className="mt-1 text-xs leading-5 text-muted-foreground">
                Reviews are available after both traders have completed the exchange.
              </p>
            </div>
            <Button
              type="button"
              variant="surface"
              size="sm"
              className="h-9 shrink-0 rounded-xl"
              aria-expanded={showReviewForm}
              onClick={() => setShowReviewForm((open) => !open)}
            >
              {showReviewForm ? "Hide review" : "Review trade"}
            </Button>
          </div>
          {showReviewForm && (
            <div className="mt-4">
              <TradeReviewForm tradeId={trade.tradeId} status={trade.status} />
            </div>
          )}
        </div>
      )}
    </motion.article>
  )
}

function TradeSummaryTile({
  label,
  value,
  icon,
  tone,
}: {
  label: string
  value: string
  icon: ReactNode
  tone: "blue" | "violet" | "cyan" | "gold"
}) {
  return (
    <div className={`marketplace-attribute marketplace-attribute-${tone}`}>
      <span className="marketplace-attribute-icon">{icon}</span>
      <div className="marketplace-attribute-copy">
        <p className="marketplace-attribute-label">{label}</p>
        <p className="marketplace-attribute-value">{value}</p>
      </div>
    </div>
  )
}

function formatTradeDate(dateString: string) {
  const date = new Date(dateString)
  if (Number.isNaN(date.getTime())) return dateString

  const now = new Date()
  const sameDay = date.toDateString() === now.toDateString()
  const yesterday = new Date(now)
  yesterday.setDate(now.getDate() - 1)

  const time = new Intl.DateTimeFormat("en-IN", {
    hour: "numeric",
    minute: "2-digit",
  }).format(date)

  if (sameDay) return `Today, ${time}`
  if (date.toDateString() === yesterday.toDateString()) return `Yesterday, ${time}`

  return new Intl.DateTimeFormat("en-IN", {
    day: "numeric",
    month: "short",
    year: date.getFullYear() === now.getFullYear() ? undefined : "numeric",
  }).format(date)
}
