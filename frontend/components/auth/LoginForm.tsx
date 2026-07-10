"use client"

import Link from "next/link"
import { useRouter, useSearchParams } from "next/navigation"

import { useForm } from "react-hook-form"

import { zodResolver } from "@hookform/resolvers/zod"

import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form"

import { Input } from "@/components/ui/input"

import { Button } from "@/components/ui/button"

import { Checkbox } from "@/components/ui/checkbox"

import { Loader2 } from "lucide-react"

import { PasswordInput } from "./PasswordInput"

import { loginSchema, LoginSchema } from "@/lib/validation/auth"

import { login } from "@/lib/api/auth/auth.client"
import { toast } from "sonner"

export function LoginForm() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const redirect = searchParams.get("redirect") ?? "/"

  const form = useForm<LoginSchema>({
    resolver: zodResolver(loginSchema),

    defaultValues: {
      email: "",
      password: "",
    },
  })

  async function onSubmit(values: LoginSchema) {
    try {
      await login(values)

      toast.success("Welcome back!")

      router.replace(redirect)

      router.refresh()
    } catch {
      toast.error("Invalid email or password")
    }
  }

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
        <FormField
          control={form.control}
          name="email"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Email</FormLabel>

              <FormControl>
                <Input placeholder="john@example.com" {...field} />
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
                <PasswordInput placeholder="••••••••" {...field} />
              </FormControl>

              <FormMessage />
            </FormItem>
          )}
        />

        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Checkbox />

            <label className="text-sm">Remember me</label>
          </div>

          <Link href="#" className="text-sm text-primary">
            Forgot password?
          </Link>
        </div>

        <Button type="submit" className="w-full cta-primary" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? (
            <>
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              Signing In...
            </>
          ) : (
            "Sign In"
          )}
        </Button>

        <p className="text-center text-sm text-muted-foreground">
          Don't have an account?{" "}
          <Link href="/register" className="text-primary">
            Create one
          </Link>
        </p>
      </form>
    </Form>
  )
}
