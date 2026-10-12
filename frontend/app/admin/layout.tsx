import type { ReactNode } from "react"

import { requireAdmin } from "@/lib/api/auth/require-admin"

import { AdminShell } from "@/components/admin/admin-shell"

export default async function AdminLayout({ children }: { children: ReactNode }) {
  const user = await requireAdmin()

  return (
    <AdminShell
      user={{
        name: user.name,
        email: user.email,
        avatarUrl: user.avatarUrl,
      }}
    >
      {children}
    </AdminShell>
  )
}