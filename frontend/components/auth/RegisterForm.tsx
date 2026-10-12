"use client"

import Link from "next/link"
import { useRouter } from "next/navigation"
import { Loader2 } from "lucide-react"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { toast } from "sonner"

import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { PasswordInput } from "./PasswordInput"
import { register } from "@/lib/api/auth/auth.client"
import { registerSchema, type RegisterSchema } from "@/lib/validation/auth"
import { getApiErrorMessage } from "@/lib/api/errors"
import { useAuth } from "@/lib/providers/auth-provider"

export function RegisterForm() {
  const router = useRouter()
  const { refreshUser } = useAuth()

  const form = useForm<RegisterSchema>({
    resolver: zodResolver(registerSchema),
    defaultValues: { name: "", email: "", phone_no: "", password: "", confirmPassword: "" },
  })

  async function onSubmit(values: RegisterSchema) {
    form.clearErrors("root.serverError")
    try {
      await register({
        name: values.name.trim(),
        email: values.email.trim(),
        password: values.password,
        phone_no: values.phone_no?.trim() || undefined,
      })
      await refreshUser()
      toast.success("Account created successfully")
      router.replace("/")
      router.refresh()
    } catch (error) {
      const message = getApiErrorMessage(error, "We couldn't create your account. Please try again.")
      form.setError("root.serverError", { type: "server", message })
      toast.error(message)
    }
  }

  const serverError = form.formState.errors.root?.serverError?.message

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-5" noValidate>
        <FormField control={form.control} name="name" render={({ field }) => (
          <FormItem>
            <FormLabel>Full name</FormLabel>
            <FormControl><Input autoComplete="name" placeholder="Your name" maxLength={60} {...field} /></FormControl>
            <FormMessage />
          </FormItem>
        )} />

        <FormField control={form.control} name="email" render={({ field }) => (
          <FormItem>
            <FormLabel>Email</FormLabel>
            <FormControl><Input type="email" autoComplete="email" placeholder="you@example.com" {...field} /></FormControl>
            <FormMessage />
          </FormItem>
        )} />

        <FormField control={form.control} name="phone_no" render={({ field }) => (
          <FormItem>
            <FormLabel>Phone <span className="font-normal text-muted-foreground">(optional)</span></FormLabel>
            <FormControl><Input type="tel" autoComplete="tel" placeholder="Phone number" maxLength={30} {...field} /></FormControl>
            <FormMessage />
          </FormItem>
        )} />

        <FormField control={form.control} name="password" render={({ field }) => (
          <FormItem>
            <FormLabel>Password</FormLabel>
            <FormControl><PasswordInput autoComplete="new-password" placeholder="At least 8 characters" {...field} /></FormControl>
            <FormMessage />
          </FormItem>
        )} />

        <FormField control={form.control} name="confirmPassword" render={({ field }) => (
          <FormItem>
            <FormLabel>Confirm password</FormLabel>
            <FormControl><PasswordInput autoComplete="new-password" placeholder="Enter the same password again" {...field} /></FormControl>
            <FormMessage />
          </FormItem>
        )} />

        {serverError && <p role="alert" className="rounded-xl border border-destructive/25 bg-destructive/5 px-3 py-2.5 text-sm text-destructive">{serverError}</p>}

        <Button type="submit" className="w-full cta-primary" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? <><Loader2 className="mr-2 h-4 w-4 animate-spin" />Creating account…</> : "Create Account"}
        </Button>

        <p className="text-center text-sm text-muted-foreground">
          Already have an account?{" "}<Link href="/login" className="text-primary underline-offset-4 hover:underline">Sign In</Link>
        </p>
      </form>
    </Form>
  )
}
