"use client"

import * as React from "react"

import { getNotificationCounts, getNotifications, markAllRead, markModuleRead, markRead } from "@/lib/api/notifications/notification"

import { onWebSocketConnected } from "@/lib/api/configs/websocket"

import { subscribeToNotifications, unsubscribeFromNotifications } from "@/lib/api/configs/websocketSubscriptions"

import type { NotificationResourceType, NotificationResponse, NotificationWebsocketResponse } from "@/lib/types/api/notifications"

interface NotificationContextValue {
  notifications: NotificationResponse[]

  unreadCount: number
  tradeAlertCount: number
  chatAlertCount: number

  loading: boolean

  refreshNotifications: () => Promise<void>

  markNotificationRead: (id: number) => Promise<void>

  markModuleNotificationsRead: (resourceType: NotificationResourceType) => Promise<void>

  markAllNotificationsRead: () => Promise<void>
}

const NotificationContext = React.createContext<NotificationContextValue | null>(null)

export function NotificationProvider({ children }: { children: React.ReactNode }) {
  const [notifications, setNotifications] = React.useState<NotificationResponse[]>([])

  const [unreadCount, setUnreadCount] = React.useState(0)

  const [tradeAlertCount, setTradeAlertCount] = React.useState(0)

  const [chatAlertCount, setChatAlertCount] = React.useState(0)

  const [loading, setLoading] = React.useState(true)

  const refreshNotifications = React.useCallback(async () => {
    try {
      const [page, counts] = await Promise.all([getNotifications(0, 20), getNotificationCounts()])

      setNotifications(page.content)

      setUnreadCount(counts.unreadCount)

      setTradeAlertCount(counts.tradeAlertCount)

      setChatAlertCount(counts.chatAlertCount)
    } catch (error) {
      console.error("Failed to load notifications", error)
    } finally {
      setLoading(false)
    }
  }, [])

  const markNotificationRead = React.useCallback(
    async (id: number) => {
      const notification = notifications.find((item) => item.id === id)

      if (!notification || notification.read) {
        return
      }

      await markRead(id)

      setNotifications((current) =>
        current.map((item) =>
          item.id === id
            ? {
                ...item,
                read: true,
                readAt: new Date().toISOString(),
              }
            : item,
        ),
      )

      setUnreadCount((count) => Math.max(0, count - 1))

      if (notification.resourceType === "TRADE") {
        setTradeAlertCount((count) => Math.max(0, count - 1))
      }

      if (notification.resourceType === "CHAT_CONVERSATION") {
        setChatAlertCount((count) => Math.max(0, count - 1))
      }
    },
    [notifications],
  )

  const markModuleNotificationsRead = React.useCallback(
    async (resourceType: NotificationResourceType) => {
      await markModuleRead(resourceType)

      setNotifications((current) =>
        current.map((notification) =>
          notification.resourceType === resourceType
            ? {
                ...notification,
                read: true,
                readAt: new Date().toISOString(),
              }
            : notification,
        ),
      )

      await refreshNotifications()
    },
    [refreshNotifications],
  )

  const markAllNotificationsRead = React.useCallback(async () => {
    await markAllRead()

    setNotifications((current) =>
      current.map((notification) => ({
        ...notification,
        read: true,
        readAt: new Date().toISOString(),
      })),
    )

    setUnreadCount(0)
    setTradeAlertCount(0)
    setChatAlertCount(0)
  }, [])

  React.useEffect(() => {
    void refreshNotifications()
  }, [refreshNotifications])
  React.useEffect(() => {
    const refreshAfterPageMutation = () => {
      void refreshNotifications().catch((error) => {
        console.error("Failed to refresh notifications after a page action", error)
      })
    }

    window.addEventListener("smarttrade:notifications-changed", refreshAfterPageMutation)
    return () => window.removeEventListener("smarttrade:notifications-changed", refreshAfterPageMutation)
  }, [refreshNotifications])

  React.useEffect(() => {
    const subscribe = () => {
      subscribeToNotifications((event: NotificationWebsocketResponse) => {
        setNotifications((current) => {
          const exists = current.some((item) => item.id === event.notification.id)

          if (exists) {
            return current
          }

          return [event.notification, ...current].slice(0, 20)
        })

        setUnreadCount(event.unreadCount)

        setTradeAlertCount(event.tradeAlertCount)

        setChatAlertCount(event.chatAlertCount)
      })
    }

    const removeConnectionListener = onWebSocketConnected(subscribe)

    return () => {
      removeConnectionListener()
      unsubscribeFromNotifications()
    }
  }, [])

  return (
    <NotificationContext.Provider
      value={{
        notifications,
        unreadCount,
        tradeAlertCount,
        chatAlertCount,
        loading,
        refreshNotifications,
        markNotificationRead,
        markModuleNotificationsRead,
        markAllNotificationsRead,
      }}
    >
      {children}
    </NotificationContext.Provider>
  )
}

export function useNotifications() {
  const context = React.useContext(NotificationContext)

  if (!context) {
    throw new Error("useNotifications must be used inside NotificationProvider")
  }

  return context
}
