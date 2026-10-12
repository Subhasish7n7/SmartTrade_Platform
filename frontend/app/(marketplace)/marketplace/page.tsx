import { Suspense } from "react"

import MarketplacePageClient from "./marketplace-client"

export default function MarketplaceRoute() {
  return (
    <Suspense
      fallback={
        <main
          className="mx-auto min-h-[60vh] max-w-[1600px] px-4 pt-24 sm:px-6 lg:px-8"
          aria-busy="true"
        >
          <p className="text-sm text-muted-foreground" role="status">
            Loading marketplace...
          </p>
        </main>
      }
    >
      <MarketplacePageClient />
    </Suspense>
  )
}
