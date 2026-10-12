export interface AdminDashboardResponse {
  users: {
    total: number
    active: number
    suspended: number
    admins: number
  }
  items: {
    total: number
    visible: number
    hidden: number
    available: number
  }
  trades: {
    open: number
    negotiating: number
    accepted: number
    disputed: number
    completed: number
    cancelled: number
    expired: number
  }
  disputes: {
    open: number
    underReview: number
    resolved: number
  }
  averageTrustScore: number
  lowTrustUserCount: number
  pendingDisputes: AdminDisputeSummaryResponse[]
  recentAdminActions: AdminAuditLogResponse[]
}

export interface AdminPageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  hasNext: boolean
}

export interface AdminUserSummaryResponse {
  userId: number
  name: string
  email: string
  phoneNo: string | null
  role: string
  accountStatus: string
  trustScore: number
  totalListings: number
  successfulTrades: number
  latitude: number | null
  longitude: number | null
  createdAt: string
}

export interface AdminUserDetailsResponse {
  user: AdminUserSummaryResponse
  recentAdminActions: AdminAuditLogResponse[]
}

export interface AdminItemSummaryResponse {
  itemId: number
  itemName: string
  sellerId: number
  sellerName: string
  sellerEmail: string
  userPrice: number | null
  newPrice: number | null
  generatedPrice: number | null
  pricingStatus: string
  category: string
  condition: string
  forSale: boolean
  forTrade: boolean
  available: boolean
  sold: boolean
  traded: boolean
  moderationStatus: string
  city: string | null
  state: string | null
  locality: string | null
  imageUrls: string[]
  createdAt: string
}

export interface AdminItemDetailsResponse {
  item: AdminItemSummaryResponse
  description: string | null
  labels: string[]
  recentAdminActions: AdminAuditLogResponse[]
}

export interface AdminTradeSummaryResponse {
  tradeId: number
  initiatorId: number
  initiatorName: string
  initiatorEmail: string
  receiverId: number
  receiverName: string
  receiverEmail: string
  status: string
  initiatorCompletionConfirmed: boolean
  receiverCompletionConfirmed: boolean
  createdAt: string
  acceptedAt: string | null
  completionRequestedAt: string | null
  completedAt: string | null
  cancelledAt: string | null
  disputedAt: string | null
}

export interface AdminTradeItemResponse {
  itemId: number
  itemName: string
  ownerId: number
  ownerName: string
  userPrice: number | null
  generatedPrice: number | null
  condition: string
}

export interface AdminTradeOfferResponse {
  offerId: number
  senderId: number
  senderName: string
  receiverId: number
  receiverName: string
  cashAdjustment: number
  createdAt: string
  senderItems: AdminTradeItemResponse[]
  receiverItems: AdminTradeItemResponse[]
}

export interface AdminTradeDetailsResponse {
  trade: AdminTradeSummaryResponse
  offerHistory: AdminTradeOfferResponse[]
  latestDispute: AdminDisputeSummaryResponse | null
}

export interface AdminDisputeSummaryResponse {
  disputeId: number
  tradeId: number
  openedById: number
  openedByName: string
  status: string
  reason: string
  resolution: string | null
  resolvedById: number | null
  resolvedByName: string | null
  faultUserId: number | null
  createdAt: string
  underReviewAt: string | null
  resolvedAt: string | null
}

export interface AdminDisputeDetailsResponse {
  dispute: AdminDisputeSummaryResponse
  description: string | null
  resolutionNotes: string | null
  trade: AdminTradeSummaryResponse
}

export interface AdminAuditLogResponse {
  id: number
  adminUserId: number
  adminName: string
  action: string
  targetType: string
  targetId: number
  reason: string
  metadata: string | null
  createdAt: string
}

export interface AdminActionRequest {
  reason: string
}

export interface ResolveDisputeRequest {
  resolution: string
  faultUserId?: number | null
  notes?: string
}