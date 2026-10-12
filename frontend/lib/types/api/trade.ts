export type TradeStatus =
  | "OPEN"
  | "NEGOTIATING"
  | "ACCEPTED"
  | "DISPUTED"
  | "COMPLETED"
  | "CANCELLED"
  | "EXPIRED"

export interface TradeItemResponse {
  itemId: number
  name: string
  imageUrl: string | null
  userPrice: number
  systemGeneratedPrice: number | null
  newPrice: number | null
  condition: string
}

export interface TradeOfferResponse {
  tradeOfferId: number
  senderId: number
  receiverId: number
  senderItems: TradeItemResponse[]
  receiverItems: TradeItemResponse[]
  cashAdjustment: number | null
  status: TradeStatus
  createdAt: string
}

export interface TradeInboxResponse {
  tradeId: number
  otherUserId: number
  otherUserName: string
  otherUserProfilePicture?: string
  status: TradeStatus
  yourItemCount: number
  theirItemCount: number
  cashAdjustment: number | null
  updatedAt: string
  latestOffer: TradeOfferResponse
}

export interface TradeHistoryResponse {
  tradeOfferId: number
  senderId: number
  senderName: string
  createdAt: string
}

export interface TradeDetailsResponse {
  tradeId: number

  initiatorId: number
  initiatorName: string
  initiatorTrustScore: number

  receiverId: number
  receiverName: string
  receiverTrustScore: number

  status: TradeStatus
  createdAt: string
  acceptedAt: string | null

  initiatorCompletionConfirmed: boolean
  receiverCompletionConfirmed: boolean
  completionRequestedAt: string | null

  completedAt: string | null
  cancelledAt: string | null
  disputedAt: string | null

  offer: TradeOfferResponse
  history: TradeHistoryResponse[]
}

export interface CreateTradeOfferRequest {
  receiverId: number
  senderItemIds: number[]
  receiverItemIds: number[]
  cashAdjustment: number
  tradeId?: number | null
  initialMessage?: string
}

export interface CreateBuyRequest {
  itemId: number
  offeredPrice: number
  initialMessage?: string
}

export type DisputeReason =
  | "COMPLETION_DISAGREEMENT"
  | "ITEM_NOT_AS_DESCRIBED"
  | "NO_SHOW"
  | "OTHER"

export type DisputeStatus = "OPEN" | "UNDER_REVIEW" | "RESOLVED"

export type DisputeResolution =
  | "MARK_COMPLETED"
  | "CANCEL_NO_FAULT"
  | "CANCEL_WITH_FAULT"
  | "REOPEN_TRADE"

export interface CreateDisputeRequest {
  reason: DisputeReason
  description: string
}

export interface TradeDisputeResponse {
  disputeId: number
  tradeId: number
  status: DisputeStatus
  reason: DisputeReason
  description: string
  resolution: DisputeResolution | null
  resolutionNotes: string | null
  createdAt: string
  underReviewAt: string | null
  resolvedAt: string | null
}