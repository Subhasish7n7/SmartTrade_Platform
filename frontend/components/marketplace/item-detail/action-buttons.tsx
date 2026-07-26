"use client"

import Link from "next/link"
import * as React from "react"

import { ArrowLeftRight, Heart, Share2, ShoppingCart } from "lucide-react"

import { Button } from "@/components/ui/button"

import { ItemResponse } from "@/lib/types/api/item"
import { cn } from "@/lib/utils"

interface Props {
  item: ItemResponse
  userId: number | null
}

export function ActionButtons({ item, userId }: Props) {
  const [wishlisted, setWishlisted] = React.useState(false)

  const isOwner = userId != null && userId === item.seller.userId

  return (
    <div className="content-surface flex flex-col gap-3">
      {isOwner ? (
        <div className="rounded-xl border border-dashed p-4 text-center text-sm text-muted-foreground">You're viewing your own item.</div>
      ) : (
        <>
          {item.forSale && (
            <Button className="w-full h-14 text-lg font-semibold rounded-xl">
              <ShoppingCart className="h-5 w-5 mr-2" />
              Buy Now - ${item.userPrice.toLocaleString()}
            </Button>
          )}

          {item.forTrade && (
            <Link href={`/trades/create/${item.seller.userId}?itemId=${item.itemId}`}>
              <Button variant="trade" className="w-full h-14 text-lg font-semibold rounded-xl">
                <ArrowLeftRight className="h-5 w-5 mr-2" />
                Make Trade Offer
              </Button>
            </Link>
          )}

          <Button
            variant="surface"
            className={cn("h-12 rounded-xl", wishlisted && "text-red-500")}
            onClick={() => setWishlisted(!wishlisted)}
          >
            <Heart className={cn("h-5 w-5 mr-2", wishlisted && "fill-current")} />
            Wishlist
          </Button>
        </>
      )}

      {/* Share is always available */}
      <Button variant="surface" className="h-12 rounded-xl">
        <Share2 className="h-5 w-5 mr-2" />
        Share
      </Button>
    </div>
  )
}
