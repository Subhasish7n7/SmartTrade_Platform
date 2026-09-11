export const ITEM_CATEGORIES = [
  "PHONES",
  "COMPUTERS",
  "GAMING",
  "CAMERAS",
  "AUDIO",
  "MONITORS",
  "BIKES",
  "WATCHES",
  "COLLECTIBLES",
  "BOOKS",
  "OTHER",
] as const

export type ItemCategory = (typeof ITEM_CATEGORIES)[number]

export const ITEM_CONDITIONS = [
  "NEW",
  "LIKE_NEW",
  "GOOD",
  "FAIR",
  "POOR",
] as const

export type ItemCondition = (typeof ITEM_CONDITIONS)[number]

export type ListingType = "SALE" | "TRADE" | "BOTH"

export type ItemSort =
  | "RELEVANCE"
  | "PRICE_LOW_TO_HIGH"
  | "PRICE_HIGH_TO_LOW"
  | "NEWEST"
  | "DISTANCE"

export interface ItemResponse {
  itemId: number
  itemName: string
  newPrice: number | null
  generatedPrice: number | null
  userPrice: number
  seller: SellerUI
  description: string
  category: ItemCategory
  condition: ItemCondition
  customCategory: string | null
  labels: string[]
  imageUrls: string[]
  latitude: number | null
  longitude: number | null
  city: string | null
  state: string | null
  locality: string | null
  available: boolean
  forTrade: boolean
  forSale: boolean
  createdAt: string
}

export interface CreateItemRequest {
  itemName: string
  userPrice: number
  description: string
  category: ItemCategory
  condition: ItemCondition
  customCategory?: string
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
export interface UpdateItemRequest {
  itemName?: string
  userPrice?: number
  description?: string
  category?: ItemCategory
  condition?: ItemCondition
  customCategory?: string
  labels?: string[]
  imageUrls?: string[]
  latitude?: number
  longitude?: number
  city?: string
  state?: string
  locality?: string
  forTrade?: boolean
  forSale?: boolean
}

export interface ItemSearchFilters {
  name?: string
  category?: ItemCategory
  conditions?: ItemCondition[]
  labels?: string[]
  minPrice?: number
  maxPrice?: number
  listingType?: ListingType
  latitude?: number
  longitude?: number
  radiusKm?: number
  sort?: ItemSort
  page?: number
  size?: number
}

export interface ItemSearchResponse {
  items: ItemResponse[]
  page: number
  size: number
  totalItems: number
  hasNext: boolean
}
export interface SellerUI {
  userId: number
  name: string
  trustScore: number
  totalListings: number
  successfulTrades: number
}