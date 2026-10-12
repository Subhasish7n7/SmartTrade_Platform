import { redirect } from "next/navigation"

import { getCurrentUser } from "../auth/auth.server"

export async function requireAdmin() {
  try {
    const user = await getCurrentUser()

    if (user.role !== "ADMIN") {
      redirect("/marketplace")
    }

    return user
  } catch {
    redirect("/login?redirect=/admin")
  }
}