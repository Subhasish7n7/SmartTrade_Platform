"use client"

import Link from "next/link"
import { useCallback, useEffect, useMemo, useRef, useState } from "react"
import {
  AlertCircle,
  ArrowLeftRight,
  Bell,
  Check,
  CheckCheck,
  Clock3,
  Loader2,
  MessageCircle,
  Package,
  RefreshCw,
  Search,
  ShieldCheck,
  Trash2,
} from "lucide-react"

import { AnimatedPage } from "@/components/marketplace/animations/section"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { deleteNotification, getNotifications } from "@/lib/api/notifications/notification"
import { useNotifications } from "@/lib/providers/notification-provider"
import type { NotificationResourceType, NotificationResponse } from "@/lib/types/api/notifications"

 type NotificationFilter = "ALL" | "UNREAD" | "TRADES" | "MESSAGES" | "SYSTEM"

const PAGE_SIZE = 20

function timestamp(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return "Time unavailable"
  return new Intl.DateTimeFormat("en-IN", { dateStyle: "medium", timeStyle: "short" }).format(date)
}

function relativeTime(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return "Recently"
  const minutes = Math.floor((Date.now() - date.getTime()) / 60_000)
  if (minutes < 1) return "Just now"
  if (minutes < 60) return `${minutes}m ago`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}h ago`
  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}d ago`
  return new Intl.DateTimeFormat("en-IN", { day: "numeric", month: "short" }).format(date)
}

function hrefFor(notification: NotificationResponse) {
  switch (notification.resourceType) {
    case "TRADE":
      // A notification does not carry the tradeOfferId required by the detail route.
      return "/trades"
    case "CHAT_CONVERSATION":
      return "/messages"
    case "ITEM":
      return notification.resourceId ? `/item/${notification.resourceId}` : "/marketplace"
    case "USER": {
      const userId = notification.actorId ?? notification.resourceId
      return userId ? `/profile/${userId}` : "/profile"
    }
    case "SYSTEM":
    default:
      return "/notifications"
  }
}

function category(notification: NotificationResponse): Exclude<NotificationFilter, "ALL" | "UNREAD"> | "OTHER" {
  if (notification.resourceType === "TRADE" || notification.type.startsWith("TRADE_") || notification.type === "BUY_OFFER_RECEIVED") return "TRADES"
  if (notification.resourceType === "CHAT_CONVERSATION" || notification.type === "CHAT_MESSAGE") return "MESSAGES"
  if (notification.resourceType === "SYSTEM" || notification.type === "SYSTEM") return "SYSTEM"
  return "OTHER"
}

function iconFor(notification: NotificationResponse) {
  const kind = category(notification)
  if (kind === "TRADES") return { Icon: ArrowLeftRight, tone: "bg-primary/10 text-primary" }
  if (kind === "MESSAGES") return { Icon: MessageCircle, tone: "bg-sky-400/10 text-sky-300" }
  if (kind === "SYSTEM") return { Icon: ShieldCheck, tone: "bg-emerald-400/10 text-emerald-300" }
  if (notification.resourceType === "ITEM") return { Icon: Package, tone: "bg-amber-400/10 text-amber-300" }
  return { Icon: Bell, tone: "bg-muted text-muted-foreground" }
}

function message(error: unknown, fallback: string) {
  return error instanceof Error && error.message.trim() ? error.message : fallback
}

function mergeUnique(current: NotificationResponse[], incoming: NotificationResponse[]) {
  const byId = new Map<number, NotificationResponse>()
  current.forEach((item) => byId.set(item.id, item))
  incoming.forEach((item) => {
    const existing = byId.get(item.id)
    byId.set(
      item.id,
      existing?.read
        ? { ...item, read: true, readAt: existing.readAt ?? item.readAt }
        : existing ? { ...existing, ...item } : item,
    )
  })
  return [...byId.values()].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
}

