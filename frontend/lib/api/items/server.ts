import { CreateItemRequest, ItemResponse } from "@/lib/types/api/item"
import { serverApi } from "../server"

export async function getMarketplaceItems() {
  const api = await serverApi()
  const response = await api.get<ItemResponse[]>("/items")

  return response.data
}

export async function getItemsByUser(userId: number) {
  const api = await serverApi()
  const response = await api.get<ItemResponse[]>(`/items/user/${userId}`)
  return response.data
}

export async function getItemById(id: number) {
  const api = await serverApi()
  const response = await api.get<ItemResponse>(`/items/${id}`)
  return response.data
}

export async function createItem(request: CreateItemRequest) {
  const api = await serverApi()
  const response = await api.post("/items", request)
  return response.data
}
