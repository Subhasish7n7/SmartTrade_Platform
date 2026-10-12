import { clientApi } from "@/lib/api/configs/client"
import type { WishlistItemResponse } from "@/lib/types/api/wishlist"

/** Fetches the authenticated user's wishlist; the server derives the user from the session. */
export async function getWishlist(): Promise<WishlistItemResponse[]> {
  const response = await clientApi.get<WishlistItemResponse[]>("/wishlist")
  return response.data
}

export async function addWishlistItem(itemId: number): Promise<WishlistItemResponse> {
  if (!Number.isSafeInteger(itemId) || itemId <= 0) {
    throw new Error("A valid item is required to add it to your wishlist.")
  }
  const response = await clientApi.post<WishlistItemResponse>(`/wishlist/${itemId}`)
  return response.data
}

export async function removeWishlistItem(itemId: number): Promise<void> {
  if (!Number.isSafeInteger(itemId) || itemId <= 0) {
    throw new Error("A valid item is required to remove it from your wishlist.")
  }
  await clientApi.delete(`/wishlist/${itemId}`)
}
