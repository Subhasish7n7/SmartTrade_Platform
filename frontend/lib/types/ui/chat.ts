export interface ChatInboxItem {
  otherUserId: number
  otherUserName: string
  avatarUrl?: string

  lastMessage: string

  lastMessageAt: string

  unreadCount: number

  activeTradeCount: number

  pinnedTradeId?: number | null
}

export interface ChatMessage {
  id: number

  senderId: number

  senderName: string

  message: string

  timestamp: string
}

export interface ChatSystemEvent {
  id: string

  type:
    | "TRADE_CREATED"
    | "COUNTER_SENT"
    | "TRADE_ACCEPTED"
    | "TRADE_CANCELLED"

  tradeId: number

  timestamp: string
}