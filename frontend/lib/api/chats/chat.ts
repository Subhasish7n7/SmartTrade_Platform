import { ChatConversationResponse,ChatMessageResponse, PinTradeRequest } from "@/lib/types/api/chat";
import { clientApi as api } from "../configs/client"
import { stompClient } from "@/lib/api/configs/websocket";
import { PageResponse } from "@/lib/types/api/page";

export function sendChatMessage( otherUserId: number, message: string) {
    if (!stompClient.connected) {
        throw new Error("WebSocket not connected");
    }

    stompClient.publish({
        destination: "/app/chat.send",
        body: JSON.stringify({ otherUserId, message,}),
    });
}

export async function getMessages( otherUserId:number,page=0,size=40){

  const response = await api.get<PageResponse<ChatMessageResponse>>
                    (`/chat/${otherUserId}/messages?page=${page}&size=${size}`)
  return response.data
}
export async function getInbox() {
  const response = await api.get<ChatConversationResponse[]>("/chat/inbox")

  return response.data
}

export async function pinTrade(otherUserId:number, payload:PinTradeRequest) {
  const response = await api.put(`/chat/${otherUserId}/pin`,payload)
  return response.data
}




