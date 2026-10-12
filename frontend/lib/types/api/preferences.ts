export type ProfileVisibility = "PUBLIC" | "PRIVATE"
export type LocationVisibility = "EXACT" | "CITY" | "HIDDEN"
export type InventoryVisibility = "PUBLIC" | "PRIVATE"
export type MessagePermission = "EVERYONE" | "TRADERS_ONLY" | "NOBODY"
export type DefaultListingType = "SALE" | "TRADE" | "BOTH"

export interface UserPreferences {
  profileVisibility: ProfileVisibility
  locationVisibility: LocationVisibility
  inventoryVisibility: InventoryVisibility
  showOnlineStatus: boolean
  acceptTradeRequests: boolean
  acceptBuyOffers: boolean
  messagePermission: MessagePermission
  minimumTraderTrustScore: number
  showUnavailableListings: boolean
  defaultListingType: DefaultListingType
  notifyTrades: boolean
  notifyMessages: boolean
  notifySystem: boolean
  loginAlerts: boolean
}

export type UpdateUserPreferencesRequest = Partial<UserPreferences>
