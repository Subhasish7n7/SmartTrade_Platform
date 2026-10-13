import axios, { type InternalAxiosRequestConfig } from "axios";

import { CLIENT_API_BASE_URL } from "@/lib/config";

type CsrfResponse = { token: string };

const clientApi = axios.create({
  baseURL: CLIENT_API_BASE_URL,
  withCredentials: true,
  xsrfCookieName: "XSRF-TOKEN",
  xsrfHeaderName: "X-XSRF-TOKEN",
  withXSRFToken: true,
});

const UNSAFE_METHODS = new Set(["post", "put", "patch", "delete"]);

let cachedCsrfToken: string | null = null;
let csrfRequest: Promise<string> | null = null;

async function getCsrfToken(): Promise<string> {
  if (cachedCsrfToken) return cachedCsrfToken;

  if (!csrfRequest) {
    csrfRequest = clientApi
      .get<CsrfResponse>("/auth/csrf")
      .then(({ data }) => {
        if (!data?.token || typeof data.token !== "string") {
          throw new Error("The server did not provide a CSRF token.");
        }

        cachedCsrfToken = data.token;
        return data.token;
      })
      .finally(() => {
        csrfRequest = null;
      });
  }

  return csrfRequest;
}

clientApi.interceptors.request.use(async (config: InternalAxiosRequestConfig) => {
  const method = (config.method ?? "get").toLowerCase();
  if (!UNSAFE_METHODS.has(method)) return config;

  // This instance is for browser requests. Server-side code uses serverApi().
  if (typeof window === "undefined") return config;

  const token = await getCsrfToken();
  config.headers.set("X-XSRF-TOKEN", token);
  return config;
});

clientApi.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    // Do not automatically retry writes: a 403 can be authorization-related too.
    if (axios.isAxiosError(error) && error.response?.status === 403) {
      cachedCsrfToken = null;
    }
    return Promise.reject(error);
  },
);

export { clientApi };