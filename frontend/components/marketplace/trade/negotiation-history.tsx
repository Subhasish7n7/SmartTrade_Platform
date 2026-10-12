"use client"

import Link from "next/link"
import { Clock3, History } from "lucide-react"

import type { TradeHistoryResponse } from "@/lib/types/api/trade"

interface Props {
  tradeId: number
  history: TradeHistoryResponse[]
  latestOfferId?: number | null
}

export function NegotiationHistory({ tradeId, history, latestOfferId }: Props) {
  return (
    <section className="trade-card overflow-hidden rounded-[24px]">
      <div className="trade-card-accent h-1" />

      <div className="flex items-center justify-between gap-4 border-b border-white/[0.07] px-5 py-4 sm:px-6">
        <div className="flex items-center gap-3">
          <span className="trade-icon h-9 w-9 shrink-0">
            <History className="h-4 w-4 text-[var(--negotiation-violet)]" />
          </span>
          <div>
            <p className="text-sm font-semibold">Negotiation History</p>
            <p className="mt-0.5 text-[11px] text-muted-foreground">
              Previous offers and revisions
            </p>
          </div>
        </div>

        <span className="rounded-lg border border-white/[0.07] bg-white/[0.025] px-2 py-1 text-[10px] font-semibold text-muted-foreground">
          {history.length} {history.length === 1 ? "offer" : "offers"}
        </span>
      </div>

      <div className="max-h-[min(42svh,22rem)] overflow-y-auto p-4 scrollbar-thin">
        {history.length === 0 ? (
          <div className="rounded-xl border border-dashed border-white/[0.08] px-4 py-8 text-center text-xs text-muted-foreground">
            No previous proposals.
          </div>
        ) : (
          <div className="relative space-y-2.5">
            <div className="pointer-events-none absolute bottom-4 left-[9px] top-4 w-px bg-white/[0.07]" />

            {history.map((offer) => {
              const isLatest = offer.tradeOfferId === latestOfferId

              return (
                <Link
                  key={offer.tradeOfferId}
                  href={{
                    pathname: `/trades/${tradeId}`,
                    query: {
                      tradeOfferId: offer.tradeOfferId,
                      ...(isLatest ? {} : { view: "history" }),
                    },
                  }}
                  className="relative flex items-center gap-3 rounded-xl border border-white/[0.055] bg-white/[0.018] px-3 py-2.5 transition hover:border-[var(--negotiation-violet)]/24 hover:bg-[var(--negotiation-violet)]/[0.035]"
                >
                  <span
                    className={
                      `relative z-10 mt-0.5 h-2.5 w-2.5 shrink-0 rounded-full border ${
                        isLatest
                          ? "border-[var(--trust-blue)] bg-[var(--trust-blue)] shadow-[0_0_10px_var(--trust-blue)]"
                          : "border-white/[0.18] bg-[var(--trade-surface-2)]"
                      }`
                    }
                  />

                  <div className="min-w-0 flex-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <p className="text-xs font-semibold">{offer.senderName}</p>
                      {isLatest && (
                        <span className="rounded-full border border-[var(--trust-blue)]/20 bg-[var(--trust-blue)]/[0.055] px-1.5 py-0.5 text-[9px] font-semibold text-[var(--trust-blue)]">
                          Latest
                        </span>
                      )}
                    </div>
                    <p className="mt-1 flex items-center gap-1.5 text-[10px] text-muted-foreground">
                      <Clock3 className="h-3 w-3" />
                      {formatDate(offer.createdAt)}
                    </p>
                  </div>

                  <span className="text-[10px] font-semibold text-[var(--negotiation-violet)]">
                    {isLatest ? "View latest" : "View"}
                  </span>
                </Link>
              )
            })}
          </div>
        )}
      </div>
    </section>
  )
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("en-IN", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value))
}
