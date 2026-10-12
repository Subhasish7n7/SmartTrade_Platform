"use client"

import Link from "next/link"
import { useState } from "react"
import { Loader2, MailCheck } from "lucide-react"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { toast } from "sonner"

import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { requestPasswordReset } from "@/lib/api/auth/auth.client"
import { forgotPasswordSchema } from "@/lib/validation/auth"
import { getApiErrorMessage } from "@/lib/api/errors"

export function ForgotPasswordForm() {
  const [submitted, setSubmitted] = useState(false)
  const form = useForm<{ email: string }>({
    resolver: zodResolver(forgotPasswordSchema),
    defaultValues: { email: "" },
  })

  async function onSubmit(values: { email: string }) {
    try {
      await requestPasswordReset(values.email.trim())
      setSubmitted(true)
      toast.success("If an account exists for that email, a reset link will be sent.")
    } catch (error) {
      form.setError("root.serverError", { type: "server", message: getApiErrorMessage(error, "We couldn't submit the request. Please try again.") })
    }
  }

  if (submitted) {
    return (
      <div className="space-y-5 text-center">
        <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl border border-primary/20 bg-primary/10 text-primary"><MailCheck className="h-7 w-7" /></div>
        <p className="text-sm leading-6 text-muted-foreground">If an account exists for that email, a reset link will be sent. Check your inbox and spam folder.</p>
        <Button asChild className="w-full cta-primary"><Link href="/login">Back to sign in</Link></Button>
      </div>
    )
  }

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-5" noValidate>
        <p className="text-sm leading-6 text-muted-foreground">Enter the email associated with your account. If it exists, we&apos;ll send instructions to reset your password.</p>
        <FormField control={form.control} name="email" render={({ field }) => (
          <FormItem><FormLabel>Email</FormLabel><FormControl><Input type="email" autoComplete="email" placeholder="you@example.com" {...field} /></FormControl><FormMessage /></FormItem>
        )} />
        {form.formState.errors.root?.serverError?.message && <p role="alert" className="text-sm text-destructive">{form.formState.errors.root.serverError.message}</p>}
        <Button type="submit" className="w-full cta-primary" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? <><Loader2 className="mr-2 h-4 w-4 animate-spin" />Sending…</> : "Send reset link"}
        </Button>
        <p className="text-center text-sm text-muted-foreground"><Link href="/login" className="text-primary hover:underline">Back to sign in</Link></p>
      </form>
    </Form>
  )
}
