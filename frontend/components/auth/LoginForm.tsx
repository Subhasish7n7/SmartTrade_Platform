"use client"

import Link from "next/link"
import { useRouter } from "next/navigation"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { Loader2 } from "lucide-react"
import { toast } from "sonner"

import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Checkbox } from "@/components/ui/checkbox"
import { PasswordInput } from "./PasswordInput"
import { loginSchema, type LoginSchema } from "@/lib/validation/auth"
import { login } from "@/lib/api/auth/auth.client"
import { getApiErrorMessage } from "@/lib/api/errors"
import { useAuth } from "@/lib/providers/auth-provider"

interface Props {
  redirect: string
}

function safeLocalRedirect(value: string): string {
  // Prevent an external URL or protocol-relative URL from becoming the target.
  if (!value.startsWith("/") || value.startsWith("//") || value.includes("\\")) return "/"
  return value
}

export function LoginForm({ redirect }: Props) {
  const router = useRouter()
  const { refreshUser } = useAuth()

  const form = useForm<LoginSchema>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", password: "", rememberMe: false },
  })

  async function onSubmit(values: LoginSchema) {
    form.clearErrors("root.serverError")
    try {
      await login(values)
      await refreshUser()
      toast.success("Welcome back!")
      router.replace(safeLocalRedirect(redirect))
      router.refresh()
    } catch (error) {
      const message = getApiErrorMessage(error, "We couldn't sign you in. Please try again.")
      form.setError("root.serverError", { type: "server", message })
      toast.error(message)
    }
  }

  const serverError = form.formState.errors.root?.serverError?.message

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-5" noValidate>
        <FormField
          control={form.control}
          name="email"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Email</FormLabel>
              <FormControl>
                <Input type="email" autoComplete="email" placeholder="john@example.com" {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="password"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Password</FormLabel>
              <FormControl>
                <PasswordInput autoComplete="current-password" placeholder="Enter your password" {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <div className="flex items-center justify-between gap-4">
          <FormField
            control={form.control}
            name="rememberMe"
            render={({ field }) => (
              <FormItem className="flex flex-row items-center gap-2 space-y-0">
                <FormControl>
                  <Checkbox checked={field.value} onCheckedChange={(checked) => field.onChange(checked === true)} />
                </FormControl>
                <FormLabel className="cursor-pointer text-sm font-normal">Remember me</FormLabel>
              </FormItem>
            )}
          />
          <Link href="/forgot-password" className="text-sm text-primary underline-offset-4 hover:underline">
            Forgot password?
          </Link>
        </div>

        {serverError && (
          <p role="alert" className="rounded-xl border border-destructive/25 bg-destructive/5 px-3 py-2.5 text-sm text-destructive">
            {serverError}
          </p>
        )}

        <Button type="submit" className="w-full cta-primary" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? <><Loader2 className="mr-2 h-4 w-4 animate-spin" />Signing in…</> : "Sign In"}
        </Button>

        <p className="text-center text-sm text-muted-foreground">
          Don&apos;t have an account?{" "}
          <Link href="/register" className="text-primary underline-offset-4 hover:underline">Create one</Link>
        </p>
      </form>
    </Form>
  )
}
