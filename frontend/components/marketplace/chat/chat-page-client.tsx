"use client"

import * as React from "react"

import { getConversation, getConversations, sendChatMessage } from "@/lib/api/chats/chat"

import { ChatInboxItem, ChatMessage } from "@/lib/types/ui/chat"

import { BackButton } from "../back-button"
import { ChatComposer } from "./chat-composer"
import { ChatHeader } from "./chat-header"
import { ChatSidebar } from "./chat-sidebar"
import { MessageBubble } from "./message-bubble"
import { PinnedTradeCard } from "./pinned-trade-card"

const mockConversations: ChatInboxItem[] = [
  {
    otherUserId: 2,
    otherUserName: "Alex Morgan",
    lastMessage: "Would you consider removing the cash adjustment?",
    lastMessageAt: "2m ago",
    unreadCount: 2,
    activeTradeCount: 3,
    pinnedTradeId: 154,
  },
  {
    otherUserId: 3,
    otherUserName: "Sarah Chen",
    lastMessage: "I'll review the offer tonight.",
    lastMessageAt: "1h ago",
    unreadCount: 0,
    activeTradeCount: 1,
    pinnedTradeId: 162,
  },
]

const mockMessages: ChatMessage[] = [
  {
    id: 1,
    senderId: 2,
    senderName: "Alex",
    message: "Hey, thanks for the offer.",
    timestamp: "2:10 PM",
  },
  {
    id: 2,
    senderId: 1,
    senderName: "You",
    message: "Would you consider removing the cash adjustment?",
    timestamp: "2:12 PM",
  },
  {
    id: 3,
    senderId: 2,
    senderName: "Alex",
    message: "Possibly. Could you add another item?",
    timestamp: "2:15 PM",
  },
]

interface Props {
  currentUserId: number
}

export function ChatPageClient({ currentUserId }: Props) {
  const [conversations, setConversations] = React.useState<ChatInboxItem[]>([])
  const [mobileConversationOpen, setMobileConversationOpen] = React.useState(false)
  const [selectedConversation, setSelectedConversation] = React.useState<ChatInboxItem | null>(null)
  const [messages, setMessages] = React.useState<ChatMessage[]>([])
  const [loading, setLoading] = React.useState(true)

  const bottomRef = React.useRef<HTMLDivElement>(null)

  React.useEffect(() => {
    const loadInbox = async () => {
      try {
        const data = await getConversations()
        setConversations(data)
        if (data.length > 0) {
          setSelectedConversation(data[0])
        }
      } catch {
        setConversations(mockConversations)
        setSelectedConversation(mockConversations[0])
      }
    }
    loadInbox()
  }, [])

  React.useEffect(() => {
    if (!selectedConversation) {
      return
    }
    const loadMessages = async () => {
      try {
        const data = await getConversation(selectedConversation.otherUserId)
        setMessages(data)
      } catch {
        setMessages(mockMessages)
      } finally {
        setLoading(false)
      }
    }
    loadMessages()
  }, [selectedConversation])

  React.useEffect(() => {
    bottomRef.current?.scrollIntoView({
      behavior: "smooth",
    })
  }, [messages])

  const handleSend = async (content: string) => {
    if (!selectedConversation) {
      return
    }

    const optimisticMessage: ChatMessage = {
      id: Date.now(),
      senderId: currentUserId,
      senderName: "You",
      message: content,
      timestamp: "Now",
    }

    setMessages((prev) => [...prev, optimisticMessage])

    try {
      await sendChatMessage(selectedConversation.otherUserId, content)
    } catch {
      /*
       * fallback mode
       */
    }
  }

  return (
    <div
      className="
        h-full
        overflow-hidden
        trade-card
      "
    >
      <div className="grid lg:grid-cols-[340px_1fr] h-full">
        {/* SIDEBAR */}

        <div
          className={`
            ${mobileConversationOpen ? "hidden lg:flex" : "flex"}

            flex-col
            bg-card/30
            backdrop-blur-xl
            border-r
            border-border/50
          `}
        >
          <div className="p-5 border-b border-border/50 space-y-2">
            

            <div>
              <h2 className="font-bold text-xl">Messages</h2>

              <p className="text-sm text-muted-foreground mt-1">Conversations and trade discussions</p>
            </div>
          </div>

          <div className="flex-1 overflow-hidden p-3">
            <ChatSidebar
              conversations={conversations}
              selectedUserId={selectedConversation?.otherUserId}
              onSelect={(conversation) => {
                setSelectedConversation(conversation)

                setMobileConversationOpen(true)
              }}
            />
          </div>
        </div>

        {/* CHAT */}

        <div
          className={`
            ${mobileConversationOpen ? "flex" : "hidden lg:flex"}

            flex-col
            h-full
            overflow-hidden
          `}
        >
          {selectedConversation ? (
            <>
              <ChatHeader
                conversation={selectedConversation}
                showConversationBack
                onConversationBack={() => setMobileConversationOpen(false)}
              />

              {selectedConversation.pinnedTradeId && <PinnedTradeCard tradeId={selectedConversation.pinnedTradeId} />}

              <div
                className="
                  flex-1
                  overflow-y-auto
                  px-6
                  py-6
                  chat-message-surface
                "
              >
                {loading ? (
                  <div className="space-y-4">
                    {Array.from({
                      length: 6,
                    }).map((_, index) => (
                      <div
                        key={index}
                        className="
                            h-16
                            rounded-2xl
                            animate-pulse
                            bg-card
                          "
                      />
                    ))}
                  </div>
                ) : (
                  <div className="space-y-4">
                    {messages.map((message, index) => {
                      const previous = messages[index - 1]

                      const showAvatar = previous?.senderId !== message.senderId

                      return (
                        <MessageBubble
                          key={message.id}
                          message={message}
                          mine={message.senderId === currentUserId}
                          showAvatar={showAvatar}
                        />
                      )
                    })}

                    <div ref={bottomRef} />
                  </div>
                )}
              </div>

              <ChatComposer onSend={handleSend} />
            </>
          ) : (
            <div
              className="
                flex
                items-center
                justify-center
                h-full
              "
            >
              <p className="text-muted-foreground">Select a conversation</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
