import axios from "axios";
import { cookies } from "next/headers";

import { API_BASE_URL } from "@/lib/config";

export async function serverApi() {
  const cookieStore = await cookies();
  const csrfToken = cookieStore.get("XSRF-TOKEN")?.value;

  const headers: Record<string, string> = {
    Cookie: cookieStore.toString(),
  };

  if (csrfToken) {
    headers["X-XSRF-TOKEN"] = csrfToken;
  }

  return axios.create({
    baseURL: API_BASE_URL,
    headers,
  });
}