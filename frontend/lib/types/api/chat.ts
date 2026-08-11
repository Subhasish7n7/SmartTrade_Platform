export interface ChatEvent {
    type: "MESSAGE_CREATED";
    message: ChatMessageResponse;
    conversation: ChatConversationResponse;
}
export interface PinTradeRequest{
    tradeId:number
}
export interface ChatMessageResponse{
    id: number
    conversationId: number
    senderId: number
    senderName: string
    message: string
    timestamp: string
}
export interface ChatMessageRequest{
    otherUserId:number
    message:string
}
export interface ChatConversationResponse {
  conversationId: number
  otherUserId: number
  otherUserName: string
  avatarUrl?: string
  lastMessage: string
  lastMessageAt: string
  unreadCount: number
  activeTradeCount: number
  pinnedTradeId?: number | null
}

