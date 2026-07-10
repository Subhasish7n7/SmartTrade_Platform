import { redirect } from "next/navigation"

import { getCurrentUser } from "@/lib/api/auth/auth.server"

export async function redirectIfAuthenticated() {
  try {
    await getCurrentUser()

    redirect("/")
  } catch {}
}
