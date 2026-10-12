"use client"

import { useState, type FormEvent } from "react"
import Link from "next/link"
import { usePathname, useRouter } from "next/navigation"
import { ArrowLeftRight, Check, Send, Share2, X } from "lucide-react"

import { WishlistButton } from "@/components/marketplace/wishlist-button"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { createBuyOffer } from "@/lib/api/trades/client"
import type { ItemResponse } from "@/lib/types/api/item"
import { cn } from "@/lib/utils"

interface Props {
  item: ItemResponse
  userId: number | null
}

const formatINR = (value: number | null | undefined) =>
  value == null
    ? "Price unavailable"
    : new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency: "INR",
        maximumFractionDigits: 0,
      }).format(value)

export function ActionButtons({ item, userId }: Props) {
  const router = useRouter()
  const pathname = usePathname()
  const [shareStatus, setShareStatus] = useState<string | null>(null)
  const [shareBusy, setShareBusy] = useState(false)
  const [buyDialogOpen, setBuyDialogOpen] = useState(false)
  const [offeredPrice, setOfferedPrice] = useState(String(item.userPrice ?? ""))
  const [initialMessage, setInitialMessage] = useState("")
  const [buyBusy, setBuyBusy] = useState(false)
  const [buyError, setBuyError] = useState<string | null>(null)

  const sellerId = item.seller?.userId
  const isOwner = userId != null && userId === sellerId
  

  function openBuyOffer() {
    if (userId == null) {
      router.push(`/login?redirect=${encodeURIComponent(pathname || `/item/${item.itemId}`)}`)
      return
    }

    setOfferedPrice(String(item.userPrice ?? ""))
    setInitialMessage("")
    setBuyError(null)
    setBuyDialogOpen(true)
  }

  async function handleSubmitBuyOffer(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setBuyError(null)

    const numericPrice = Number(offeredPrice)
    if (!Number.isFinite(numericPrice) || numericPrice <= 0) {
      setBuyError("Enter a valid offer price greater than ₹0.")
      return
    }

    if (initialMessage.trim().length > 2000) {
      setBuyError("The message cannot exceed 2,000 characters.")
      return
    }

    setBuyBusy(true)
    try {
      await createBuyOffer({
        itemId: item.itemId,
        offeredPrice: numericPrice,
        ...(initialMessage.trim() ? { initialMessage: initialMessage.trim() } : {}),
      })
      setBuyDialogOpen(false)
      router.push("/trades")
    } catch (error) {
      setBuyError(getApiErrorMessage(error))
    } finally {
      setBuyBusy(false)
    }
  }

  async function handleShare() {
    setShareBusy(true)
    setShareStatus(null)
    try {
      const url = window.location.href
      if (typeof navigator.share === "function") {
        await navigator.share({ title: item.itemName, url })
        setShareStatus("Share opened.")
      } else if (navigator.clipboard?.writeText) {
        await navigator.clipboard.writeText(url)
        setShareStatus("Item link copied.")
      } else {
        setShareStatus("Sharing is not supported in this browser.")
      }
    } catch (error) {
      // Closing the native share sheet is not an error worth surfacing.
      if (!(error instanceof Error && error.name === "AbortError")) {
        try {
          await navigator.clipboard.writeText(window.location.href)
          setShareStatus("Item link copied.")
        } catch {
          setShareStatus("Could not share this item. Copy the page URL from your browser.")
        }
      }
    } finally {
      setShareBusy(false)
    }
  }
  

  return (
    <>
      <section className="content-surface !p-4 sm:!p-5">
        <div className="mb-4">
          <p className="text-[10px] font-semibold uppercase tracking-[0.16em] text-muted-foreground">Item actions</p>
          <h2 className="mt-1 text-base font-semibold">Interested in this listing?</h2>
          <p className="mt-1 text-xs leading-5 text-muted-foreground">
            Send a buy offer or propose a trade. The seller receives your offer and message.
          </p>
        </div>

        <div className="flex flex-col gap-2.5">
          {isOwner ? (
            <div className="rounded-xl border border-dashed border-border/70 bg-muted/15 px-4 py-3 text-center text-sm text-muted-foreground">
              You’re viewing your own listing.
            </div>
          ) : !item.available ? (
            <div className="rounded-xl border border-border/60 bg-muted/15 px-4 py-3 text-center text-sm text-muted-foreground">
              This listing is currently unavailable for new offers.
            </div>
          ) : (
            <>
              {item.forSale && sellerId != null && (
                <Button
                  type="button"
                  onClick={openBuyOffer}
                  className={cn(
                    "cta-primary h-12 w-full justify-between rounded-xl px-4 text-sm font-semibold",
                    "shadow-none transition-all duration-200 hover:-translate-y-0.5 hover:shadow-lg",
                  )}
                >
                  <span className="inline-flex items-center gap-2.5">
                    <Send className="h-4 w-4" />
                    Make buy offer
                  </span>
                  <span className="rounded-lg border border-white/15 bg-black/10 px-2.5 py-1 text-xs font-bold">
                    Asking {formatINR(item.userPrice)}
                  </span>
                </Button>
              )}

              {item.forTrade && sellerId != null && (
                <Button
                  asChild
                  variant="trade"
                  className={cn(
                    "h-12 w-full justify-center rounded-xl border border-[var(--trust-blue)]/25",
                    "bg-[var(--trust-blue)]/[0.07] text-sm font-semibold text-foreground shadow-none",
                    "transition-all duration-200 hover:-translate-y-0.5 hover:border-[var(--trust-blue)]/45",
                    "hover:bg-[var(--trust-blue)]/[0.12]",
                  )}
                >
                  <Link
                    href={{
                      pathname: `/trades/create/${sellerId}`,
                      query: { itemId: item.itemId.toString() },
                    }}
                  >
                    <ArrowLeftRight className="mr-2 h-4 w-4 text-[var(--trust-blue)]" />
                    Make trade offer
                  </Link>
                </Button>
              )}

              <WishlistButton itemId={item.itemId} label className="h-11 w-full" />
            </>
          )}

          <Button
            type="button"
            variant="surface"
            onClick={() => void handleShare()}
            disabled={shareBusy}
            className={cn(
              "h-11 w-full rounded-xl border border-border/60 bg-white/[0.025] text-sm font-semibold",
              "text-muted-foreground transition-colors hover:border-[var(--negotiation-violet)]/30",
              "hover:bg-[var(--negotiation-violet)]/[0.06] hover:text-foreground",
            )}
          >
            {shareStatus === "Item link copied." ? <Check className="mr-2 h-4 w-4 text-emerald-400" /> : <Share2 className="mr-2 h-4 w-4" />}
            {shareBusy ? "Preparing share…" : shareStatus === "Item link copied." ? "Link copied" : "Share item"}
          </Button>

          {shareStatus && (
            <p
              aria-live="polite"
              className={cn(
                "text-center text-xs",
                shareStatus.startsWith("Could not") || shareStatus.startsWith("Sharing is")
                  ? "text-destructive"
                  : "text-muted-foreground",
              )}
            >
              {shareStatus}
            </p>
          )}
        </div>
      </section>

      {buyDialogOpen && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4">
          <button
            type="button"
            aria-label="Close buy offer dialog"
            disabled={buyBusy}
            onClick={() => setBuyDialogOpen(false)}
            className="absolute inset-0 cursor-default bg-black/70 backdrop-blur-sm"
          />
          <div
            role="dialog"
            aria-modal="true"
            aria-labelledby="buy-offer-title"
            className="relative z-10 max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-2xl border border-border/70 bg-card p-5 shadow-2xl sm:p-6"
          >
            <div className="mb-5 flex items-start justify-between gap-4">
              <div>
                <p className="text-[10px] font-semibold uppercase tracking-[0.16em] text-muted-foreground">Buy offer</p>
                <h2 id="buy-offer-title" className="mt-1 text-lg font-semibold">Make an offer for {item.itemName}</h2>
                <p className="mt-1 text-sm text-muted-foreground">Asking price: {formatINR(item.userPrice)}</p>
              </div>
              <Button
                type="button"
                variant="ghost"
                size="icon"
                aria-label="Close buy offer dialog"
                disabled={buyBusy}
                onClick={() => setBuyDialogOpen(false)}
                className="shrink-0 hover:!bg-[var(--negotiation-violet)]/[0.09]"
              >
                <X className="h-4 w-4" />
              </Button>
            </div>

            <form className="space-y-4" onSubmit={(event) => void handleSubmitBuyOffer(event)}>
              <div>
                <label htmlFor="buy-offer-price" className="mb-2 block text-sm font-medium">Your offer (INR)</label>
                <Input
                  id="buy-offer-price"
                  name="offeredPrice"
                  type="number"
                  min="0.01"
                  step="0.01"
                  required
                  autoFocus
                  value={offeredPrice}
                  onChange={(event) => setOfferedPrice(event.target.value)}
                  disabled={buyBusy}
                  className="h-11"
                />
              </div>

              <div>
                <label htmlFor="buy-offer-message" className="mb-2 block text-sm font-medium">Message to seller <span className="font-normal text-muted-foreground">(optional)</span></label>
                <textarea
                  id="buy-offer-message"
                  name="initialMessage"
                  rows={3}
                  maxLength={2000}
                  value={initialMessage}
                  onChange={(event) => setInitialMessage(event.target.value)}
                  disabled={buyBusy}
                  placeholder="Add a note for the seller. Leave blank to use the default message."
                  className="w-full resize-y rounded-xl border border-border/70 bg-background/55 px-3 py-2.5 text-sm leading-6 text-foreground outline-none transition focus-visible:ring-2 focus-visible:ring-[var(--negotiation-violet)]/50 disabled:opacity-60"
                />
                <p className="mt-1 text-right text-[11px] text-muted-foreground">{initialMessage.length}/2,000</p>
              </div>

              {buyError && (
                <p role="alert" className="rounded-xl border border-destructive/25 bg-destructive/[0.06] px-3 py-2.5 text-sm text-destructive">
                  {buyError}
                </p>
              )}

              <div className="flex flex-col-reverse gap-2 pt-1 sm:flex-row sm:justify-end">
                <Button
                  type="button"
                  variant="ghost"
                  disabled={buyBusy}
                  onClick={() => setBuyDialogOpen(false)}
                  className="h-11 rounded-xl hover:!bg-white/[0.05]"
                >
                  Cancel
                </Button>
                <Button
                  type="submit"
                  disabled={buyBusy}
                  className="cta-primary h-11 rounded-xl px-5 font-semibold"
                >
                  {buyBusy ? "Sending offer…" : "Send buy offer"}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </>
  )
}

function getApiErrorMessage(error: unknown): string {
  if (error && typeof error === "object" && "response" in error) {
    const response = (error as { response?: { data?: unknown } }).response
    const data = response?.data
    if (data && typeof data === "object") {
      const message = (data as { message?: unknown; error?: unknown }).message
      const errorText = (data as { message?: unknown; error?: unknown }).error
      if (typeof message === "string" && message.trim()) return message
      if (typeof errorText === "string" && errorText.trim()) return errorText
    }
  }

  return error instanceof Error
    ? error.message
    : "The buy offer could not be sent. Please try again."
}
