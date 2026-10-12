"use client"

import { motion } from "framer-motion"
import { Search } from "lucide-react"
import Link from "next/link"
import { useRouter, useSearchParams } from "next/navigation"
import * as React from "react"

import { FilterPanel, FloatingFilterButton } from "@/components/marketplace/filter-sidebar"
import { MarketplaceItemCard, MarketplaceItemCardSkeleton } from "@/components/marketplace/item-card"
import { searchMarketplaceItems } from "@/lib/api/items/client"
import {
  appendMarketplacePage,
  getMarketplaceCache,
  persistMarketplaceFilters,
  persistMarketplaceScroll,
  readPersistedMarketplaceFilters,
  readPersistedMarketplaceScroll,
  refreshMarketplaceCache,
  setMarketplaceFirstPage,
} from "@/lib/cache/marketplace-cache"
import type { ItemResponse, ItemSearchFilters } from "@/lib/types/api/item"

const DEFAULT_FILTERS: ItemSearchFilters = {
  sort: "RELEVANCE",
  radiusKm: undefined,
  page: 0,
  size: 24,
}

interface LoadOptions {
  background?: boolean
}

export default function MarketplacePage() {
  const router = useRouter()
  const searchParams = useSearchParams()

  const [wishlisted, setWishlisted] = React.useState<Set<number>>(new Set())
  const [isFilterOpen, setIsFilterOpen] = React.useState(false)
  const [isLoading, setIsLoading] = React.useState(true)
  const [isLoadingMore, setIsLoadingMore] = React.useState(false)
  const [items, setItems] = React.useState<ItemResponse[]>([])
  const [filters, setFilters] = React.useState<ItemSearchFilters>(DEFAULT_FILTERS)
  const [hasNext, setHasNext] = React.useState(false)
  const [totalItems, setTotalItems] = React.useState(0)
  const [loadError, setLoadError] = React.useState<string | null>(null)

  const initializedRef = React.useRef(false)
  const querySyncInitialisedRef = React.useRef(false)
  const requestSequenceRef = React.useRef(0)
  const restoreScrollRef = React.useRef(true)
  const latestFiltersRef = React.useRef(filters)

  const queryHasName = searchParams.has("name")
  const queryName = searchParams.get("name")?.trim() || undefined

  const hasLocation = filters.latitude !== undefined && filters.longitude !== undefined

  React.useEffect(() => {
    latestFiltersRef.current = filters
  }, [filters])

  const applyResponse = React.useCallback((nextFilters: ItemSearchFilters, data: ReturnType<typeof refreshMarketplaceCache>) => {
    const resolvedFilters = {
      ...nextFilters,
      page: data.page ?? 0,
    }

    setFilters(resolvedFilters)
    setItems(data.items)
    setTotalItems(data.totalItems)
    setHasNext(data.hasNext)

    persistMarketplaceFilters(resolvedFilters)

    if (restoreScrollRef.current) {
      restoreScrollRef.current = false

      const savedScroll = readPersistedMarketplaceScroll()

      if (savedScroll > 0) {
        window.requestAnimationFrame(() => {
          window.scrollTo(0, savedScroll)
        })
      }
    }
  }, [])

  const loadItems = React.useCallback(
    async (nextFilters: ItemSearchFilters, options: LoadOptions = {}) => {
      const background = options.background ?? false
      const requestId = ++requestSequenceRef.current

      const normalisedFilters: ItemSearchFilters = {
        ...nextFilters,
        page: 0,
        size: nextFilters.size ?? 24,
      }

      if (!background) {
        setIsLoading(true)
        setLoadError(null)
      }

      try {
        const data = await searchMarketplaceItems(normalisedFilters)

        if (requestId !== requestSequenceRef.current) {
          return
        }

        const resolvedData = background ? refreshMarketplaceCache(normalisedFilters, data) : data

        if (!background) {
          setMarketplaceFirstPage(normalisedFilters, data)
        }

        applyResponse(normalisedFilters, resolvedData)
      } catch (error) {
        if (requestId !== requestSequenceRef.current) {
          return
        }

        console.error("Failed to fetch marketplace items:", error)

        if (!background) {
          setLoadError("Couldn't connect to the marketplace right now.")
        }
      } finally {
        if (requestId === requestSequenceRef.current && !background) {
          setIsLoading(false)
        }
      }
    },
    [applyResponse],
  )

  /*
   * Resolve the user's current coordinates whenever a
   * location-dependent filter needs them.
   *
   * Distance filter:
   *   radiusKm !== undefined
   *
   * Distance sorting:
   *   sort === "DISTANCE"
   */
  const ensureLocationForFilters = React.useCallback(async (nextFilters: ItemSearchFilters): Promise<ItemSearchFilters> => {
    const needsLocation = nextFilters.radiusKm !== undefined || nextFilters.sort === "DISTANCE"

    const alreadyHasLocation = nextFilters.latitude !== undefined && nextFilters.longitude !== undefined

    if (!needsLocation || alreadyHasLocation) {
      return nextFilters
    }

    if (!navigator.geolocation) {
      throw new Error("Geolocation is not available in this browser.")
    }

    const position = await new Promise<GeolocationPosition>((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(resolve, reject, {
        enableHighAccuracy: false,
        timeout: 5000,
        maximumAge: 60_000,
      })
    })

    return {
      ...nextFilters,
      latitude: position.coords.latitude,
      longitude: position.coords.longitude,
    }
  }, [])

  React.useEffect(() => {
    if (initializedRef.current) {
      return
    }

    initializedRef.current = true

    const persisted = readPersistedMarketplaceFilters()

    let initialFilters: ItemSearchFilters = persisted
      ? {
          ...persisted,
          page: 0,
          size: 24,
        }
      : {
          ...DEFAULT_FILTERS,
          page: 0,
          size: 24,
        }

    /*
     * Search text comes from the URL,
     * not persisted marketplace filters.
     */
    if (queryHasName) {
      initialFilters = {
        ...initialFilters,
        name: queryName,
        page: 0,
      }
    }

    setFilters(initialFilters)

    const cached = getMarketplaceCache(initialFilters)

    if (cached) {
      applyResponse(initialFilters, cached.data)

      setLoadError(null)
      setIsLoading(false)

      if (!cached.isFresh) {
        void loadItems(initialFilters, { background: true })
      }

      return
    }

    if (initialFilters.latitude !== undefined && initialFilters.longitude !== undefined) {
      void loadItems(initialFilters)
      return
    }

    if (!navigator.geolocation) {
      void loadItems(initialFilters)
      return
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        void loadItems({
          ...initialFilters,
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
          page: 0,
        })
      },
      () => {
        /*
         * Location is optional for the default
         * "Anywhere" view.
         */
        void loadItems(initialFilters)
      },
      {
        enableHighAccuracy: false,
        timeout: 5000,
        maximumAge: 60_000,
      },
    )
  }, [applyResponse, loadItems, queryHasName, queryName])

  React.useEffect(() => {
    if (!initializedRef.current) {
      return
    }

    /*
     * The initialization effect already consumed
     * the initial query parameter.
     */
    if (!querySyncInitialisedRef.current) {
      querySyncInitialisedRef.current = true
      return
    }

    const current = latestFiltersRef.current

    const nextName = queryHasName ? queryName : undefined

    if ((current.name ?? undefined) === nextName) {
      return
    }

    restoreScrollRef.current = false

    void loadItems({
      ...current,
      name: nextName,
      page: 0,
    })

    window.requestAnimationFrame(() => {
      window.scrollTo({
        top: 0,
        behavior: "smooth",
      })
    })
  }, [loadItems, queryHasName, queryName])

  React.useEffect(() => {
    return () => {
      persistMarketplaceScroll(window.scrollY)
    }
  }, [])

  const loadMore = async () => {
    if (!hasNext || isLoadingMore) {
      return
    }

    const requestId = ++requestSequenceRef.current

    const nextPage = (filters.page ?? 0) + 1

    try {
      setIsLoadingMore(true)

      const data = await searchMarketplaceItems({
        ...filters,
        page: nextPage,
      })

      if (requestId !== requestSequenceRef.current) {
        return
      }

      const merged = appendMarketplacePage(filters, data)

      const nextFilters = {
        ...filters,
        page: merged.page,
      }

      setItems(merged.items)
      setFilters(nextFilters)
      setTotalItems(merged.totalItems)
      setHasNext(merged.hasNext)

      persistMarketplaceFilters(nextFilters)
    } catch (error) {
      if (requestId !== requestSequenceRef.current) {
        return
      }

      console.error("Failed to load more marketplace items:", error)
    } finally {
      if (requestId === requestSequenceRef.current) {
        setIsLoadingMore(false)
      }
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

  const handleFiltersApply = async (nextFilters: ItemSearchFilters) => {
    restoreScrollRef.current = false

    try {
      const filtersWithLocation = await ensureLocationForFilters(nextFilters)

      const resolvedFilters: ItemSearchFilters = {
        ...filtersWithLocation,
        page: 0,
        size: 24,
      }

      persistMarketplaceFilters(resolvedFilters)

      setIsFilterOpen(false)

      window.requestAnimationFrame(() => {
        window.scrollTo({
          top: 0,
          behavior: "smooth",
        })
      })

      void loadItems(resolvedFilters)
    } catch (error) {
      console.error("Location is required for this marketplace filter:", error)
    }
  }

  /*
   * Every non-default radius is an active filter.
   */
  const activeFilterCount = React.useMemo(() => {
    let count = 0

    if (filters.category) count++

    if (filters.conditions?.length) {
      count++
    }

    if (filters.minPrice !== undefined) {
      count++
    }

    if (filters.maxPrice !== undefined) {
      count++
    }

    if (filters.listingType) {
      count++
    }

    if (filters.radiusKm !== undefined) {
      count++
    }

    if (filters.labels?.length) {
      count++
    }

    if ((filters.sort ?? "RELEVANCE") !== "RELEVANCE") {
      count++
    }

    return count
  }, [filters])

  const handleClearFilters = React.useCallback(() => {
    restoreScrollRef.current = false

    const nextFilters: ItemSearchFilters = {
      ...DEFAULT_FILTERS,

      /*
       * Search remains independent
       * from filters.
       */
      name: filters.name,

      /*
       * Keep already-resolved coordinates,
       * but clear the actual radius.
       */
      latitude: filters.latitude,
      longitude: filters.longitude,

      radiusKm: undefined,
      sort: "RELEVANCE",
      page: 0,
      size: 24,
    }

    persistMarketplaceFilters(nextFilters)

    window.requestAnimationFrame(() => {
      window.scrollTo({
        top: 0,
        behavior: "smooth",
      })
    })

    void loadItems(nextFilters)
  }, [filters.latitude, filters.longitude, filters.name, loadItems])

  const hasClearableFilters = activeFilterCount > 0

  return (
    <div className="min-h-screen bg-background marketplace-bg">
      <main className="relative z-10 mx-auto max-w-[1600px] px-4 pb-24 pt-24 sm:px-6 lg:px-8">
        <div className="marketplace-results-header">
          <div className="min-w-0">
            <h1 className="marketplace-section-title text-xl sm:text-2xl">
              {filters.name ? `Results for “${filters.name}”` : hasLocation ? "Listings around you" : "Marketplace listings"}
            </h1>
          </div>

          <div className="marketplace-results-actions">
            <span className="marketplace-toolbar-chip">{totalItems.toLocaleString("en-IN")} active listings</span>

            {hasClearableFilters && (
              <button type="button" onClick={handleClearFilters} className="marketplace-toolbar-action">
                Clear filters
              </button>
            )}
          </div>
        </div>

        {isLoading ? (
          <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 lg:gap-6">
            {Array.from({
              length: 12,
            }).map((_, index) => (
              <MarketplaceItemCardSkeleton key={index} />
            ))}
          </div>
        ) : loadError && items.length === 0 ? (
          <div className="marketplace-empty-state">
            <div className="marketplace-empty-state-icon">
              <Search className="h-5 w-5" />
            </div>

            <h2 className="mt-4 text-xl font-semibold text-foreground">Couldn't load marketplace</h2>

            <p className="mt-2 max-w-md text-sm leading-6 text-muted-foreground">{loadError}</p>

            <button
              type="button"
              onClick={() => void loadItems(latestFiltersRef.current)}
              className="mt-5 cta-primary h-11 rounded-xl px-6 font-semibold"
            >
              Try again
            </button>
          </div>
        ) : items.length === 0 ? (
          <div className="marketplace-empty-state">
            <div className="marketplace-empty-state-icon">
              <Search className="h-5 w-5" />
            </div>

            <h2 className="mt-4 text-xl font-semibold text-foreground">No listings found</h2>

            <p className="mt-2 max-w-md text-sm leading-6 text-muted-foreground">
              Try a different search, loosen the price range, widen the distance, or change your filters.
            </p>
          </div>
        ) : (
          <motion.div
            initial={{
              opacity: 0,
            }}
            animate={{
              opacity: 1,
            }}
            transition={{
              duration: 0.3,
            }}
            className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 lg:gap-6"
          >
            {items.map((item, index) => (
              <motion.div
                key={item.itemId}
                initial={{
                  opacity: 0,
                  y: 12,
                }}
                animate={{
                  opacity: 1,
                  y: 0,
                }}
                transition={{
                  duration: 0.3,
                  delay: Math.min(index * 0.02, 0.2),
                }}
              >
                <Link href={`/item/${item.itemId}`}>
                  <MarketplaceItemCard item={item} isWishlisted={wishlisted.has(item.itemId)} onWishlistToggle={toggleWishlist} />
                </Link>
              </motion.div>
            ))}
          </motion.div>
        )}

        {!isLoading && !loadError && hasNext && (
          <motion.div
            initial={{
              opacity: 0,
            }}
            animate={{
              opacity: 1,
            }}
            className="mt-12 text-center"
          >
            <motion.button
              whileHover={{
                scale: 1.015,
              }}
              whileTap={{
                scale: 0.985,
              }}
              disabled={isLoadingMore}
              onClick={loadMore}
              className="cta-primary h-12 rounded-2xl px-8 font-semibold disabled:opacity-50"
            >
              {isLoadingMore ? "Loading more…" : "Load more listings"}
            </motion.button>
          </motion.div>
        )}
      </main>

      <FilterPanel isOpen={isFilterOpen} onClose={() => setIsFilterOpen(false)} filters={filters} onApply={handleFiltersApply} />

      <FloatingFilterButton onClick={() => setIsFilterOpen(true)} activeCount={activeFilterCount} />
    </div>
  )
}
