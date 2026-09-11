"use client"

import Link from "next/link"
import type { ReactNode } from "react"
import { motion } from "framer-motion"
import {
  ArrowLeftRight,
  BadgeCheck,
  CheckCircle2,
  Edit3,
  Mail,
  MessageCircle,
  Package,
  ShieldCheck,
  Sparkles,
} from "lucide-react"

import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { MyUserProfile, PublicUserProfile } from "@/lib/types/api/profile"
import { cn } from "@/lib/utils"

interface Props {
  profile: MyUserProfile | PublicUserProfile
  isOwner: boolean
  onEdit?: () => void
}

export function ProfileHeader({ profile, isOwner, onEdit }: Props) {
  const initials = profile.name
    .split(" ")
    .filter(Boolean)
    .map((part) => part[0])
    .join("")
    .slice(0, 2)
    .toUpperCase()

  const trust = Math.max(0, Math.min(100, Math.round(profile.trustScore)))
  const trustLabel = trust >= 90 ? "Highly trusted" : trust >= 70 ? "Trusted trader" : trust >= 50 ? "Established trader" : "New trader"
  const isVerified = trust >= 90 && profile.successfulTrades >= 10

  return (
    <motion.section
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      className="trade-card overflow-hidden rounded-[30px]"
    >
      <div className="trade-card-accent" />

      <div className="relative overflow-hidden p-5 sm:p-7">
        <div className="pointer-events-none absolute -right-20 -top-28 h-72 w-72 rounded-full bg-[var(--negotiation-violet)]/10 blur-3xl" />
        <div className="pointer-events-none absolute -bottom-28 left-1/4 h-64 w-64 rounded-full bg-[var(--trust-blue)]/8 blur-3xl" />

        <div className="relative flex flex-col gap-6 xl:flex-row xl:items-start xl:justify-between">
          <div className="flex min-w-0 flex-1 gap-4 sm:gap-5">
            <Avatar className="profile-avatar h-20 w-20 shrink-0 rounded-[22px] sm:h-24 sm:w-24 sm:rounded-[26px]">
              {profile.avatarUrl && <AvatarImage src={profile.avatarUrl} alt={profile.name} />}
              <AvatarFallback className="bg-gradient-to-br from-[var(--trust-blue)]/20 via-[var(--negotiation-violet)]/15 to-[var(--premium-gold)]/10 text-2xl font-bold">
                {initials}
              </AvatarFallback>
            </Avatar>

            <div className="min-w-0 pt-0.5">
              <div className="flex flex-wrap items-center gap-2">
                <h1 className="text-2xl font-bold tracking-tight sm:text-3xl">{profile.name}</h1>
                {isVerified && (
                  <Badge className="border-none bg-[var(--trust-blue)]/10 text-[var(--trust-blue)]">
                    <BadgeCheck className="mr-1 h-3.5 w-3.5" />
                    Trusted
                  </Badge>
                )}
              </div>

              <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-2 text-sm text-muted-foreground">
                <span className="inline-flex items-center gap-1.5">
                  <ShieldCheck className={cn("h-4 w-4", trust >= 90 ? "text-emerald-400" : trust >= 70 ? "text-amber-400" : "text-muted-foreground")} />
                  {trustLabel}
                </span>

                <span className="inline-flex items-center gap-1.5">
                  <ArrowLeftRight className="h-3.5 w-3.5 text-[var(--negotiation-violet)]" />
                  {profile.successfulTrades} successful trades
                </span>

                <span className="inline-flex items-center gap-1.5">
                  <Package className="h-3.5 w-3.5 text-[var(--premium-gold)]" />
                  {profile.totalListings} total listings
                </span>
              </div>

              {isOwner && (
                <div className="mt-3 flex flex-wrap gap-x-4 gap-y-2 text-sm text-muted-foreground">
                  <span className="inline-flex items-center gap-1.5">
                    <Mail className="h-3.5 w-3.5" />
                    {profile.email}
                  </span>
                  {profile.phoneNo && <span>{profile.phoneNo}</span>}
                </div>
              )}

              <div className="mt-4 flex flex-wrap gap-2">
                <Badge variant="outline" className="border-border/60 bg-white/[0.02]">
                  <Sparkles className="mr-1 h-3.5 w-3.5 text-primary" />
                  Marketplace member
                </Badge>
                {profile.totalListings > 0 && (
                  <Badge variant="outline" className="border-border/60 bg-white/[0.02]">
                    Active seller
                  </Badge>
                )}
              </div>
            </div>
          </div>

          <div className="flex shrink-0 flex-wrap gap-2 xl:justify-end">
            {isOwner ? (
              <Button type="button" variant="surface" className="rounded-xl" onClick={onEdit}>
                <Edit3 className="mr-2 h-4 w-4" />
                Edit Profile
              </Button>
            ) : (
              <Button asChild className="cta-primary rounded-xl">
                <Link href={`/chat/${profile.id}`}>
                  <MessageCircle className="mr-2 h-4 w-4" />
                  Message
                </Link>
              </Button>
            )}
          </div>
        </div>

        <div className="relative mt-6 grid grid-cols-2 overflow-hidden rounded-2xl border border-border/50 bg-white/[0.02] sm:grid-cols-4">
          <ProfileMetric
            label="Trust Score"
            value={`${trust}%`}
            icon={<ShieldCheck className="h-4 w-4" />}
            tone="trust"
            showBar
            score={trust}
          />
          <ProfileMetric label="Successful Trades" value={profile.successfulTrades.toLocaleString()} icon={<CheckCircle2 className="h-4 w-4" />} tone="success" />
          <ProfileMetric label="Total Listings" value={profile.totalListings.toLocaleString()} icon={<Package className="h-4 w-4" />} tone="gold" />
          <ProfileMetric label="Reputation" value={trustLabel} icon={<Sparkles className="h-4 w-4" />} tone="violet" />
        </div>
      </div>
    </motion.section>
  )
}

function ProfileMetric({
  label,
  value,
  icon,
  tone,
  showBar,
  score,
}: {
  label: string
  value: string
  icon: ReactNode
  tone: "trust" | "success" | "gold" | "violet"
  showBar?: boolean
  score?: number
}) {
  const toneClass = {
    trust: "text-[var(--trust-blue)]",
    success: "text-emerald-400",
    gold: "text-[var(--premium-gold)]",
    violet: "text-[var(--negotiation-violet)]",
  }[tone]

  return (
    <div className="relative border-b border-border/40 px-4 py-4 sm:border-b-0 sm:border-r last:border-0">
      <div className="flex items-center gap-2 text-xs text-muted-foreground">
        <span className={toneClass}>{icon}</span>
        {label}
      </div>
      <div className="mt-1.5 text-lg font-bold sm:text-xl">{value}</div>
      {showBar && score !== undefined && (
        <div className="mt-2 h-1 overflow-hidden rounded-full bg-white/[0.06]">
          <div
            className="h-full rounded-full bg-gradient-to-r from-[var(--trust-blue)] via-[var(--negotiation-violet)] to-[var(--premium-gold)]"
            style={{ width: `${score}%` }}
          />
        </div>
      )}
    </div>
  )
}
