import { CreateItemRequest, ItemResponse } from "@/lib/types/api/item"
import { clientApi } from "../client"

export async function getMarketplaceItems() {
  const response = await clientApi.get<ItemResponse[]>("/items")

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
  const response = await clientApi.post("/items", request)
  return response.data
}
