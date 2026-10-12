import Link from "next/link"

import { Activity, ArrowLeft, CheckCircle2, Clock3, Handshake, ShieldAlert } from "lucide-react"
import { AnimatedPage } from "@/components/marketplace/animations/section"
import { TradeComparison } from "@/components/marketplace/trade/trade-comparison"
import { NegotiationHistory } from "@/components/marketplace/trade/negotiation-history"
import { TradeDetailClient } from "@/components/marketplace/trade/trade-detail-client"
import { TradeReviewForm } from "@/components/marketplace/trade/trade-review-form"
import { TradeFairnessCard } from "@/components/marketplace/trade/trade-fairness-card"
import { TradeTrustLevel } from "@/components/marketplace/trade/trade-trust-level"

import { TradeStatusBadge } from "@/components/marketplace/trade/trade-status-badge"

import { Button } from "@/components/ui/button"

import { getTrade } from "@/lib/api/trades/server"

import { requireUser } from "@/lib/auth/require-user"

import type { TradeDetailsResponse, TradeItemResponse, TradeStatus } from "@/lib/types/api/trade"

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

  const numericTradeId = Number(tradeId)

  const numericOfferId = Number(tradeOfferId)

  const currentPath = `/trades/${tradeId}?tradeOfferId=${tradeOfferId}${isHistoryMode ? "&view=history" : ""}`

  const user = await requireUser(currentPath)

  let trade: TradeDetailsResponse

  try {
    trade = await getTrade(numericTradeId, numericOfferId)
  } catch (error) {
    console.error("Failed to load trade:", { tradeId, tradeOfferId, error })

    throw error
  }

  const latestOfferId = trade.history?.[0]?.tradeOfferId ?? null

  const isLatestOffer = latestOfferId === trade.offer.tradeOfferId

  const isCurrentUserOffer = trade.offer.senderId === user.id

  const otherUserId = trade.initiatorId === user.id ? trade.receiverId : trade.initiatorId

  const otherUserName = trade.initiatorId === user.id ? trade.receiverName : trade.initiatorName

  const offerSenderName = trade.offer.senderId === trade.initiatorId ? trade.initiatorName : trade.receiverName

  const offerSenderTrustScore = trade.offer.senderId === trade.initiatorId ? trade.initiatorTrustScore : trade.receiverTrustScore

  const yourItems: TradeItemResponse[] = isCurrentUserOffer ? trade.offer.senderItems : trade.offer.receiverItems

  const theirItems: TradeItemResponse[] = isCurrentUserOffer ? trade.offer.receiverItems : trade.offer.senderItems

  const cashAdjustment = trade.offer.cashAdjustment ?? 0

  const senderListedValue = sumValue(trade.offer.senderItems, (item) => item.userPrice) + cashAdjustment

  const receiverListedValue = sumValue(trade.offer.receiverItems, (item) => item.userPrice)

  const senderUsedValue =
    sumValue(
      trade.offer.senderItems,

      (item) => item.systemGeneratedPrice ?? item.userPrice,
    ) + cashAdjustment

  const receiverUsedValue = sumValue(
    trade.offer.receiverItems,

    (item) => item.systemGeneratedPrice ?? item.userPrice,
  )

  const senderNewValue = sumOptionalValue(trade.offer.senderItems, (item) => item.newPrice)
  const receiverNewValue = sumOptionalValue(trade.offer.receiverItems, (item) => item.newPrice)
  const yourValue = isCurrentUserOffer ? senderListedValue : receiverListedValue
  const theirValue = isCurrentUserOffer ? receiverListedValue : senderListedValue
  const yourUsedValue = isCurrentUserOffer ? senderUsedValue : receiverUsedValue
  const theirUsedValue = isCurrentUserOffer ? receiverUsedValue : senderUsedValue
  const yourNewValue = isCurrentUserOffer ? (senderNewValue == null ? null : senderNewValue + cashAdjustment) : receiverNewValue
  const theirNewValue = isCurrentUserOffer ? receiverNewValue : senderNewValue == null ? null : senderNewValue + cashAdjustment
  const youConfirmed = trade.initiatorId === user.id ? trade.initiatorCompletionConfirmed : trade.receiverCompletionConfirmed
  const theyConfirmed = trade.initiatorId === user.id ? trade.receiverCompletionConfirmed : trade.initiatorCompletionConfirmed
  const canRespondToOffer =
    !isHistoryMode && isLatestOffer && !isCurrentUserOffer && (trade.status === "OPEN" || trade.status === "NEGOTIATING")
  const backHref = isHistoryMode && latestOfferId ? `/trades/${tradeId}?tradeOfferId=${latestOfferId}` : "/trades"
  const backLabel = isHistoryMode ? "Back to Latest Offer" : "Back to Trades"

  return (
    <main className="min-h-screen pb-28 pt-20">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <AnimatedPage>
          <div className="space-y-5">
            <header className="content-surface !p-4 sm:!p-5">
              <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
                <div className="min-w-0">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="inline-flex h-7 w-7 items-center justify-center rounded-lg border border-[var(--trust-blue)]/22 bg-[var(--trust-blue)]/[0.07] text-[var(--trust-blue)]">
                      <Handshake className="h-3.5 w-3.5" />
                    </span>

                    <span className="text-[11px] font-semibold uppercase tracking-[0.18em] text-muted-foreground">
                      Trade #{trade.tradeId}
                    </span>

                    <TradeStatusBadge status={trade.status} />

                    {isHistoryMode && (
                      <span className="rounded-full border border-white/[0.10] bg-white/[0.025] px-2.5 py-1 text-[10px] font-semibold text-muted-foreground">
                        Historical offer
                      </span>
                    )}
                  </div>

                  <h1 className="mt-2 text-2xl font-bold tracking-tight sm:text-3xl">
                    {isCurrentUserOffer ? "Your offer" : `Offer from ${offerSenderName}`}
                  </h1>

                  <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-2 text-xs text-muted-foreground">
                    <span className="inline-flex items-center gap-1.5">
                      <Clock3 className="h-3.5 w-3.5" />

                      {formatDate(trade.offer.createdAt)}
                    </span>

                    {!isCurrentUserOffer && <TradeTrustLevel score={offerSenderTrustScore} compact />}
                  </div>

                  {isCurrentUserOffer && isLatestOffer && !isHistoryMode && (
                    <p className="mt-3 max-w-2xl text-sm leading-6 text-muted-foreground">
                      You sent the latest proposal. {otherUserName} needs to respond before another offer can be acted on.
                    </p>
                  )}

                  {!isLatestOffer && !isHistoryMode && (
                    <p className="mt-3 max-w-2xl text-sm leading-6 text-muted-foreground">
                      This is not the latest proposal in the negotiation.
                    </p>
                  )}
                </div>

                <div className="flex w-full shrink-0 flex-col gap-2 sm:w-auto sm:items-stretch lg:items-end">
                  <Link
                    href={backHref}
                    className="inline-flex h-10 items-center justify-center gap-2 rounded-xl border border-white/[0.07] bg-white/[0.025] px-3.5 text-xs font-semibold text-muted-foreground transition hover:border-white/[0.12] hover:text-foreground"
                  >
                    <ArrowLeft className="h-3.5 w-3.5" />

                    {backLabel}
                  </Link>

                  <Link href="/messages">
                    <Button
                      variant="surface"
                      className="h-10 w-full rounded-xl border-[var(--chat-cyan)]/20 bg-[var(--chat-cyan)]/[0.06] font-semibold sm:min-w-36"
                    >
                      Open Chat
                    </Button>
                  </Link>
                </div>
              </div>
            </header>

            <section className="grid gap-5 lg:grid-cols-[minmax(0,1fr)_19rem] lg:items-start">
              <div className="min-w-0 space-y-5">
                <TradeComparison
                  yourItems={yourItems}
                  theirItems={theirItems}
                  yourLabel="You give"
                  theirLabel="You receive"
                  showValuation
                />

                <TradeActivity
                  status={trade.status}
                  offerId={trade.offer.tradeOfferId}
                  history={trade.history ?? []}
                  offerCreatedAt={trade.offer.createdAt}
                  offerSenderName={offerSenderName}
                  otherUserName={otherUserName}
                  isCurrentUserOffer={isCurrentUserOffer}
                  isLatestOffer={isLatestOffer}
                  isHistoryMode={isHistoryMode}
                  youConfirmed={youConfirmed}
                  theyConfirmed={theyConfirmed}
                  completedAt={trade.completedAt}
                />

                <NegotiationHistory tradeId={trade.tradeId} history={trade.history ?? []} latestOfferId={latestOfferId} />
              </div>

              <aside className="space-y-4 lg:sticky lg:top-24">
                <TradeFairnessCard
                  yourValue={yourValue}
                  theirValue={theirValue}
                  usedYourValue={yourUsedValue}
                  usedTheirValue={theirUsedValue}
                  newYourValue={yourNewValue}
                  newTheirValue={theirNewValue}
                  cashAdjustment={cashAdjustment}
                  yourItemCount={yourItems.length}
                  theirItemCount={theirItems.length}
                />

                <div className="content-surface !p-4">
                  <p className="text-[10px] font-semibold uppercase tracking-[0.16em] text-muted-foreground">Trader trust</p>

                  <div className="mt-2 flex items-start justify-between gap-3">
                    <div>
                      <p className="font-semibold">{offerSenderName}</p>

                      <p className="mt-1 text-xs leading-5 text-muted-foreground">
                        Trust is based on marketplace behavior, not reviews alone.
                      </p>
                    </div>
                  </div>

                  <div className="mt-3">
                    <TradeTrustLevel score={offerSenderTrustScore} />
                  </div>
                </div>
              </aside>
            </section>

            {!isHistoryMode && (
              <TradeDetailClient
                tradeId={trade.tradeId}
                otherUserId={otherUserId}
                currentUserId={user.id}
                status={trade.status}
                isLatestOffer={isLatestOffer}
                isCurrentUserOffer={isCurrentUserOffer}
                canRespondToOffer={canRespondToOffer}
                youConfirmed={youConfirmed}
                theyConfirmed={theyConfirmed}
                completionRequestedAt={trade.completionRequestedAt}
              />
            )}

            {!isHistoryMode && trade.status === "COMPLETED" && <TradeReviewForm tradeId={trade.tradeId} status={trade.status} />}
          </div>
        </AnimatedPage>
      </div>
    </main>
  )
}

