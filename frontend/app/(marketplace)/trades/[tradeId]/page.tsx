import { AnimatedPage, Section } from "@/components/marketplace/animations/section"
import { BackButton } from "@/components/marketplace/back-button"
import { NegotiationHistory } from "@/components/marketplace/trade/negotiation-history"
import { TradeComparison } from "@/components/marketplace/trade/trade-comparison"
import { TradeDetailClient } from "@/components/marketplace/trade/trade-detail-client"
import { TradeFairnessCard } from "@/components/marketplace/trade/trade-fairness-card"
import { TradeStatusBadge } from "@/components/marketplace/trade/trade-status-badge"
import { Button } from "@/components/ui/button"
import { getTrade } from "@/lib/api/trades/server"
import { requireUser } from "@/lib/auth/require-user"
import { TradeDetailsResponse, TradeItemResponse } from "@/lib/types/api/trade"
import { MessageCircle } from "lucide-react"
import Link from "next/link"
import { notFound } from "next/navigation"
interface Props {
  params: Promise<{
    tradeId: string
  }>
  searchParams: Promise<{
    tradeOfferId: string
    view?: "history"
  }>
}

export default async function TradeDetailPage({ params, searchParams }: Props) {
  const { tradeId } = await params
  const { tradeOfferId, view } = await searchParams
  const isHistoryMode = view === "history"
  const currentPath = `/trades/${tradeId}`
  const user = await requireUser(currentPath)

  let trade: TradeDetailsResponse

  try {
    trade = await getTrade(Number(tradeId), Number(tradeOfferId))
  } catch {
    notFound()
  }
  const theirItems: TradeItemResponse[] = trade?.offer.receiverItems ?? []
  const yourItems: TradeItemResponse[] = trade?.offer.senderItems ?? []
  const yourValue = yourItems.reduce((sum, item) => sum + item.userPrice, 0)
  const theirValue = theirItems.reduce((sum, item) => sum + item.userPrice, 0)
  const cashAdjustment = trade?.offer.cashAdjustment ?? 0
  const status = trade?.status ?? "NEGOTIATING"

  return (
    <main className="pt-20 pb-24">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <AnimatedPage>
          <Section>
            <BackButton />
          </Section>
          <Section>
            <div className="trade-inbox-hero rounded-2xl px-6 py-5 mb-5 border border-border/50">
              <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4 ">
                <div>
                  <h1 className="text-3xl font-bold bg-gradient-to-r from-[var(--trust-blue)] via-[var(--negotiation-violet)] to-[var(--premium-gold)] bg-clip-text text-transparent">
                    Trade #{tradeId}
                  </h1>
                  <p className="text-muted-foreground mt-2">
                    {isHistoryMode ? "Viewing a previous trade offer." : "Review the current proposal and continue negotiations."}
                  </p>
                  <div className="mt-4">
                    <TradeStatusBadge status={status} />
                    {isHistoryMode && (
                      <div className="mt-2">
                        <span className="rounded-full border px-3 py-1 text-xs font-medium">Historical Offer</span>
                      </div>
                    )}
                  </div>
                </div>

                <Link href="/messages">
                  <Button className="glow-trust bg-[var(--trust-blue)] hover:opacity-90">
                    <MessageCircle className="mr-2 h-4 w-4" />
                    Open Negotiation Chat
                  </Button>
                </Link>
              </div>
            </div>
          </Section>
          <Section>
            <div className="grid grid-cols-3 lg:grid-cols-5 gap-3 mb-6">
              <div className="trade-stat">
                <p className="text-xs text-muted-foreground">Status</p>

                <p className="font-semibold mt-1">{status}</p>
              </div>

              <div className="trade-stat">
                <p className="text-xs text-muted-foreground">Cash Adjustment</p>

                <p className="font-semibold mt-1">{cashAdjustment}</p>
              </div>

              <div className="trade-stat">
                <p className="text-xs text-muted-foreground">Your Items</p>

                <p className="font-semibold mt-1">{yourItems.length}</p>
              </div>

              <div className="trade-stat">
                <p className="text-xs text-muted-foreground">Their Items</p>

                <p className="font-semibold mt-1">{theirItems.length}</p>
              </div>

              <div className="trade-stat">
                <p className="text-xs text-muted-foreground">Difference</p>

                <p className="font-semibold mt-1">₹{Math.abs(theirValue - yourValue).toLocaleString()}</p>
              </div>
            </div>
          </Section>
          <div className="trade-section-divider mb-6" />
          <div className="grid lg:grid-cols-3 gap-6">
            <div className="lg:col-span-2 space-y-6">
              <Section>
                <TradeComparison yourItems={yourItems} theirItems={theirItems} />
              </Section>
              <Section>
                <div className="content-surface p-5 ">
                  <div className="flex items-center gap-3 mb-4">
                    <div className="trade-icon">
                      <MessageCircle className="h-5 w-5 text-primary" />
                    </div>
                    <div>
                      <h3 className="font-semibold">Offer Message</h3>

                      <p className="text-sm text-muted-foreground">Included with the trade proposal</p>
                    </div>
                  </div>

                  <div className="rounded-2xl bg-background/40 border border-border/50 p-4">
                    Hi, I'm interested in this trade. Let me know what you think.
                  </div>
                </div>
              </Section>

              {!isHistoryMode && (
                <Section>
                  <NegotiationHistory tradeId={trade.tradeId} history={trade.history ?? []} />
                </Section>
              )}
            </div>

            <div className="space-y-6">
              <Section>
                <TradeFairnessCard yourValue={yourValue} theirValue={theirValue} />
              </Section>

              <Section>
                <div className="content-surface p-5">
                  <div className="flex items-center gap-3 mb-4">
                    <div className="trade-icon">
                      <MessageCircle className="h-5 w-5 text-[var(--chat-cyan)]" />
                    </div>

                    <div>
                      <h3 className="font-semibold">Conversation</h3>

                      <p className="text-sm text-muted-foreground">Continue negotiating</p>
                    </div>
                  </div>

                  <Link href="/messages">
                    <Button className="w-full cta-primary">
                      <MessageCircle className="h-4 w-4 mr-2" />
                      Open Chat
                    </Button>
                  </Link>
                </div>
              </Section>
              {!isHistoryMode && status !== "COMPLETED" && status !== "CANCELLED" && status !== "EXPIRED" && (
                <Section>
                  <TradeDetailClient tradeId={trade.tradeId} />
                </Section>
              )}
            </div>
          </div>
        </AnimatedPage>
      </div>
    </main>
  )
}
