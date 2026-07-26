"use client"

import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"

import { Button } from "@/components/ui/button"

import { MessageCircle, Pencil } from "lucide-react"

import { UserProfile } from "@/app/profile/page"

interface Props {
  profile: UserProfile
}

export function ProfileHeader({ profile }: Props) {
  const initials = profile.name
    .split(" ")
    .map((x) => x[0])
    .join("")
    .slice(0, 2)

  return (
    <div className="content-surface">
      <div className="flex flex-col gap-3 p-2 lg:flex-row lg:items-center lg:justify-between">
        <div className="flex gap-3">
          <Avatar className="h-15 w-15">
            <AvatarImage src={profile.avatar} />

            <AvatarFallback className="text-2xl font-bold">{initials}</AvatarFallback>
          </Avatar>

          <div>
            <h1 className="text-2xl font-bold">{profile.name}</h1>
            <div className="mt-1 flex flex-wrap gap-5 text-sm text-muted-foreground">
              <span>{profile.email}</span>

              <span>{profile.phone}</span>

              <span>Joined {profile.joinedAt}</span>
            </div>
          </div>
        </div>

        <div className="flex gap-3">
          <Button>
            <MessageCircle className="mr-2 h-4 w-4" />
            Messages
          </Button>

          <Button variant="surface">
            <Pencil className="mr-2 h-4 w-4" />
            Edit
          </Button>
        </div>
      </div>
    </div>
  )
}
