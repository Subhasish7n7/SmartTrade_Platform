"use client"

import { motion } from "framer-motion"
import { ArrowRight, Clock3, MessageCircle } from "lucide-react"
import Link from "next/link"

import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import { TradeInboxResponse } from "@/lib/types/api/trade"
import { TradeStatusBadge } from "./trade-status-badge"

export function TradeInboxCard(trade: TradeInboxResponse) {
  return (
    <motion.div whileHover={{ y: -4 }} transition={{ duration: 0.2 }} className="trade-card trade-card-hover">
      <div className="trade-card-accent" />

      <div className="p-6">
        <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-5">
          <div className="flex items-center gap-4">
            <Avatar className="h-14 w-14 ring-2 ring-primary/20">
              <AvatarImage src={trade.otherUserProfilePicture} />
              <AvatarFallback>{trade.otherUserName.slice(0, 2)}</AvatarFallback>
            </Avatar>

            <div>
              <div className="flex items-center gap-2 flex-wrap">
                <h3 className="font-semibold text-lg">{trade.otherUserName}</h3>

                <TradeStatusBadge status={trade.status} />
              </div>

              <div className="flex items-center gap-4 mt-2 text-sm">
                <span className="flex items-center gap-1 text-muted-foreground">
                  <Clock3 className="h-4 w-4" />
                  {trade.updatedAt}
                </span>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-3 gap-6 text-center">
            <div>
              <p className="text-xs text-muted-foreground">Your Items</p>

              <p className="font-bold text-xl">{trade.yourItemCount}</p>
            </div>

            <div>
              <p className="text-xs text-muted-foreground">Their Items</p>

              <p className="font-bold text-xl">{trade.theirItemCount}</p>
            </div>

            <div>
              <p className="text-xs text-muted-foreground">Difference</p>

              <p className="font-bold text-warning">₹{trade.valueDifference.toLocaleString()}</p>
            </div>
          </div>
        </div>

        <div className="flex flex-wrap gap-3 mt-6">
          <Link href={`/trades/${trade.tradeId}?tradeOfferId=${trade.latestOffer.tradeOfferId}`}>
            <Button>
              <ArrowRight className="h-4 w-4 mr-2" />
              Open Trade
            </Button>
          </Link>

          <Link href={"/messages"}>
            <Button variant="secondary" className="gap-2">
              <MessageCircle className="h-4 w-4" />
              Open Chat
            </Button>
          </Link>
        </div>
      </div>
    </motion.div>
  )
}
