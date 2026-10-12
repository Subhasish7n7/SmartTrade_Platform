import Link from "next/link"
import { ArrowLeft } from "lucide-react"

import { AuthLayout } from "@/components/auth/AuthLayout"
import { ForgotPasswordForm } from "@/components/auth/ForgotPasswordForm"
import { redirectIfAuthenticated } from "@/lib/auth/redirect-if-authenticated"

export default async function ForgotPasswordPage() {
  await redirectIfAuthenticated()

  return (
    <AuthLayout title="Reset your password" subtitle="We’ll help you get back into your account.">
      <div className="mb-5">
        <Link href="/login" className="inline-flex items-center gap-2 text-sm text-muted-foreground transition-colors hover:text-foreground">
          <ArrowLeft className="h-4 w-4" /> Back to sign in
        </Link>
      </div>
      <ForgotPasswordForm />
    </AuthLayout>
  )
}
