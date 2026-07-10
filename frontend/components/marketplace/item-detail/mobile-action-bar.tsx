"use client"

import {
  ArrowLeftRight,
  Heart,
  Share2,
  ShoppingCart,
} from "lucide-react"

import { Button } from "@/components/ui/button"
import Link from "next/link"

interface Props {
  price: number
  itemId:number
}

export function MobileActionBar({ price, itemId}: Props) {
  return (
    <div className="lg:hidden fixed bottom-0 left-0 right-0 p-4 glass-strong border-t border-border z-40 bg-background/95 backdrop-blur">
      <div className="flex items-center gap-3 mb-3">
        <div className="flex-1">
          <p className="text-xs text-muted-foreground">
            Listed Price
          </p>

          <p className="text-xl font-bold">
            ${price.toLocaleString()}
          </p>
        </div>

        <Button
          variant="ghost"
          size="icon"
        >
          <Heart className="h-5 w-5" />
        </Button>

        <Button
          variant="ghost"
          size="icon"
        >
          <Share2 className="h-5 w-5" />
        </Button>
      </div>

      <div className="grid grid-cols-2 gap-3">
        <Button className="h-12 rounded-xl font-semibold">
          <ShoppingCart className="h-4 w-4 mr-2" />

          Buy Now
        </Button>

        <Link href={`/trades/create/${itemId}`}>
        <Button
          variant="outline"
          className="h-12 rounded-xl font-semibold"
        >
          <ArrowLeftRight className="h-4 w-4 mr-2" />

          Trade
        </Button>
        </Link>
      </div>
    </div>
  )
}