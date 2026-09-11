"use client"

import { IMessage, StompSubscription } from "@stomp/stompjs"

import { stompClient } from "./websocket"

import { ChatEvent, ChatPresenceEvent } from "@/lib/types/api/chat"

import { NotificationWebsocketResponse } from "@/lib/types/api/notifications"

let chatSubscription: StompSubscription | null = null
let presenceSubscription: StompSubscription | null = null
let notificationSubscription: StompSubscription | null = null

export function subscribeToChat(callback: (event: ChatEvent) => void): StompSubscription | null {
  if (!stompClient.connected) {
    return null
  }

  chatSubscription?.unsubscribe()

  chatSubscription = stompClient.subscribe("/user/queue/messages", (frame: IMessage) => {
    try {
      callback(JSON.parse(frame.body) as ChatEvent)
    } catch (error) {
      console.error("Failed to parse chat WebSocket event", error)
    }
  })

  return chatSubscription
}

export function unsubscribeFromChat() {
  chatSubscription?.unsubscribe()
  chatSubscription = null
}

export function subscribeToPresence(callback: (event: ChatPresenceEvent) => void): StompSubscription | null {
  if (!stompClient.connected) {
    return null
  }

  presenceSubscription?.unsubscribe()

  presenceSubscription = stompClient.subscribe("/user/queue/presence", (frame: IMessage) => {
    try {
      callback(JSON.parse(frame.body) as ChatPresenceEvent)
    } catch (error) {
      console.error("Failed to parse presence WebSocket event", error)
    }
  })

  return presenceSubscription
}

export function unsubscribeFromPresence() {
  presenceSubscription?.unsubscribe()
  presenceSubscription = null
}

export function subscribeToNotifications(callback: (event: NotificationWebsocketResponse) => void): StompSubscription | null {
  if (!stompClient.connected) {
    return null
  }

  notificationSubscription?.unsubscribe()

  notificationSubscription = stompClient.subscribe("/user/queue/notifications", (frame: IMessage) => {
    try {
      callback(JSON.parse(frame.body) as NotificationWebsocketResponse)
    } catch (error) {
      console.error("Failed to parse notification WebSocket event", error)
    }
  })

  return notificationSubscription
}

export function unsubscribeFromNotifications() {
  notificationSubscription?.unsubscribe()
  notificationSubscription = null
}
