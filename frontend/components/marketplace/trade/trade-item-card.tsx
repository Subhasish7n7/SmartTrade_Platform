"use client"

import Image from "next/image"
import { CheckCircle2, Lock, Sparkles } from "lucide-react"
import { motion } from "framer-motion"

import type { TradeItemResponse } from "@/lib/types/api/trade"
import { cn } from "@/lib/utils"

interface Props {
  item: TradeItemResponse
  selected?: boolean
  disabled?: boolean
  requestedItem?: boolean
  showValuation?: boolean
  onClick?: () => void
}

export function TradeItemCard({
  item,
  selected,
  disabled,
  requestedItem,
  showValuation = false,
  onClick,
}: Props) {
  return (
    <motion.div
      whileHover={{ y: disabled ? 0 : -2 }}
      transition={{ duration: 0.15 }}
      className={cn(
        "relative cursor-pointer overflow-hidden rounded-xl border border-border/50 bg-card/60 p-2.5 backdrop-blur-xl transition-all",
        selected &&
          "border-[var(--trust-blue)] bg-[var(--trust-blue)]/[0.08] shadow-[0_0_18px_rgba(59,130,246,.13)]",
        disabled && "cursor-not-allowed opacity-50",
      )}
      onClick={onClick}
    >
      <div className="absolute bottom-2 right-2 z-10 flex flex-col items-end gap-1">
        {requestedItem && (
          <div
            className="flex h-5 w-5 items-center justify-center rounded-full border border-[var(--premium-gold)]/30 bg-[var(--premium-gold)]/15 text-[10px]"
            title="Requested item"
          >
            ⭐
          </div>
        )}
        {selected && (
          <div className="flex h-5 w-5 items-center justify-center rounded-full border border-[var(--trust-blue)]/30 bg-[var(--trust-blue)]/15">
            <CheckCircle2 className="h-3.5 w-3.5 text-[var(--trust-blue)]" />
          </div>
        )}
      </div>

      <div className="flex gap-2.5">
        <div className="relative h-[68px] w-[68px] shrink-0 overflow-hidden rounded-lg border border-white/[0.05] bg-white/[0.025]">
          {item.imageUrl ? (
            <Image
              src={item.imageUrl}
              alt={item.name}
              fill
              className="object-cover"
              sizes="68px"
            />
          ) : (
            <div className="flex h-full w-full items-center justify-center text-[10px] text-muted-foreground">
              No image
            </div>
          )}

          {selected && (
            <div className="absolute inset-0 flex items-center justify-center bg-[var(--trust-blue)]/20">
              <CheckCircle2 className="h-5 w-5 text-white" />
            </div>
          )}

          {disabled && (
            <div className="absolute inset-0 flex items-center justify-center bg-background/80">
              <Lock className="h-5 w-5" />
            </div>
          )}
        </div>

        <div className="min-w-0 flex-1">
          <h4 className="line-clamp-1 pr-5 text-[13px] font-semibold leading-tight">
            {item.name}
          </h4>

          <div className="mt-0.5 flex items-center gap-1 text-[10px] text-muted-foreground">
            <Sparkles className="h-3 w-3 shrink-0 text-[var(--negotiation-violet)]" />
            <span className="truncate">{formatCondition(item.condition)}</span>
          </div>

          <div className="mt-1.5 flex flex-wrap items-center gap-x-2 gap-y-1">
            <span className="text-[13px] font-bold tabular-nums">
              ₹{item.userPrice.toLocaleString("en-IN")}
            </span>

            {showValuation && (
              <>
                <span className="text-[9px] uppercase tracking-[0.08em] text-muted-foreground/70">
                  listed
                </span>
                <span className="text-[10px] text-[var(--negotiation-violet)]">
                  Used {item.systemGeneratedPrice == null ? "N/A" : `₹${item.systemGeneratedPrice.toLocaleString("en-IN")}`}
                </span>
                <span className="text-[10px] text-[var(--trust-blue)]">
                  New {item.newPrice == null ? "N/A" : `₹${item.newPrice.toLocaleString("en-IN")}`}
                </span>
              </>
            )}
          </div>
        </div>
      </div>
    </motion.div>
  )
}

function formatCondition(value: string) {
  return value
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (letter) => letter.toUpperCase())
}