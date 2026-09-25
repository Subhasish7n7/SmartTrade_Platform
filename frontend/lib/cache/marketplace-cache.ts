import type { ItemSearchFilters, ItemSearchResponse } from "@/lib/types/api/item"

const FILTER_STORAGE_KEY = "smarttrade:marketplace:filters:v3"
const SCROLL_STORAGE_KEY = "smarttrade:marketplace:scroll:v3"

const FRESH_TTL_MS = 60_000
const RETAIN_TTL_MS = 5 * 60_000

interface CacheEntry {
  data: ItemSearchResponse
  cachedAt: number
}

const marketplaceCache = new Map<string, CacheEntry>()

function normaliseFilters(filters: ItemSearchFilters) {
  return {
    name: filters.name?.trim().toLowerCase() || undefined,
    category: filters.category,
    conditions: filters.conditions?.slice().sort(),
    labels: filters.labels?.slice().sort(),
    minPrice: filters.minPrice,
    maxPrice: filters.maxPrice,
    listingType: filters.listingType,
    latitude: filters.latitude,
    longitude: filters.longitude,
    radiusKm: filters.radiusKm,
    sort: filters.sort ?? "RELEVANCE",
    size: filters.size ?? 24,
  }
}

function cacheKey(filters: ItemSearchFilters) {
  // `page` is deliberately excluded.
  // One filter configuration owns one accumulated marketplace result.
  return JSON.stringify(normaliseFilters(filters))
}

function dedupeItems(items: ItemSearchResponse["items"]) {
  const seen = new Set<number>()

  return items.filter((item) => {
    if (seen.has(item.itemId)) {
      return false
    }

    seen.add(item.itemId)
    return true
  })
}

function pruneExpiredEntries() {
  const now = Date.now()

  for (const [key, entry] of marketplaceCache.entries()) {
    if (now - entry.cachedAt > RETAIN_TTL_MS) {
      marketplaceCache.delete(key)
    }
  }
}

export function getMarketplaceCache(filters: ItemSearchFilters) {
  if (typeof window === "undefined") {
    return null
  }

  pruneExpiredEntries()

  const entry = marketplaceCache.get(cacheKey(filters))

  if (!entry) {
    return null
  }

  return {
    data: entry.data,
    cachedAt: entry.cachedAt,
    isFresh: Date.now() - entry.cachedAt < FRESH_TTL_MS,
  }
}

export function setMarketplaceFirstPage(filters: ItemSearchFilters, data: ItemSearchResponse) {
  if (typeof window === "undefined") {
    return
  }

  pruneExpiredEntries()

  marketplaceCache.set(cacheKey(filters), {
    data: {
      ...data,
      page: 0,
      items: dedupeItems(data.items),
    },
    cachedAt: Date.now(),
  })
}

export function appendMarketplacePage(filters: ItemSearchFilters, data: ItemSearchResponse) {
  if (typeof window === "undefined") {
    return data
  }

  const key = cacheKey(filters)
  const previous = marketplaceCache.get(key)

  if (!previous) {
    marketplaceCache.set(key, {
      data,
      cachedAt: Date.now(),
    })

    return data
  }

  const merged: ItemSearchResponse = {
    ...data,
    page: Math.max(previous.data.page, data.page),
    items: dedupeItems([...previous.data.items, ...data.items]),
    totalItems: data.totalItems,
    hasNext: data.hasNext,
  }

  marketplaceCache.set(key, {
    data: merged,
    // Loading more is not a new freshness point for page 0.
    cachedAt: previous.cachedAt,
  })

  return merged
}

export function refreshMarketplaceCache(filters: ItemSearchFilters, firstPage: ItemSearchResponse) {
  if (typeof window === "undefined") {
    return firstPage
  }

  const key = cacheKey(filters)
  const previous = marketplaceCache.get(key)

  if (!previous) {
    setMarketplaceFirstPage(filters, firstPage)
    return firstPage
  }

  const pageSize = firstPage.size || filters.size || 24
  const previousTail = previous.data.items.slice(pageSize)

  const refreshed: ItemSearchResponse = {
    ...firstPage,
    page: previous.data.page,
    items: dedupeItems([...firstPage.items, ...previousTail]),
    totalItems: firstPage.totalItems,
    hasNext: firstPage.hasNext || previous.data.hasNext,
  }

  marketplaceCache.set(key, {
    data: refreshed,
    cachedAt: Date.now(),
  })

  return refreshed
}

export function clearMarketplaceCache() {
  marketplaceCache.clear()
}

export function readPersistedMarketplaceFilters(): ItemSearchFilters | null {
  if (typeof window === "undefined") {
    return null
  }

  try {
    const raw = window.sessionStorage.getItem(FILTER_STORAGE_KEY)

    if (!raw) {
      return null
    }

    const parsed = JSON.parse(raw) as ItemSearchFilters

    if (!parsed || typeof parsed !== "object") {
      return null
    }

    const { name: _legacyName, ...persistedFilters } = parsed

    return persistedFilters
  } catch {
    return null
  }
}

export function persistMarketplaceFilters(filters: ItemSearchFilters) {
  if (typeof window === "undefined") {
    return
  }

  try {
    const { name: _name, ...persistableFilters } = filters

    window.sessionStorage.setItem(
      FILTER_STORAGE_KEY,
      JSON.stringify({
        ...persistableFilters,
        page: filters.page ?? 0,
      }),
    )
  } catch {
    // Storage can fail in private/restricted browser contexts.
  }
}

export function readPersistedMarketplaceScroll() {
  if (typeof window === "undefined") {
    return 0
  }

  try {
    const raw = window.sessionStorage.getItem(SCROLL_STORAGE_KEY)
    const value = raw ? Number(raw) : 0

    return Number.isFinite(value) ? Math.max(0, value) : 0
  } catch {
    return 0
  }
}

export function persistMarketplaceScroll(scrollY: number) {
  if (typeof window === "undefined") {
    return
  }

  try {
    window.sessionStorage.setItem(SCROLL_STORAGE_KEY, String(Math.max(0, Math.round(scrollY))))
  } catch {
    // Storage can fail in private/restricted browser contexts.
  }
}
