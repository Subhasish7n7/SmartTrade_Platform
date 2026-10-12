"use client"

import Link from "next/link"
import { useRouter } from "next/navigation"
import * as React from "react"
import { AnimatePresence, motion } from "framer-motion"
import {
  ArrowLeft,
  Eye,
  Minus,
  Plus,
  RefreshCw,
  Search,
  Send,
} from "lucide-react"

import { AnimatedPage } from "@/components/marketplace/animations/section"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { createTradeOffer } from "@/lib/api/trades/client"
import type { TradeItemResponse, TradeOfferResponse } from "@/lib/types/api/trade"
import { TradeFairnessCard } from "./trade-fairness-card"
import { TradeItemCard } from "./trade-item-card"

interface Props {
  receiverId: number
  currentUserId: number
  selectedItemId?: number
  tradeId?: number
  isCounterOffer: boolean
  myItems: TradeItemResponse[]
  theirItems: TradeItemResponse[]
  initialOffer?: TradeOfferResponse | null
}

const DEFAULT_TRADE_MESSAGE =
  "Hi! I've sent you a trade offer. Let me know what you think."

export function TradeBuilderClient({
  receiverId,
  currentUserId,
  selectedItemId,
  tradeId,
  isCounterOffer,
  myItems,
  theirItems,
  initialOffer,
}: Props) {
  const [selectedMine, setSelectedMine] = React.useState<number[]>(() => {
    if (!initialOffer) return []

    return initialOffer.senderId === currentUserId
      ? initialOffer.senderItems.map((item) => item.itemId)
      : initialOffer.receiverItems.map((item) => item.itemId)
  })

  const [selectedTheirs, setSelectedTheirs] = React.useState<number[]>(() => {
    if (initialOffer) {
      return initialOffer.senderId === currentUserId
        ? initialOffer.receiverItems.map((item) => item.itemId)
        : initialOffer.senderItems.map((item) => item.itemId)
    }

    return selectedItemId !== undefined ? [selectedItemId] : []
  })

  const [cashAdjustment, setCashAdjustment] = React.useState(() => {
    if (!initialOffer || initialOffer.senderId !== currentUserId) {
      return 0
    }

    return initialOffer.cashAdjustment ?? 0
  })

  const [initialMessage, setInitialMessage] = React.useState("")
  const [mineQuery, setMineQuery] = React.useState("")
  const [theirQuery, setTheirQuery] = React.useState("")
  const [submitting, setSubmitting] = React.useState(false)

  const router = useRouter()

  const filteredMine = React.useMemo(() => {
    const query = mineQuery.trim().toLowerCase()
    if (!query) return myItems
    return myItems.filter((item) => item.name.toLowerCase().includes(query))
  }, [mineQuery, myItems])

  const filteredTheirs = React.useMemo(() => {
    const query = theirQuery.trim().toLowerCase()
    if (!query) return theirItems
    return theirItems.filter((item) => item.name.toLowerCase().includes(query))
  }, [theirQuery, theirItems])

  const orderedMine = React.useMemo(
    () => [
      ...filteredMine.filter((item) => selectedMine.includes(item.itemId)),
      ...filteredMine.filter((item) => !selectedMine.includes(item.itemId)),
    ],
    [filteredMine, selectedMine],
  )

  const orderedTheirs = React.useMemo(
    () => [
      ...filteredTheirs.filter((item) => selectedTheirs.includes(item.itemId)),
      ...filteredTheirs.filter((item) => !selectedTheirs.includes(item.itemId)),
    ],
    [filteredTheirs, selectedTheirs],
  )

  const selectedMyItems = React.useMemo(
    () => myItems.filter((item) => selectedMine.includes(item.itemId)),
    [myItems, selectedMine],
  )

  const selectedTheirItems = React.useMemo(
    () => theirItems.filter((item) => selectedTheirs.includes(item.itemId)),
    [theirItems, selectedTheirs],
  )

  const listedYourValue = sumValue(selectedMyItems, (item) => item.userPrice) + cashAdjustment
  const listedTheirValue = sumValue(selectedTheirItems, (item) => item.userPrice)

  const usedYourValue =
    sumValue(selectedMyItems, (item) => item.systemGeneratedPrice ?? item.userPrice) + cashAdjustment
  const usedTheirValue = sumValue(
    selectedTheirItems,
    (item) => item.systemGeneratedPrice ?? item.userPrice,
  )

  const newYourValueBase = sumOptionalValue(selectedMyItems, (item) => item.newPrice)
  const newTheirValue = sumOptionalValue(selectedTheirItems, (item) => item.newPrice)
  const newYourValue = newYourValueBase == null ? null : newYourValueBase + cashAdjustment

  const difference = Math.abs(listedYourValue - listedTheirValue)
  const selectionReady = selectedMine.length > 0 && selectedTheirs.length > 0

  function toggleMine(item: TradeItemResponse) {
    setSelectedMine((previous) =>
      previous.includes(item.itemId)
        ? previous.filter((id) => id !== item.itemId)
        : [...previous, item.itemId],
    )
  }

  function toggleTheirs(item: TradeItemResponse) {
    setSelectedTheirs((previous) =>
      previous.includes(item.itemId)
        ? previous.filter((id) => id !== item.itemId)
        : [...previous, item.itemId],
    )
  }

  async function handleCreateTradeOffer() {
    if (selectedMine.length === 0) {
      window.alert("Select at least one of your items.")
      return
    }

    if (selectedTheirs.length === 0) {
      window.alert("Select at least one item from the other trader.")
      return
    }

    if (initialMessage.trim().length > 2000) {
      window.alert("The message cannot exceed 2000 characters.")
      return
    }

    try {
      setSubmitting(true)

      await createTradeOffer({
        receiverId,
        tradeId,
        senderItemIds: selectedMine,
        receiverItemIds: selectedTheirs,
        cashAdjustment,
        initialMessage: initialMessage.trim() || undefined,
      })

      router.push("/trades")
    } catch (error) {
      console.error(error)
      window.alert(
        error instanceof Error
          ? error.message
          : isCounterOffer
            ? "Failed to create counter offer."
            : "Failed to create trade offer.",
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="min-h-screen pb-28 pt-20">
      <div className="mx-auto max-w-[1500px] px-4 sm:px-6 lg:px-8">
        <AnimatedPage>
          <div className="space-y-5">
            <header className="trade-inbox-hero rounded-[22px] border border-border/50 px-4 py-3.5 sm:px-5">
              <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
                <div className="min-w-0">
                  <button
                    type="button"
                    onClick={() => router.back()}
                    className="inline-flex h-10 items-center gap-2 rounded-xl border border-white/[0.07] bg-white/[0.025] px-3.5 text-xs font-semibold text-muted-foreground transition hover:border-white/[0.12] hover:text-foreground"
                  >
                    <ArrowLeft className="h-3.5 w-3.5" />
                    Back
                  </button>
                </div>

                <div className="flex items-center gap-2">
                  {isCounterOffer && (
                    <RefreshCw className="h-4 w-4 text-[var(--negotiation-violet)]" />
                  )}
                  <div>
                    <p className="text-[10px] font-semibold uppercase tracking-[0.18em] text-muted-foreground">
                      Trade workspace
                    </p>
                    <h1 className="text-xl font-bold tracking-tight sm:text-2xl">
                      {isCounterOffer ? "Counter Offer" : "Create Trade Offer"}
                    </h1>
                  </div>
                </div>

                <Button
                  onClick={handleCreateTradeOffer}
                  disabled={submitting || !selectionReady}
                  className="cta-primary h-10 w-full rounded-xl px-4 text-sm font-semibold sm:min-w-40 lg:w-auto"
                >
                  <Send className="mr-2 h-4 w-4" />
                  {submitting
                    ? isCounterOffer
                      ? "Sending counter..."
                      : "Creating..."
                    : isCounterOffer
                      ? "Send Counter Offer"
                      : "Create Trade Offer"}
                </Button>
              </div>
            </header>

            {isCounterOffer && (
              <div className="rounded-xl border border-[var(--negotiation-violet)]/20 bg-[var(--negotiation-violet)]/[0.045] px-3.5 py-2.5">
                <p className="text-xs font-semibold">Countering the latest proposal</p>
                <p className="mt-0.5 text-[11px] leading-5 text-muted-foreground">
                  The current proposal is preselected. Change items or cash before sending your revision.
                </p>
              </div>
            )}

            <section className="grid gap-5 lg:grid-cols-[minmax(0,1fr)_19rem] lg:items-start">
              <div className="min-w-0 space-y-5">
                <section className="trade-card overflow-hidden rounded-[24px]">
                  <div className="trade-card-accent h-1" />
                  <div className="border-b border-white/[0.07] px-5 py-4 sm:px-6">
                    <div className="flex items-center gap-2.5">
                      <span className="trade-icon h-9 w-9 shrink-0">
                        <RefreshCw className="h-4 w-4 text-[var(--trust-blue)]" />
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
                    <InventoryPanel
                      title="Your inventory"
                      items={orderedMine}
                      totalItems={myItems.length}
                      selectedIds={selectedMine}
                      query={mineQuery}
                      onQueryChange={setMineQuery}
                      onToggle={toggleMine}
                    />
                    <InventoryPanel
                      title="Their inventory"
                      items={orderedTheirs}
                      totalItems={theirItems.length}
                      selectedIds={selectedTheirs}
                      query={theirQuery}
                      onQueryChange={setTheirQuery}
                      onToggle={toggleTheirs}
                      requestedItemId={selectedItemId}
                    />
                  </div>
                </section>

                <OfferMessage
                  value={initialMessage}
                  onChange={setInitialMessage}
                />

                <OfferSummary
                  yourItemCount={selectedMine.length}
                  theirItemCount={selectedTheirs.length}
                  yourValue={listedYourValue}
                  theirValue={listedTheirValue}
                  cashAdjustment={cashAdjustment}
                  difference={difference}
                  isCounterOffer={isCounterOffer}
                />
              </div>

              <aside className="space-y-4 lg:sticky lg:top-24">
                <TradeFairnessCard
                  yourValue={listedYourValue}
                  theirValue={listedTheirValue}
                  usedYourValue={usedYourValue}
                  usedTheirValue={usedTheirValue}
                  newYourValue={newYourValue}
                  newTheirValue={newTheirValue}
                  yourItemCount={selectedMine.length}
                  theirItemCount={selectedTheirs.length}
                  compact
                />

                <CashAdjustmentCard
                  cashAdjustment={cashAdjustment}
                  setCashAdjustment={setCashAdjustment}
                />
              </aside>
            </section>
          </div>
        </AnimatedPage>
      </div>
    </main>
  )
}

function InventoryPanel({
  title,
  items,
  totalItems,
  selectedIds,
  query,
  onQueryChange,
  onToggle,
  requestedItemId,
}: {
  title: string
  items: TradeItemResponse[]
  totalItems: number
  selectedIds: number[]
  query: string
  onQueryChange(value: string): void
  onToggle(item: TradeItemResponse): void
  requestedItemId?: number
}) {
  return (
    <div className="min-w-0 p-4 sm:p-5">
      <div className="flex items-center justify-between gap-3">
        <div className="min-w-0">
          <p className="text-sm font-semibold">{title}</p>
          <p className="mt-0.5 text-[11px] text-muted-foreground">
            {totalItems} available · {selectedIds.length} selected
          </p>
        </div>
        <span className="rounded-lg border border-white/[0.07] bg-white/[0.025] px-2 py-1 text-[10px] font-semibold text-muted-foreground">
          Select items
        </span>
      </div>

      <div className="relative mt-2.5">
        <Search className="pointer-events-none absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-muted-foreground" />
        <Input
          value={query}
          onChange={(event) => onQueryChange(event.target.value)}
          placeholder="Search inventory"
          className="h-8 rounded-xl pl-9 text-xs"
        />
      </div>

      <div className="mt-3 max-h-[min(58svh,36rem)] overflow-y-auto pr-1 scrollbar-thin">
        {items.length === 0 ? (
          <div className="rounded-xl border border-dashed border-white/[0.08] px-4 py-8 text-center">
            <p className="text-xs font-medium text-muted-foreground">No matching items</p>
            {query.trim() && (
              <p className="mt-1 text-[11px] text-muted-foreground/70">Try a different search.</p>
            )}
          </div>
        ) : (
          <AnimatePresence initial={false}>
            <div className="grid grid-cols-1 gap-2 2xl:grid-cols-2">
              {items.map((item) => (
                <motion.div
                  key={item.itemId}
                  initial={{ opacity: 0, y: 4 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -4 }}
                  transition={{ duration: 0.14 }}
                  className="relative"
                >
                  <TradeItemCard
                    item={item}
                    selected={selectedIds.includes(item.itemId)}
                    requestedItem={requestedItemId === item.itemId}
                    onClick={() => onToggle(item)}
                  />

                  <Link href={`/item/${item.itemId}`} className="absolute right-2 top-2 z-20">
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
                </motion.div>
              ))}
            </div>
          </AnimatePresence>
        )}
      </div>
    </div>
  )
}

function CashAdjustmentCard({
  cashAdjustment,
  setCashAdjustment,
}: {
  cashAdjustment: number
  setCashAdjustment(value: number): void
}) {
  return (
    <section className="trade-card overflow-hidden rounded-[22px]">
      <div className="trade-card-accent h-1" />
      <div className="p-4">
        <div className="flex items-center justify-between gap-3">
          <div>
            <p className="text-[10px] font-semibold uppercase tracking-[0.16em] text-muted-foreground">
              Cash adjustment
            </p>
            <p className="mt-1 text-[11px] leading-5 text-muted-foreground">
              Extra cash you add to this proposal.
            </p>
          </div>
          <span className="text-lg font-bold tabular-nums">₹{cashAdjustment.toLocaleString("en-IN")}</span>
        </div>

        <Input
          type="number"
          min={0}
          value={cashAdjustment === 0 ? "" : cashAdjustment}
          placeholder="0"
          onChange={(event) =>
            setCashAdjustment(
              event.target.value === "" ? 0 : Math.max(0, Number(event.target.value)),
            )
          }
          className="mt-3 h-9 rounded-xl"
        />

        <div className="mt-2 grid grid-cols-3 gap-1.5">
          {[500, 1000, 5000].map((amount) => (
            <Button
              key={amount}
              type="button"
              size="sm"
              variant="secondary"
              className="h-8 rounded-lg text-[10px]"
              onClick={() => setCashAdjustment(cashAdjustment + amount)}
            >
              +{amount.toLocaleString("en-IN")}
            </Button>
          ))}
        </div>

        <div className="mt-2 flex items-center gap-1.5">
          <Button
            type="button"
            size="icon"
            variant="secondary"
            className="h-8 w-8 rounded-lg"
            onClick={() => setCashAdjustment(Math.max(0, cashAdjustment - 500))}
          >
            <Minus className="h-4 w-4" />
          </Button>

          <div className="flex-1 text-center text-sm font-bold tabular-nums">
            ₹{cashAdjustment.toLocaleString("en-IN")}
          </div>

          <Button
            type="button"
            size="icon"
            variant="secondary"
            className="h-8 w-8 rounded-lg"
            onClick={() => setCashAdjustment(cashAdjustment + 500)}
          >
            <Plus className="h-4 w-4" />
          </Button>
        </div>
      </div>
    </section>
  )
}

function OfferMessage({
  value,
  onChange,
}: {
  value: string
  onChange(value: string): void
}) {
  return (
    <section className="content-surface !p-5 sm:!p-6">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-[10px] font-semibold uppercase tracking-[0.16em] text-muted-foreground">
            Message
          </p>
          <h2 className="mt-1 text-base font-semibold">Add context to the offer</h2>
        </div>
        <span className="text-[10px] tabular-nums text-muted-foreground">{value.length}/2000</span>
      </div>

      <textarea
        value={value}
        onChange={(event) => onChange(event.target.value)}
        maxLength={2000}
        rows={4}
        placeholder={`${DEFAULT_TRADE_MESSAGE} Leave blank to use this default.`}
        className="mt-3 w-full resize-none rounded-xl border border-white/[0.09] bg-background/45 px-3 py-2.5 text-xs leading-5 outline-none placeholder:text-muted-foreground/55 focus:border-[var(--trust-blue)]/38 focus:ring-1 focus:ring-[var(--trust-blue)]/10"
      />
    </section>
  )
}

function OfferSummary({
  yourItemCount,
  theirItemCount,
  yourValue,
  theirValue,
  cashAdjustment,
  difference,
  isCounterOffer,
}: {
  yourItemCount: number
  theirItemCount: number
  yourValue: number
  theirValue: number
  cashAdjustment: number
  difference: number
  isCounterOffer: boolean
}) {
  const balanced = yourValue === theirValue
  const youGiveMore = yourValue > theirValue

  return (
    <section className="content-surface !p-5 sm:!p-6">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-[10px] font-semibold uppercase tracking-[0.16em] text-muted-foreground">
            Proposal summary
          </p>
          <h2 className="mt-1 text-base font-semibold">
            {isCounterOffer ? "Counter offer ready" : "Offer ready"}
          </h2>
        </div>
        <span className="rounded-full border border-white/[0.08] bg-white/[0.025] px-2 py-1 text-[10px] font-semibold text-muted-foreground">
          {balanced ? "Even listed value" : "Review before sending"}
        </span>
      </div>

      <div className="mt-4 grid gap-2 sm:grid-cols-2 lg:grid-cols-4">
        <SummaryStat label="You give" value={`${yourItemCount} ${yourItemCount === 1 ? "item" : "items"}`} />
        <SummaryStat label="You receive" value={`${theirItemCount} ${theirItemCount === 1 ? "item" : "items"}`} />
        <SummaryStat label="Cash" value={`₹${cashAdjustment.toLocaleString("en-IN")}`} />
        <SummaryStat label="Listed gap" value={`₹${difference.toLocaleString("en-IN")}`} />
      </div>

      <p className="mt-3 text-[11px] leading-5 text-muted-foreground">
        {balanced
          ? "The listed values are even. Condition and valuation references are the next useful checks."
          : youGiveMore
            ? `You are giving ₹${difference.toLocaleString("en-IN")} more at listed value.`
            : `You are receiving ₹${difference.toLocaleString("en-IN")} more at listed value.`}
      </p>
    </section>
  )
}

function SummaryStat({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-xl border border-white/[0.055] bg-white/[0.018] px-3 py-2.5">
      <p className="text-[9px] font-semibold uppercase tracking-[0.08em] text-muted-foreground">{label}</p>
      <p className="mt-1 text-[11px] font-bold tabular-nums">{value}</p>
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
