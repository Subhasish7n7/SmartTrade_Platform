"use client";

import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

import { clientApi } from "@/lib/api/configs/client";
import { API_BASE_URL } from "@/lib/config";

type WebSocketTicketResponse = {
  ticket: string;
  expiresInSeconds: number;
};

type ConnectionListener = () => void;
const connectionListeners = new Set<ConnectionListener>();

export const stompClient = new Client({
  webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws`),

  // STOMP.js awaits this before opening each connection, including reconnects.
  beforeConnect: async () => {
    const { data } = await clientApi.get<WebSocketTicketResponse>("/auth/ws-ticket");

    if (!data?.ticket || typeof data.ticket !== "string") {
      throw new Error("The server did not issue a WebSocket ticket.");
    }

    stompClient.connectHeaders = {
      ...stompClient.connectHeaders,
      "X-WS-TICKET": data.ticket,
    };
  },

  reconnectDelay: 5000,

  onConnect: () => {
    console.log("WebSocket connected");
    connectionListeners.forEach((listener) => listener());
  },

  onDisconnect: () => {
    console.log("WebSocket disconnected");
  },

  onStompError: (frame) => {
    console.error("STOMP Error:", frame.headers["message"]);
  },
});

export function onWebSocketConnected(listener: ConnectionListener): () => void {
  connectionListeners.add(listener);

  if (stompClient.connected) listener();

  return () => {
    connectionListeners.delete(listener);
  };
}