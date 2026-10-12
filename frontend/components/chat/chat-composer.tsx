"use client"

import * as React from "react"

import { ImagePlus, Paperclip, Send, Smile, X } from "lucide-react"

import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"

interface Props {
  onSend: (content: string) => void
}

const EMOJIS = [
  "😀",
  "😂",
  "🤣",
  "😊",
  "😍",
  "🥰",
  "😎",
  "🤔",
  "😅",
  "🙌",
  "👍",
  "👎",
  "❤️",
  "🔥",
  "🎉",
  "👏",
  "💯",
  "😢",
  "😮",
  "😡",
  "🙏",
  "🤝",
  "✅",
  "❌",
]

export function ChatComposer({ onSend }: Props) {
  const [message, setMessage] = React.useState("")
  const [selectedFiles, setSelectedFiles] = React.useState<File[]>([])
  const [emojiOpen, setEmojiOpen] = React.useState(false)

  const imageInputRef = React.useRef<HTMLInputElement>(null)
  const fileInputRef = React.useRef<HTMLInputElement>(null)

  const send = () => {
    const trimmed = message.trim()

    if (!trimmed) return

    onSend(trimmed)
    setMessage("")
    setEmojiOpen(false)
  }

  const addEmoji = (emoji: string) => {
    setMessage((current) => current + emoji)
  }

  const handleImageSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(event.target.files ?? [])

    if (!files.length) return

    setSelectedFiles((previous) => [...previous, ...files])

    event.target.value = ""
  }

  const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(event.target.files ?? [])

    if (!files.length) return

    setSelectedFiles((previous) => [...previous, ...files])

    event.target.value = ""
  }

  const removeSelectedFile = (index: number) => {
    setSelectedFiles((previous) => previous.filter((_, fileIndex) => fileIndex !== index))
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
      <input ref={imageInputRef} type="file" accept="image/*" multiple className="hidden" onChange={handleImageSelect} />

      <input ref={fileInputRef} type="file" multiple className="hidden" onChange={handleFileSelect} />

      {selectedFiles.length > 0 && (
        <div className="mb-3 flex flex-wrap gap-2">
          {selectedFiles.map((file, index) => (
            <div
              key={`${file.name}-${file.lastModified}-${index}`}
              className="
                flex
                items-center
                gap-2
                rounded-xl
                border
                border-border/50
                bg-background/50
                px-3
                py-2
                text-xs
              "
            >
              <span className="max-w-52 truncate">{file.name}</span>

              <button
                type="button"
                onClick={() => removeSelectedFile(index)}
                className="
                  text-muted-foreground
                  transition
                  hover:text-foreground
                "
              >
                <X className="h-3.5 w-3.5" />
              </button>
            </div>
          ))}
        </div>
      )}

      <div className="relative flex items-center gap-2">
        <Button
          type="button"
          size="icon"
          variant="ghost"
          className="shrink-0"
          onClick={() => imageInputRef.current?.click()}
          title="Add images"
        >
          <ImagePlus className="h-5 w-5" />
        </Button>

        <Button
          type="button"
          size="icon"
          variant="ghost"
          className="shrink-0"
          onClick={() => fileInputRef.current?.click()}
          title="Attach files"
        >
          <Paperclip className="h-5 w-5" />
        </Button>

        <Input
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          placeholder="Message..."
          onKeyDown={(e) => {
            if (e.key === "Enter" && !e.shiftKey) {
              e.preventDefault()
              send()
            }
          }}
          className="
            h-12
            bg-background/50
          "
        />

        <div className="relative">
          <Button
            type="button"
            size="icon"
            variant="ghost"
            className="hidden sm:flex"
            onClick={() => setEmojiOpen((open) => !open)}
            title="Emoji"
          >
            <Smile className="h-5 w-5" />
          </Button>

          {emojiOpen && (
            <div
              className="
                absolute
                bottom-14
                right-0
                z-50
                w-64
                rounded-2xl
                border
                border-border/60
                bg-card
                p-3
                shadow-xl
              "
            >
              <div className="grid grid-cols-8 gap-1">
                {EMOJIS.map((emoji) => (
                  <button
                    key={emoji}
                    type="button"
                    onClick={() => addEmoji(emoji)}
                    className="
                      flex
                      h-8
                      w-8
                      items-center
                      justify-center
                      rounded-lg
                      text-lg
                      transition
                      hover:bg-muted
                    "
                  >
                    {emoji}
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>

        <Button
          type="button"
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
