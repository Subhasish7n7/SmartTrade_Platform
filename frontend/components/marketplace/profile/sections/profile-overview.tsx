"use client"

import Link from "next/link"
import { motion } from "framer-motion"
import {
  ArrowRight,
  ArrowUpRight,
  CheckCircle2,
  Package,
  RefreshCw,
  ShieldCheck,
  Sparkles,
} from "lucide-react"

import { Button } from "@/components/ui/button"
import { ItemResponse } from "@/lib/types/api/item"
import { MyUserProfile, PublicUserProfile } from "@/lib/types/api/profile"

import Image from "next/image"
import type { ReactNode } from "react"
import { Badge } from "@/components/ui/badge"

interface Props {
  profile: MyUserProfile | PublicUserProfile
  items: ItemResponse[]
  isOwner: boolean
}

export function ProfileOverview({ profile, items, isOwner }: Props) {
  const availableItems = items.filter((item) => item.available)
  const tradeableItems = availableItems.filter((item) => item.forTrade)
  const saleItems = availableItems.filter((item) => item.forSale)
  const trust = Math.round(Math.max(0, Math.min(100, profile.trustScore)))
  const featuredItems = availableItems.slice(0, 3)

  return (
    <div className="space-y-5">
      <section className="grid gap-5 xl:grid-cols-[minmax(0,1.25fr)_minmax(290px,.75fr)]">
        <div className="content-surface overflow-hidden p-0">
          <div className="trade-card-accent" />
          <div className="relative overflow-hidden p-5 sm:p-6">
            <div className="pointer-events-none absolute -right-24 -top-24 h-56 w-56 rounded-full bg-[var(--trust-blue)]/10 blur-3xl" />
            <div className="pointer-events-none absolute -bottom-28 left-1/3 h-48 w-48 rounded-full bg-[var(--negotiation-violet)]/8 blur-3xl" />

            <div className="relative">
              <div className="flex items-center gap-2 text-[11px] font-semibold uppercase tracking-[0.18em] text-muted-foreground">
                <Sparkles className="h-3.5 w-3.5 text-primary" />
                Trader reputation
              </div>

              <div className="mt-5 flex flex-col gap-5 sm:flex-row sm:items-center">
                <div className="relative flex h-28 w-28 shrink-0 items-center justify-center rounded-full border border-[var(--trust-blue)]/20 bg-background/35 shadow-[0_0_40px_rgba(59,130,246,.10)]">
                  <div
                    className="absolute inset-2 rounded-full"
                    style={{
                      background: `conic-gradient(var(--trust-blue) ${trust * 3.6}deg, rgba(255,255,255,.05) 0deg)`,
                    }}
                  />
                  <div className="absolute inset-[7px] rounded-full bg-background" />
                  <div className="relative text-center">
                    <p className="text-2xl font-bold">{trust}%</p>
                    <p className="text-[9px] uppercase tracking-[0.14em] text-muted-foreground">Trust</p>
                  </div>
                </div>

                <div className="min-w-0">
                  <h2 className="text-xl font-bold tracking-tight">A strong marketplace reputation</h2>
                  <p className="mt-2 max-w-xl text-sm leading-6 text-muted-foreground">
                    {trust >= 90
                      ? "This trader has built a strong history of successful transactions and reliable marketplace activity."
                      : trust >= 70
                        ? "This trader has an established transaction history with a healthy marketplace reputation."
                        : "Continue building successful transactions to strengthen marketplace reputation."}
                  </p>

                  <div className="mt-4 flex flex-wrap gap-2">
                    <ReputationPill icon={<ShieldCheck />} label={`${trust}% trust`} />
                    <ReputationPill icon={<CheckCircle2 />} label={`${profile.successfulTrades} completed`} />
                    <ReputationPill icon={<Package />} label={`${profile.totalListings} listings`} />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="content-surface overflow-hidden p-0">
          <div className="trade-card-accent" />
          <div className="p-5 sm:p-6">
            <div className="flex items-center justify-between gap-4">
              <div>
                <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-muted-foreground">Marketplace presence</p>
                <h2 className="mt-2 text-xl font-bold">At a glance</h2>
              </div>
              <RefreshCw className="h-5 w-5 text-[var(--negotiation-violet)]" />
            </div>

            <div className="mt-5 space-y-2.5">
              <PresenceRow label="Active listings" value={availableItems.length} />
              <PresenceRow label="Available for trade" value={tradeableItems.length} accent="violet" />
              <PresenceRow label="Available for sale" value={saleItems.length} accent="gold" />
              <PresenceRow label="Successful trades" value={profile.successfulTrades} accent="green" />
            </div>

            <Button asChild variant="surface" className="mt-5 w-full rounded-xl">
              <Link href="/inventory">
                Explore inventory
                <ArrowRight className="ml-2 h-4 w-4" />
              </Link>
            </Button>
          </div>
        </div>
      </section>

      <section id="inventory" className="content-surface overflow-hidden p-0">
        <div className="trade-card-accent" />
        <div className="flex items-end justify-between gap-4 p-5 sm:p-6">
          <div>
            <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-muted-foreground">Marketplace</p>
            <h2 className="mt-2 text-xl font-bold">Featured listings</h2>
            <p className="mt-1 text-sm text-muted-foreground">A snapshot of what this trader currently has available.</p>
          </div>

          <Button asChild variant="surface" className="hidden rounded-xl sm:inline-flex">
            <Link href="#inventory">View all</Link>
          </Button>
        </div>

        <div className="border-t border-border/40 p-4 sm:p-5">
          {featuredItems.length > 0 ? (
            <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
              {featuredItems.map((item, index) => (
                <motion.div key={item.itemId} initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: index * 0.04 }}>
                  <FeaturedItemCard item={item} />
                </motion.div>
              ))}
            </div>
          ) : (
            <div className="py-14 text-center">
              <Package className="mx-auto h-8 w-8 text-muted-foreground" />
              <h3 className="mt-4 font-semibold">No active listings</h3>
              <p className="mt-1 text-sm text-muted-foreground">There is nothing available in this trader's marketplace inventory right now.</p>
              {isOwner && (
                <Button asChild className="cta-primary mt-5 rounded-xl">
                  <Link href="/list-item">
                    List an item
                    <ArrowUpRight className="ml-2 h-4 w-4" />
                  </Link>
                </Button>
              )}
            </div>
          )}
        </div>
      </section>
    </div>
  )
}

