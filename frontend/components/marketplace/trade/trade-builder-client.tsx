"use client"

import { AnimatedPage, Section } from "@/components/marketplace/animations/section"
import { BackButton } from "@/components/marketplace/back-button"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { createTradeOffer } from "@/lib/api/trades/client"
import { TradeItemResponse } from "@/lib/types/api/trade"
import { AnimatePresence, motion } from "framer-motion"
import { Eye, Minus, Plus, RefreshCw } from "lucide-react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import * as React from "react"
import { TradeFairnessCard } from "./trade-fairness-card"
import { TradeItemCard } from "./trade-item-card"

interface Props {
  receiverId: number
  selectedItemId?: number
  tradeId?: number
  isCounterOffer: boolean
  myItems: TradeItemResponse[]
  theirItems: TradeItemResponse[]
}

export function TradeBuilderClient({ receiverId, selectedItemId, tradeId, isCounterOffer, myItems, theirItems }: Props) {
  const [selectedMine, setSelectedMine] = React.useState<number[]>([])

  const [selectedTheirs, setSelectedTheirs] = React.useState<number[]>(selectedItemId !== undefined ? [selectedItemId] : [])

  const [cashAdjustment, setCashAdjustment] = React.useState(0)
  const [submitting, setSubmitting] = React.useState(false)

  const router = useRouter()

  function toggleMine(item: TradeItemResponse) {
    setSelectedMine((prev) => (prev.includes(item.itemId) ? prev.filter((id) => id !== item.itemId) : [...prev, item.itemId]))
  }

  function toggleTheirs(item: TradeItemResponse) {
    setSelectedTheirs((prev) => (prev.includes(item.itemId) ? prev.filter((id) => id !== item.itemId) : [...prev, item.itemId]))
  }

  const sortedMine = [
    ...myItems.filter((item) => selectedMine.includes(item.itemId)),
    ...myItems.filter((item) => !selectedMine.includes(item.itemId)),
  ]

  const sortedTheirs = [
    ...theirItems.filter((item) => selectedTheirs.includes(item.itemId)),
    ...theirItems.filter((item) => !selectedTheirs.includes(item.itemId)),
  ]

  const myValue =
    myItems.filter((item) => selectedMine.includes(item.itemId)).reduce((sum, item) => sum + item.userPrice, 0) + cashAdjustment

  const theirValue = theirItems.filter((item) => selectedTheirs.includes(item.itemId)).reduce((sum, item) => sum + item.userPrice, 0)

  const difference = Math.abs(myValue - theirValue)

  const youReceiveMore = theirValue > myValue

  async function handleCreateTradeOffer() {
    if (selectedMine.length === 0) {
      alert("Select at least one of your items.")
      return
    }

    if (selectedTheirs.length === 0) {
      alert("Select at least one item from the seller.")
      return
    }

    try {
      setSubmitting(true)

      const trade = await createTradeOffer({
        receiverId,
        tradeId,
        senderItemIds: selectedMine,
        receiverItemIds: selectedTheirs,
        cashAdjustment,
      })

      console.log(isCounterOffer ? "Counter offer created" : "Trade offer created", trade)

      router.push(`/trades`)
    } catch (error) {
      console.error(error)

      alert(isCounterOffer ? "Failed to create counter offer." : "Failed to create trade offer.")
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="pt-20 pb-24">
      <div className="mx-auto max-w-[1600px] px-4 sm:px-6 lg:px-8">
        <AnimatedPage>
          <Section>
            <div className="grid grid-cols-3 items-center mb-4">
              <div className="flex items-center gap-5">
                <BackButton />
              </div>

              <div className="justify-self-center">
                <div className="flex items-center gap-2">
                  {isCounterOffer && <RefreshCw className="h-5 w-5 text-[var(--negotiation-violet)]" />}

                  <h1 className="text-2xl font-bold bg-gradient-to-r from-[var(--trust-blue)] via-[var(--negotiation-violet)] to-[var(--premium-gold)] bg-clip-text text-transparent">
                    {isCounterOffer ? "Counter Offer" : "Create Trade Offer"}
                  </h1>
                </div>
              </div>

              <div className="justify-self-end">
                <Button onClick={handleCreateTradeOffer} disabled={submitting} className="cta-primary h-11 w-full rounded-xl font-semibold">
                  {submitting
                    ? isCounterOffer
                      ? "Countering..."
                      : "Creating..."
                    : isCounterOffer
                      ? "Send Counter Offer"
                      : "Create Trade Offer"}
                </Button>
              </div>
            </div>

            {isCounterOffer && (
              <div className="mb-4 rounded-xl border border-[var(--negotiation-violet)]/30 bg-[var(--negotiation-violet)]/5 px-4 py-3">
                <p className="text-sm font-medium">You are making a counter offer</p>
                <p className="text-xs text-muted-foreground mt-1">Adjust the items and cash amount below, then send your new proposal.</p>
              </div>
            )}
          </Section>

          <Section>
            <div className="flex flex-col gap-4">
              <div className="grid grid-cols-5 gap-2">
                <div className="trade-summary-pill">
                  <span className="text-xs text-muted-foreground">Offer</span>
                  <span className="text-xl font-bold">{selectedMine.length}</span>
                </div>

                <div className="trade-summary-pill">
                  <span className="text-xs text-muted-foreground">Receive</span>
                  <span className="text-xl font-bold">{selectedTheirs.length}</span>
                </div>

                <div className="trade-summary-pill">
                  <span className="text-xs text-muted-foreground">Your Value</span>
                  <span className="font-semibold text-lg">₹{myValue.toLocaleString()}</span>
                </div>

                <div className="trade-summary-pill">
                  <span className="text-xs text-muted-foreground">Their Value</span>
                  <span className="font-semibold text-lg">₹{theirValue.toLocaleString()}</span>
                </div>

                <div
                  className={`trade-summary-pill ${
                    difference === 0 ? "" : youReceiveMore ? "border-emerald-500/30" : "border-amber-500/30"
                  }`}
                >
                  <span className="text-xs text-muted-foreground">Difference</span>

                  <span className="font-bold text-lg">₹{difference.toLocaleString()}</span>
                </div>
              </div>

              <div className="grid gap-4 lg:grid-cols-[1fr_1fr_285px] items-start">
                <section className="trade-card rounded-[26px] overflow-hidden flex flex-col h-[455px]">
                  <div className="trade-card-accent" />

                  <div className="px-4 py-2.5 border-b border-border/50">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <h3 className="font-semibold text-[15px]">Your Inventory</h3>

                        <span className="text-xs text-muted-foreground">{myItems.length} items</span>
                      </div>

                      <div className="trade-stat px-2 py-1 text-xs">{selectedMine.length} Selected</div>
                    </div>

                    <Input placeholder="Search..." className="mt-2 h-7 text-xs" />
                  </div>

                  <div className="flex-1 overflow-y-auto p-3 scrollbar-thin">
                    <AnimatePresence>
                      <div className="grid grid-cols-2 gap-2 content-start">
                        {sortedMine.map((item) => (
                          <motion.div key={item.itemId} layout transition={{ duration: 0.25 }} className="relative">
                            <TradeItemCard item={item} selected={selectedMine.includes(item.itemId)} onClick={() => toggleMine(item)} />

                            <Link href={`/item/${item.itemId}`} className="absolute left-2 top-2 z-20">
                              <Button size="sm" className="h-6 w-6" variant="secondary">
                                <Eye className="h-4 w-4" />
                              </Button>
                            </Link>
                          </motion.div>
                        ))}
                      </div>
                    </AnimatePresence>
                  </div>
                </section>

                <section className="trade-card rounded-[26px] overflow-hidden flex flex-col h-[455px]">
                  <div className="trade-card-accent" />

                  <div className="px-4 py-2.5 border-b border-border/50">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <h3 className="font-semibold text-[15px]">Their Inventory</h3>

                        <span className="text-xs text-muted-foreground">{theirItems.length} items</span>
                      </div>

                      <div className="trade-stat px-2 py-1 text-xs">{selectedTheirs.length} Selected</div>
                    </div>

                    <Input placeholder="Search..." className="mt-2 h-7 text-xs" />
                  </div>

                  <div className="flex-1 overflow-y-auto p-3 scrollbar-thin">
                    <AnimatePresence>
                      <div className="grid grid-cols-2 gap-2 content-start">
                        {sortedTheirs.map((item) => (
                          <motion.div key={item.itemId} layout transition={{ duration: 0.25 }} className="relative">
                            <TradeItemCard
                              item={item}
                              requestedItem={selectedItemId !== undefined && item.itemId === selectedItemId}
                              selected={selectedTheirs.includes(item.itemId)}
                              onClick={() => toggleTheirs(item)}
                            />

                            <Link href={`/item/${item.itemId}`} className="absolute left-2 top-2 z-20">
                              <Button size="sm" className="h-6 w-6" variant="secondary">
                                <Eye className="h-4 w-4" />
                              </Button>
                            </Link>
                          </motion.div>
                        ))}
                      </div>
                    </AnimatePresence>
                  </div>
                </section>

                <section className="trade-card rounded-[26px] overflow-hidden h-[455px] flex flex-col">
                  <div className="trade-card-accent" />

                  <div className="px-5 py-4 border-b border-border/50">
                    <h3 className="font-semibold">Trade Controls</h3>
                  </div>

                  <div className="flex flex-1 flex-col p-2.5">
                    <div className="content-surface p-2.5">
                      <h4 className="font-medium text-sm mb-2">Cash Adjustment</h4>

                      <Input
                        type="number"
                        min={0}
                        className="h-8 placeholder:text-foreground"
                        placeholder="0"
                        value={cashAdjustment === 0 ? "" : cashAdjustment}
                        onChange={(e) => setCashAdjustment(e.target.value === "" ? 0 : Number(e.target.value))}
                      />

                      <div className="grid grid-cols-3 gap-1 mt-1.5">
                        {[500, 1000, 5000].map((amount) => (
                          <Button
                            key={amount}
                            size="sm"
                            className="h-7 text-xs"
                            variant="secondary"
                            onClick={() => setCashAdjustment(cashAdjustment + amount)}
                          >
                            +{amount}
                          </Button>
                        ))}
                      </div>

                      <div className="flex items-center gap-1.5 mt-2">
                        <Button
                          size="icon"
                          className="h-7 w-7"
                          variant="secondary"
                          onClick={() => setCashAdjustment(Math.max(0, cashAdjustment - 500))}
                        >
                          <Minus className="h-4 w-4" />
                        </Button>

                        <div className="flex-1 text-center text-sm font-semibold">₹{cashAdjustment.toLocaleString()}</div>

                        <Button size="icon" className="h-7 w-7" variant="secondary" onClick={() => setCashAdjustment(cashAdjustment + 500)}>
                          <Plus className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>

                    <div className="mt-auto space-y-2">
                      <TradeFairnessCard yourValue={myValue} theirValue={theirValue} cashAdjustment={cashAdjustment} />
                    </div>
                  </div>
                </section>
              </div>
            </div>
          </Section>
        </AnimatedPage>
      </div>
    </main>
  )
}
