export interface notficationResponse{
     id:number
     type: type
     resourceType: resourceType
     resourceId?:number
     actorId:number
     actorName:string
     message:string
     read:number
     createdAt:string
     readAt:string
}
export interface NotificationWebsocketResponse{
    NotificationResponse:notficationResponse
    unreadCount:number
}
export type type =
    |"TRADE_OFFER_CREATED"
    |"TRADE_OFFER_RECEIVED"
    |"TRADE_ACCEPTED"
    |"TRADE_COMPLETION_REQUESTED"
    |"TRADE_COMPLETED"
    |"TRADE_CANCELLED"
    |"TRADE_EXPIRED"
    |"BUY_OFFER_RECEIVED"
    |"CHAT_MESSAGE"
    |"SYSTEM";
export type resourceType = "TRADE" | "CHAT_CONVERSATION" | "ITEM" | "USER" | "SYSTEM";