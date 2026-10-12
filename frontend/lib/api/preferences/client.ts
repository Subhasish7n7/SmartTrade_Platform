"use client"

import { clientApi } from "@/lib/api/configs/client"
import type { UpdateUserPreferencesRequest, UserPreferences } from "@/lib/types/api/preferences"

export async function getMyPreferences(): Promise<UserPreferences> {
  const { data } = await clientApi.get<UserPreferences>("/users/me/preferences")
  return data
}

export async function updateMyPreferences(update: UpdateUserPreferencesRequest): Promise<UserPreferences> {
  const { data } = await clientApi.patch<UserPreferences>("/users/me/preferences", update)
  return data
}
