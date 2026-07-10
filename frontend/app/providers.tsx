"use client"

import { NavigationProvider } from "@/components/navigation-history"

export function Providers({ children }: { children: React.ReactNode }) {
  return (
      <NavigationProvider>
        {children}
      </NavigationProvider>
  )
}
