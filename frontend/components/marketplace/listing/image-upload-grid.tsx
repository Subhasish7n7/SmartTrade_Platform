"use client"

import { ImagePlus, Loader2, Star, Trash2 } from "lucide-react"
import Image from "next/image"
import { useRef, useState } from "react"

import { Button } from "@/components/ui/button"
import { uploadImage } from "@/lib/api/cloudinary"

interface Props {
  value: string[]
  onChange: (urls: string[]) => void
}

const MAX_IMAGES = 8

export function ImageUploadGrid({ value, onChange }: Props) {
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const inputRef = useRef<HTMLInputElement>(null)

  async function uploadFiles(files: FileList | null) {
    if (!files?.length) return

    setError(null)

    const remainingSlots = MAX_IMAGES - value.length

    if (remainingSlots <= 0) {
      setError(`You can upload up to ${MAX_IMAGES} images.`)
      return
    }

    const selected = Array.from(files).slice(0, remainingSlots)

    if (files.length > remainingSlots) {
      setError(`Only ${remainingSlots} more image${remainingSlots === 1 ? "" : "s"} can be added.`)
    }

    try {
      setUploading(true)

      const results = await Promise.all(selected.map((file) => uploadImage(file, "item")))

      onChange([...value, ...results.map((result) => result.secureUrl)])
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to upload images.")
    } finally {
      setUploading(false)

      if (inputRef.current) {
        inputRef.current.value = ""
      }
    }
  }

  function removeImage(index: number) {
    onChange(value.filter((_, imageIndex) => imageIndex !== index))
  }

  function makePrimary(index: number) {
    if (index === 0) return
    const next = [...value]
    const [selected] = next.splice(index, 1)
    next.unshift(selected)
    onChange(next)
  }

  return (
    <div>
      <input
        ref={inputRef}
        type="file"
        multiple
        accept="image/jpeg,image/png,image/webp"
        className="hidden"
        disabled={uploading || value.length >= MAX_IMAGES}
        onChange={(e) => uploadFiles(e.target.files)}
      />

      <div className="grid gap-3 sm:grid-cols-[1.35fr_1fr_1fr]">
        {value.length > 0 ? (
          <div className="relative aspect-[1.12] overflow-hidden rounded-2xl border border-border/45 bg-muted/20 sm:row-span-2">
            <Image src={value[0]} alt="Primary listing image" fill className="object-cover" />
            <div className="absolute inset-x-0 bottom-0 bg-gradient-to-t from-background/85 to-transparent px-3 pb-3 pt-12">
              <div className="flex items-center justify-between">
                <span className="inline-flex items-center gap-1 text-xs font-medium">
                  <Star className="h-3 w-3 fill-[var(--premium-gold)] text-[var(--premium-gold)]" />
                  Primary photo
                </span>
                <span className="text-[10px] text-muted-foreground">1 / {value.length}</span>
              </div>
            </div>
            <Button
              type="button"
              size="icon"
              variant="destructive"
              className="absolute right-2 top-2 h-8 w-8 rounded-full"
              onClick={() => removeImage(0)}
            >
              <Trash2 className="h-3.5 w-3.5" />
            </Button>
          </div>
        ) : (
          <button
            type="button"
            disabled={uploading}
            onClick={() => inputRef.current?.click()}
            className="group flex min-h-52 cursor-pointer flex-col items-center justify-center rounded-2xl border border-dashed border-border/60 bg-white/[0.012] transition hover:border-primary/30 hover:bg-primary/[0.02] disabled:cursor-not-allowed disabled:opacity-60 sm:row-span-2"
          >
            <span className="flex h-12 w-12 items-center justify-center rounded-2xl border border-border/50 bg-card/60 text-primary transition group-hover:scale-105">
              <ImagePlus className="h-5 w-5" />
            </span>
            <span className="mt-4 text-sm font-medium">Add listing photos</span>
            <span className="mt-1 text-xs text-muted-foreground">JPG, PNG, WEBP or AVIF · up to {MAX_IMAGES}</span>
          </button>
        )}

        {value.slice(1, 5).map((image, index) => {
          const actualIndex = index + 1
          return (
            <div key={image} className="group relative aspect-square overflow-hidden rounded-2xl border border-border/45 bg-muted/20">
              <Image
                src={image}
                alt={`Listing image ${actualIndex + 1}`}
                fill
                className="object-cover transition-transform duration-500 group-hover:scale-105"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-background/65 via-transparent to-transparent opacity-0 transition-opacity group-hover:opacity-100" />
              <div className="absolute right-2 top-2 flex gap-1 opacity-0 transition group-hover:opacity-100">
                <Button
                  type="button"
                  size="icon"
                  variant="secondary"
                  className="h-7 w-7 rounded-full bg-background/70"
                  title="Make primary"
                  onClick={() => makePrimary(actualIndex)}
                >
                  <Star className="h-3 w-3" />
                </Button>
                <Button
                  type="button"
                  size="icon"
                  variant="destructive"
                  className="h-7 w-7 rounded-full"
                  onClick={() => removeImage(actualIndex)}
                >
                  <Trash2 className="h-3 w-3" />
                </Button>
              </div>
            </div>
          )
        })}

        {value.length < MAX_IMAGES && value.length > 0 && (
          <button
            type="button"
            onClick={() => inputRef.current?.click()}
            disabled={uploading}
            className="flex aspect-square cursor-pointer items-center justify-center rounded-2xl border border-dashed border-border/50 bg-white/[0.01] text-muted-foreground transition hover:border-primary/25 hover:bg-primary/[0.02] disabled:cursor-not-allowed disabled:opacity-60"
          >
            {uploading ? <Loader2 className="h-5 w-5 animate-spin text-primary" /> : <ImagePlus className="h-5 w-5" />}
          </button>
        )}
      </div>

      {value.length > 5 && (
        <div className="mt-3 grid grid-cols-3 gap-3 sm:grid-cols-4">
          {value.slice(5).map((image, index) => {
            const actualIndex = index + 5
            return (
              <div key={image} className="group relative aspect-square overflow-hidden rounded-xl border border-border/45">
                <Image src={image} alt={`Listing image ${actualIndex + 1}`} fill className="object-cover" />
                <Button
                  type="button"
                  size="icon"
                  variant="destructive"
                  className="absolute right-1.5 top-1.5 h-7 w-7 rounded-full opacity-0 transition group-hover:opacity-100"
                  onClick={() => removeImage(actualIndex)}
                >
                  <Trash2 className="h-3 w-3" />
                </Button>
              </div>
            )
          })}
        </div>
      )}

      <div className="mt-3 flex items-center justify-between gap-3 text-[11px] text-muted-foreground">
        <span>
          {value.length}/{MAX_IMAGES} photos
        </span>
        <span>First photo is your cover image.</span>
      </div>
      {uploading && (
        <div className="mt-2 flex items-center gap-2 text-xs text-primary">
          <Loader2 className="h-3.5 w-3.5 animate-spin" />
          Uploading images…
        </div>
      )}
      {error && <p className="mt-2 text-xs text-destructive">{error}</p>}
    </div>
  )
}
