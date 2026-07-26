// components/marketplace/profile/profile-inventory-tab.tsx
"use client"

import Link from "next/link"

import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"

import { Eye, PackageOpen } from "lucide-react"

import { ProfileItem } from "@/app/profile/page"

interface Props {
  items: ProfileItem[]
}

export function ProfileInventoryTab({ items }: Props) {
  if (items.length === 0) {
    return (
      <div className="content-surface py-20 text-center">
        <PackageOpen className="mx-auto h-12 w-12 text-muted-foreground" />

        <h2 className="mt-5 text-2xl font-semibold">
          No Listings
        </h2>

        <p className="mt-2 text-muted-foreground">
          This user hasn't listed any items yet.
        </p>
      </div>
    )
  }

  return (
    <div className="grid gap-5 sm:grid-cols-2 xl:grid-cols-3">
      {items.map((item) => (
        <div key={item.id} className="trade-card trade-card-hover">

          <div className="trade-card-accent" />

          <div className="aspect-square animate-shimmer" />

          <div className="p-5">

            <div className="flex flex-wrap gap-2 mb-3">

              {item.forSale && (
                <Badge>
                  For Sale
                </Badge>
              )}

              {item.forTrade && (
                <Badge
                  variant="secondary"
                >
                  Trade
                </Badge>
              )}

              <Badge
                variant="outline"
              >
                {item.condition}
              </Badge>

            </div>

            <h3 className="font-semibold text-lg">
              {item.name}
            </h3>

            <p className="mt-2 text-2xl font-bold">
              ₹{item.price.toLocaleString()}
            </p>

            <Link href={`/item/${item.id}`}>
              <Button className="w-full mt-5 cta-primary">
                <Eye className="mr-2 h-4 w-4" />
                View Item
              </Button>
            </Link>

          </div>

        </div>
      ))}
    </div>
  )
}