export default function NotificationsPage() {
  const live = useNotifications()
  const { notifications: liveNotifications, unreadCount, markNotificationRead, markAllNotificationsRead, markModuleNotificationsRead } = live
  const [items, setItems] = useState<NotificationResponse[]>([])
  const [page, setPage] = useState(0)
  const [hasMore, setHasMore] = useState(false)
  const [initialLoading, setInitialLoading] = useState(true)
  const [loadingMore, setLoadingMore] = useState(false)
  const [working, setWorking] = useState(false)
  const [busyId, setBusyId] = useState<number | null>(null)
  const [filter, setFilter] = useState<NotificationFilter>("ALL")
  const [query, setQuery] = useState("")
  const [error, setError] = useState<string | null>(null)
  const deletedIds = useRef(new Set<number>())

  const loadFirstPage = useCallback(async () => {
    setInitialLoading(true)
    setError(null)
    try {
      const response = await getNotifications(0, PAGE_SIZE)
      setItems(response.content)
      setPage(response.number)
      setHasMore(!response.last)
    } catch (loadError) {
      setError(message(loadError, "Notifications could not be loaded."))
    } finally {
      setInitialLoading(false)
    }
  }, [])

  useEffect(() => {
    void loadFirstPage()
  }, [loadFirstPage])

  // Consume the existing provider's websocket feed. This does not create another STOMP connection.
  useEffect(() => {
    if (liveNotifications.length === 0) return
    const notDeleted = liveNotifications.filter((item) => !deletedIds.current.has(item.id))
    setItems((current) => mergeUnique(current, notDeleted))
  }, [liveNotifications])

  const visible = useMemo(() => {
    const needle = query.trim().toLocaleLowerCase()
    return items.filter((item) => {
      if (filter === "UNREAD" && item.read) return false
      if (filter !== "ALL" && filter !== "UNREAD" && category(item) !== filter) return false
      if (!needle) return true
      return [item.actorName, item.message, item.type, item.resourceType, item.resourceId?.toString()]
        .filter(Boolean)
        .join(" ")
        .toLocaleLowerCase()
        .includes(needle)
    })
  }, [filter, items, query])

  const filterCounts = useMemo(() => ({
    ALL: items.length,
    UNREAD: items.filter((item) => !item.read).length,
    TRADES: items.filter((item) => category(item) === "TRADES").length,
    MESSAGES: items.filter((item) => category(item) === "MESSAGES").length,
    SYSTEM: items.filter((item) => category(item) === "SYSTEM").length,
  }), [items])

  async function markOne(id: number) {
    setBusyId(id)
    setError(null)
    try {
      await markNotificationRead(id)
      setItems((current) => current.map((item) => item.id === id ? { ...item, read: true, readAt: new Date().toISOString() } : item))
    } catch (actionError) {
      setError(message(actionError, "This notification could not be marked as read."))
    } finally {
      setBusyId(null)
    }
  }

  async function markEverythingRead() {
    setWorking(true)
    setError(null)
    try {
      await markAllNotificationsRead()
      setItems((current) => current.map((item) => ({ ...item, read: true, readAt: item.readAt ?? new Date().toISOString() })))
    } catch (actionError) {
      setError(message(actionError, "Notifications could not be marked as read."))
    } finally {
      setWorking(false)
    }
  }

  async function markFilteredModuleRead(resourceType: NotificationResourceType) {
    setWorking(true)
    setError(null)
    try {
      await markModuleNotificationsRead(resourceType)
      setItems((current) => current.map((item) => item.resourceType === resourceType ? { ...item, read: true, readAt: item.readAt ?? new Date().toISOString() } : item))
    } catch (actionError) {
      setError(message(actionError, "This notification category could not be marked as read."))
    } finally {
      setWorking(false)
    }
  }

  async function removeNotification(item: NotificationResponse) {
    setBusyId(item.id)
    setError(null)
    try {
      // Use the provider's deletion action if the existing provider exposes it.
      const providerWithOptionalDelete = live as typeof live & { deleteNotificationById?: (id: number) => Promise<void> }
      if (providerWithOptionalDelete.deleteNotificationById) {
        await providerWithOptionalDelete.deleteNotificationById(item.id)
      } else {
        // Keep badge counts accurate when using the older provider contract.
        if (!item.read) await markNotificationRead(item.id)
        await deleteNotification(item.id)
        window.dispatchEvent(new Event("smarttrade:notifications-changed"))
      }
      deletedIds.current.add(item.id)
      setItems((current) => current.filter((notification) => notification.id !== item.id))
    } catch (actionError) {
      setError(message(actionError, "This notification could not be deleted."))
    } finally {
      setBusyId(null)
    }
  }

  async function loadMore() {
    if (loadingMore || !hasMore) return
    setLoadingMore(true)
    setError(null)
    try {
      const response = await getNotifications(page + 1, PAGE_SIZE)
      setItems((current) => mergeUnique(current, response.content.filter((item) => !deletedIds.current.has(item.id))))
      setPage(response.number)
      setHasMore(!response.last)
    } catch (loadError) {
      setError(message(loadError, "More notifications could not be loaded."))
    } finally {
      setLoadingMore(false)
    }
  }

  const hasFilters = filter !== "ALL" || query.trim().length > 0

  return (
    <main className="min-h-screen px-4 pb-24 pt-20 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-5xl">
        <AnimatedPage>
          <div className="space-y-5">
            <header className="content-surface !p-5 sm:!p-6">
              <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div className="flex items-start gap-3">
                  <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl border border-primary/20 bg-primary/[0.07] text-primary"><Bell className="h-5 w-5" /></span>
                  <div>
                    <p className="text-[11px] font-semibold uppercase tracking-[0.16em] text-muted-foreground">Your activity</p>
                    <h1 className="mt-1 text-2xl font-bold tracking-tight sm:text-3xl">Notifications</h1>
                    <p className="mt-1 text-sm text-muted-foreground">Trade updates, messages, and account activity in one place.</p>
                  </div>
                </div>
                <div className="flex items-center gap-2 self-start rounded-xl border border-border/60 bg-background/30 px-3 py-2 text-sm text-muted-foreground sm:self-auto">
                  <span className={`h-2 w-2 rounded-full ${unreadCount > 0 ? "bg-primary" : "bg-emerald-400"}`} />
                  <span><strong className="text-foreground">{unreadCount}</strong> unread</span>
                </div>
              </div>
            </header>

            <section className="content-surface !p-4 sm:!p-5">
              <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
                <label className="relative block flex-1">
                  <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                  <Input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search notification text or sender" aria-label="Search notifications" className="h-10 rounded-xl pl-9" />
                </label>
                <div className="flex flex-wrap gap-2">
                  {(["ALL", "UNREAD", "TRADES", "MESSAGES", "SYSTEM"] as NotificationFilter[]).map((value) => (
                    <button key={value} type="button" onClick={() => setFilter(value)} aria-pressed={filter === value} className={`inline-flex h-9 items-center gap-2 rounded-xl border px-3 text-xs font-semibold transition ${filter === value ? "border-primary/35 bg-primary/10 text-foreground" : "border-border/60 bg-background/30 text-muted-foreground hover:text-foreground"}`}>
                      {value === "ALL" ? "All" : value === "UNREAD" ? "Unread" : value === "TRADES" ? "Trades" : value === "MESSAGES" ? "Messages" : "System"}
                      <span className="rounded-full bg-background/60 px-1.5 py-0.5 text-[10px] tabular-nums">{filterCounts[value]}</span>
                    </button>
                  ))}
                </div>
              </div>
              <div className="mt-4 flex flex-wrap items-center gap-2 border-t border-border/50 pt-4">
                <Button type="button" variant="surface" disabled={working || unreadCount === 0} onClick={() => void markEverythingRead()} className="h-9 rounded-lg text-xs"><CheckCheck className="mr-2 h-3.5 w-3.5" />Mark all read</Button>
                {filter === "TRADES" && <Button type="button" variant="ghost" disabled={working || filterCounts.TRADES === 0} onClick={() => void markFilteredModuleRead("TRADE")} className="h-9 rounded-lg text-xs">Mark trades read</Button>}
                {filter === "MESSAGES" && <Button type="button" variant="ghost" disabled={working || filterCounts.MESSAGES === 0} onClick={() => void markFilteredModuleRead("CHAT_CONVERSATION")} className="h-9 rounded-lg text-xs">Mark messages read</Button>}
                {filter === "SYSTEM" && <Button type="button" variant="ghost" disabled={working || filterCounts.SYSTEM === 0} onClick={() => void markFilteredModuleRead("SYSTEM")} className="h-9 rounded-lg text-xs">Mark system read</Button>}
                {hasFilters && <button type="button" onClick={() => { setFilter("ALL"); setQuery("") }} className="ml-auto text-xs font-medium text-muted-foreground underline-offset-4 hover:text-foreground hover:underline">Clear filters</button>}
              </div>
              {error && <p role="alert" className="mt-4 flex items-start gap-2 rounded-xl border border-destructive/30 bg-destructive/5 p-3 text-sm text-destructive"><AlertCircle className="mt-0.5 h-4 w-4 shrink-0" />{error}<button type="button" className="ml-auto shrink-0 underline" onClick={() => void loadFirstPage()}>Retry</button></p>}
            </section>

            <section className="space-y-3">
              {initialLoading ? (
                <div className="content-surface flex min-h-56 items-center justify-center gap-2 text-sm text-muted-foreground"><Loader2 className="h-4 w-4 animate-spin" />Loading notifications…</div>
              ) : visible.length > 0 ? (
                visible.map((item) => {
                  const { Icon, tone } = iconFor(item)
                  return (
                    <article key={item.id} className={`rounded-2xl border bg-card/50 p-4 transition-colors sm:p-5 ${item.read ? "border-border/55" : "border-primary/25 bg-primary/[0.025]"}`}>
                      <div className="flex items-start gap-3 sm:gap-4">
                        <span className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-xl ${tone}`}><Icon className="h-5 w-5" /></span>
                        <div className="min-w-0 flex-1">
                          <div className="flex flex-wrap items-center gap-2">
                            <p className="text-sm font-semibold">{item.actorName || "SmartTrade"}</p>
                            {!item.read && <span className="rounded-full border border-primary/25 bg-primary/10 px-2 py-0.5 text-[10px] font-semibold text-primary">Unread</span>}
                            <span className="ml-auto text-[11px] text-muted-foreground" title={timestamp(item.createdAt)}>{relativeTime(item.createdAt)}</span>
                          </div>
                          <Link href={hrefFor(item)} onClick={() => { if (!item.read) void markOne(item.id) }} className="mt-1 block text-sm leading-6 text-muted-foreground transition-colors hover:text-foreground">
                            {item.message}
                          </Link>
                          <p className="mt-1 text-[10px] uppercase tracking-[0.12em] text-muted-foreground/70">{item.type.replaceAll("_", " ")}</p>
                          <div className="mt-3 flex flex-wrap gap-2">
                            {!item.read && <Button type="button" variant="ghost" size="sm" disabled={busyId === item.id} onClick={() => void markOne(item.id)} className="h-8 rounded-lg px-2.5 text-xs"><Check className="mr-1.5 h-3.5 w-3.5" />Mark read</Button>}
                            <Button type="button" variant="ghost" size="sm" disabled={busyId === item.id} onClick={() => void removeNotification(item)} className="h-8 rounded-lg px-2.5 text-xs text-muted-foreground hover:text-destructive">
                              {busyId === item.id ? <Loader2 className="mr-1.5 h-3.5 w-3.5 animate-spin" /> : <Trash2 className="mr-1.5 h-3.5 w-3.5" />}Delete
                            </Button>
                          </div>
                        </div>
                      </div>
                    </article>
                  )
                })
              ) : (
                <div className="content-surface flex min-h-64 flex-col items-center justify-center px-6 py-12 text-center">
                  <span className="flex h-12 w-12 items-center justify-center rounded-2xl border border-border/60 bg-background/30 text-muted-foreground"><Bell className="h-5 w-5" /></span>
                  <h2 className="mt-4 text-lg font-semibold">{error && items.length === 0 ? "Couldn’t load notifications" : hasFilters ? "No matching notifications" : "You're all caught up"}</h2>
                  <p className="mt-1 max-w-md text-sm leading-6 text-muted-foreground">{error && items.length === 0 ? "Check your connection or sign-in status, then try again." : hasFilters ? "Try another search or filter, or load older notifications." : "New trade updates, messages, and account notices will appear here."}</p>
                  {error && items.length === 0 ? <Button type="button" variant="surface" className="mt-4 rounded-xl" onClick={() => void loadFirstPage()}>Try again</Button> : hasFilters && <Button type="button" variant="surface" className="mt-4 rounded-xl" onClick={() => { setFilter("ALL"); setQuery("") }}>Clear filters</Button>}
                </div>
              )}
            </section>

            {hasMore && !initialLoading && <div className="flex justify-center"><Button type="button" variant="surface" disabled={loadingMore} onClick={() => void loadMore()} className="h-10 rounded-xl px-5">{loadingMore ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <RefreshCw className="mr-2 h-4 w-4" />}{loadingMore ? "Loading…" : "Load older notifications"}</Button></div>}
            {!hasMore && !initialLoading && items.length > 0 && <p className="flex items-center justify-center gap-2 py-2 text-xs text-muted-foreground"><Clock3 className="h-3.5 w-3.5" />You're at the end of your notifications.</p>}
          </div>
        </AnimatedPage>
      </div>
    </main>
  )
}