function FeaturedItemCard({ item }: { item: ItemResponse }) {
  const image = item.imageUrls?.[0] ?? "/placeholder.jpg"
  const listingType = item.forTrade && item.forSale ? "Trade / Sale" : item.forTrade ? "Trade Only" : "For Sale"

  return (
    <Link
      href={`/item/${item.itemId}`}
      className="group block overflow-hidden rounded-2xl border border-border/55 bg-card/55 backdrop-blur-xl transition-all duration-300 hover:-translate-y-1 hover:border-primary/25 hover:shadow-[0_0_30px_rgba(59,130,246,.10)]"
    >
      <div className="relative aspect-[4/3] overflow-hidden bg-muted/30">
        <Image src={image} alt={item.itemName} fill className="object-cover transition-transform duration-500 group-hover:scale-105" />
        <div className="absolute inset-0 bg-gradient-to-t from-background/80 via-transparent to-transparent" />
        <Badge className="absolute left-3 top-3 border-none bg-background/75 backdrop-blur-md">{listingType}</Badge>
      </div>
      <div className="p-4">
        <h3 className="line-clamp-1 font-semibold transition-colors group-hover:text-primary">{item.itemName}</h3>
        <p className="mt-1 text-xs text-muted-foreground">{formatEnum(item.condition)} · {formatEnum(item.category)}</p>
        <div className="mt-4 flex items-end justify-between gap-3">
          <div>
            <p className="text-[10px] uppercase tracking-wider text-muted-foreground">Listed value</p>
            <p className="mt-0.5 text-lg font-bold">₹{item.userPrice.toLocaleString()}</p>
          </div>
          <span className="text-xs text-muted-foreground transition-colors group-hover:text-primary">View →</span>
        </div>
      </div>
    </Link>
  )
}

function formatEnum(value: string) {
  return value
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ")
}

function ReputationPill({ icon, label }: { icon: ReactNode; label: string }) {
  return (
    <span className="inline-flex items-center gap-1.5 rounded-full border border-border/50 bg-white/[0.025] px-3 py-1.5 text-xs text-muted-foreground">
      <span className="text-primary">{icon}</span>
      {label}
    </span>
  )
}

function PresenceRow({
  label,
  value,
  accent = "blue",
}: {
  label: string
  value: number
  accent?: "blue" | "violet" | "gold" | "green"
}) {
  const accentClass = {
    blue: "bg-[var(--trust-blue)]",
    violet: "bg-[var(--negotiation-violet)]",
    gold: "bg-[var(--premium-gold)]",
    green: "bg-emerald-400",
  }[accent]

  return (
    <div className="flex items-center justify-between rounded-xl border border-border/40 bg-white/[0.018] px-3.5 py-3">
      <div className="flex items-center gap-2.5 text-sm">
        <span className={`h-2 w-2 rounded-full ${accentClass}`} />
        {label}
      </div>
      <span className="font-semibold">{value.toLocaleString()}</span>
    </div>
  )
}
