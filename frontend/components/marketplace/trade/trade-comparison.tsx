"use client"

import Link from "next/link"

import { Eye, ArrowLeftRight } from "lucide-react"

import { Button } from "@/components/ui/button"
import { TradeItemCard } from "./trade-item-card"
import { TradeItemResponse } from "@/lib/types/api/trade"

interface Props {
  yourItems: TradeItemResponse[]
  theirItems: TradeItemResponse[]
}

export function TradeComparison({yourItems, theirItems,}: Props) {
  return (
    <div className="trade-card rounded-[28px] overflow-hidden">
      <div className="trade-card-accent" />

      <div className="p-5">
        <div className="flex items-center gap-2 mb-5">
          <ArrowLeftRight className="h-5 w-5 text-primary" />

          <h2 className="font-semibold">
            Trade Comparison
          </h2>
        </div>

        <div className="grid md:grid-cols-2 gap-5">

          <div>
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-medium">
                Your Items
              </h3>

              <span className="text-xs text-muted-foreground">
                {yourItems.length} items
              </span>
            </div>

            <div className="h-100 overflow-y-auto pr-2 scrollbar-thin">
              <div className="space-y-3">
              {yourItems.map((item) => (
                <div
                  key={item.itemId}
                  className="relative"
                >
                  <TradeItemCard
                    item={item}
                    selected={false}
                    onClick={() => {}}
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
                </div>
              ))}
              </div>
            </div>
          </div>

          <div>
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-medium">
                Their Items
              </h3>

              <span className="text-xs text-muted-foreground">
                {theirItems.length} items
              </span>
            </div>

            <div className="space-y-3">
              {theirItems.map((item) => (
                <div
                  key={item.itemId}
                  className="relative"
                >
                  <TradeItemCard
                    item={item}
                    selected={false}
                    onClick={() => {}}
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
                </div>
              ))}
            </div>
          </div>

        </div>
      </div>
    </div>
  )
}