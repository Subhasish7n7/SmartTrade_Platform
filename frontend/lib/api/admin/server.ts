import type {
  AdminAuditLogResponse,
  AdminDashboardResponse,
  AdminDisputeDetailsResponse,
  AdminDisputeSummaryResponse,
  AdminItemDetailsResponse,
  AdminItemSummaryResponse,
  AdminPageResponse,
  AdminTradeDetailsResponse,
  AdminTradeSummaryResponse,
  AdminUserDetailsResponse,
  AdminUserSummaryResponse,
} from "@/lib/types/api/admin"

import { serverApi } from "../configs/server"

interface PageParams {
  page?: number
  size?: number
  search?: string
}

export async function getAdminDashboard() {
  const api = await serverApi()
  const response = await api.get<AdminDashboardResponse>("/admin/dashboard")
  return response.data
}

export async function getAdminUsers(params: PageParams & {
  role?: string
  status?: string
}) {
  const api = await serverApi()
  const response = await api.get<AdminPageResponse<AdminUserSummaryResponse>>("/admin/users", {
    params: {
      page: params.page ?? 0,
      size: params.size ?? 20,
      search: params.search || undefined,
      role: params.role || undefined,
      status: params.status || undefined,
    },
  })
  return response.data
}

export async function getAdminUser(userId: number) {
  const api = await serverApi()
  const response = await api.get<AdminUserDetailsResponse>(`/admin/users/${userId}`)
  return response.data
}

export async function getAdminItems(params: PageParams & {
  moderationStatus?: string
  available?: boolean
}) {
  const api = await serverApi()
  const response = await api.get<AdminPageResponse<AdminItemSummaryResponse>>("/admin/items", {
    params: {
      page: params.page ?? 0,
      size: params.size ?? 20,
      search: params.search || undefined,
      moderationStatus: params.moderationStatus || undefined,
      available: params.available,
    },
  })
  return response.data
}

export async function getAdminItem(itemId: number) {
  const api = await serverApi()
  const response = await api.get<AdminItemDetailsResponse>(`/admin/items/${itemId}`)
  return response.data
}

export async function getAdminTrades(params: PageParams & { status?: string }) {
  const api = await serverApi()
  const response = await api.get<AdminPageResponse<AdminTradeSummaryResponse>>("/admin/trades", {
    params: {
      page: params.page ?? 0,
      size: params.size ?? 20,
      search: params.search || undefined,
      status: params.status || undefined,
    },
  })
  return response.data
}

export async function getAdminTrade(tradeId: number) {
  const api = await serverApi()
  const response = await api.get<AdminTradeDetailsResponse>(`/admin/trades/${tradeId}`)
  return response.data
}

export async function getAdminDisputes(params: {
  page?: number
  size?: number
  status?: string
}) {
  const api = await serverApi()
  const response = await api.get<AdminPageResponse<AdminDisputeSummaryResponse>>("/admin/disputes", {
    params: {
      page: params.page ?? 0,
      size: params.size ?? 20,
      status: params.status || undefined,
    },
  })
  return response.data
}

export async function getAdminDispute(disputeId: number) {
  const api = await serverApi()
  const response = await api.get<AdminDisputeDetailsResponse>(`/admin/disputes/${disputeId}`)
  return response.data
}

export async function getAdminAuditLogs(params: {
  page?: number
  size?: number
  action?: string
  targetType?: string
}) {
  const api = await serverApi()
  const response = await api.get<AdminPageResponse<AdminAuditLogResponse>>("/admin/audit-logs", {
    params: {
      page: params.page ?? 0,
      size: params.size ?? 50,
      action: params.action || undefined,
      targetType: params.targetType || undefined,
    },
  })
  return response.data
}