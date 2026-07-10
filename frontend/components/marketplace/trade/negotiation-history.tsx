"use client"

import Link from "next/link"
import {History,ChevronDown,Clock3,} from "lucide-react"
import { Collapsible, CollapsibleTrigger, CollapsibleContent,} from "@/components/ui/collapsible"
import { TradeHistoryResponse } from "@/lib/types/api/trade"

interface Props {
  tradeId: number
  history: TradeHistoryResponse[]
}
export function NegotiationHistory({tradeId, history}: Props) {
  return (
    <Collapsible defaultOpen>
      <div className="trade-card rounded-[28px] overflow-hidden">
        <div className="trade-card-accent" />

        <CollapsibleTrigger className="w-full p-5">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <History className="h-5 w-5 text-primary" />

              <div className="text-left">
                <h3 className="font-semibold">
                  Negotiation History
                </h3>

                <p className="text-sm text-muted-foreground">
                  Previous offers and revisions
                </p>
              </div>
            </div>

            <ChevronDown className="h-4 w-4" />
          </div>
        </CollapsibleTrigger>

        <CollapsibleContent>
          <div className="px-5 pb-5 space-y-3">
            {history.map((offer) => (
              <Link
                key={offer.tradeOfferId}
                href={{pathname: `/trades/${tradeId}`,
                  query: {
                    tradeOfferId: offer.tradeOfferId,
                    view: "history",
                  },
                }}
              >
                <div className="trade-stat hover:border-primary/20 transition-all">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="font-medium">
                        {offer.senderName}
                      </p>

                      <div className="flex items-center gap-2 text-xs text-muted-foreground mt-1">
                        <Clock3 className="h-3 w-3" />
                        {offer.createdAt}
                      </div>
                    </div>

                    <span className="text-primary text-sm">
                      View
                    </span>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        </CollapsibleContent>
      </div>
    </Collapsible>
  )
}