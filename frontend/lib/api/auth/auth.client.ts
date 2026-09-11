"use client"

import { clientApi } from "../configs/client"

import { LoginRequest, RegisterRequest } from "@/lib/types/api/auth"

export async function login(request: LoginRequest) {
  await clientApi.post("/auth/login", request)
}

export async function register(request: RegisterRequest) {
  await clientApi.post("/auth/register", request)
}

export async function logout() {
  // set user to null in order to deactivate ws. setUser(null)
  await clientApi.post("/auth/logout")
}
