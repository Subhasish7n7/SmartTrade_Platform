"use client"

import { ChatMessage } from "@/lib/types/ui/chat"

interface Props {
  message: ChatMessage
  showAvatar?: boolean
  mine: boolean
}

export function MessageBubble({
  message,
  mine,
}: Props) {
  return (
    <div
      className={`
        flex
        ${
          mine
            ? "justify-end"
            : "justify-start"
        }
      `}
    >
      <div
        className={`
          max-w-[75%]
          rounded-3xl
          px-5
          py-4

          ${
            mine
              ? `
                bg-[var(--trust-blue)]/10
                border
                border-[var(--trust-blue)]/20
              `
              : `
                glass
              `
          }
        `}
      >
        <p>{message.message}</p>

        <p
          className="
            text-xs
            mt-2
            text-muted-foreground
          "
        >
          {message.timestamp}
        </p>
      </div>
    </div>
  )
}