"use client"

import { motion } from "framer-motion"
import { ArrowRight, ArrowUpRight, CheckCircle2, MessageCircle, Package, ShieldCheck, Star } from "lucide-react"
import Image from "next/image"
import Link from "next/link"
import type { ReactNode } from "react"

import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { ItemResponse } from "@/lib/types/api/item"
import { MyUserProfile, PublicUserProfile } from "@/lib/types/api/profile"

import { getTrustScoreBand, toTrustRating } from "@/components/trust-score"
import { ProfileTab } from "../profile-sidebar"

interface Props {
  profile: MyUserProfile | PublicUserProfile
  items: ItemResponse[]
  isOwner: boolean
  onNavigate(tab: ProfileTab): void
}

export function ProfileOverview({ profile, items, isOwner, onNavigate }: Props) {
  const availableItems = items.filter((item) => item.available)
  const tradeableItems = availableItems.filter((item) => item.forTrade)
  const saleItems = availableItems.filter((item) => item.forSale)
  const featuredItems = availableItems.slice(0, 3)

  const trust = toTrustRating(profile.trustScore) ?? 50
  const trustLabel = getTrustScoreBand(trust).label

  return (
    <div className="space-y-5">
      <section className="grid gap-5 xl:grid-cols-[minmax(0,1.2fr)_minmax(300px,.8fr)]">
        <div className="rounded-2xl border border-border/60 bg-card/50 p-5 backdrop-blur-xl sm:p-6">
          <SectionEyebrow icon={<ShieldCheck className="h-3.5 w-3.5" />} text="Marketplace reputation" />

          <div className="mt-5 flex flex-col gap-5 sm:flex-row sm:items-center">
            <TrustGauge value={trust} />

            <div className="min-w-0">
              <h2 className="text-xl font-bold tracking-tight">{trustLabel}</h2>
              <p className="mt-2 max-w-xl text-sm leading-6 text-muted-foreground">
                Reputation reflects marketplace activity and successful transactions. Use reviews and trade history together when evaluating
                a counterparty.
              </p>

              <div className="mt-4 flex flex-wrap gap-2">
                <ReputationPill icon={<ShieldCheck />} label={`${trust}% trust`} />
                <ReputationPill icon={<CheckCircle2 />} label={`${profile.successfulTrades} completed`} />
                <ReputationPill icon={<Package />} label={`${profile.totalListings} listings`} />
              </div>

              <Button type="button" variant="surface" className="mt-4 rounded-xl" onClick={() => onNavigate("reviews")}>
                <Star className="mr-2 h-4 w-4" />
                View reviews
              </Button>
            </div>
          </div>
        </div>

        <div className="rounded-2xl border border-border/60 bg-card/50 p-5 backdrop-blur-xl sm:p-6">
          <SectionEyebrow icon={<Package className="h-3.5 w-3.5" />} text="Marketplace presence" />
          <h2 className="mt-2 text-xl font-bold">At a glance</h2>

          <div className="mt-5 space-y-2">
            <PresenceRow label="Active listings" value={availableItems.length} />
            <PresenceRow label="Available for trade" value={tradeableItems.length} accent="violet" />
            <PresenceRow label="Available for sale" value={saleItems.length} accent="gold" />
            <PresenceRow label="Successful trades" value={profile.successfulTrades} accent="green" />
          </div>

          <Button type="button" variant="surface" className="mt-5 w-full rounded-xl" onClick={() => onNavigate("inventory")}>
            Explore inventory
            <ArrowRight className="ml-2 h-4 w-4" />
          </Button>
        </div>
      </section>

      <section className="overflow-hidden rounded-2xl border border-border/60 bg-card/50 backdrop-blur-xl">
        <div className="flex flex-col gap-4 border-b border-border/50 px-5 py-5 sm:flex-row sm:items-end sm:justify-between sm:px-6">
          <div>
            <SectionEyebrow icon={<Package className="h-3.5 w-3.5" />} text="Marketplace" />
            <h2 className="mt-2 text-xl font-bold">Featured listings</h2>
            <p className="mt-1 text-sm text-muted-foreground">A snapshot of what this trader currently has available.</p>
          </div>

          <Button type="button" variant="surface" className="rounded-xl" onClick={() => onNavigate("inventory")}>
            View all
            <ArrowRight className="ml-2 h-4 w-4" />
          </Button>
        </div>

        <div className="p-4 sm:p-5">
          {featuredItems.length > 0 ? (
            <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
              {featuredItems.map((item, index) => (
                <motion.div
                  key={item.itemId}
                  initial={{ opacity: 0, y: 6 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: index * 0.04 }}
                >
                  <FeaturedItemCard item={item} />
                </motion.div>
              ))}
            </div>
          ) : (
            <div className="px-4 py-14 text-center">
              <Package className="mx-auto h-8 w-8 text-muted-foreground" />
              <h3 className="mt-4 font-semibold">No active listings</h3>
              <p className="mx-auto mt-1 max-w-md text-sm leading-6 text-muted-foreground">
                There is nothing currently available in this trader&apos;s marketplace inventory.
              </p>
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

      {!isOwner && (
        <section className="flex flex-col gap-4 rounded-2xl border border-border/60 bg-card/50 px-5 py-5 backdrop-blur-xl sm:flex-row sm:items-center sm:justify-between sm:px-6">
          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.15em] text-muted-foreground">Ready to connect?</p>
            <h2 className="mt-1 text-lg font-semibold">Message this trader about a listing.</h2>
          </div>
          <Button asChild className="cta-primary rounded-xl">
            <Link href={`/chat/${profile.id}`}>
              <MessageCircle className="mr-2 h-4 w-4" />
              Start conversation
            </Link>
          </Button>
        </section>
      )}
    </div>
  )
}

function TrustGauge({ value }: { value: number }) {
  return (
    <div
      className="relative flex h-28 w-28 shrink-0 items-center justify-center rounded-full"
      style={{
        background: `conic-gradient(from -90deg, var(--trust-blue) ${value * 3.6}deg, rgba(255,255,255,.07) 0deg)`,
      }}
    >
      <div className="absolute inset-[5px] rounded-full bg-background" />
      <div className="relative text-center">
        <p className="text-2xl font-bold">{Math.round(value)}%</p>
        <p className="text-[9px] font-semibold uppercase tracking-[0.14em] text-muted-foreground">Trust</p>
      </div>
    </div>
  )
}

function SectionEyebrow({ icon, text }: { icon: ReactNode; text: string }) {
  return (
    <div className="flex items-center gap-2 text-[11px] font-semibold uppercase tracking-[0.16em] text-muted-foreground">
      <span className="text-[var(--trust-blue)]">{icon}</span>
      {text}
    </div>
  )
}

function ReputationPill({ icon, label }: { icon: ReactNode; label: string }) {
  return (
    <span className="inline-flex items-center gap-1.5 rounded-full border border-border/50 bg-white/[0.025] px-3 py-1.5 text-xs text-muted-foreground">
      <span className="text-[var(--trust-blue)]">{icon}</span>
      {label}
    </span>
  )
}

function PresenceRow({ label, value, accent = "blue" }: { label: string; value: number; accent?: "blue" | "violet" | "gold" | "green" }) {
  const accentClass = {
    blue: "bg-[var(--trust-blue)]",
    violet: "bg-[var(--negotiation-violet)]",
    gold: "bg-[var(--premium-gold)]",
    green: "bg-emerald-400",
  }[accent]

  return (
    <div className="flex items-center justify-between rounded-xl border border-border/45 bg-white/[0.015] px-3.5 py-3">
      <div className="flex items-center gap-2.5 text-sm">
        <span className={`h-1.5 w-1.5 rounded-full ${accentClass}`} />
        {label}
      </div>
      <span className="font-semibold">{value.toLocaleString()}</span>
    </div>
  )
}

function FeaturedItemCard({ item }: { item: ItemResponse }) {
  const image = item.images?.[0]?.secureUrl ?? "/placeholder.jpg"
  const listingType = item.forTrade && item.forSale ? "Trade / Sale" : item.forTrade ? "Trade Only" : "For Sale"

  return (
    <Link
      href={`/item/${item.itemId}`}
      className="group block overflow-hidden rounded-2xl border border-border/55 bg-background/20 transition hover:-translate-y-0.5 hover:border-[var(--trust-blue)]/25"
    >
      <div className="relative aspect-[4/3] overflow-hidden bg-muted/30">
        <Image src={image} alt={item.itemName} fill className="object-cover transition-transform duration-500 group-hover:scale-[1.035]" />
        <div className="absolute inset-0 bg-gradient-to-t from-background/75 via-transparent to-transparent" />
        <Badge className="absolute left-3 top-3 border-none bg-background/78 backdrop-blur-md">{listingType}</Badge>
      </div>

      <div className="p-4">
        <h3 className="line-clamp-1 font-semibold transition-colors group-hover:text-primary">{item.itemName}</h3>
        <p className="mt-1 text-xs text-muted-foreground">
          {formatEnum(item.condition)} · {formatEnum(item.category)}
        </p>
        <div className="mt-4 flex items-end justify-between gap-3">
          <div>
            <p className="text-[10px] uppercase tracking-wider text-muted-foreground">Listed value</p>
            <p className="mt-0.5 text-lg font-bold">₹{item.userPrice.toLocaleString("en-IN")}</p>
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
