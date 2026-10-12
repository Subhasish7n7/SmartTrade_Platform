import type { ReactNode } from "react"

import { AdminHeader } from "./admin-header"
import { AdminSidebar } from "./admin-sidebar"

interface Props {
  children: ReactNode
  user: {
    name: string
    email: string
    avatarUrl: string
  }
}

export function AdminShell({ children, user }: Props) {
  return (
    <div className="min-h-screen bg-background text-foreground">
      <div className="pointer-events-none fixed inset-0 -z-10 overflow-hidden">
        <div className="absolute -left-32 top-12 h-96 w-96 rounded-full bg-[var(--trust-blue)]/[0.055] blur-3xl" />
        <div className="absolute right-[-10rem] top-24 h-[32rem] w-[32rem] rounded-full bg-[var(--negotiation-violet)]/[0.045] blur-3xl" />
        <div className="absolute bottom-[-14rem] left-1/3 h-96 w-96 rounded-full bg-[var(--premium-gold)]/[0.025] blur-3xl" />
      </div>

      <AdminSidebar />

      <div className="lg:pl-[258px]">
        <AdminHeader user={user} />
        <main className="min-h-[calc(100vh-4rem)] px-4 pb-10 pt-[4.75rem] sm:px-6 lg:px-7">
          <div className="mx-auto max-w-[1520px]">{children}</div>
        </main>
      </div>
    </div>
  )
}