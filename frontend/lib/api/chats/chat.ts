import { clientApi as api } from "../configs/client"
import { stompClient } from "@/lib/api/configs/websocket";

export function sendChatMessage(
    otherUserId: number,
    message: string
) {
    if (!stompClient.connected) {
        throw new Error("WebSocket not connected");
    }

    stompClient.publish({
        destination: "/app/chat.send",
        body: JSON.stringify({
            otherUserId,
            message,
        }),
    });
}

export async function getConversations() {
  const response = await api.get("/chat")

  return response.data
}

export async function getConversation(otherUserId: number) {
  const response = await api.get(`/chat/${otherUserId}`)

  return response.data
}


