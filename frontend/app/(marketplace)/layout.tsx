// app/(marketplace)/layout.tsx

import { Suspense } from "react"

import { PageTransition } from "@/components/marketplace/animations/page-transition"
import { Navbar } from "@/components/marketplace/navbar"

export default function MarketplaceLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen bg-background marketplace-bg">
      <Suspense fallback={<div className="h-16" aria-hidden="true" />}>
        <Navbar />
      </Suspense>
      <PageTransition>{children}</PageTransition>
    </div>
  )
}
