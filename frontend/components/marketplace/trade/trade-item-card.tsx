"use client"

import Image from "next/image"
import Link from "next/link"

import { motion } from "framer-motion"

import {CheckCircle2, Lock, Eye, Sparkles,} from "lucide-react"

import { cn } from "@/lib/utils"
import { Badge } from "@/components/ui/badge"
import { TradeItemResponse } from "@/lib/types/api/trade"

interface Props {
  item: TradeItemResponse

  selected?: boolean

  disabled?: boolean

  requestedItem?: boolean

  onClick?: () => void
}

export function TradeItemCard({ item, selected, disabled, requestedItem, onClick,}: Props) {
  return (
    <motion.div
      whileHover={{ y: -4 }}
      className={cn("relative glass rounded-xl p-3 transition-all cursor-pointer"
        , selected &&
            `
            border-[var(--trust-blue)]
            ring-2
            ring-[var(--trust-blue)]/40
            bg-[var(--trust-blue)]/5
            shadow-[0_0_35px_rgba(59,130,246,.2)]
            `
        , disabled && "opacity-50")}
      onClick={onClick}
    >
      <div className="flex gap-3">
        <div className="absolute bottom-3 right-3 z-20 flex gap-2">
          {requestedItem && (
            <Badge
              className="
              bg-[var(--premium-gold)]/20
              text-[var(--premium-gold)]
              border-[var(--premium-gold)]/30
              "
            >
              Requested
            </Badge>
          )}

          {selected && (
            <Badge
              className="
              bg-[var(--trust-blue)]/20
              text-[var(--trust-blue)]
              border-[var(--trust-blue)]/30
              "
            >
              Selected
            </Badge>
          )}
        </div>

        <div className="relative h-20 w-20 rounded-lg overflow-hidden shrink-0">
          <Image
            src={item.imageUrl}
            alt={item.name}
            fill
            className="object-cover"
          />

          {selected && (
            <div className="absolute inset-0 bg-primary/20 flex items-center justify-center">
              <CheckCircle2 className="h-7 w-7 text-primary" />
            </div>
          )}

          {disabled && (
            <div className="absolute inset-0 bg-background/80 flex items-center justify-center">
              <Lock className="h-5 w-5" />
            </div>
          )}
        </div>

        <div className="flex-1 min-w-0">

          <h4 className="font-medium line-clamp-1">
            {item.name}
          </h4>

          <div className="mt-1 flex items-center gap-1 text-xs text-muted-foreground">
            <Sparkles className="h-3 w-3 text-primary" />
            {item.condition}
          </div>

          <div className="mt-2 flex items-center justify-between">

            <div>
              <p className="text-xs text-muted-foreground">
                Value
              </p>

              <p className="font-semibold">
                ₹{item.userPrice.toLocaleString()}
              </p>
            </div>
          </div>
        </div>
      </div>
    </motion.div>
  )
}