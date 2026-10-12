"use client"

import { motion } from "framer-motion"
import { Heart, Loader2 } from "lucide-react"
import { useEffect, useState } from "react"
import { usePathname, useRouter } from "next/navigation"

import { Button } from "@/components/ui/button"
import {
  loadWishlistIds,
  setWishlistSaved,
  subscribeWishlist,
} from "@/lib/api/wishlist/store"
import { cn } from "@/lib/utils"

interface Props {
  itemId: number
  className?: string
  /** Optional initial state while the shared persisted wishlist cache loads. */
  initialSaved?: boolean
  /** Renders as an overlay in the top-right corner of a product image. */
  overlay?: boolean
  /** Renders a full-width, text-labelled control for item-detail actions. */
  label?: boolean
}

/** Persistent wishlist button shared by marketplace cards and item-detail actions. */
export function WishlistButton({
  itemId,
  className,
  initialSaved,
  overlay = false,
  label = false,
}: Props) {
  const pathname = usePathname()
  const router = useRouter()
  const [loginRequired, setLoginRequired] = useState(false)
  const [saved, setSaved] = useState<boolean>(initialSaved ?? false)
  const [checking, setChecking] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let active = true

    async function refreshState() {
      setChecking(true)
      try {
        const ids = await loadWishlistIds()
        if (active) {
          setSaved(ids.has(itemId))
          setError(null)
          setLoginRequired(false)
        }
      } catch (loadError: unknown) {
        if (active) {
          const authError = isAuthenticationError(loadError)
          setLoginRequired(authError)
          // Don't show an authentication warning under every public card.
          setError(authError ? null : "Wishlist state could not be loaded.")
        }
      } finally {
        if (active) setChecking(false)
      }
    }

    const unsubscribe = subscribeWishlist((ids, reset) => {
      if (active) setSaved(ids.has(itemId))
      if (active && reset) void refreshState()
    })

    void refreshState()
    return () => {
      active = false
      unsubscribe()
    }
  }, [itemId])

  async function toggle() {
    if (busy || checking) return

    setBusy(true)
    setError(null)
    setLoginRequired(false)
    try {
      await setWishlistSaved(itemId, !saved)
      // The shared store publishes canonical state to every mounted heart.
    } catch (actionError: unknown) {
      setError(actionError instanceof Error ? actionError.message : "Wishlist could not be updated.")
      setLoginRequired(isAuthenticationError(actionError))
    } finally {
      setBusy(false)
    }
  }

  return (
    <span
      className={cn(
        "inline-flex min-w-0 flex-col gap-1",
        label
          ? "w-full items-stretch"
          : overlay
            ? "absolute right-3 top-3 z-20 items-end"
            : "items-start",
      )}
    >
      <Button
        type="button"
        variant="ghost"
        size={label ? "default" : "icon-lg"}
        aria-label={saved ? "Remove from wishlist" : "Add to wishlist"}
        aria-pressed={saved}
        title={saved ? "Remove from wishlist" : "Add to wishlist"}
        disabled={busy || checking}
        onClick={(event) => {
          // A product card may be nested in a Link. Keep this action independent.
          event.preventDefault()
          event.stopPropagation()
          void toggle()
        }}
        className={cn(
          "group/wishlist relative shrink-0 rounded-xl border transition-all duration-200",
          "focus-visible:ring-2 focus-visible:ring-[var(--negotiation-violet)]/60",
          overlay && [
            "h-10 w-10 border-white/15 bg-background/70 text-foreground",
            "shadow-lg backdrop-blur-xl icon-glow icon-glow-violet",
            "hover:!border-[var(--negotiation-violet)]/45 hover:!bg-background/85 hover:!text-foreground",
          ].join(" "),
          !overlay && !label && [
            "icon-glow icon-glow-violet",
            "hover:!bg-transparent hover:!text-[var(--negotiation-violet)]",
          ].join(" "),
          label && [
            "h-11 w-full justify-center gap-2 border-[var(--negotiation-violet)]/25",
            "bg-[var(--negotiation-violet)]/[0.06] px-4 text-sm font-semibold text-foreground",
            "hover:!border-[var(--negotiation-violet)]/45 hover:!bg-[var(--negotiation-violet)]/[0.12] hover:!text-foreground",
          ].join(" "),
          saved && [
            "!border-rose-400/45 !bg-rose-500/15 !text-rose-400",
            "hover:!border-rose-400/60 hover:!bg-rose-500/20 hover:!text-rose-300",
          ].join(" "),
          className,
        )}
      >
        {busy ? (
          <Loader2 className="h-[19px] w-[19px] animate-spin" />
        ) : (
          <motion.span
            key={saved ? "saved" : "unsaved"}
            initial={{ scale: 0.72, rotate: saved ? -10 : 0 }}
            animate={{ scale: 1, rotate: 0 }}
            transition={{ type: "spring", stiffness: 480, damping: 22 }}
            className="inline-flex items-center justify-center"
          >
            <Heart
              className={cn(
                "h-[21px] w-[21px] transition-colors duration-200",
                saved
                  ? "fill-rose-500 stroke-rose-500 drop-shadow-[0_0_7px_rgba(244,63,94,0.28)]"
                  : "fill-transparent stroke-current group-hover/wishlist:!text-[var(--negotiation-violet)]",
              )}
            />
          </motion.span>
        )}
        {label && <span>{saved ? "Saved to wishlist" : "Add to wishlist"}</span>}
      </Button>

      {error && (
        <span
          role="alert"
          className={cn(
            "max-w-56 rounded-lg border border-destructive/25 bg-background/95 px-2.5 py-1.5 text-xs text-destructive shadow-md backdrop-blur",
            overlay && "text-right",
          )}
        >
          {error}{" "}
          {loginRequired && (
            <button
              type="button"
              className="font-semibold underline underline-offset-2"
              onClick={(event) => {
                event.preventDefault()
                event.stopPropagation()
                router.push(`/login?redirect=${encodeURIComponent(pathname || "/marketplace")}`)
              }}
            >
              Sign in
            </button>
          )}
        </span>
      )}
    </span>
  )
}

function isAuthenticationError(error: unknown): boolean {
  if (!error || typeof error !== "object" || !("response" in error)) return false
  const response = (error as { response?: { status?: unknown } }).response
  return response?.status === 401 || response?.status === 403
}
