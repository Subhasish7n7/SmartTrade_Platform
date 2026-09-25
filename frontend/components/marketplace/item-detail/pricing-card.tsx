"use client"
import { AlertTriangle, CheckCircle2, Clock3, RefreshCw, TrendingDown, TrendingUp } from "lucide-react"
import { useEffect, useMemo, useRef, useState } from "react"

import { Button } from "@/components/ui/button"
import { getItemById, refreshItemPrice } from "@/lib/api/items/client"
import type { ItemResponse } from "@/lib/types/api/item"

import { DealRating } from "./deal-rating"

interface Props {
  item: ItemResponse
}

const PRICE_POLL_TIMEOUT_MS = 30_000

const formatPrice = (price: number | null | undefined) => (price == null ? "N/A" : `₹${price.toLocaleString("en-IN")}`)

function formatDate(dateString: string) {
  return new Intl.DateTimeFormat("en-IN", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(dateString))
}

export function PricingCard({ item: initialItem }: Props) {
  const [item, setItem] = useState<ItemResponse>(initialItem)

  const [requestingRefresh, setRequestingRefresh] = useState(false)

  const [requestError, setRequestError] = useState<string | null>(null)

  const [pollNotice, setPollNotice] = useState<string | null>(null)

  const pollCancelledRef = useRef(false)

  useEffect(() => {
    setItem(initialItem)
  }, [initialItem])

  useEffect(() => {
    return () => {
      pollCancelledRef.current = true
    }
  }, [])

  const marketPrice = item.newPrice
  const listedPrice = item.userPrice
  const generatedPrice = item.generatedPrice

  const marketDifference = useMemo(() => {
    if (marketPrice == null || marketPrice <= 0 || listedPrice == null) {
      return null
    }

    return Math.round(((marketPrice - listedPrice) / marketPrice) * 100)
  }, [marketPrice, listedPrice])

  const savingsAmount = marketPrice != null && listedPrice != null ? marketPrice - listedPrice : null

  const isPricingInProgress = item.pricingStatus === "IN_PROGRESS"

  const hasFetchedPrice = item.priceFetchedAt != null

  const isFresh = useMemo(() => {
    if (item.pricingStatus !== "READY" || !item.nextPriceRefreshAt) {
      return false
    }

    return Date.now() < new Date(item.nextPriceRefreshAt).getTime()
  }, [item.pricingStatus, item.nextPriceRefreshAt])

  const refreshDisabled = requestingRefresh || isPricingInProgress || isFresh

  const refreshLabel = (() => {
    if (requestingRefresh || isPricingInProgress) {
      return "Refreshing market price…"
    }

    if (requestError || item.pricingStatus === "FAILED") {
      return "Try again"
    }

    if (isFresh) {
      return "Market price is up to date"
    }

    return "Refresh market price"
  })()

  const priceStatus = (() => {
    if (requestError) {
      return {
        label: "Update failed",
        className: "marketplace-status-failed",
        icon: <AlertTriangle className="h-4 w-4" />,
      }
    }

    switch (item.pricingStatus) {
      case "READY":
        return {
          label: "Ready",
          className: "marketplace-status-ready",
          icon: <CheckCircle2 className="h-4 w-4" />,
        }

      case "IN_PROGRESS":
        return {
          label: "Updating…",
          className: "marketplace-status-progress",
          icon: <RefreshCw className="h-4 w-4 animate-spin" />,
        }

      case "FAILED":
        return {
          label: "Update failed",
          className: "marketplace-status-failed",
          icon: <AlertTriangle className="h-4 w-4" />,
        }

      case "PENDING":
      default:
        return {
          label: "Waiting for pricing",
          className: "marketplace-status-pending",
          icon: <Clock3 className="h-4 w-4" />,
        }
    }
  })()

  async function waitForPricingCompletion(itemId: number) {
    const startedAt = Date.now()

    let delayMs = 1000

    while (Date.now() - startedAt < PRICE_POLL_TIMEOUT_MS) {
      if (pollCancelledRef.current) {
        return
      }

      await new Promise<void>((resolve) => {
        window.setTimeout(resolve, delayMs)
      })

      if (pollCancelledRef.current) {
        return
      }

      const current = await getItemById(itemId)

      if (pollCancelledRef.current) {
        return
      }

      setItem(current)

      if (current.pricingStatus === "READY" || current.pricingStatus === "FAILED") {
        return
      }

      delayMs = Math.min(Math.round(delayMs * 1.4), 3000)
    }

    setPollNotice("The market update is still running. Your latest saved values remain visible.")
  }

  async function handleRefresh() {
    if (refreshDisabled) {
      return
    }

    pollCancelledRef.current = false

    setRequestError(null)
    setRequestingRefresh(true)

    try {
      const refresh = await refreshItemPrice(item.itemId)

      /*
       * The refresh endpoint acknowledges the job.
       * It does NOT mean the provider has finished.
       */

      if (refresh.outcome === "NOT_FOUND") {
        throw new Error("This listing could not be found.")
      }

      if (refresh.outcome === "ALREADY_FRESH") {
        const latest = await getItemById(item.itemId)

        setItem(latest)

        return
      }

      if (refresh.outcome === "ALREADY_RUNNING") {
        const latest = await getItemById(item.itemId)

        setItem(latest)
      } else {
        /*
         * STARTED:
         * immediately reflect the acknowledged server state.
         */
        const latest = await getItemById(item.itemId)
        setItem(latest)
      }

      await waitForPricingCompletion(item.itemId)
    } catch (error) {
      console.error("Could not refresh pricing", error)

      setRequestError(error instanceof Error ? error.message : "We could not update the market price.")
    } finally {
      setRequestingRefresh(false)
    }
  }

  const hasDealComparison = generatedPrice != null && generatedPrice > 0 && listedPrice != null

  return (
    <div className="marketplace-pricing p-5 sm:p-6">
      <div className="relative space-y-5">
        <div className="flex items-start justify-between gap-4">
          <div className="min-w-0">
            <p className="eyebrow mb-2">Listed Price</p>

            <p className="marketplace-pricing-value text-4xl font-bold leading-none tracking-tight sm:text-[2.7rem]">
              {formatPrice(listedPrice)}
            </p>

            {marketDifference != null && savingsAmount != null && (
              <p
                className={
                  marketDifference > 0
                    ? "mt-2 text-sm font-semibold text-emerald-300"
                    : marketDifference < 0
                      ? "mt-2 text-sm font-semibold text-amber-300"
                      : "mt-2 text-sm font-medium text-muted-foreground"
                }
              >
                {marketDifference > 0
                  ? `${formatPrice(savingsAmount)} below market · ${marketDifference}% lower`
                  : marketDifference < 0
                    ? `${formatPrice(Math.abs(savingsAmount))} above market · ${Math.abs(marketDifference)}% higher`
                    : "Listed around the estimated market price"}
              </p>
            )}
          </div>

          {marketDifference != null && marketDifference !== 0 && (
            <div
              className={
                marketDifference > 0
                  ? "shrink-0 rounded-full border border-emerald-400/20 bg-emerald-400/10 px-2.5 py-1.5 text-xs font-semibold text-emerald-300"
                  : "shrink-0 rounded-full border border-amber-400/20 bg-amber-400/10 px-2.5 py-1.5 text-xs font-semibold text-amber-300"
              }
            >
              {marketDifference > 0 ? (
                <TrendingDown className="mr-1 inline h-3.5 w-3.5" />
              ) : (
                <TrendingUp className="mr-1 inline h-3.5 w-3.5" />
              )}
              {Math.abs(marketDifference)}%
            </div>
          )}
        </div>

        <div>
          <div className="mb-2 flex items-center justify-between">
            <p className="text-[10px] font-semibold uppercase tracking-[0.16em] text-muted-foreground">Market references</p>
          </div>

          <div className="marketplace-pricing-reference-row">
            <div className="marketplace-pricing-reference marketplace-pricing-market">
              <p className="marketplace-pricing-reference-label">New market</p>

              <p className="marketplace-pricing-reference-value">{formatPrice(marketPrice)}</p>

              <p className="marketplace-pricing-reference-copy">Current retail reference</p>
            </div>

            <div className="marketplace-pricing-reference marketplace-pricing-used">
              <p className="marketplace-pricing-reference-label">Used estimate</p>

              <p className="marketplace-pricing-reference-value">{formatPrice(generatedPrice)}</p>

              <p className="marketplace-pricing-reference-copy">Condition-adjusted estimate</p>
            </div>
          </div>
        </div>

        <div className="marketplace-status">
          <span className="marketplace-status-label">Market pricing</span>

          <div className={`marketplace-status-value ${priceStatus.className}`}>
            {priceStatus.icon}
            <span>{priceStatus.label}</span>
          </div>
        </div>

        {hasFetchedPrice && item.priceFetchedAt && (
          <div className="flex items-center justify-between gap-4 text-[11px]">
            <span className="text-muted-foreground">Last lookup</span>

            <span className="text-right text-foreground/75">{formatDate(item.priceFetchedAt)}</span>
          </div>
        )}

        {item.pricingStatus === "PENDING" && !requestError && (
          <div className="rounded-xl border border-primary/15 bg-primary/[0.045] px-3.5 py-3">
            <div className="flex items-center gap-2">
              <Clock3 className="h-4 w-4 shrink-0 text-primary" />

              <p className="text-xs leading-5 text-foreground/80">
                Market pricing is being prepared. Your listing remains fully usable while the estimate is generated.
              </p>
            </div>
          </div>
        )}

        {isPricingInProgress && (
          <div className="rounded-xl border border-[var(--trust-blue)]/18 bg-[var(--trust-blue)]/[0.045] px-3.5 py-3">
            <div className="flex items-center gap-2">
              <RefreshCw className="h-4 w-4 shrink-0 animate-spin text-[var(--trust-blue)]" />

              <p className="text-xs leading-5 text-foreground/80">Updating the latest market reference…</p>
            </div>
          </div>
        )}

        {pollNotice && (
          <div className="rounded-xl border border-[var(--trust-blue)]/18 bg-[var(--trust-blue)]/[0.045] px-3.5 py-3">
            <p className="text-xs leading-5 text-foreground/80">{pollNotice}</p>
          </div>
        )}

        {(item.pricingStatus === "FAILED" || requestError) && (
          <div className="rounded-xl border border-amber-400/20 bg-amber-400/[0.055] px-3.5 py-3">
            <div className="flex items-start gap-2">
              <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0 text-amber-300" />

              <div className="min-w-0">
                <p className="text-xs font-semibold text-amber-200">Market update failed</p>

                <p className="mt-1 text-[11px] leading-5 text-muted-foreground">
                  {requestError ??
                    item.pricingErrorMessage ??
                    "The last market lookup could not be completed. Your listing and existing values are unchanged."}
                </p>
              </div>
            </div>
          </div>
        )}

        <Button
          type="button"
          variant="surface"
          className="marketplace-action-secondary h-11 w-full rounded-xl font-semibold"
          onClick={handleRefresh}
          disabled={refreshDisabled}
        >
          <RefreshCw className={`mr-2 h-4 w-4 ${requestingRefresh || isPricingInProgress ? "animate-spin" : ""}`} />

          {refreshLabel}
        </Button>

        {isFresh && item.nextPriceRefreshAt && (
          <p className="text-center text-[10px] leading-4 text-muted-foreground">
            Current estimate remains fresh until {formatDate(item.nextPriceRefreshAt)}.
          </p>
        )}

        {hasDealComparison && <DealRating userPrice={listedPrice} aiEstimate={generatedPrice} />}
      </div>
    </div>
  )
}
