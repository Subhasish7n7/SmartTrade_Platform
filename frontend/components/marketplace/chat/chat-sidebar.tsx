"use client"

import * as React from "react"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Search } from "lucide-react"
import { Input } from "@/components/ui/input"
import { ChatInboxItem } from "@/lib/types/ui/chat"

interface Props {
  conversations: ChatInboxItem[]

  selectedUserId?: number

  onSelect: (
    conversation: ChatInboxItem
  ) => void
}

export function ChatSidebar({conversations,selectedUserId,onSelect,}: Props) {
  const [search, setSearch] = React.useState("")
  const filtered = conversations.filter((conversation) =>
      conversation.otherUserName
        .toLowerCase()
        .includes(search.toLowerCase())
  )
  return (
    <div className="h-full overflow-y-auto">
      <div className="space-y-2">
        <div className="mb-3">
          <Input
            placeholder="Search conversations..."
            value={search}
            onChange={(e) =>setSearch(e.target.value)}
          />
        </div>
        {filtered.map(
          (conversation) => (
            <button
              key={conversation.otherUserId}
              onClick={() =>onSelect(conversation)}
              className={`
                w-full
                text-left
                rounded-2xl
                p-4
                transition-all
                ${
                  selectedUserId ===
                  conversation.otherUserId
                    ? "trade-card border border-[var(--trust-blue)]/30"
                    : "glass hover:border-border"
                }
              `}
            >
              <div className="flex gap-3">
                <Avatar>
                  <AvatarImage
                    src={
                      conversation.avatarUrl
                    }
                  />
                  <AvatarFallback>
                    {conversation.otherUserName.slice(
                      0,
                      2
                    )}
                  </AvatarFallback>
                </Avatar>

                <div className="min-w-0 flex-1">
                  <div className="flex justify-between">
                    <p className="font-medium truncate">
                      {
                        conversation.otherUserName
                      }
                    </p>

                    <span className="text-xs text-muted-foreground">
                      {
                        conversation.lastMessageAt
                      }
                    </span>
                  </div>

                  <p className="text-sm text-muted-foreground truncate mt-1">
                    {
                      conversation.lastMessage
                    }
                  </p>

                  <div className="flex items-center justify-between mt-2">
                    <span className="text-xs text-[var(--trust-blue)]">
                      {
                        conversation.activeTradeCount
                      }{" "}
                      Active Trades
                    </span>

                    {conversation.unreadCount >
                      0 && (
                      <div
                        className="
                          h-5
                          min-w-5
                          rounded-full
                          px-1
                          text-xs
                          flex
                          items-center
                          justify-center
                          bg-[var(--trust-blue)]
                          text-white
                        "
                      >
                        {
                          conversation.unreadCount
                        }
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </button>
          )
        )}
      </div>
    </div>
  )
}