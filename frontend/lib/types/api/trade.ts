export type TradeStatus =
  | "OPEN"
  | "NEGOTIATING"
  | "ACCEPTED"
  | "COMPLETED"
  | "CANCELLED"
  | "EXPIRED";

export interface TradeOfferResponse {
  tradeOfferId: number
  senderId: number
  receiverId: number
  senderItems: TradeItemResponse[]
  receiverItems: TradeItemResponse[]
  cashAdjustment: number
  status: TradeStatus

  createdAt: string
}
export interface TradeItemResponse{
  itemId:number
  name:string
  imageUrl:string
  userPrice:number
  systemGeneratedPrice:number
  condition:string
}
export interface TradeInboxResponse{
  tradeId:number
  otherUserId:number
  otherUserName:string
  otherUserProfilePicture?:string 
  status:TradeStatus
  yourItemCount:number
  theirItemCount:number
  valueDifference:number
  updatedAt:string
  latestOffer:TradeOfferResponse
}
export interface TradeHistoryResponse{
  tradeOfferId:number
  senderId:number
  senderName:string
  createdAt:string
}
export interface TradeDetailsResponse{
  tradeId:number
  initiatorId:number
  receiverId:number
  initiatorName:string
  receiverName:string
  status:TradeStatus
  createdAt:string
  offer:TradeOfferResponse
  history:TradeHistoryResponse[]
}

export interface CreateTradeOfferRequest {
  receiverId: number
  senderItemIds: number[]
  receiverItemIds: number[]
  cashAdjustment: number
  tradeId?: number | null
}
export interface CreateBuyRequest{
  itemId:number
  offeredPrice:number
}