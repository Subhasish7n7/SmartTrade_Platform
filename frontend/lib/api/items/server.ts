import { CreateItemRequest, ItemResponse } from "@/lib/types/api/item"
import { serverApi } from "../configs/server"

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

export async function getMyInventory() {
  const api = await serverApi()
  const response = await api.get<ItemResponse[]>("/items/me/inventory")
  return response.data
}



