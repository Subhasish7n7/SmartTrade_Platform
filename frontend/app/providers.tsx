"use client";

import { useEffect } from "react";

import {
  onWebSocketConnected,
  stompClient,
} from "@/lib/api/configs/websocket";

import {
  subscribeToNotifications,
  unsubscribeFromNotifications,
} from "@/lib/api/configs/websocketSubscriptions";

export function Providers({
  children,
}: {
  children: React.ReactNode;
}) {
  useEffect(() => {
    const unsubscribeConnection = onWebSocketConnected(() => {
      subscribeToNotifications((notification) => {
        console.log("Notification received:", notification);

        // later:
        // toast(notification.notification)
        // badge++
        // invalidate cache
      });
    });

    stompClient.activate();

    return () => {
      unsubscribeConnection();

      unsubscribeFromNotifications();

      stompClient.deactivate();
    };
  }, []);

  return children;
}