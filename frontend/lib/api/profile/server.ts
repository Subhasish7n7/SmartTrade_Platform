import { MyUserProfile, PublicUserProfile } from "@/lib/types/api/profile"
import { serverApi } from "../configs/server"

export async function getMyProfile() {
  const api = await serverApi()
  const response = await api.get<MyUserProfile>("/users/me/profile")
  return response.data
}

export async function getPublicProfile(userId: number) {
  const api = await serverApi()
  const response = await api.get<PublicUserProfile>(`/users/${userId}/profile`)
  return response.data
}
