"use client"

import { ArrowLeftRight, Bell, Check, Clock3, MessageCircle, ShieldCheck } from "lucide-react"
import Link from "next/link"

import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"

import { Button } from "@/components/ui/button"

import { useNotifications } from "@/lib/providers/notification-provider"

import type { NotificationResponse, NotificationType } from "@/lib/types/api/notifications"

interface NotificationProps {}

function getNotificationIcon(type: NotificationType) {
  switch (type) {
    case "TRADE_OFFER_CREATED":
    case "TRADE_OFFER_RECEIVED":
    case "TRADE_ACCEPTED":
    case "TRADE_COMPLETION_REQUESTED":
    case "TRADE_COMPLETED":
    case "TRADE_CANCELLED":
    case "TRADE_EXPIRED":
    case "BUY_OFFER_RECEIVED":
      return ArrowLeftRight

    case "CHAT_MESSAGE":
      return MessageCircle

    case "SYSTEM":
      return ShieldCheck

    default:
      return Bell
  }
}

function getNotificationIconClass(type: NotificationType) {
  switch (type) {
    case "TRADE_OFFER_CREATED":
    case "TRADE_OFFER_RECEIVED":
    case "TRADE_ACCEPTED":
    case "TRADE_COMPLETION_REQUESTED":
    case "TRADE_COMPLETED":
    case "BUY_OFFER_RECEIVED":
      return "bg-primary/10 text-primary"

    case "TRADE_CANCELLED":
    case "TRADE_EXPIRED":
      return "bg-warning/10 text-warning"

    case "CHAT_MESSAGE":
      return "bg-info/10 text-info"

    case "SYSTEM":
      return "bg-success/10 text-success"

    default:
      return "bg-muted text-muted-foreground"
  }
}

function formatNotificationTime(createdAt: string) {
  const created = new Date(createdAt)
  const now = new Date()

  const diffMs = now.getTime() - created.getTime()
  const diffMinutes = Math.floor(diffMs / 60000)

  if (diffMinutes < 1) {
    return "Just now"
  }

  if (diffMinutes < 60) {
    return `${diffMinutes} minute${diffMinutes === 1 ? "" : "s"} ago`
  }

  const diffHours = Math.floor(diffMinutes / 60)

  if (diffHours < 24) {
    return `${diffHours} hour${diffHours === 1 ? "" : "s"} ago`
  }

  const diffDays = Math.floor(diffHours / 24)

  if (diffDays < 7) {
    return `${diffDays} day${diffDays === 1 ? "" : "s"} ago`
  }

  return created.toLocaleDateString(undefined, {
    month: "short",
    day: "numeric",
  })
}

function getNotificationHref(notification: NotificationResponse) {
  switch (notification.resourceType) {
    case "TRADE":
      return "/trades"

    case "CHAT_CONVERSATION":
      return "/messages"

    case "ITEM":
      return `/item/${notification.resourceId}`

    case "USER":
      return "/profile"

    default:
      return "/notifications"
  }
}
console.log()

export function Notification({}: NotificationProps) {
  const { notifications, unreadCount, loading, markNotificationRead, markAllNotificationsRead } = useNotifications()

  return (
    <Popover>
      <PopoverTrigger asChild>
        <Button variant="ghost" size="icon-lg" className="icon-glow icon-glow-cyan relative hover:bg-transparent dark:hover:bg-transparent">
          <Bell className="h-[21px] w-[21px]" />

          {unreadCount > 0 && <span className="nav-count nav-count-primary">{unreadCount > 99 ? "99+" : unreadCount}</span>}

          <span className="sr-only">Notifications</span>
        </Button>
      </PopoverTrigger>

      <PopoverContent
        align="end"
        sideOffset={12}
        className="w-[380px] overflow-hidden rounded-xl border-border/60 bg-card/95 p-0 shadow-2xl backdrop-blur-2xl"
      >
        <div className="flex items-center justify-between px-4 py-3">
          <div>
            <h3 className="text-sm font-semibold">Notifications</h3>

            <p className="text-xs text-muted-foreground">{unreadCount === 0 ? "You're all caught up" : `${unreadCount} unread`}</p>
          </div>

          {unreadCount > 0 && (
            <button
              type="button"
              onClick={() => void markAllNotificationsRead()}
              className="text-xs text-muted-foreground transition-colors hover:text-foreground"
            >
              Mark all read
            </button>
          )}
        </div>

        <div className="border-t border-border/50" />

        <div className="max-h-[360px] overflow-y-auto scrollbar-thin">
          {loading ? (
            <div className="flex items-center justify-center px-4 py-10">
              <Clock3 className="h-4 w-4 animate-pulse text-muted-foreground" />
            </div>
          ) : notifications.length === 0 ? (
            <div className="flex flex-col items-center justify-center px-4 py-10 text-center">
              <div className="mb-3 flex h-10 w-10 items-center justify-center rounded-xl bg-muted/50">
                <Check className="h-5 w-5 text-muted-foreground" />
              </div>

              <p className="text-sm font-medium">No notifications</p>

              <p className="mt-1 text-xs text-muted-foreground">You're all caught up.</p>
            </div>
          ) : (
            notifications.map((notification) => {
              const Icon = getNotificationIcon(notification.type)
              const iconClass = getNotificationIconClass(notification.type)

              const content = (
                <div
                  className={`border-t border-border/40 px-4 py-3 transition-colors hover:bg-white/[0.035] ${
                    !notification.read ? "bg-primary/[0.025]" : ""
                  }`}
                >
                  <div className="flex gap-3">
                    <div className={`mt-0.5 flex h-9 w-9 shrink-0 items-center justify-center rounded-xl ${iconClass}`}>
                      <Icon className="h-4 w-4" />
                    </div>

                    <div className="min-w-0 flex-1">
                      <div className="flex items-start justify-between gap-2">
                        <p className="text-sm font-medium">{notification.actorName ? notification.actorName : "SmartTrade"}</p>

                        {!notification.read && <span className="mt-1.5 h-2 w-2 shrink-0 rounded-full bg-primary" />}
                      </div>

                      <p className="mt-0.5 text-sm text-muted-foreground">{notification.message}</p>

                      <p className="mt-1.5 text-[11px] text-muted-foreground/70">{formatNotificationTime(notification.createdAt)}</p>
                    </div>
                  </div>
                </div>
              )

              return (
                <Link
                  key={notification.id}
                  href={getNotificationHref(notification)}
                  onClick={() => {
                    if (!notification.read) {
                      void markNotificationRead(notification.id)
                    }
                  }}
                >
                  {content}
                </Link>
              )
            })
          )}
        </div>

        <div className="border-t border-border/50 p-2">
          <Link
            href="/notifications"
            className="flex h-9 items-center justify-center rounded-lg text-xs font-medium text-muted-foreground transition-colors hover:bg-white/[0.04] hover:text-foreground"
          >
            View all notifications
          </Link>
        </div>
      </PopoverContent>
    </Popover>
  )
}
