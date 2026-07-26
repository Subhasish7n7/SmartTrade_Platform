import { ArrowLeft } from "lucide-react"
import Link from "next/link"

import { AnimatedPage, Section } from "@/components/marketplace/animations/section"

import { AuthLayout } from "@/components/auth/AuthLayout"
import { RegisterForm } from "@/components/auth/RegisterForm"

import { redirectIfAuthenticated } from "@/lib/auth/redirect-if-authenticated"

export default async function RegisterPage() {
  await redirectIfAuthenticated()

  return (
    <AnimatedPage>
      <Section>
        <div className="absolute left-6 top-6 z-50">
          <Link href="/" className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground transition-colors">
            <ArrowLeft className="h-4 w-4" />
            Browse Marketplace
          </Link>
        </div>
      </Section>

      <Section>
        <AuthLayout title="Create Account" subtitle="Join SmartTrade and start trading securely.">
          <RegisterForm />
        </AuthLayout>
      </Section>
    </AnimatedPage>
  )
}
