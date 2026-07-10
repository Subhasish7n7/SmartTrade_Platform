"use client"

import Link from "next/link"
import * as React from "react"

import { ArrowLeftRight, Heart, Share2, ShoppingCart } from "lucide-react"

import { Button } from "@/components/ui/button"

import { cn } from "@/lib/utils"

interface Props {
  price: number
  forSale: boolean
  forTrade: boolean
  itemId: number
  otherUserId:number
}

export function ActionButtons({ price, forSale, forTrade, itemId, otherUserId }: Props) {
  const [wishlisted, setWishlisted] = React.useState(false)

  return (
    <div className="content-surface flex flex-col gap-3">
      {forSale && (
        <Button className="w-full h-14 text-lg font-semibold rounded-xl">
          <ShoppingCart className="h-5 w-5 mr-2" />
          Buy Now - ${price.toLocaleString()}
        </Button>
      )}

      {forTrade && (
        <Link href={`/trades/create/${otherUserId}?itemId=${itemId}`}>
          <Button variant="trade" className="w-full h-14 text-lg font-semibold rounded-xl">
            <ArrowLeftRight className="h-5 w-5 mr-2" />
            Make Trade Offer
          </Button>
        </Link>  
      )}
      <div className="grid grid-cols-2 gap-3">
        <Button
          variant="surface"
          className={cn("h-12 rounded-xl", wishlisted && "text-red-500")}
          onClick={() => setWishlisted(!wishlisted)}
        >
          <Heart className={cn("h-5 w-5 mr-2", wishlisted && "fill-current")} />
          Wishlist
        </Button>

        <Button variant="surface" className="h-12 rounded-xl">
          <Share2 className="h-5 w-5 mr-2" />
          Share
        </Button>
      </div>
      
    </div>
  )
}
