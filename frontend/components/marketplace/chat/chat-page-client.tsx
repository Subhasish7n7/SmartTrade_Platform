"use client"

import * as React from "react"

import { getInbox, getMessages, sendChatMessage } from "@/lib/api/chats/chat"
import { onWebSocketConnected } from "@/lib/api/configs/websocket"
import { subscribeToChat, unsubscribeFromChat } from "@/lib/api/configs/websocketSubscriptions"
import { ChatConversationResponse, ChatMessageResponse } from "@/lib/types/api/chat"
import { ChatComposer } from "./chat-composer"
import { ChatHeader } from "./chat-header"
import { ChatSidebar } from "./chat-sidebar"
import { MessageBubble } from "./message-bubble"
import { PinnedTradeCard } from "./pinned-trade-card"


interface Props {
  currentUserId: number
}

export function ChatPageClient({ currentUserId }: Props) {
  const [conversations, setConversations] = React.useState<ChatConversationResponse[]>([])
  const [mobileConversationOpen, setMobileConversationOpen] = React.useState(false)
  const [selectedConversation, setSelectedConversation] = React.useState<ChatConversationResponse | null>(null)
  const [messages, setMessages] = React.useState<ChatMessageResponse[]>([])
  const [loading, setLoading] = React.useState(true)
  const selectedConversationRef = React.useRef<ChatConversationResponse | null>(null)

  React.useEffect(() => {
    selectedConversationRef.current = selectedConversation
  }, [selectedConversation])

  const bottomRef = React.useRef<HTMLDivElement>(null)

  React.useEffect(() => {
    const loadInbox = async () => {
      try {
        const data = await getInbox()
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
        const data = await getMessages(selectedConversation.otherUserId)
        setMessages([...data.content].reverse())
      } catch {
        setMessages(mockMessages)
      } finally {
        setLoading(false)
      }
    }
    loadMessages()
  }, [selectedConversation])

  React.useEffect(() => {
    const unsubscribeConnection = onWebSocketConnected(() => {
      subscribeToChat((event) => {
        setConversations((prev) => {
          const existingIndex = prev.findIndex((conversation) => conversation.otherUserId === event.conversation.otherUserId)

          if (existingIndex === -1) {
            return [event.conversation, ...prev]
          }

          const updated = [...prev]

          updated[existingIndex] = event.conversation

          const [conversation] = updated.splice(existingIndex, 1)

          return [conversation, ...updated]
        })

        if (selectedConversationRef.current?.otherUserId === event.conversation.otherUserId) {
          setMessages((prev) => [...prev, event.message])
        }
      })
    })

    return () => {
      unsubscribeConnection()
      unsubscribeFromChat()
    }
  }, [])

  React.useEffect(() => {
    bottomRef.current?.scrollIntoView({
      behavior: "smooth",
    })
  }, [messages])

  const handleSend = async (content: string) => {
    if (!selectedConversation) return
    sendChatMessage(selectedConversation.otherUserId, content)
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