function TradeActivity({
  status,

  offerId,

  history,

  offerCreatedAt,

  offerSenderName,

  otherUserName,

  isCurrentUserOffer,

  isLatestOffer,

  isHistoryMode,

  youConfirmed,

  theyConfirmed,

  completedAt,
}: {
  status: TradeStatus

  offerId: number

  history: Array<{ tradeOfferId: number }>

  offerCreatedAt: string

  offerSenderName: string

  otherUserName: string

  isCurrentUserOffer: boolean

  isLatestOffer: boolean

  isHistoryMode: boolean

  youConfirmed: boolean

  theyConfirmed: boolean

  completedAt: string | null
}) {
  const historyIndex = history.findIndex((offer) => offer.tradeOfferId === offerId)

  const offerNumber = historyIndex >= 0 ? history.length - historyIndex : Math.max(1, history.length)

  let title = `Offer #${offerNumber} activity`

  let stateLabel = "Current proposal"

  let stateCopy = `${offerSenderName} sent this proposal.`

  let icon = <Activity className="h-4 w-4 text-[var(--chat-cyan)]" />

  if (isHistoryMode) {
    title = `Offer #${offerNumber} activity`

    stateLabel = "Historical proposal"

    stateCopy = "This proposal is preserved for negotiation history and is no longer the current offer."
  } else if (status === "ACCEPTED") {
    title = "Trade accepted"

    stateLabel = "Completion in progress"

    stateCopy = youConfirmed
      ? `${otherUserName} still needs to confirm the exchange.`
      : theyConfirmed
        ? "The other trader confirmed. You still need to confirm the exchange."
        : "Both participants still need to confirm the exchange."

    icon = <CheckCircle2 className="h-4 w-4 text-emerald-300" />
  } else if (status === "COMPLETED") {
    title = "Trade completed"

    stateLabel = "Finalized"

    stateCopy = completedAt
      ? `The exchange was finalized on ${formatDate(completedAt)}.`
      : "Both participants confirmed completion and the platform finalized the exchange."

    icon = <CheckCircle2 className="h-4 w-4 text-emerald-300" />
  } else if (status === "DISPUTED") {
    title = "Trade under dispute"

    stateLabel = "Participant actions paused"

    stateCopy = "The trade is being handled through the dispute process."

    icon = <ShieldAlert className="h-4 w-4 text-amber-300" />
  } else if (status === "CANCELLED") {
    title = "Trade cancelled"

    stateLabel = "Closed"

    stateCopy = "This trade is no longer accepting offers."
  } else if (status === "EXPIRED") {
    title = "Trade expired"

    stateLabel = "Closed"

    stateCopy = "This trade is no longer active."
  } else if (isLatestOffer) {
    stateLabel = isCurrentUserOffer ? "Waiting for their response" : "Your response is needed"

    stateCopy = isCurrentUserOffer
      ? `${otherUserName} is expected to respond to your latest proposal.`
      : "Review the current proposal and use the actions below to respond."
  }

  return (
    <section className="content-surface !p-5 sm:!p-6">
      <div className="flex items-start gap-3">
        <span className="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-xl border border-[var(--chat-cyan)]/18 bg-[var(--chat-cyan)]/[0.06]">
          {icon}
        </span>

        <div className="min-w-0 flex-1">
          <p className="text-[10px] font-semibold uppercase tracking-[0.16em] text-muted-foreground">Trade activity</p>

          <div className="mt-1 flex flex-wrap items-center gap-2">
            <h2 className="text-lg font-semibold">{title}</h2>

            <span className="rounded-full border border-white/[0.08] bg-white/[0.025] px-2 py-0.5 text-[9px] font-semibold text-muted-foreground">
              {isHistoryMode ? "History" : stateLabel}
            </span>
          </div>

          <p className="mt-1 text-sm leading-6 text-muted-foreground">{stateCopy}</p>

          <div className="mt-4 grid gap-2 sm:grid-cols-3">
            <ActivityStat label="Proposal" value={`#${offerNumber}`} />

            <ActivityStat label="Sent by" value={offerSenderName} />

            <ActivityStat label="Created" value={formatDate(offerCreatedAt)} />
          </div>
        </div>
      </div>
    </section>
  )
}

function ActivityStat({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-xl border border-white/[0.055] bg-white/[0.018] px-3 py-2.5">
      <p className="text-[9px] font-semibold uppercase tracking-[0.08em] text-muted-foreground">{label}</p>

      <p className="mt-1 truncate text-[11px] font-semibold" title={value}>
        {value}
      </p>
    </div>
  )
}

function sumValue(items: TradeItemResponse[], selector: (item: TradeItemResponse) => number) {
  return items.reduce((sum, item) => sum + selector(item), 0)
}

function sumOptionalValue(
  items: TradeItemResponse[],

  selector: (item: TradeItemResponse) => number | null,
) {
  if (items.length === 0) return 0

  if (items.some((item) => selector(item) == null)) return null

  return items.reduce((sum, item) => sum + (selector(item) ?? 0), 0)
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("en-IN", {
    dateStyle: "medium",

    timeStyle: "short",
  }).format(new Date(value))
}
