"use client"

import { motion } from "framer-motion"
import { ArrowLeftRight, Copy, MessageCircle, Package, Share2, ShieldCheck } from "lucide-react"
import Link from "next/link"
import * as React from "react"

import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
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

  const trust =profile.trustScore
  const trustLabel = trust >= 900 ? "Highly trusted" : trust >= 700 ? "Trusted trader" : trust >= 500 ? "Established trader" : "New trader"

  const [copied, setCopied] = React.useState(false)

  const shareProfile = async () => {
    const url = window.location.href

    try {
      if (navigator.share) {
        await navigator.share({ title: `${profile.name} on SmartTrade`, url })
        return
      }

      await navigator.clipboard.writeText(url)
      setCopied(true)
      window.setTimeout(() => setCopied(false), 1600)
    } catch {
      // User cancelled sharing or the browser blocked clipboard access.
    }
  }

  return (
    <motion.section
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      className="rounded-3xl border border-border/60 bg-card/55 p-5 backdrop-blur-xl sm:p-6 lg:p-6"
    >
      <div className="flex items-center justify-between gap-5">
        <div className="flex min-w-0 items-center gap-4 sm:gap-5">
          <Avatar className="profile-avatar h-20 w-20 shrink-0 rounded-[22px] sm:h-24 sm:w-24 sm:rounded-[26px]">
            {profile.avatarUrl && <AvatarImage src={profile.avatarUrl} alt={profile.name} />}
            <AvatarFallback className="bg-[var(--trust-blue)]/8 text-2xl font-bold text-[var(--trust-blue)]">{initials}</AvatarFallback>
          </Avatar>

          <div className="min-w-0">
            <h1 className="truncate text-2xl font-bold tracking-tight sm:text-3xl">{profile.name}</h1>

            <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-2 text-sm text-muted-foreground">
              <span className="inline-flex items-center gap-1.5">
                <ShieldCheck
                  className={cn("h-4 w-4", trust >= 90 ? "text-emerald-400" : trust >= 70 ? "text-amber-400" : "text-muted-foreground")}
                />
                {trustLabel}
              </span>
              <span className="inline-flex items-center gap-1.5">
                <ArrowLeftRight className="h-3.5 w-3.5 text-[var(--negotiation-violet)]" />
                {profile.successfulTrades.toLocaleString()} successful trades
              </span>
              <span className="inline-flex items-center gap-1.5">
                <Package className="h-3.5 w-3.5 text-[var(--premium-gold)]" />
                {profile.totalListings.toLocaleString()} listings
              </span>
            </div>

            <div className="mt-3 flex flex-wrap items-center gap-2 text-xs">
              <span className="rounded-full border border-border/60 bg-white/[0.025] px-3 py-1.5 text-muted-foreground">
                Marketplace member
              </span>
              {profile.totalListings > 0 && (
                <span className="rounded-full border border-border/60 bg-white/[0.025] px-3 py-1.5 text-muted-foreground">
                  Active seller
                </span>
              )}
            </div>
          </div>
        </div>

        <div className="flex shrink-0 items-center gap-2">
          {isOwner ? (
            <Button asChild className="cta-primary">
              <Link href="/profile/edit">Edit profile</Link>
            </Button>
          ) : (
            <>
              <Button type="button" variant="surface" className="rounded-xl" onClick={shareProfile}>
                {copied ? <Copy className="mr-2 h-4 w-4" /> : <Share2 className="mr-2 h-4 w-4" />}
                {copied ? "Copied" : "Share"}
              </Button>
              <Button asChild className="cta-primary rounded-xl">
                <Link href={`/messages/${profile.id}`}>
                  <MessageCircle className="mr-2 h-4 w-4" />
                  Message
                </Link>
              </Button>
            </>
          )}
        </div>
      </div>
    </motion.section>
  )
}
