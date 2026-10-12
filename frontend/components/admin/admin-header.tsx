"use client"

import {
  ChevronDown,
  LogOut,
  Store,
  User,
} from "lucide-react"
import { useRouter } from "next/navigation"
import Link from "next/link"
import * as React from "react"
import { toast } from "sonner"

import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { logout } from "@/lib/api/auth/auth.client"
import { stompClient } from "@/lib/api/configs/websocket"
import { useAuth } from "@/lib/providers/auth-provider"

interface Props {
  user: {
    name: string
    email: string
    avatarUrl: string
  }
}

export function AdminHeader({ user }: Props) {
  const router = useRouter()
  const { setUser } = useAuth()

  async function handleLogout() {
    try {
      await stompClient.deactivate()
      await logout()
      setUser(null)
      router.replace("/login")
      router.refresh()
    } catch (error) {
      console.error("Logout failed", error)
      toast.error("Failed to log out")
    }
  }

  return (
    <header className="fixed inset-x-0 top-0 z-50 border-b border-white/[0.06] bg-[rgba(8,13,21,.78)] backdrop-blur-2xl lg:left-[258px]">
      <div className="flex h-16 items-center justify-between gap-4 px-4 sm:px-6 lg:px-7">
        <div className="pl-12 lg:pl-0">
          <p className="text-xs font-semibold uppercase tracking-[0.16em] text-muted-foreground">Operations</p>
          <p className="mt-0.5 text-sm font-semibold">Marketplace Administration</p>
        </div>

        <div className="flex items-center gap-2">
          <Link
            href="/marketplace"
            className="hidden items-center gap-2 rounded-xl border border-white/[0.07] bg-white/[0.025] px-3 py-2 text-xs font-medium text-muted-foreground transition hover:border-white/[0.12] hover:text-foreground sm:inline-flex"
          >
            <Store className="h-3.5 w-3.5" />
            Marketplace
          </Link>

          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <button className="flex items-center gap-2 rounded-xl border border-white/[0.07] bg-white/[0.025] px-2 py-1.5 transition hover:border-white/[0.12] hover:bg-white/[0.04]">
                <Avatar className="h-8 w-8 rounded-lg">
                  <AvatarImage src={user.avatarUrl} />
                  <AvatarFallback className="rounded-lg bg-[var(--trust-blue)]/[0.09] text-xs font-bold">
                    {user.name.slice(0, 2).toUpperCase()}
                  </AvatarFallback>
                </Avatar>
                <div className="hidden text-left sm:block">
                  <p className="max-w-32 truncate text-xs font-semibold">{user.name}</p>
                  <p className="text-[10px] text-muted-foreground">Administrator</p>
                </div>
                <ChevronDown className="h-4 w-4 text-muted-foreground" />
              </button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-60 glass-strong border-border/60 p-2">
              <DropdownMenuLabel className="px-3 py-2">
                <p className="truncate text-sm font-semibold">{user.name}</p>
                <p className="truncate text-xs font-normal text-muted-foreground">{user.email}</p>
              </DropdownMenuLabel>
              <DropdownMenuSeparator />
              <DropdownMenuItem asChild className="premium-menu-item">
                <Link href="/profile">
                  <User className="mr-2 h-4 w-4" />
                  Profile
                </Link>
              </DropdownMenuItem>
              <DropdownMenuItem asChild className="premium-menu-item">
                <Link href="/marketplace">
                  <Store className="mr-2 h-4 w-4" />
                  Marketplace
                </Link>
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem
                onClick={() => void handleLogout()}
                className="premium-menu-item cursor-pointer text-destructive focus:text-destructive"
              >
                <LogOut className="mr-2 h-4 w-4" />
                Log out
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>
    </header>
  )
}