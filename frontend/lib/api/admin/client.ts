import type {
  AdminActionRequest,
  AdminDisputeDetailsResponse,
  ResolveDisputeRequest,
} from "@/lib/types/api/admin"

import { clientApi } from "../configs/client"

export async function suspendAdminUser(userId: number, request: AdminActionRequest) {
  await clientApi.patch(`/admin/users/${userId}/suspend`, request)
}

export async function restoreAdminUser(userId: number, request: AdminActionRequest) {
  await clientApi.patch(`/admin/users/${userId}/restore`, request)
}

export async function hideAdminItem(itemId: number, request: AdminActionRequest) {
  await clientApi.patch(`/admin/items/${itemId}/hide`, request)
}

export async function restoreAdminItem(itemId: number, request: AdminActionRequest) {
  await clientApi.patch(`/admin/items/${itemId}/restore`, request)
}

export async function startAdminDisputeReview(disputeId: number, request: AdminActionRequest) {
  await clientApi.patch(`/admin/disputes/${disputeId}/review`, request)
}

export async function resolveAdminDispute(
  disputeId: number,
  request: ResolveDisputeRequest,
) {
  const response = await clientApi.post<AdminDisputeDetailsResponse>(
    `/admin/disputes/${disputeId}/resolve`,
    request,
  )
  return response.data
}