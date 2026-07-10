"use client"

import * as React from "react"
import Link from "next/link"

import { ArrowLeftRight, Eye, Plus, Minus} from "lucide-react"
import { Button } from "@/components/ui/button"
import { TradeItemCard } from "./trade-item-card"
import { TradeFairnessCard } from "./trade-fairness-card"
import { AnimatePresence, motion } from "framer-motion"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { TradeItemResponse } from "@/lib/types/api/trade"

interface Props {
  selectedItemId:number
  myItems: TradeItemResponse[]
  theirItems: TradeItemResponse[]
}

export function TradeBuilderClient({selectedItemId, myItems, theirItems,}: Props) {
  const [selectedMine, setSelectedMine] = React.useState<number[]>([])
  const [selectedTheirs, setSelectedTheirs] = React.useState<number[]>([selectedItemId,])
  const [cashAdjustment, setCashAdjustment] = React.useState(0)
  const [tradeMessage, setTradeMessage] =
  React.useState(
    "Hi, I'm interested in this trade. Let me know what you think."
  )

  function toggleMine(item: TradeItemResponse) {
    setSelectedMine((prev) =>
      prev.includes(item.itemId)
        ? prev.filter((id) => id !== item.itemId)
        : [...prev, item.itemId]
    )
  }

  function toggleTheirs(
    item: TradeItemResponse
  ) {
    setSelectedTheirs((prev) =>
      prev.includes(item.itemId)
        ? prev.filter(
            (id) => id !== item.itemId
          )
        : [...prev, item.itemId]
    )
  }

  const sortedMine = [
    ...myItems.filter((item) =>
      selectedMine.includes(item.itemId)
    ),
    ...myItems.filter(
      (item) =>
        !selectedMine.includes(item.itemId)
    ),
  ]

  const sortedTheirs = [
    ...theirItems.filter((item) =>
      selectedTheirs.includes(item.itemId)
    ),
    ...theirItems.filter(
      (item) =>
        !selectedTheirs.includes(item.itemId)
    ),
  ]

  const myValue = myItems
    .filter((item) =>
      selectedMine.includes(item.itemId)
    )
    .reduce(
      (sum, item) =>
        sum + item.userPrice,
      0
    )

  const theirValue =
    theirItems
      .filter((item) =>
        selectedTheirs.includes(
          item.itemId
        )
      )
      .reduce(
        (sum, item) =>
          sum + item.userPrice,
        0
      ) + cashAdjustment

  return (
    <div className="grid lg:grid-cols-12 gap-6">
      {/* LEFT */}
      <div className="lg:col-span-8 flex">
        <div className="trade-card rounded-[28px] overflow-hidden flex-1">
        <div className="trade-card-accent" />

        <div className="p-5">
          <div className="flex items-center gap-2 mb-5">
            <ArrowLeftRight className="h-5 w-5 text-primary" />

            <h2 className="font-semibold">
              Build Trade Offer
            </h2>
          </div>

          <div className="grid md:grid-cols-2 gap-5">
            {/* MY ITEMS */}
            <div>
              <div className="flex items-center justify-between mb-4">
                <h3 className="font-medium">
                  Your Items
                </h3>

                <span className="text-xs text-muted-foreground">
                  {selectedMine.length}
                  {" "}selected
                </span>
              </div>

              <div className="h-[900px] overflow-y-auto pr-2 scrollbar-thin">
                <AnimatePresence>
                <div className="space-y-3">
                  {sortedMine.map((item) => (
                    <motion.div
                        layout
                        transition={{
                            duration: 0.25,
                        }}
                        key={item.itemId}
                        className="relative"
                        >
                      <TradeItemCard
                        item={item}
                        selected={selectedMine.includes(
                          item.itemId
                        )}
                        onClick={() =>
                          toggleMine(item)
                        }
                      />

                      <Link
                        href={`/item/${item.itemId}`}
                        className="absolute top-3 left-3 z-20"
                      >
                        <Button
                          size="sm"
                          variant="secondary"
                        >
                          <Eye className="h-4 w-4" />
                        </Button>
                      </Link>
                    </motion.div>
                  ))}
                </div>
                </AnimatePresence>
              </div>
            </div>

            {/* THEIR ITEMS */}
            <div>
              <div className="flex items-center justify-between mb-4">
                <h3 className="font-medium">
                  Their Items
                </h3>

                <span className="text-xs text-muted-foreground">
                  {selectedTheirs.length}
                  {" "}selected
                </span>
              </div>

              <div className="h-[900px] overflow-y-auto pr-2 scrollbar-thin">
                <AnimatePresence>
                <div className="space-y-3">
                  {sortedTheirs.map((item) => (
                    <motion.div
                        layout
                        transition={{
                            duration: 0.25,
                        }}
                        key={item.itemId}
                        className="relative"
                        >
                      <TradeItemCard
                        item={item}
                        requestedItem={
                          item.itemId === selectedItemId
                        }
                        selected={selectedTheirs.includes(
                          item.itemId
                        )}
                        onClick={() =>
                          toggleTheirs(item)
                        }
                      />

                      <Link
                        href={`/item/${item.itemId}`}
                        className="absolute top-3 left-3 z-20"
                      >
                        <Button
                          size="sm"
                          variant="secondary"
                        >
                          <Eye className="h-4 w-4" />
                        </Button>
                      </Link>
                    </motion.div>
                  ))}
                </div>
                </AnimatePresence>
              </div>
            </div>
          </div>
        </div>
          
        </div>
      </div>

      {/* RIGHT */}
      <div className="lg:col-span-4">
        <div className="sticky top-24 space-y-4">
          <div className="trade-card rounded-[28px] overflow-hidden">
          <div className="trade-card-accent" />

          <div className="p-5">
            <h3 className="font-semibold mb-5">
              Trade Summary
            </h3>

            <div className="grid grid-cols-2 gap-3 mb-4">

              <div className="trade-stat flex items-center justify-between">
                <span className="text-sm text-muted-foreground">
                  Offer
                </span>

                <span className="font-bold">
                  {selectedMine.length}
                </span>
              </div>

              <div className="trade-stat flex items-center justify-between">
                <span className="text-sm text-muted-foreground">
                  Receive
                </span>

                <span className="font-bold">
                  {selectedTheirs.length}
                </span>
              </div>

            </div>

            <div className="trade-section-divider my-4" />

            <div className="space-y-3">

              <div className="flex justify-between">
                <span className="text-muted-foreground">
                  Your Value
                </span>

                <span className="font-semibold">
                  ₹{myValue.toLocaleString()}
                </span>
              </div>

              <div className="flex justify-between">
                <span className="text-muted-foreground">
                  Their Value
                </span>

                <span className="font-semibold">
                  ₹{theirValue.toLocaleString()}
                </span>
              </div>

              <div className="flex justify-between">
                <span className="text-muted-foreground">
                  Difference
                </span>

                <span className="font-semibold">
                  ₹{Math.abs(
                    myValue - theirValue
                  ).toLocaleString()}
                </span>
              </div>

            </div>
          </div>
        </div>

          <div className="content-surface p-5">
            <h3 className="font-semibold mb-4">
              Cash Adjustment
            </h3>

            <div className="space-y-4">
              <Input
                type="number"
                value={cashAdjustment}
                onChange={(e) =>
                  setCashAdjustment(
                    Number(e.target.value)
                  )
                }
              />

              <div className="grid grid-cols-3 gap-2">
                {[500, 1000, 5000].map(
                  (amount) => (
                    <Button
                      key={amount}
                      variant="secondary"
                      size="sm"
                      onClick={() =>
                        setCashAdjustment(
                          cashAdjustment +
                            amount
                        )
                      }
                    >
                      +{amount}
                    </Button>
                  )
                )}
              </div>

              <div className="flex items-center gap-3">
                <Button
                  size="icon"
                  variant="secondary"
                  onClick={() =>
                    setCashAdjustment(
                      Math.max(
                        0,
                        cashAdjustment - 500
                      )
                    )
                  }
                >
                  <Minus className="h-4 w-4" />
                </Button>

                <div className="flex-1 text-center font-bold">
                  ₹{cashAdjustment.toLocaleString()}
                </div>

                <Button
                  size="icon"
                  variant="secondary"
                  onClick={() =>
                    setCashAdjustment(
                      cashAdjustment + 500
                    )
                  }
                >
                  <Plus className="h-4 w-4" />
                </Button>
              </div>
            </div>
          </div>

          <div className="content-surface p-5">
            <h3 className="font-semibold mb-3">
              Message to Seller
            </h3>

            <Textarea
              value={tradeMessage}
              onChange={(e) =>
                setTradeMessage(
                  e.target.value
                )
              }
              rows={3}
            />
          </div>

          <TradeFairnessCard
            yourValue={myValue}
            theirValue={theirValue}
          />

          <Button
            size="lg"
            className="w-full"
          >
            Create Trade Offer
          </Button>
        </div>
      </div>
    </div>
    
  )
}