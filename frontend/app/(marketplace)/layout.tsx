// app/(marketplace)/layout.tsx

import { PageTransition } from "@/components/marketplace/animations/page-transition"
import { Navbar } from "@/components/marketplace/navbar"

export default function MarketplaceLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen bg-background marketplace-bg">
      <Navbar />
      <PageTransition>
        {children}
      </PageTransition>
    </div>
  )
}
