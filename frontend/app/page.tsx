import { Suspense } from "react"

import { MarketplaceEntry } from "@/components/marketplace/marketplace-entry"

export default function HomePage() {
  return (
    <Suspense
      fallback={
        <main className="min-h-screen bg-background marketplace-bg">
          <div className="h-16" aria-hidden="true" />
        </main>
      }
    >
      <MarketplaceEntry />
    </Suspense>
  )
}
