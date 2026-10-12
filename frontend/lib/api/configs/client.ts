import axios, { type InternalAxiosRequestConfig } from "axios"

import { API_BASE_URL } from "@/lib/config"

type CsrfResponse = { token: string }

const clientApi = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  xsrfCookieName: "XSRF-TOKEN",
  xsrfHeaderName: "X-XSRF-TOKEN",
  // Axios can read the XSRF cookie only when the browser exposes it to the
  // frontend origin. We additionally use the raw token returned by /auth/csrf
  // so cross-origin API deployments do not depend on document.cookie access.
  withXSRFToken: true,
})

const UNSAFE_METHODS = new Set(["post", "put", "patch", "delete"])

let cachedCsrfToken: string | null = null
let csrfRequest: Promise<string> | null = null

async function getCsrfToken(): Promise<string> {
  if (cachedCsrfToken) return cachedCsrfToken

  if (!csrfRequest) {
    csrfRequest = clientApi
      .get<CsrfResponse>("/auth/csrf")
      .then(({ data }) => {
        if (!data?.token || typeof data.token !== "string") {
          throw new Error("The server did not provide a CSRF token.")
        }
        cachedCsrfToken = data.token
        return data.token
      })
      .finally(() => {
        csrfRequest = null
      })
  }

  return csrfRequest
}

clientApi.interceptors.request.use(async (config: InternalAxiosRequestConfig) => {
  const method = (config.method ?? "get").toLowerCase()
  if (!UNSAFE_METHODS.has(method)) return config

  // This client is for browser-side requests. Server components should use
  // serverApi(), which forwards the incoming request cookies separately.
  if (typeof window === "undefined") return config

  const token = await getCsrfToken()
  config.headers.set("X-XSRF-TOKEN", token)
  return config
})

clientApi.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    // A later request will bootstrap a fresh token rather than reusing a stale
    // cached token after a CSRF-related failure. We do not retry unsafe calls
    // automatically because a 403 may also mean access is forbidden.
    if (axios.isAxiosError(error) && error.response?.status === 403) {
      cachedCsrfToken = null
    }
    return Promise.reject(error)
  },
)

export { clientApi }
