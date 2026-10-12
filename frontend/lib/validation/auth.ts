import { z } from "zod"

const passwordSchema = z
  .string()
  .min(8, "Password must be at least 8 characters")
  .max(128, "Password must be 128 characters or fewer")

export const loginSchema = z.object({
  email: z.string().trim().email("Enter a valid email address"),
  password: z.string().min(1, "Password is required").max(128, "Password must be 128 characters or fewer"),
  rememberMe: z.boolean().default(false),
})

export type LoginSchema = z.infer<typeof loginSchema>

export const registerSchema = z
  .object({
    name: z.string().trim().min(2, "Name must be at least 2 characters").max(60, "Name must be 60 characters or fewer"),
    email: z.string().trim().email("Enter a valid email address"),
    // Keep phone optional, but reject a provided value that is only whitespace.
    phone_no: z.string().trim().max(30, "Phone number is too long").optional(),
    password: passwordSchema,
    confirmPassword: z.string().min(1, "Confirm your password"),
  })
  .refine((data) => data.password === data.confirmPassword, {
    path: ["confirmPassword"],
    message: "Passwords do not match",
  })
  .refine((data) => !data.phone_no || data.phone_no.trim().length > 0, {
    path: ["phone_no"],
    message: "Enter a valid phone number or leave this field blank",
  })

export type RegisterSchema = z.infer<typeof registerSchema>

export const newPasswordSchema = z
  .string()
  .min(8, "Password must be at least 8 characters")
  .max(128, "Password must be 128 characters or fewer")

export const forgotPasswordSchema = z.object({
  email: z.string().trim().email("Enter a valid email address"),
})

export const resetPasswordSchema = z
  .object({
    newPassword: newPasswordSchema,
    confirmPassword: z.string().min(1, "Confirm your password"),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    path: ["confirmPassword"],
    message: "Passwords do not match",
  })
