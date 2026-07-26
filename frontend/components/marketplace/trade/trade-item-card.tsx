"use client"

import Image from "next/image"

import { motion } from "framer-motion"

import { CheckCircle2, Lock, Sparkles } from "lucide-react"

import { TradeItemResponse } from "@/lib/types/api/trade"
import { cn } from "@/lib/utils"

interface Props {
  item: TradeItemResponse

  selected?: boolean

  disabled?: boolean

  requestedItem?: boolean

  onClick?: () => void
}

export function TradeItemCard({ item, selected, disabled, requestedItem, onClick }: Props) {
  return (
    <motion.div
      whileHover={{ y: -2 }}
      transition={{ duration: 0.15 }}
      className={cn(
        `
    relative
    overflow-hidden
    rounded-xl
    border
    border-border/50
    bg-card/60
    backdrop-blur-xl
    p-2.5
    cursor-pointer
    transition-all
    `,
        selected &&
          `
border-[var(--trust-blue)]
bg-[var(--trust-blue)]/8
shadow-[0_0_18px_rgba(59,130,246,.15)]
`,
        disabled && "opacity-50",
      )}
      onClick={onClick}
    >
      <div className="flex gap-2.5">
        <div className="absolute bottom-2 right-2 flex flex-col items-end gap-1">
          {requestedItem && (
            <div
              className="
            h-5
            w-5
            rounded-full
            flex
            items-center
            justify-center
            bg-[var(--premium-gold)]/15
            border
            border-[var(--premium-gold)]/30
            "
              title="Requested Item"
            >
              ⭐
            </div>
          )}

          {selected && (
            <div
              className="
            h-5
            w-5
            rounded-full
            flex
            items-center
            justify-center
            bg-[var(--trust-blue)]/15
            border
            border-[var(--trust-blue)]/30
            "
            >
              <CheckCircle2 className="h-3.5 w-3.5 text-[var(--trust-blue)]" />
            </div>
          )}
        </div>

        <div className="relative h-[68px] w-[68px] rounded-lg overflow-hidden shrink-0">
          <Image src={item.imageUrl} alt={item.name} fill className="object-cover" />

          {selected && (
            <div className="absolute inset-0 bg-primary/20 flex items-center justify-center">
              <CheckCircle2 className="h-5 w-5 text-primary" />
            </div>
          )}

          {disabled && (
            <div className="absolute inset-0 bg-background/80 flex items-center justify-center">
              <Lock className="h-5 w-5" />
            </div>
          )}
        </div>

        <div className="flex-1 min-w-0 flex flex-col justify-center">
          <h4 className="font-medium text-[15px] leading-tight line-clamp-1">{item.name}</h4>

          <div className="mt-0.5 flex items-center gap-1 text-[11px] text-muted-foreground">
            <Sparkles className="h-3 w-3 text-primary shrink-0" />
            <span className="truncate">{item.condition}</span>
          </div>

          <div className="mt-1 font-semibold text-[15px]">₹{item.userPrice.toLocaleString()}</div>
        </div>
      </div>
    </motion.div>
  )
}
