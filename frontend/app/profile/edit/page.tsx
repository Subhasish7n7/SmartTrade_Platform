import { isAxiosError } from "axios"
import { redirect } from "next/navigation"

import { ProfileEditForm } from "@/components/profile/profile-edit-form"
import { getMyProfile } from "@/lib/api/profile/server"

export default async function ProfileEditPage() {
  let profile

  try {
    profile = await getMyProfile()
  } catch (error) {
    if (isAxiosError(error) && error.response?.status === 401) {
      redirect("/login?next=%2Fprofile%2Fedit")
    }

    throw error
  }

  return <ProfileEditForm initialProfile={profile} />
}