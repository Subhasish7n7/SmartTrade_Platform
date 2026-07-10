"use client"

import Link from "next/link"

import { useRouter } from "next/navigation"

import { Loader2 } from "lucide-react"

import { useForm } from "react-hook-form"

import { zodResolver } from "@hookform/resolvers/zod"

import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form"

import { Input } from "@/components/ui/input"

import { Button } from "@/components/ui/button"

import { PasswordInput } from "./PasswordInput"

import { register } from "@/lib/api/auth/auth.client"
import { registerSchema, RegisterSchema } from "@/lib/validation/auth"
import { toast } from "sonner"

export function RegisterForm() {
  const router = useRouter()

  const form = useForm<RegisterSchema>({
    resolver: zodResolver(registerSchema),

    defaultValues: {
      name: "",

      email: "",

      phone_no: "",

      password: "",

      confirmPassword: "",
    },
  })

  async function onSubmit(values: RegisterSchema) {
    try {
      await register({
        name: values.name,
        email: values.email,
        password: values.password,
        phone_no: values.phone_no,
      })

      toast.success("Account created successfully")

      router.replace("/")

      router.refresh()
    } catch {
      toast.error("Unable to register")
    }
  }

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-5">
        {(["name", "email", "phone_no"] as const).map((fieldName) => (
          <FormField
            key={fieldName}
            control={form.control}
            name={fieldName}
            render={({ field }) => (
              <FormItem>
                <FormLabel>{fieldName === "phone_no" ? "Phone" : fieldName.charAt(0).toUpperCase() + fieldName.slice(1)}</FormLabel>

                <FormControl>
                  <Input {...field} />
                </FormControl>

                <FormMessage />
              </FormItem>
            )}
          />
        ))}

        <FormField
          control={form.control}
          name="password"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Password</FormLabel>
              <FormControl>
                <PasswordInput {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="confirmPassword"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Confirm Password</FormLabel>
              <FormControl>
                <PasswordInput {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <Button type="submit" className="w-full cta-primary" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? (
            <>
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              Creating...
            </>
          ) : (
            "Create Account"
          )}
        </Button>

        <p className="text-center text-sm text-muted-foreground">
          Already have an account?{" "}
          <Link href="/login" className="text-primary">
            Sign In
          </Link>
        </p>
      </form>
    </Form>
  )
}
