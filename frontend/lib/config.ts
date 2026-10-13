export const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL!;

/** Browser REST requests use the same-origin Next.js external rewrite. */
export const CLIENT_API_BASE_URL = "/api/backend";

export const IMAGE_FALLBACK = "/placeholder.jpg";