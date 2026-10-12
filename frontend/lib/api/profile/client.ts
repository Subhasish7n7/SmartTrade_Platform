import { clientApi } from "@/lib/api/configs/client"
import type { CloudinaryUploadResult } from "@/lib/api/cloudinary"
import type { MyUserProfile } from "@/lib/types/api/profile"

export interface UpdateMyProfileRequest {
  name: string
  phoneNo?: string
  avatar?: Pick<
    CloudinaryUploadResult,
    "secureUrl" | "publicId" | "assetId" | "width" | "height" | "format" | "bytes"
  >
  clearAvatar?: boolean
}

export async function updateMyProfile(
  payload: UpdateMyProfileRequest,
): Promise<MyUserProfile> {
  const { data } = await clientApi.patch<MyUserProfile>("/users/me/profile", payload)
  return data
}