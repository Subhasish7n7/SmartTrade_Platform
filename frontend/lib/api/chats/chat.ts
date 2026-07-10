import { serverApi } from "../server"

export async function getConversations() {
  const api = await serverApi()
  const response = await api.get("/chat")

  return response.data
}

export async function getConversation(otherUserId: number) {
  const api = await serverApi()
  const response = await api.get(`/chat/${otherUserId}`)

  return response.data
}

export async function sendChatMessage(otherUserId: number, message: string) {
  const api = await serverApi()
  return api.post("/chat/message", {
    otherUserId,
    message,
  })
}
