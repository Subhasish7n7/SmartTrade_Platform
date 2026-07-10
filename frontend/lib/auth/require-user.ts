import { redirect } from "next/navigation"

import { getCurrentUser } from "@/lib/api/auth/auth.server"

export async function requireUser(currentPath: string) {
  try {
    return await getCurrentUser()
  } catch {
    redirect(`/login?redirect=${encodeURIComponent(currentPath)}`)
  }
}
