import axios from "axios"

type ErrorBody = {
  message?: unknown
  error?: unknown
  detail?: unknown
  errors?: unknown
}

function firstUsefulMessage(value: unknown): string | null {
  if (typeof value === "string" && value.trim()) return value.trim()
  if (Array.isArray(value)) {
    for (const entry of value) {
      const candidate = firstUsefulMessage(entry)
      if (candidate) return candidate
    }
  }
  if (value && typeof value === "object") {
    for (const entry of Object.values(value as Record<string, unknown>)) {
      const candidate = firstUsefulMessage(entry)
      if (candidate) return candidate
    }
  }
  return null
}

function isSafeServerMessage(message: string): boolean {
  // Never echo stack traces, SQL/database text, or framework exception names.
  return !/(exception|stack trace|sqlstate|constraint| at [\w.$]+\(|org\.springframework|java\.[\w.]+|select\s+.+\s+from\s+)/i.test(message)
}

export function getApiErrorMessage(
  error: unknown,
  fallback = "Something went wrong. Please try again.",
): string {
  if (!axios.isAxiosError<ErrorBody>(error)) {
    return error instanceof Error && error.message ? error.message : fallback
  }

  const status = error.response?.status
  const body = error.response?.data

  if (status === 401) return "Your email or password is incorrect."
  if (status === 403) return "Your session or security token has expired. Please try again."
  if (status === 404) return "The requested resource could not be found."
  if (status === 409) return "An account with these details already exists."
  if (status === 429) return "Too many attempts. Please wait a moment and try again."
  if (status && status >= 500) return "The server could not complete the request. Please try again later."
  if (!error.response) return "Could not reach SmartTrade. Check your internet connection and try again."

  if (body && typeof body === "object") {
    const message = firstUsefulMessage(body.message) ?? firstUsefulMessage(body.detail) ?? firstUsefulMessage(body.errors)
    if (message && isSafeServerMessage(message)) return message
  }

  if (status === 400 || status === 422) return "Please check the information you entered and try again."
  return fallback
}
