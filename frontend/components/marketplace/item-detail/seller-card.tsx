"use client"

import { ShieldCheck, Star } from "lucide-react"

import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { cn } from "@/lib/utils"
import { SellerUI } from "@/lib/types/api/item"
import Link from "next/link"

interface Props {
  seller: SellerUI
}

export function SellerCard({ seller }: Props) {
  const trust = Math.round(seller.trustScore)
  const trustColor = trust >= 900 ? "text-emerald-300" : trust >= 700 ? "text-amber-300" : "text-[var(--trust-blue)]"
  const trustLabel = trust >= 900 ? "Highly trusted" : trust >= 700 ? "Trusted trader" : "Marketplace trader"

  return (
    <div className="content-surface relative overflow-hidden p-6">
      <div className="pointer-events-none absolute -right-20 -top-24 h-44 w-44 rounded-full bg-[var(--trust-blue)]/[0.055] blur-3xl" />
      <div className="pointer-events-none absolute -bottom-24 left-1/4 h-40 w-40 rounded-full bg-[var(--negotiation-violet)]/[0.045] blur-3xl" />

      <div className="relative">
        <div className="mb-4 flex items-center gap-2">
          <span className="h-1.5 w-1.5 rounded-full bg-[var(--trust-blue)] shadow-[0_0_10px_var(--trust-blue)]" />
          <h3 className="text-lg font-semibold">Seller</h3>
          <span className="text-xs text-muted-foreground">Marketplace trader</span>
        </div>

        <div className="flex items-start gap-4">
          <Link href={`/profile/${seller.userId}`}>
          <Avatar className="profile-avatar h-16 w-16 shrink-0 rounded-2xl">
            {seller.avatar}
            <AvatarFallback className="bg-[var(--trust-blue)]/[0.08] text-base font-semibold text-foreground">
              {seller.name?.charAt(0)?.toUpperCase() ?? "S"}
            </AvatarFallback>
          </Avatar>
          </Link>

          <div className="min-w-0 flex-1">
            <div className="flex flex-wrap items-center gap-2">
              <span className="font-semibold">{seller.name}</span>
              <span className={cn("inline-flex items-center gap-1 rounded-full border border-border/50 bg-white/[0.025] px-2 py-1 text-[11px] font-medium", trustColor)}>
                <ShieldCheck className="h-3.5 w-3.5" />
                {trustLabel}
              </span>
            </div>

            <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-2 text-sm text-muted-foreground">
              <span className="inline-flex items-center gap-1.5">
                <Star className="h-3.5 w-3.5 fill-[var(--premium-gold)] text-[var(--premium-gold)]" />
                <span className="text-foreground/85">{trust}%</span> Trust
              </span>

              <span>
                {seller.successfulTrades} successful trades
              </span>

              <span>
                {seller.totalListings} active listings
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
