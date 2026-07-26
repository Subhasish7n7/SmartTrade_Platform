"use client";

import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

import { API_BASE_URL } from "@/lib/config";

export const stompClient = new Client({
  webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws`),

  reconnectDelay: 5000,

  onConnect: () => {
    console.log("WebSocket connected");
  },

  onDisconnect: () => {
    console.log("WebSocket disconnected");
  },

  onStompError: (frame) => {
    console.error("STOMP Error:", frame.headers["message"]);
  },
});