import type {
  CreateItemRequest,
  ItemResponse,
  ItemSearchFilters,
  ItemSearchResponse,
  PricingRefreshResponse,
  UpdateItemRequest,
} from "@/lib/types/api/item"

import { clientApi } from "../configs/client"

export async function searchMarketplaceItems(filters: ItemSearchFilters = {}) {
  const response = await clientApi.post<ItemSearchResponse>("/items/search", {
    name: filters.name?.trim() || undefined,
    category: filters.category,
    conditions: filters.conditions,
    labels: filters.labels,
    minPrice: filters.minPrice,
    maxPrice: filters.maxPrice,
    listingType: filters.listingType,
    latitude: filters.latitude,
    longitude: filters.longitude,
    radiusKm: filters.radiusKm,
    sort: filters.sort ?? "RELEVANCE",
    page: filters.page ?? 0,
    size: filters.size ?? 24,
  })

  return response.data
}

export async function getMarketplaceItems() {
  const response = await clientApi.get<ItemResponse[]>("/items")
  return response.data
}

export async function getRecentMarketplaceItems(page = 0, size = 25): Promise<ItemResponse[]> {
  const response = await clientApi.get<ItemResponse[]>("/items/recent", {
    params: { page, size},
  })

  return response.data
}

export async function getItemsByUser(userId: number) {
  const response = await clientApi.get<ItemResponse[]>(`/items/user/${userId}`)
  return response.data
}

export async function getItemById(id: number) {
  const response = await clientApi.get<ItemResponse>(`/items/${id}`)
  return response.data
}

export async function createItem(request: CreateItemRequest) {
  const response = await clientApi.post<ItemResponse>("/items", request)
  return response.data
}

export async function updateItem(itemId: number, request: UpdateItemRequest) {
  const response = await clientApi.patch<ItemResponse>(`/items/${itemId}`, request)
  return response.data
}

export async function refreshItemPrice(itemId: number) {
  const response = await clientApi.post<PricingRefreshResponse>(`/items/${itemId}/price/refresh`)
  return response.data
}

export async function updateItemAvailability(itemId: number, available: boolean) {
  await clientApi.patch(`/items/${itemId}/availability`, undefined, {
    params: { available },
  })
}

export async function deleteItem(itemId: number) {
  await clientApi.delete(`/items/${itemId}`)
}
