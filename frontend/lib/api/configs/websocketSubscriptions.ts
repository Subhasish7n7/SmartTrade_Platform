"use client";

import { IMessage, StompSubscription } from "@stomp/stompjs";

import { stompClient } from "./websocket";

import { ChatEvent } from "@/lib/types/api/chat";
import { NotificationWebsocketResponse } from "@/lib/types/api/notifications";

let chatSubscription: StompSubscription | null = null;
let notificationSubscription: StompSubscription | null = null;

export function subscribeToChat(
  callback: (event: ChatEvent) => void
): StompSubscription | null {
  if (!stompClient.connected) {
    console.warn("Cannot subscribe to chat: WebSocket not connected");
    return null;
  }

  chatSubscription?.unsubscribe();

  chatSubscription = stompClient.subscribe(
    "/user/queue/messages",
    (frame: IMessage) => {
      try {
        const event = JSON.parse(frame.body) as ChatEvent;

        callback(event);
      } catch (error) {
        console.error(
          "Failed to parse chat WebSocket event",
          error
        );
      }
    }
  );

  return chatSubscription;
}

export function unsubscribeFromChat() {
  chatSubscription?.unsubscribe();
  chatSubscription = null;
}

export function subscribeToNotifications(
  callback: (event: NotificationWebsocketResponse) => void
): StompSubscription | null {
  if (!stompClient.connected) {
    console.warn(
      "Cannot subscribe to notifications: WebSocket not connected"
    );

    return null;
  }

  notificationSubscription?.unsubscribe();

  notificationSubscription = stompClient.subscribe(
    "/user/queue/notifications",
    (frame: IMessage) => {
      try {
        const event =
          JSON.parse(frame.body) as NotificationWebsocketResponse;

        callback(event);
      } catch (error) {
        console.error(
          "Failed to parse notification WebSocket event",
          error
        );
      }
    }
  );

  return notificationSubscription;
}

export function unsubscribeFromNotifications() {
  notificationSubscription?.unsubscribe();
  notificationSubscription = null;
}