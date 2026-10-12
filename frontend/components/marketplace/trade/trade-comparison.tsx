"use client"

import Link from "next/link"
import { ArrowLeftRight, Eye } from "lucide-react"

import { Button } from "@/components/ui/button"
import type { TradeItemResponse } from "@/lib/types/api/trade"
import { TradeItemCard } from "./trade-item-card"

interface Props {
  yourItems: TradeItemResponse[]
  theirItems: TradeItemResponse[]
  yourLabel?: string
  theirLabel?: string
  showValuation?: boolean
}

export function TradeComparison({
  yourItems,
  theirItems,
  yourLabel = "You give",
  theirLabel = "You receive",
  showValuation = false,
}: Props) {
  return (
    <section className="trade-card overflow-hidden rounded-[24px]">
      <div className="trade-card-accent h-1" />

      <div className="border-b border-white/[0.07] px-5 py-4 sm:px-6">
        <div className="flex items-center gap-2.5">
          <span className="trade-icon h-9 w-9 shrink-0">
            <ArrowLeftRight className="h-4 w-4 text-[var(--trust-blue)]" />
          </span>
          <div>
            <p className="text-[10px] font-semibold uppercase tracking-[0.16em] text-muted-foreground">
              Proposal
            </p>
            <h2 className="mt-0.5 text-base font-semibold sm:text-lg">Trade comparison</h2>
          </div>
        </div>
      </div>

      <div className="grid md:grid-cols-2 md:divide-x md:divide-white/[0.07] md:items-start">
        <TradeSide
          title={yourLabel}
          items={yourItems}
          accent="var(--trust-blue)"
          showValuation={showValuation}
        />
        <TradeSide
          title={theirLabel}
          items={theirItems}
          accent="var(--negotiation-violet)"
          showValuation={showValuation}
        />
      </div>
    </section>
  )
}

function TradeSide({
  title,
  items,
  accent,
  showValuation,
}: {
  title: string
  items: TradeItemResponse[]
  accent: string
  showValuation: boolean
}) {
  const listedTotal = items.reduce((sum, item) => sum + item.userPrice, 0)

  return (
    <div className="min-w-0 p-4 sm:p-5">
      <div className="mb-3 flex items-center justify-between gap-3">
        <div className="min-w-0">
          <p className="text-sm font-semibold" style={{ color: accent }}>
            {title}
          </p>
          <p className="mt-0.5 text-[11px] text-muted-foreground">
            {items.length} {items.length === 1 ? "item" : "items"}
          </p>
        </div>

        <span className="rounded-lg border border-white/[0.07] bg-white/[0.025] px-2 py-1 text-[10px] font-semibold text-foreground/80">
          ₹{listedTotal.toLocaleString("en-IN")}
        </span>
      </div>

      <div className="max-h-[min(58svh,36rem)] overflow-y-auto pr-1 scrollbar-thin">
        {items.length === 0 ? (
          <div className="rounded-xl border border-dashed border-white/[0.08] px-4 py-8 text-center text-xs text-muted-foreground">
            No items in this side of the offer.
          </div>
        ) : (
          <div className="space-y-2.5">
            {items.map((item) => (
              <div key={item.itemId} className="relative">
                <TradeItemCard
                  item={item}
                  selected={false}
                  onClick={() => {}}
                  showValuation={showValuation}
                />

                <Link
                  href={`/item/${item.itemId}`}
                  className="absolute right-2.5 top-2.5 z-20"
                >
                  <Button
                    size="icon"
                    variant="secondary"
                    className="h-7 w-7 rounded-lg border border-white/[0.10] bg-background/80 backdrop-blur"
                    onClick={(event) => event.stopPropagation()}
                    aria-label={`View ${item.name}`}
                  >
                    <Eye className="h-3.5 w-3.5" />
                  </Button>
                </Link>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
