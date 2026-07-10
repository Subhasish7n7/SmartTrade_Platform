import { serverApi } from "../server"

import { CurrentUser } from "@/lib/types/api/auth"

export async function getCurrentUser() {
  const api = await serverApi()

  const { data } = await api.get<CurrentUser>("/auth/me")

  return data
}
