export const ITEM_CATEGORIES = [
  "PHONES",
  "COMPUTERS",
  "GAMING",
  "CAMERAS",
  "AUDIO",
  "MONITORS",
  "BIKES",
  "CARS",
  "WATCHES",
  "FURNITURE",
  "HOME_APPLIANCES",
  "HOME_DECOR",
  "KITCHEN",
  "BOOKS",
  "CLOTHING",
  "SHOES",
  "BAGS",
  "SPORTS",
  "MUSICAL_INSTRUMENTS",
  "TOYS",
  "COLLECTIBLES",
  "TOOLS",
  "PET_SUPPLIES",
  "BABY_KIDS",
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

export type ItemPricingStatus =
  | "PENDING"
  | "IN_PROGRESS"
  | "READY"
  | "FAILED"

export type PricingRefreshOutcome =
  | "STARTED"
  | "ALREADY_RUNNING"
  | "ALREADY_FRESH"
  | "NOT_FOUND"

export type PricingRefreshResponse = {
  itemId: number
  outcome: PricingRefreshOutcome
  pricingStatus: ItemPricingStatus | null
  pricingVersion: number
  userPrice: number | null
  newPrice: number | null
  generatedPrice: number | null
  priceFetchedAt: string | null
  nextPriceRefreshAt: string | null
  pricingErrorCode: string | null
  pricingErrorMessage: string | null
  message: string
}

export type ItemSort =
  | "RELEVANCE"
  | "PRICE_LOW_TO_HIGH"
  | "PRICE_HIGH_TO_LOW"
  | "NEWEST"
  | "DISTANCE"

export interface ItemImageResponse {
  id: number
  secureUrl: string
  publicId: string
  assetId: string
  width: number
  height: number
  format: string
  bytes: number
  sortOrder: number
  createdAt: string
}

export interface ItemImageRequest {
  id: number | null
  secureUrl: string
  publicId: string
  assetId: string
  width: number
  height: number
  format: string
  bytes: number
  sortOrder: number
}

export interface ItemImageDraft extends Omit<ItemImageRequest, "id"> {
  id: number | null
  deleteToken?: string | null
}

export interface ItemResponse {
  itemId: number
  itemName: string
  newPrice: number | null
  generatedPrice: number | null
  userPrice: number
  pricingStatus: ItemPricingStatus
  priceFetchedAt: string | null
  nextPriceRefreshAt: string | null
  pricingErrorCode: string | null
  pricingErrorMessage: string | null
  seller: SellerUI
  description: string
  category: ItemCategory
  condition: ItemCondition
  customCategory: string | null
  labels: string[]
  images: ItemImageResponse[]
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
  images: ItemImageRequest[]
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
  images?: ItemImageRequest[]
  removedImageIds?: number[]
  latitude?: number
  longitude?: number
  city?: string
  state?: string
  locality?: string
  clearCoordinates?: boolean
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