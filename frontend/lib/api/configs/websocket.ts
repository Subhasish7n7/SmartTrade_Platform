"use client"

import { Client } from "@stomp/stompjs"
import SockJS from "sockjs-client"

import { API_BASE_URL } from "@/lib/config"

type ConnectionListener = () => void

const connectionListeners = new Set<ConnectionListener>()

export const stompClient = new Client({
  webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws`),

  reconnectDelay: 5000,

  onConnect: () => {
    console.log("WebSocket connected")

    connectionListeners.forEach((listener) => {
      listener()
    })
  },

  onDisconnect: () => {
    console.log("WebSocket disconnected")
  },

  onStompError: (frame) => {
    console.error("STOMP Error:", frame.headers["message"])
  },
})

export function onWebSocketConnected(listener: ConnectionListener): () => void {
  connectionListeners.add(listener)

  if (stompClient.connected) {
    listener()
  }

  return () => {
    connectionListeners.delete(listener)
  }
}

