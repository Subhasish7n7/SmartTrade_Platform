import type {
  NotificationResourceType,
  NotificationResponse,
  NotificationCounts,
  NotificationPage
} from "@/lib/types/api/notifications"

import { clientApi } from "../configs/client"



export async function getNotifications(
  page = 0,
  size = 20,
) {
  const response = await clientApi.get<NotificationPage>(
    "/notifications",
    {
      params: {
        page,
        size,
      },
    },
  )

  return response.data
}

export async function deleteNotification(id: number) {
  await clientApi.delete(`/notifications/${id}`)
}

export async function markRead(id: number) {
  await clientApi.patch(`/notifications/${id}/read`)
}

export async function markAllRead() {
  await clientApi.patch("/notifications/read-all")
}

export async function markModuleRead(
  resourceType: NotificationResourceType,
) {
  await clientApi.patch("/notifications/module-read", null, {
    params: {
      resourceType,
    },
  })
}

export async function unreadCount() {
  const response = await clientApi.get<number>(
    "/notifications/unread-count",
  )

  return response.data
}
export async function getNotificationCounts() {
  const response = await clientApi.get<NotificationCounts>(
    "/notifications/counts",
  )

  return response.data
}