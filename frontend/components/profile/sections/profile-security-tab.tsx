"use client"

import { useState } from "react"
import type { ReactNode } from "react"
import { z } from "zod"
import { zodResolver } from "@hookform/resolvers/zod"
import { useForm } from "react-hook-form"
import { CheckCircle2, KeyRound, Loader2, ShieldCheck } from "lucide-react"
import { toast } from "sonner"

import { Button } from "@/components/ui/button"
import { PasswordInput } from "@/components/auth/PasswordInput"
import { changePassword } from "@/lib/api/auth/auth.client"
import { getApiErrorMessage } from "@/lib/api/errors"

const schema = z.object({
  currentPassword: z.string().min(1, "Enter your current password"),
  newPassword: z.string().min(8, "Password must be at least 8 characters").max(128, "Password must be 128 characters or fewer"),
  confirmPassword: z.string().min(1, "Confirm your new password"),
}).refine((values) => values.newPassword === values.confirmPassword, { path: ["confirmPassword"], message: "Passwords do not match" }).refine((values) => values.currentPassword !== values.newPassword, { path: ["newPassword"], message: "Choose a password different from your current one" })

type Fields = z.infer<typeof schema>

export function ProfileSecurityTab() {
  const [success, setSuccess] = useState(false)
  const form = useForm<Fields>({
    resolver: zodResolver(schema),
    defaultValues: { currentPassword: "", newPassword: "", confirmPassword: "" },
  })

  async function onSubmit(values: Fields) {
    setSuccess(false)
    form.clearErrors("root.serverError")
    try {
      await changePassword(values.currentPassword, values.newPassword)
      form.reset()
      setSuccess(true)
      toast.success("Password changed successfully")
    } catch (error) {
      const message = getApiErrorMessage(error, "We couldn't change your password. Please try again.")
      form.setError("root.serverError", { type: "server", message })
      toast.error(message)
    }
  }

  return (
    <div className="space-y-5">
      <section className="rounded-2xl border border-border/60 bg-background/20 p-4 sm:p-5">
        <div className="mb-4 flex items-center gap-3"><span className="flex h-10 w-10 items-center justify-center rounded-xl border border-primary/15 bg-primary/5 text-primary"><KeyRound className="h-5 w-5" /></span><div><h3 className="font-semibold">Change password</h3><p className="mt-1 text-xs text-muted-foreground">Use at least 8 characters. Your current session stays signed in.</p></div></div>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4" noValidate>
          <Field label="Current password" error={form.formState.errors.currentPassword?.message}><PasswordInput autoComplete="current-password" {...form.register("currentPassword")} /></Field>
          <Field label="New password" error={form.formState.errors.newPassword?.message}><PasswordInput autoComplete="new-password" {...form.register("newPassword")} /></Field>
          <Field label="Confirm new password" error={form.formState.errors.confirmPassword?.message}><PasswordInput autoComplete="new-password" {...form.register("confirmPassword")} /></Field>
          {form.formState.errors.root?.serverError?.message && <p role="alert" className="rounded-xl border border-destructive/25 bg-destructive/5 p-3 text-sm text-destructive">{form.formState.errors.root.serverError.message}</p>}
          {success && <p role="status" className="flex items-center gap-2 text-sm text-emerald-300"><CheckCircle2 className="h-4 w-4" />Password updated.</p>}
          <Button type="submit" className="cta-primary" disabled={form.formState.isSubmitting}>{form.formState.isSubmitting ? <><Loader2 className="mr-2 h-4 w-4 animate-spin" />Updating…</> : "Update password"}</Button>
        </form>
      </section>

      <section className="rounded-2xl border border-border/60 bg-background/20 p-4 sm:p-5">
        <div className="flex items-start gap-3"><span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl border border-border/70 bg-background text-muted-foreground"><ShieldCheck className="h-5 w-5" /></span><div className="min-w-0"><h3 className="font-semibold">Additional account protection</h3><p className="mt-1 text-sm leading-5 text-muted-foreground">These controls are disabled until the backend supports them. They do not simulate a security change.</p><div className="mt-4 space-y-3"><div className="flex items-center justify-between gap-3"><div><p className="text-sm font-medium">Two-factor authentication</p><p className="text-xs text-muted-foreground">Not configured</p></div><Button variant="outline" size="sm" disabled>Unavailable</Button></div><div className="flex items-center justify-between gap-3"><div><p className="text-sm font-medium">Sign out other sessions</p><p className="text-xs text-muted-foreground">Session management endpoint not available.</p></div><Button variant="outline" size="sm" disabled>Unavailable</Button></div></div></div></div>
      </section>
    </div>
  )
}

function Field({ label, error, children }: { label: string; error?: string; children: ReactNode }) {
  return <div className="space-y-1.5"><label className="text-sm font-medium">{label}</label>{children}{error && <p role="alert" className="text-xs text-destructive">{error}</p>}</div>
}
