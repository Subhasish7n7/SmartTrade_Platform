"use client"
import { AnimatedPage, Section } from "@/components/marketplace/animations/section"
import { BackButton } from "@/components/marketplace/back-button"
import { TradeInboxCard } from "@/components/marketplace/trade/trade-inbox-card"
import { TradeInboxResponse } from "@/lib/types/api/trade"
import { CheckCircle2, Clock3, Handshake, Inbox } from "lucide-react"
import * as React from "react"
interface props {
  trades: TradeInboxResponse[]
}

export function TradeInboxClient({ trades }: props) {
  const openTrades = trades.filter((t) => t.status === "OPEN").length
  const negotiatingTrades = trades.filter((t) => t.status === "NEGOTIATING").length
  const completedTrades = trades.filter((t) => t.status === "COMPLETED").length
  const totalTrades = trades.length
  const [filter, setFilter] = React.useState<"ALL" | "OPEN" | "NEGOTIATING" | "COMPLETED">("ALL")
  const filteredTrades = filter === "ALL" ? trades : trades.filter((trade) => trade.status === filter)

  return (
    <main className="relative z-10 pt-20 pb-24">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <AnimatedPage>
          <Section className="trade-inbox-hero rounded-2xl px-6 py-4 mb-4 border border-border/50">
            <h1 className="text-2xl md:text-3xl font-bold bg-gradient-to-r from-[var(--trust-blue)] via-[var(--negotiation-violet)] to-[var(--premium-gold)] bg-clip-text text-transparent">
              Trade Inbox
            </h1>

            <p className="text-muted-foreground mt-2">Active negotiations, offers, and completed trades.</p>
          </Section>
          <Section
            className="
          grid
          grid-cols-2
          lg:grid-cols-4
          gap-3
          mb-6
        "
          >
            <button onClick={() => setFilter("ALL")} className={`trade-filter-btn ${filter === "ALL" ? "trade-filter-active" : ""}`}>
              <Inbox className="h-4 w-4" />

              <span>Total</span>

              <span className="ml-auto font-bold">{totalTrades}</span>
            </button>

            <button
              onClick={() => setFilter("OPEN")}
              className={`
            trade-filter-btn
            ${filter === "OPEN" ? "trade-filter-active" : ""}
          `}
            >
              <Clock3 className="h-4 w-4" />

              <span>Open</span>

              <span className="ml-auto font-bold">{openTrades}</span>
            </button>

            <button
              onClick={() => setFilter("NEGOTIATING")}
              className={`
            trade-filter-btn
            ${filter === "NEGOTIATING" ? "trade-filter-active" : ""}
          `}
            >
              <Handshake className="h-4 w-4" />

              <span>Negotiating</span>

              <span className="ml-auto font-bold">{negotiatingTrades}</span>
            </button>

            <button
              onClick={() => setFilter("COMPLETED")}
              className={`
            trade-filter-btn
            ${filter === "COMPLETED" ? "trade-filter-active" : ""}
          `}
            >
              <CheckCircle2 className="h-4 w-4" />

              <span>Completed</span>

              <span className="ml-auto font-bold">{completedTrades}</span>
            </button>
          </Section>
          <div className="trade-section-divider mb-5" />
          <div className="mb-4">
            <h2 className="text-xl font-semibold">Active Trades</h2>

            <p className="text-sm text-muted-foreground mt-1">Track ongoing negotiations and respond to offers.</p>
          </div>

          <div className="space-y-4">
            <div className="flex items-center justify-between mb-4">
              <h2 className="font-semibold">Trades</h2>
              <span className="text-sm text-muted-foreground">{filteredTrades.length} results</span>
            </div>
            {filteredTrades.map((trade) => (
              <Section key={trade.tradeId}>
                <TradeInboxCard {...trade} />
              </Section>
            ))}
          </div>
        </AnimatedPage>
      </div>
    </main>
  )
}
