"use client"

import Link from "next/link"
import { useState } from "react"
import { useRouter } from "next/navigation"
import { Loader2, CheckCircle2 } from "lucide-react"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { toast } from "sonner"

import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form"
import { Button } from "@/components/ui/button"
import { PasswordInput } from "@/components/auth/PasswordInput"
import { resetPassword } from "@/lib/api/auth/auth.client"
import { resetPasswordSchema } from "@/lib/validation/auth"
import { getApiErrorMessage } from "@/lib/api/errors"

type ResetFields = { newPassword: string; confirmPassword: string }

export function ResetPasswordForm({ token }: { token: string }) {
  const router = useRouter()
  const [complete, setComplete] = useState(false)
  const form = useForm<ResetFields>({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: { newPassword: "", confirmPassword: "" },
  })

  async function onSubmit(values: ResetFields) {
    form.clearErrors("root.serverError")
    if (!token.trim()) {
      form.setError("root.serverError", { type: "validate", message: "This reset link is missing its token. Request a new reset link." })
      return
    }

    try {
      await resetPassword(token, values.newPassword)
      setComplete(true)
      toast.success("Password reset successfully")
    } catch (error) {
      form.setError("root.serverError", { type: "server", message: getApiErrorMessage(error, "This reset link may have expired. Request a new link and try again.") })
    }
  }

  if (complete) {
    return (
      <div className="space-y-5 text-center">
        <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl border border-emerald-500/20 bg-emerald-500/10 text-emerald-400"><CheckCircle2 className="h-7 w-7" /></div>
        <p className="text-sm leading-6 text-muted-foreground">Your password has been changed. Sign in with your new password.</p>
        <Button className="w-full cta-primary" onClick={() => router.replace("/login")}>Go to sign in</Button>
      </div>
    )
  }

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-5" noValidate>
        <FormField control={form.control} name="newPassword" render={({ field }) => (
          <FormItem><FormLabel>New password</FormLabel><FormControl><PasswordInput autoComplete="new-password" placeholder="At least 8 characters" {...field} /></FormControl><FormMessage /></FormItem>
        )} />
        <FormField control={form.control} name="confirmPassword" render={({ field }) => (
          <FormItem><FormLabel>Confirm new password</FormLabel><FormControl><PasswordInput autoComplete="new-password" placeholder="Enter it again" {...field} /></FormControl><FormMessage /></FormItem>
        )} />
        {form.formState.errors.root?.serverError?.message && (
          <div role="alert" className="space-y-2 rounded-xl border border-destructive/25 bg-destructive/5 p-3 text-sm text-destructive">
            <p>{form.formState.errors.root.serverError.message}</p>
            <Link href="/forgot-password" className="underline underline-offset-4">Request a new reset link</Link>
          </div>
        )}
        <Button type="submit" className="w-full cta-primary" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? <><Loader2 className="mr-2 h-4 w-4 animate-spin" />Resetting password…</> : "Reset password"}
        </Button>
      </form>
    </Form>
  )
}
