
import Link from "next/link"

import { MarketplaceItemCard } from "@/components/marketplace/item-card"
import type { ItemResponse } from "@/lib/types/api/item"

interface Props {
  items: ItemResponse[]
}

export function SimilarItems({ items }: Props) {
  const visibleItems = items
    .filter(
      (item) =>
        item.available && (item.forSale || item.forTrade),
    )
    .slice(0, 3)

  if (visibleItems.length === 0) {
    return null
  }

  return (
    <section>
      <div className="mb-5 flex items-end justify-between gap-4">
        <div>
          <p className="eyebrow">Marketplace</p>

          <h2 className="mt-2 text-xl font-bold tracking-tight">
            Similar listings
          </h2>

          <p className="mt-1 text-sm text-muted-foreground">
            Listings in the same category that may interest you.
          </p>
        </div>

        <span className="hidden rounded-full border border-border/50 bg-white/[0.02] px-2.5 py-1 text-[11px] text-muted-foreground sm:inline-flex">
          {visibleItems.length}{" "}
          {visibleItems.length === 1 ? "match" : "matches"}
        </span>
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
        {visibleItems.map((item) => (
          <Link
            key={item.itemId}
            href={`/item/${item.itemId}`}
            className="group block rounded-2xl outline-none transition-transform hover:-translate-y-1 focus-visible:ring-2 focus-visible:ring-primary/60"
          >
            <MarketplaceItemCard item={item} />
          </Link>
        ))}
      </div>
    </section>
  )
}
