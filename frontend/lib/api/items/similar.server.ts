import { serverApi } from "@/lib/api/configs/server"

import type {
  ItemResponse,
  ItemSearchFilters,
  ItemSearchResponse,
} from "@/lib/types/api/item"

export async function getSimilarItems(
  currentItem: ItemResponse,
  limit = 3,
): Promise<ItemResponse[]> {
  const api = await serverApi()

  // Fetch extra candidates so excluding the current item and duplicates
  // does not leave us with too few cards.
  const baseFilters: ItemSearchFilters = {
    category: currentItem.category,
    sort: "NEWEST",
    page: 0,
    size: Math.max(limit * 4, 12),
  }

  async function search(filters: ItemSearchFilters) {
    const { data } = await api.post<ItemSearchResponse>(
      "/items/search",
      filters,
    )

    return data.items ?? []
  }

  const sharedLabels = [
    ...new Set(
      (currentItem.labels ?? [])
        .map((label) => label.trim())
        .filter(Boolean),
    ),
  ]

  let preferredMatches: ItemResponse[] = []

  // First prefer items from the same category with matching labels.
  if (sharedLabels.length > 0) {
    try {
      preferredMatches = await search({
        ...baseFilters,
        labels: sharedLabels,
      })
    } catch {
      // Fall back to category-only recommendations below.
    }
  }

  const results: ItemResponse[] = []
  const seen = new Set<number>([currentItem.itemId])

  function addUnique(items: ItemResponse[]) {
    for (const candidate of items) {
      if (
        !candidate.available ||
        (!candidate.forSale && !candidate.forTrade) ||
        seen.has(candidate.itemId)
      ) {
        continue
      }

      seen.add(candidate.itemId)
      results.push(candidate)

      if (results.length >= limit) {
        return
      }
    }
  }

  addUnique(preferredMatches)

  // Broaden to the same category only when needed.
  if (results.length < limit) {
    const categoryMatches = await search(baseFilters)
    addUnique(categoryMatches)
  }

  return results.slice(0, limit)
}