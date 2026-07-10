"use client"

import * as React from "react"

import {
  Send,
  ImagePlus,
  Paperclip,
  Smile,
} from "lucide-react"

import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"

interface Props {
  onSend: (
    content: string
  ) => void
}

export function ChatComposer({
  onSend,
}: Props) {
  const [message, setMessage] =
    React.useState("")

  const send = () => {
    const trimmed =
      message.trim()

    if (!trimmed) return

    onSend(trimmed)

    setMessage("")
  }

  return (
    <div
      className="
        border-t
        border-border/50
        bg-card/40
        backdrop-blur-xl
        p-4
      "
    >
      <div className="flex items-center gap-2">

        <Button
          size="icon"
          variant="ghost"
          className="shrink-0"
        >
          <ImagePlus className="h-5 w-5" />
        </Button>

        <Button
          size="icon"
          variant="ghost"
          className="shrink-0"
        >
          <Paperclip className="h-5 w-5" />
        </Button>

        <Input
          value={message}
          onChange={(e) =>
            setMessage(
              e.target.value
            )
          }
          placeholder="Message..."
          onKeyDown={(e) => {
            if (
              e.key === "Enter" &&
              !e.shiftKey
            ) {
              e.preventDefault()
              send()
            }
          }}
          className="
            h-12
            bg-background/50
          "
        />

        <Button
          size="icon"
          variant="ghost"
          className="hidden sm:flex"
        >
          <Smile className="h-5 w-5" />
        </Button>

        <Button
          onClick={send}
          className="
            h-12
            w-12
            shrink-0
            bg-[var(--trust-blue)]
            hover:opacity-90
          "
        >
          <Send className="h-4 w-4" />
        </Button>

      </div>
    </div>
  )
}