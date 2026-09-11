"use client"

import * as React from "react"

import { clientApi } from "@/lib/api/configs/client"
import { CurrentUser } from "@/lib/types/api/auth"

interface AuthContextValue {
  user: CurrentUser | null
  loading: boolean
  refreshUser: () => Promise<void>
  setUser: React.Dispatch<React.SetStateAction<CurrentUser | null>>
}

const AuthContext = React.createContext<AuthContextValue | null>(null)

export function AuthProvider({ children,}: {
  children: React.ReactNode
}) {
  const [user, setUser] = React.useState<CurrentUser | null>(null)
  const [loading, setLoading] = React.useState(true)

  const refreshUser = React.useCallback(async () => {
    try {
      const { data } = await clientApi.get<CurrentUser>("/auth/me")

      setUser(data)
    } catch {
      setUser(null)
    } finally {
      setLoading(false)
    }
  }, [])

  React.useEffect(() => {
    refreshUser()
  }, [refreshUser])

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        refreshUser,
        setUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = React.useContext(AuthContext)

  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider")
  }

  return context
}