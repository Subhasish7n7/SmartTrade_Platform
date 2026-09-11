"use client"

import { motion } from "framer-motion"
import { MapPin } from "lucide-react"
import Link from "next/link"
import * as React from "react"

import { FilterPanel, FloatingFilterButton } from "@/components/marketplace/filter-sidebar"
import { Hero } from "@/components/marketplace/hero"
import { MarketplaceItemCard, MarketplaceItemCardSkeleton } from "@/components/marketplace/item-card"
import { Navbar } from "@/components/marketplace/navbar"
import { searchMarketplaceItems } from "@/lib/api/items/client"
import { ItemResponse, ItemSearchFilters, ItemSort } from "@/lib/types/api/item"

export default function MarketplacePage() {
  const [wishlisted, setWishlisted] = React.useState<Set<number>>(new Set())
  const [isFilterOpen, setIsFilterOpen] = React.useState(false)
  const [isLoading, setIsLoading] = React.useState(true)
  const [isLoadingMore, setIsLoadingMore] = React.useState(false)
  const [items, setItems] = React.useState<ItemResponse[]>([])
  const [filters, setFilters] = React.useState<ItemSearchFilters>({
    sort: "RELEVANCE",
    radiusKm: 25,
    page: 0,
    size: 24,
  })
  const [hasNext, setHasNext] = React.useState(false)
  const hasLocation = filters.latitude !== undefined && filters.longitude !== undefined
  const [totalItems, setTotalItems] = React.useState(0)
  const loadItems = React.useCallback(async (nextFilters: ItemSearchFilters) => {
    try {
      setIsLoading(true)

      const data = await searchMarketplaceItems({
        ...nextFilters,
        page: 0,
      })

      setItems(data.items)

      setTotalItems(data.totalItems)

      setHasNext(data.hasNext)

      setFilters({
        ...nextFilters,
        page: 0,
      })
    } catch (error) {
      console.error("Failed to fetch marketplace items:", error)
    } finally {
      setIsLoading(false)
    }
  }, [])

  React.useEffect(() => {
    const loadInitialItems = () => {
      const baseFilters: ItemSearchFilters = {
        sort: "RELEVANCE",
        page: 0,
        size: 24,
        radiusKm: 25,
      }

      if (!navigator.geolocation) {
        loadItems(baseFilters)
        return
      }

      navigator.geolocation.getCurrentPosition(
        (position) => {
          loadItems({
            ...baseFilters,
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
          })
        },
        () => {
          loadItems(baseFilters)
        },
      )
    }

    loadInitialItems()
  }, [loadItems])

  const loadMore = async () => {
    if (!hasNext || isLoadingMore) {
      return
    }

    try {
      setIsLoadingMore(true)

      const nextPage = (filters.page ?? 0) + 1

      const data = await searchMarketplaceItems({
        ...filters,
        page: nextPage,
      })

      setItems((previous) => [...previous, ...data.items])

      setFilters((previous) => ({
        ...previous,
        page: nextPage,
      }))

      setHasNext(data.hasNext)
    } catch (error) {
      console.error("Failed to load more marketplace items:", error)
    } finally {
      setIsLoadingMore(false)
    }
  }

  const toggleWishlist = (id: number) => {
    setWishlisted((previous) => {
      const next = new Set(previous)

      if (next.has(id)) {
        next.delete(id)
      } else {
        next.add(id)
      }

      return next
    })
  }

  const handleFiltersApply = (nextFilters: ItemSearchFilters) => {
    setIsFilterOpen(false)

    loadItems({
      ...nextFilters,
      page: 0,
      size: 24,
    })
  }

  const handleSortChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const sort = event.target.value as ItemSort

    loadItems({
      ...filters,
      sort,
      page: 0,
    })
  }

  const activeFilterCount = React.useMemo(() => {
    let count = 0
    if (filters.category) count++
    if (filters.conditions?.length) count++
    if (filters.minPrice !== undefined) count++
    if (filters.maxPrice !== undefined) count++
    if (filters.listingType) count++
    if (filters.radiusKm !== undefined && filters.radiusKm !== 25) count++
    if (filters.labels?.length) count++
    return count
  }, [filters])

  return (
    <div className="min-h-screen bg-background">
      <Navbar />

      <Hero />

      <main className="mx-auto max-w-400 px-4 sm:px-6 lg:px-8 pb-24">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h2 className="text-2xl sm:text-3xl font-bold text-foreground">{hasLocation ? "Nearby Listings" : "Marketplace Listings"}</h2>

            <p className="text-sm text-muted-foreground flex items-center gap-1.5 mt-1">
              <MapPin className="h-4 w-4" />
              {totalItems} items available
            </p>
          </div>

          <div className="hidden sm:flex items-center gap-3">
            <span className="text-sm text-muted-foreground">Sort by:</span>

            <select
              value={filters.sort ?? "RELEVANCE"}
              onChange={handleSortChange}
              className="bg-muted/50 border border-border/50 rounded-xl px-4 py-2 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all"
            >
              <option value="RELEVANCE">Relevance</option>

              <option value="PRICE_LOW_TO_HIGH">Price: Low to High</option>

              <option value="PRICE_HIGH_TO_LOW">Price: High to Low</option>

              <option value="NEWEST">Newest First</option>

              <option value="DISTANCE">Distance</option>
            </select>
          </div>
        </div>

        {isLoading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5 lg:gap-6">
            {Array.from({ length: 12 }).map((_, i) => (
              <MarketplaceItemCardSkeleton key={i} />
            ))}
          </div>
        ) : items.length === 0 ? (
          <div className="py-24 text-center">
            <h3 className="text-xl font-semibold text-foreground">No listings found</h3>

            <p className="mt-2 text-sm text-muted-foreground">Try changing your search or filters.</p>
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
                initial={{
                  opacity: 0,
                  y: 20,
                }}
                animate={{
                  opacity: 1,
                  y: 0,
                }}
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

        {!isLoading && hasNext && (
          <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="mt-12 text-center">
            <motion.button
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              disabled={isLoadingMore}
              onClick={loadMore}
              className="px-10 py-4 rounded-2xl glass-strong border-primary/20 text-foreground font-semibold transition-all hover:border-primary/40 hover:shadow-lg hover:shadow-primary/10 disabled:opacity-50"
            >
              {isLoadingMore ? "Loading..." : "Load More Items"}
            </motion.button>
          </motion.div>
        )}
      </main>

      <FilterPanel isOpen={isFilterOpen} onClose={() => setIsFilterOpen(false)} filters={filters} onApply={handleFiltersApply} />

      <FloatingFilterButton onClick={() => setIsFilterOpen(true)} activeCount={activeFilterCount} />
    </div>
  )
}
