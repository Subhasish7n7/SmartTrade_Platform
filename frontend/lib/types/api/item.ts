export interface ItemResponse {
  itemId: number
  itemName: string
  newPrice: number
  generatedPrice: number
  userPrice: number
  seller: SellerUI
  description: string
  category: string
  condition: string
  labels: string[]
  imageUrls: string[]
  latitude: number
  longitude: number
  city: string
  state: string
  locality: string
  available: boolean
  forTrade: boolean
  forSale: boolean
  createdAt: string
}
export interface CreateItemRequest {
  itemName: string
  userPrice: number
  description: string
  category: string
  condition: string
  labels: string[]
  imageUrls: string[]
  latitude?: number
  longitude?: number
  city?: string
  state?: string
  locality?: string
  forTrade: boolean
  forSale: boolean
}

export interface SellerUI {
  userId: number,
  name: string,
  trustScore: number
  totalListings: number
  successfulTrades: number
}
