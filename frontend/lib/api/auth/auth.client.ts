"use client"

import { clientApi } from "../configs/client"
import type { LoginRequest, RegisterRequest } from "@/lib/types/api/auth"

export async function login(request: LoginRequest) {
  await clientApi.post("/auth/login", request)
}

export async function register(request: RegisterRequest) {
  console.log("this is the request", request)
  await clientApi.post("/auth/register", request)
}

export async function logout() {
  await clientApi.post("/auth/logout")
}

export async function requestPasswordReset(email: string) {
  // The endpoint intentionally returns an empty 200 response whether or not
  // the account exists. Do not inspect response.data to decide what to show.
  await clientApi.post("/auth/forgot-password", { email })
}

export async function resetPassword(token: string, newPassword: string) {
  await clientApi.post("/auth/reset-password", { token, newPassword })
}

export async function changePassword(currentPassword: string, newPassword: string) {
  await clientApi.post("/auth/change-password", { currentPassword, newPassword })
}
