import Link from "next/link"
import { MarketplaceItemCard } from "@/components/marketplace/item-card"
import { demoItem } from "@/lib/mock/item-detail"

export function SimilarItems() {
  const items = [demoItem, demoItem, demoItem]

  return (
    <div>
      <div className="divider-label mb-6">Similar Items</div>

      <div className="grid grid-cols-1 md:grid-cols-3 xl:grid-cols-3 gap-4">
        {items.map((item, index) => (
          <Link key={index} href={`/item/${index + 2}`} className="transition-transform hover:-translate-y-1">
            <MarketplaceItemCard item={item} />
          </Link>
        ))}
      </div>
    </div>
  )
}