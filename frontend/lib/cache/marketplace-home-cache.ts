import type { ItemResponse } from "@/lib/types/api/item"

const HOME_CACHE_TTL_MS = 60_000

let cachedItems: ItemResponse[] | null = null
let cachedAt = 0

export function getMarketplaceHomeCache() {
  if (!cachedItems) {
    return null
  }

  return {
    items: cachedItems,
    cachedAt,
    isFresh: Date.now() - cachedAt < HOME_CACHE_TTL_MS,
  }
}

export function setMarketplaceHomeCache(items: ItemResponse[]) {
  cachedItems = items
  cachedAt = Date.now()
}

export function clearMarketplaceHomeCache() {
  cachedItems = null
  cachedAt = 0
}
