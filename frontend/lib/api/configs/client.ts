import axios from "axios"

import { API_BASE_URL } from "@/lib/config"

const clientApi = axios.create({
  baseURL: API_BASE_URL,

  // Send the HttpOnly access_token cookie.
  withCredentials: true,

  // Spring's CookieCsrfTokenRepository defaults.
  xsrfCookieName: "XSRF-TOKEN",
  xsrfHeaderName: "X-XSRF-TOKEN",

  // Important because Vercel and Render are different origins.
  // Axios otherwise only enables automatic XSRF headers for same-origin requests.
  withXSRFToken: true,
})

export { clientApi }