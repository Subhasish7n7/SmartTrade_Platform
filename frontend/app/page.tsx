"use client"
import { motion } from "framer-motion"
import { MapPin } from "lucide-react"
import Link from "next/link"
import * as React from "react"

import { FilterPanel, FloatingFilterButton } from "@/components/marketplace/filter-sidebar"
import { Hero } from "@/components/marketplace/hero"
import { MarketplaceItemCard, MarketplaceItemCardSkeleton } from "@/components/marketplace/item-card"
import { Navbar } from "@/components/marketplace/navbar"

import { getMarketplaceItems } from "@/lib/api/items/client"
import { ItemResponse } from "@/lib/types/api/item"

export default function MarketplacePage() {
  const [wishlisted, setWishlisted] = React.useState<Set<number>>(new Set())
  const [isFilterOpen, setIsFilterOpen] = React.useState(false)
  const [isLoading, setIsLoading] = React.useState(true)

  const [items, setItems] = React.useState<ItemResponse[]>([])

  React.useEffect(() => {
    const fetchMarketplaceItems = async () => {
      try {
        const data = await getMarketplaceItems()
        setItems(data)
      } catch (error) {
        console.error("Failed to fetch marketplace items:", error)
      } finally {
        setIsLoading(false)
      }
    }

    fetchMarketplaceItems()
  }, [])

  const toggleWishlist = (id: number) => {
    setWishlisted((prev) => {
      const next = new Set(prev)

      if (next.has(id)) {
        next.delete(id)
      } else {
        next.add(id)
      }

      return next
    })
  }

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <Hero />

      <main className="mx-auto max-w-400 px-4 sm:px-6 lg:px-8 pb-24">
        {/* Section Header */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h2 className="text-2xl sm:text-3xl font-bold text-foreground">Nearby Listings</h2>

            <p className="text-sm text-muted-foreground flex items-center gap-1.5 mt-1">
              <MapPin className="h-4 w-4" />
              {items.length} items within 25 miles
            </p>
          </div>

          <div className="hidden sm:flex items-center gap-3">
            <span className="text-sm text-muted-foreground">Sort by:</span>

            <select className="bg-muted/50 border border-border/50 rounded-xl px-4 py-2 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all">
              <option>Relevance</option>
              <option>Price: Low to High</option>
              <option>Price: High to Low</option>
              <option>Newest First</option>
              <option>Distance</option>
            </select>
          </div>
        </div>

        {/* Items Grid */}
        {isLoading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5 lg:gap-6">
            {Array.from({ length: 12 }).map((_, i) => (
              <MarketplaceItemCardSkeleton key={i} />
            ))}
          </div>
        ) : (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.4 }}
            className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5 lg:gap-6"
          >
            {items.map((item, index) => (
              <motion.div
                key={item.itemId}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{
                  duration: 0.4,
                  delay: index * 0.03,
                }}
              >
                <Link href={`/item/${item.itemId}`}>
                  <MarketplaceItemCard item={item} isWishlisted={wishlisted.has(item.itemId)} onWishlistToggle={toggleWishlist} />
                </Link>
              </motion.div>
            ))}
          </motion.div>
        )}

        {/* Load More */}
        {!isLoading && (
          <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 0.5 }} className="mt-12 text-center">
            <motion.button
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              className="px-10 py-4 rounded-2xl glass-strong border-primary/20 text-foreground font-semibold transition-all hover:border-primary/40 hover:shadow-lg hover:shadow-primary/10"
            >
              Load More Items
            </motion.button>
          </motion.div>
        )}
      </main>

      {/* Filter Panel */}
      <FilterPanel isOpen={isFilterOpen} onClose={() => setIsFilterOpen(false)} />

      {/* Floating Filter Button */}
      <FloatingFilterButton onClick={() => setIsFilterOpen(true)} activeCount={0} />
    </div>
  )
}
