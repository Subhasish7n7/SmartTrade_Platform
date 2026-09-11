"use client"

import { useEffect } from "react"

import { stompClient } from "@/lib/api/configs/websocket"

import { AuthProvider, useAuth } from "@/lib/providers/auth-provider"
import { NotificationProvider } from "@/lib/providers/notification-provider"

function WebSocketManager() {
  const { user, loading } = useAuth()

  useEffect(() => {
    console.log("[WS MANAGER] effect", {
      loading,
      user: user?.email ?? null,
      active: stompClient.active,
      connected: stompClient.connected,
    })

    if (loading) {
      console.log("[WS MANAGER] auth still loading")
      return
    }

    if (!user) {
      console.log("[WS MANAGER] no user → deactivate")

      void stompClient.deactivate()

      return
    }

    console.log("[WS MANAGER] user exists → activate")

    void stompClient.activate()

    return () => {
      console.log("[WS MANAGER] cleanup → deactivate")
      void stompClient.deactivate()
    }
  }, [user, loading])

  return null
}

export function Providers({ children }: { children: React.ReactNode }) {
  return (
    <AuthProvider>
      <WebSocketManager />
      <NotificationProvider>{children}</NotificationProvider>
    </AuthProvider>
  )
}
