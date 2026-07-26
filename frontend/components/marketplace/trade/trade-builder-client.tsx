"use client"

import { AnimatedPage, Section } from "@/components/marketplace/animations/section"
import { BackButton } from "@/components/marketplace/back-button"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { createTradeOffer } from "@/lib/api/trades/client"
import { TradeItemResponse } from "@/lib/types/api/trade"
import { AnimatePresence, motion } from "framer-motion"
import { Eye, Minus, Plus } from "lucide-react"
import Link from "next/link"
import * as React from "react"
import { TradeFairnessCard } from "./trade-fairness-card"
import { TradeItemCard } from "./trade-item-card"
import { useRouter } from "next/navigation"

interface Props {
  receiverId: number
  selectedItemId: number
  myItems: TradeItemResponse[]
  theirItems: TradeItemResponse[]
}

export function TradeBuilderClient({ receiverId, selectedItemId, myItems, theirItems }: Props) {
  const [selectedMine, setSelectedMine] = React.useState<number[]>([])
  const [selectedTheirs, setSelectedTheirs] = React.useState<number[]>([selectedItemId])
  const [cashAdjustment, setCashAdjustment] = React.useState(0)
  const [submitting, setSubmitting] = React.useState(false)

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

  const myValue = myItems.filter((item) => selectedMine.includes(item.itemId)).reduce((sum, item) => sum + item.userPrice, 0)

  const theirValue = theirItems.filter((item) => selectedTheirs.includes(item.itemId)).reduce((sum, item) => sum + item.userPrice, 0) + cashAdjustment

  const difference = Math.abs(myValue - theirValue)

  const youReceiveMore = theirValue > myValue
  const router = useRouter()

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
        senderItemIds: selectedMine,
        receiverItemIds: selectedTheirs,
        cashAdjustment,
      })

      console.log("Trade created", trade)
      router.push(`/trades`)

    } catch (error) {
      console.error(error)

      alert("Failed to create trade offer.")
    } finally {
      setSubmitting(false)
    }
  }
  return (
    <main className="pt-20 pb-24">
      <div className="mx-auto max-w-[1600px] px-4 sm:px-6 lg:px-8">
        <AnimatedPage>
          {/* Compact Workspace Header */}
          <Section>
            <div className="grid grid-cols-3 items-center mb-4">
              <div className="flex items-center gap-5">
                <BackButton />
              </div>

              <div className="justify-self-center">
                <h1 className="text-2xl  font-bold bg-gradient-to-r from-[var(--trust-blue)] via-[var(--negotiation-violet)] to-[var(--premium-gold)] bg-clip-text text-transparent">
                  Create Trade Offer
                </h1>
              </div>
              <div className="justify-self-end">
                <Button onClick={handleCreateTradeOffer} disabled={submitting} className="cta-primary h-11 w-full rounded-xl font-semibold">
                  {submitting ? "Creating..." : "Create Trade Offer"}
                </Button>
              </div>
            </div>
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
                  className={`trade-summary-pill ${difference === 0 ? "" : youReceiveMore ? "border-emerald-500/30" : "border-amber-500/30"}`}
                >
                  <span className="text-xs text-muted-foreground">Difference</span>

                  <span className="font-bold text-lg">₹{difference.toLocaleString()}</span>
                </div>
              </div>
              {/* WORKSPACE */}
              <div className="grid gap-4 lg:grid-cols-[1fr_1fr_285px] items-start">
                {/* MY INVENTORY */}

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

                {/* THEIR INVENTORY */}

                <section className="trade-card rounded-[26px] overflow-hidden flex flex-col h-[455px]">
                  <div className="trade-card-accent" />

                  <div className="px-4 py-2.5 border-b border-border/50">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <h3 className="font-semibold text-[15px]"> Inventory</h3>

                        <span className="text-xs text-muted-foreground">{myItems.length} items</span>
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
                              requestedItem={item.itemId === selectedItemId}
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
                        className="h-8"
                        value={cashAdjustment}
                        onChange={(e) => setCashAdjustment(Number(e.target.value))}
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
                      <TradeFairnessCard yourValue={myValue} theirValue={theirValue} />
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
