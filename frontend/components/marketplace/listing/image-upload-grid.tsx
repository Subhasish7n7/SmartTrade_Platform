"use client"

import { ImagePlus, Loader2, Star, Trash2, UploadCloud } from "lucide-react"
import Image from "next/image"
import { useEffect, useRef, useState } from "react"

import { Button } from "@/components/ui/button"
import { deleteImageByToken, uploadImage } from "@/lib/api/cloudinary"
import type { ItemImageDraft } from "@/lib/types/api/item"

interface Props {
  value: ItemImageDraft[]
  onChange: (images: ItemImageDraft[]) => void
  onExistingImageRemoved: (imageId: number) => void
}

const MAX_IMAGES = 8

export async function cleanupUnpersistedItemImages(images: ItemImageDraft[]) {
  await Promise.all(
    images
      .filter((image) => image.id === null)
      .map((image) => deleteImageByToken(image.deleteToken)),
  )
}

export function ImageUploadGrid({ value, onChange, onExistingImageRemoved }: Props) {
  const [uploading, setUploading] = useState(false)
  const [dragging, setDragging] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const inputRef = useRef<HTMLInputElement>(null)
  const valueRef = useRef(value)

  useEffect(() => {
    valueRef.current = value
  }, [value])

  useEffect(() => {
    return () => {
      void cleanupUnpersistedItemImages(valueRef.current)
    }
  }, [])

  function reindex(images: ItemImageDraft[]) {
    return images.map((image, index) => ({ ...image, sortOrder: index }))
  }

  async function uploadFiles(files: FileList | File[] | null) {
    if (!files?.length) return
    setError(null)

    const remainingSlots = MAX_IMAGES - value.length
    if (remainingSlots <= 0) {
      setError(`You can upload up to ${MAX_IMAGES} images.`)
      return
    }

    const selected = Array.from(files).slice(0, remainingSlots)

    if (Array.from(files).length > remainingSlots) {
      setError(`Only ${remainingSlots} more image${remainingSlots === 1 ? "" : "s"} can be added.`)
    }

    try {
      setUploading(true)
      const results = await Promise.all(
        selected.map((file) => uploadImage(file, "item")),
      )

      const uploaded = results.map<ItemImageDraft>((result) => ({
        id: null,
        secureUrl: result.secureUrl,
        publicId: result.publicId,
        assetId: result.assetId,
        width: result.width,
        height: result.height,
        format: result.format,
        bytes: result.bytes,
        sortOrder: 0,
        deleteToken: result.deleteToken,
      }))

      onChange(reindex([...value, ...uploaded]))
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to upload images.")
    } finally {
      setUploading(false)
      if (inputRef.current) inputRef.current.value = ""
    }
  }

  async function removeImage(index: number) {
    const image = value[index]
    if (!image) return

    if (image.id !== null) {
      // Existing persisted image: draft-only removal. DB is untouched until Save.
      onExistingImageRemoved(image.id)
    } else {
      // New upload: safe to attempt Cloudinary deletion immediately.
      await deleteImageByToken(image.deleteToken)
    }

    onChange(
      reindex(value.filter((_, imageIndex) => imageIndex !== index)),
    )
  }

  function makePrimary(index: number) {
    if (index === 0) return
    const next = [...value]
    const [selected] = next.splice(index, 1)
    next.unshift(selected)
    onChange(reindex(next))
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
        onChange={(event) => void uploadFiles(event.target.files)}
      />

      <div
        onDragOver={(event) => {
          event.preventDefault()
          if (!uploading && value.length < MAX_IMAGES) setDragging(true)
        }}
        onDragLeave={() => setDragging(false)}
        onDrop={(event) => {
          event.preventDefault()
          setDragging(false)
          if (!uploading && value.length < MAX_IMAGES) {
            void uploadFiles(event.dataTransfer.files)
          }
        }}
        className={`grid gap-3 rounded-[22px] transition ${
          dragging ? "ring-1 ring-primary/40 ring-offset-2 ring-offset-background" : ""
        } sm:grid-cols-[1.35fr_1fr_1fr]`}
      >
        {value.length > 0 ? (
          <div className="relative aspect-[1.12] overflow-hidden rounded-2xl border border-white/[0.07] bg-muted/20 shadow-[0_18px_40px_rgba(0,0,0,.10)] sm:row-span-2">
            <Image src={value[0].secureUrl} alt="Primary listing image" fill className="object-cover" />
            <div className="absolute inset-0 bg-gradient-to-t from-background/90 via-transparent to-transparent" />
            <div className="absolute inset-x-0 bottom-0 px-3 pb-3 pt-12">
              <div className="flex items-center justify-between">
                <span className="inline-flex items-center gap-1 text-xs font-medium">
                  <Star className="h-3 w-3 fill-[var(--premium-gold)] text-[var(--premium-gold)]" />
                  Primary photo
                </span>
                <span className="rounded-full border border-white/10 bg-black/30 px-2 py-1 text-[10px] text-muted-foreground backdrop-blur-md">
                  1 / {value.length}
                </span>
              </div>
            </div>
            <Button type="button" size="icon" variant="destructive" className="absolute right-2 top-2 h-8 w-8 rounded-full shadow-lg" onClick={() => void removeImage(0)} aria-label="Remove primary image">
              <Trash2 className="h-3.5 w-3.5" />
            </Button>
          </div>
        ) : (
          <button type="button" disabled={uploading} onClick={() => inputRef.current?.click()} className={`group relative flex min-h-52 cursor-pointer flex-col items-center justify-center rounded-2xl border border-dashed p-6 text-center transition sm:row-span-2 ${dragging ? "border-primary/40 bg-primary/[0.05]" : "border-white/[0.08] bg-white/[0.012] hover:border-primary/30 hover:bg-primary/[0.025]"} disabled:cursor-not-allowed disabled:opacity-60`}>
            <span className="pointer-events-none absolute inset-3 rounded-xl border border-white/[0.025]" />
            <span className="relative flex h-12 w-12 items-center justify-center rounded-2xl border border-primary/10 bg-primary/[0.04] text-primary shadow-[0_0_26px_rgba(45,212,191,.05)] transition group-hover:scale-105">
              <UploadCloud className="h-5 w-5" />
            </span>
            <span className="relative mt-4 text-sm font-medium">{uploading ? "Uploading photos…" : "Drop photos here"}</span>
            <span className="relative mt-1 text-xs text-muted-foreground">or click to browse · JPG, PNG, WEBP</span>
            <span className="relative mt-3 rounded-full border border-white/[0.06] bg-white/[0.02] px-2.5 py-1 text-[10px] text-muted-foreground">Up to {MAX_IMAGES} images</span>
          </button>
        )}

        {value.slice(1, 5).map((image, index) => {
          const actualIndex = index + 1
          return (
            <div key={`${image.id ?? image.assetId}-${actualIndex}`} className="group relative aspect-square overflow-hidden rounded-2xl border border-white/[0.07] bg-muted/20">
              <Image src={image.secureUrl} alt={`Listing image ${actualIndex + 1}`} fill className="object-cover transition-transform duration-500 group-hover:scale-105" />
              <div className="absolute inset-0 bg-gradient-to-t from-background/70 via-transparent to-transparent opacity-0 transition-opacity group-hover:opacity-100" />
              <div className="absolute left-2 top-2 rounded-full border border-white/10 bg-black/30 px-2 py-1 text-[9px] font-semibold text-white/80 backdrop-blur-md">{actualIndex + 1}</div>
              <div className="absolute right-2 top-2 flex gap-1 opacity-0 transition group-hover:opacity-100">
                <Button type="button" size="icon" variant="secondary" className="h-7 w-7 rounded-full border border-white/10 bg-background/70" title="Make primary" onClick={() => makePrimary(actualIndex)} aria-label={`Make photo ${actualIndex + 1} primary`}><Star className="h-3 w-3" /></Button>
                <Button type="button" size="icon" variant="destructive" className="h-7 w-7 rounded-full" onClick={() => void removeImage(actualIndex)} aria-label={`Remove photo ${actualIndex + 1}`}><Trash2 className="h-3 w-3" /></Button>
              </div>
            </div>
          )
        })}

        {value.length < MAX_IMAGES && value.length > 0 && (
          <button type="button" onClick={() => inputRef.current?.click()} disabled={uploading} className="flex aspect-square cursor-pointer items-center justify-center rounded-2xl border border-dashed border-white/[0.07] bg-white/[0.01] text-muted-foreground transition hover:border-primary/25 hover:bg-primary/[0.02] disabled:cursor-not-allowed disabled:opacity-60" aria-label="Add more photos">
            {uploading ? <Loader2 className="h-5 w-5 animate-spin text-primary" /> : <ImagePlus className="h-5 w-5" />}
          </button>
        )}
      </div>

      {value.length > 5 && (
        <div className="mt-3 grid grid-cols-3 gap-3 sm:grid-cols-4">
          {value.slice(5).map((image, index) => {
            const actualIndex = index + 5
            return (
              <div key={`${image.id ?? image.assetId}-${actualIndex}`} className="group relative aspect-square overflow-hidden rounded-xl border border-white/[0.07]">
                <Image src={image.secureUrl} alt={`Listing image ${actualIndex + 1}`} fill className="object-cover" />
                <div className="absolute left-1.5 top-1.5 rounded-full border border-white/10 bg-black/30 px-1.5 py-1 text-[9px] text-white/75 backdrop-blur-md">{actualIndex + 1}</div>
                <Button type="button" size="icon" variant="destructive" className="absolute right-1.5 top-1.5 h-7 w-7 rounded-full opacity-0 transition group-hover:opacity-100" onClick={() => void removeImage(actualIndex)} aria-label={`Remove photo ${actualIndex + 1}`}><Trash2 className="h-3 w-3" /></Button>
              </div>
            )
          })}
        </div>
      )}

      <div className="mt-3 flex flex-wrap items-center justify-between gap-3 text-[11px] text-muted-foreground">
        <span>{value.length}/{MAX_IMAGES} photos</span>
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