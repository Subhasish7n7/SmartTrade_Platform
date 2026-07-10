"use client"

import Link from "next/link"

import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from "@/components/ui/avatar"

import { Button } from "@/components/ui/button"
import { ChatInboxItem } from "@/lib/types/ui/chat"
import { ArrowLeft } from "lucide-react"

interface Props {
  conversation: ChatInboxItem
  showConversationBack?: boolean
  onConversationBack?: () => void
}

export function ChatHeader({conversation,showConversationBack,onConversationBack}: Props) {
  return (
    <div className="border-b border-border/50 p-5">
      <div className="flex items-center justify-between">
        <div className="flex gap-3 items-center">
          {
            showConversationBack && (
              <div className="flex items-center gap-2 lg:hidden">
                <button
                  onClick={onConversationBack}
                  className="
                    flex
                    items-center
                    gap-2
                    text-muted-foreground
                    hover:text-foreground
                  "
                >
                  <ArrowLeft className="h-4 w-4" />
                  Conversations
                </button>
              </div>
            )
          }
          <Avatar className="h-12 w-12">
            <AvatarImage
              src={
                conversation.avatarUrl
              }
            />

            <AvatarFallback>
              {conversation.otherUserName.slice(0,2 )}
            </AvatarFallback>
          </Avatar>
          <div className="mt-3">
            <h2 className="font-semibold">
              {conversation.otherUserName}
            </h2>
            <div className="flex items-center gap-2">
              <div className="h-2 w-2 rounded-full bg-green-500"/>
              <span className="text-xs text-muted-foreground">
                Online
              </span>
            </div>
            

            <p className="text-sm text-muted-foreground">
              {
                conversation.activeTradeCount
              }{" "}
              Active Trades
            </p>
            
          </div>
        </div>

        <Link
          href={`/trades?user=${conversation.otherUserId}`}
        >
          <Button
            variant="secondary"
          >
            View Trades
          </Button>
        </Link>
      </div>
    </div>
  )
}