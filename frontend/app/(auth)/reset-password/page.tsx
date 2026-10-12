import Link from "next/link"
import { ArrowLeft } from "lucide-react"

import { AuthLayout } from "@/components/auth/AuthLayout"
import { ResetPasswordForm } from "@/components/auth/ResetPasswordForm"

interface Props {
  searchParams: Promise<{ token?: string }>
}

export default async function ResetPasswordPage({ searchParams }: Props) {
  const { token = "" } = await searchParams

  return (
    <AuthLayout title="Choose a new password" subtitle="Use a strong password you haven’t used before.">
      <div className="mb-5">
        <Link href="/login" className="inline-flex items-center gap-2 text-sm text-muted-foreground transition-colors hover:text-foreground">
          <ArrowLeft className="h-4 w-4" /> Back to sign in
        </Link>
      </div>
      <ResetPasswordForm token={token} />
    </AuthLayout>
  )
}
