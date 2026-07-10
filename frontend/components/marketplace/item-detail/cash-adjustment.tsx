"use client"

import * as React from "react"

import {
  Minus,
  Plus,
} from "lucide-react"

import { Button } from "@/components/ui/button"

import { cn } from "@/lib/utils"

export function CashAdjustment() {
  const [cashAdjustment, setCashAdjustment] =
    React.useState(0)

  return (
    <div>
      <p className="text-sm text-muted-foreground mb-2">
        Cash Adjustment
      </p>

      <div className="flex items-center gap-3">
        <Button
          variant="outline"
          size="icon"
          onClick={() =>
            setCashAdjustment(
              (prev) =>
                Math.max(
                  -500,
                  prev - 50
                )
            )
          }
        >
          <Minus className="h-4 w-4" />
        </Button>

        <div className="flex-1 text-center">
          <span
            className={cn(
              "text-2xl font-bold",
              cashAdjustment > 0 &&
                "text-emerald-400",
              cashAdjustment < 0 &&
                "text-red-400"
            )}
          >
            {cashAdjustment >= 0
              ? "+"
              : ""}
            $
            {Math.abs(
              cashAdjustment
            )}
          </span>

          <p className="text-xs text-muted-foreground mt-1">
            {cashAdjustment > 0
              ? "You receive"
              : cashAdjustment < 0
              ? "You pay"
              : "No cash"}
          </p>
        </div>

        <Button
          variant="outline"
          size="icon"
          onClick={() =>
            setCashAdjustment(
              (prev) =>
                Math.min(
                  500,
                  prev + 50
                )
            )
          }
        >
          <Plus className="h-4 w-4" />
        </Button>
      </div>
    </div>
  )
}