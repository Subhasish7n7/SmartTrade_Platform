export type NotificationType =
  | "TRADE_OFFER_CREATED"
  | "TRADE_OFFER_RECEIVED"
  | "TRADE_ACCEPTED"
  | "TRADE_COMPLETION_REQUESTED"
  | "TRADE_COMPLETED"
  | "TRADE_CANCELLED"
  | "TRADE_EXPIRED"
  | "BUY_OFFER_RECEIVED"
  | "CHAT_MESSAGE"
  | "SYSTEM"

export type NotificationResourceType =
  | "TRADE"
  | "CHAT_CONVERSATION"
  | "ITEM"
  | "USER"
  | "SYSTEM"

export interface NotificationResponse {
  id: number
  type: NotificationType
  resourceType: NotificationResourceType
  resourceId?: number | null
  actorId?: number | null
  actorName?: string | null
  message: string
  read: boolean
  createdAt: string
  readAt?: string | null
}
export interface NotificationPage {
  content: NotificationResponse[]
  number: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
  numberOfElements: number
  empty: boolean
}
export interface NotificationCounts {
  unreadCount: number
  tradeAlertCount: number
  chatAlertCount: number
}

export interface NotificationWebsocketResponse {
  notification: NotificationResponse
  unreadCount: number
  tradeAlertCount:number
  chatAlertCount:number
}