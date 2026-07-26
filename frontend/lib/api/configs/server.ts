import axios from "axios";
import { cookies } from "next/headers";

import { API_BASE_URL } from "@/lib/config";

export async function serverApi() {
    const cookieStore = await cookies();

    return axios.create({
        baseURL: API_BASE_URL,
        headers: {
            Cookie: cookieStore.toString(),
        },
    });
}