"use client"

import * as React from "react"
import Image from "next/image"
import { ChevronLeft, ChevronRight, Expand, X } from "lucide-react"

import { cn } from "@/lib/utils"
import { IMAGE_FALLBACK } from "@/lib/config"
import type { ItemImageResponse } from "@/lib/types/api/item"

interface Props {
  images: ItemImageResponse[]
  itemName: string
}

export function ItemGallery({ images, itemName }: Props) {
  const [currentImageIndex, setCurrentImageIndex] = React.useState(0)
  const [isFullscreen, setIsFullscreen] = React.useState(false)

  const galleryImages = React.useMemo(
    () =>
      [...(images ?? [])]
        .filter((image) => Boolean(image?.secureUrl?.trim()))
        .sort((a, b) => a.sortOrder - b.sortOrder),
    [images],
  )

  React.useEffect(() => {
    setCurrentImageIndex((current) =>
      galleryImages.length === 0 ? 0 : Math.min(current, galleryImages.length - 1),
    )
  }, [galleryImages.length])

  React.useEffect(() => {
    if (!isFullscreen) return

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") {
        setIsFullscreen(false)
        return
      }

      if (event.key === "ArrowRight") nextImage()
      if (event.key === "ArrowLeft") prevImage()
    }

    window.addEventListener("keydown", handleKeyDown)
    return () => window.removeEventListener("keydown", handleKeyDown)
  }, [isFullscreen, galleryImages.length])

  const currentImage = galleryImages[currentImageIndex]?.secureUrl ?? IMAGE_FALLBACK

  function nextImage() {
    if (galleryImages.length < 2) return
    setCurrentImageIndex((previous) => (previous + 1) % galleryImages.length)
  }

  function prevImage() {
    if (galleryImages.length < 2) return
    setCurrentImageIndex((previous) => (previous - 1 + galleryImages.length) % galleryImages.length)
  }

  return (
    <>
      <div className="content-surface relative space-y-4 overflow-hidden rounded-[28px] p-4 sm:p-5">
        <div className="pointer-events-none absolute -right-24 -top-24 h-56 w-56 rounded-full bg-[var(--trust-blue)]/[0.045] blur-3xl" />
        <div className="pointer-events-none absolute -left-24 bottom-0 h-48 w-48 rounded-full bg-[var(--negotiation-violet)]/[0.035] blur-3xl" />

        <div className="relative aspect-[4/3] overflow-hidden rounded-2xl border border-border/60 bg-muted/30 shadow-[inset_0_1px_0_rgba(255,255,255,0.04)]">
          <Image
            src={currentImage}
            alt={itemName}
            fill
            priority
            className="object-cover"
          />

          <div className="pointer-events-none absolute inset-0 bg-gradient-to-t from-background/35 via-transparent to-background/5" />

          {galleryImages.length > 1 && (
            <>
              <button
                type="button"
                onClick={prevImage}
                aria-label="Previous image"
                className="absolute left-4 top-1/2 flex h-10 w-10 -translate-y-1/2 items-center justify-center rounded-full border border-white/10 bg-background/70 text-foreground shadow-[0_8px_24px_rgba(0,0,0,.18)] backdrop-blur-md transition hover:border-[var(--trust-blue)]/30 hover:bg-background/85"
              >
                <ChevronLeft className="h-5 w-5" />
              </button>

              <button
                type="button"
                onClick={nextImage}
                aria-label="Next image"
                className="absolute right-4 top-1/2 flex h-10 w-10 -translate-y-1/2 items-center justify-center rounded-full border border-white/10 bg-background/70 text-foreground shadow-[0_8px_24px_rgba(0,0,0,.18)] backdrop-blur-md transition hover:border-[var(--trust-blue)]/30 hover:bg-background/85"
              >
                <ChevronRight className="h-5 w-5" />
              </button>
            </>
          )}

          <button
            type="button"
            onClick={() => setIsFullscreen(true)}
            aria-label="Open image fullscreen"
            className="absolute right-4 top-4 flex h-10 w-10 items-center justify-center rounded-full border border-white/10 bg-background/70 text-foreground shadow-[0_8px_24px_rgba(0,0,0,.18)] backdrop-blur-md transition hover:border-[var(--trust-blue)]/30 hover:bg-background/85"
          >
            <Expand className="h-5 w-5" />
          </button>

          {galleryImages.length > 0 && (
            <div className="absolute bottom-4 left-4 rounded-full border border-white/10 bg-background/70 px-3 py-1 text-[11px] font-medium text-foreground/90 shadow-sm backdrop-blur-md">
              {currentImageIndex + 1} / {galleryImages.length}
            </div>
          )}
        </div>

        {galleryImages.length > 0 && (
          <div className="relative flex gap-3 overflow-x-auto pb-1">
            {galleryImages.map((image, index) => (
              <button
                key={image.id}
                type="button"
                onClick={() => setCurrentImageIndex(index)}
                aria-label={`View ${itemName} image ${index + 1}`}
                className={cn(
                  "relative h-20 w-20 shrink-0 overflow-hidden rounded-xl border border-border/60 bg-muted/20 transition",
                  currentImageIndex === index &&
                    "border-[var(--trust-blue)]/50 ring-2 ring-[var(--trust-blue)]/20 ring-offset-2 ring-offset-background",
                )}
              >
                <Image
                  src={image.secureUrl}
                  alt=""
                  fill
                  className="object-cover transition-transform duration-300 hover:scale-105"
                />
                {currentImageIndex !== index && (
                  <span className="pointer-events-none absolute inset-0 bg-background/10 transition-colors hover:bg-transparent" />
                )}
              </button>
            ))}
          </div>
        )}
      </div>

      {isFullscreen && (
        <div className="fixed inset-0 z-50 flex flex-col bg-background/98 backdrop-blur-xl">
          <div className="flex items-center justify-between border-b border-border/50 bg-background/85 p-4 backdrop-blur-md">
            <span className="text-sm text-muted-foreground">
              {galleryImages.length > 0 ? `${currentImageIndex + 1} / ${galleryImages.length}` : "Preview"}
            </span>

            <button
              type="button"
              onClick={() => setIsFullscreen(false)}
              aria-label="Close fullscreen"
              className="flex h-9 w-9 items-center justify-center rounded-full border border-border/60 bg-white/[0.025] transition hover:bg-white/[0.05]"
            >
              <X className="h-5 w-5" />
            </button>
          </div>

          <div className="relative flex-1">
            <Image src={currentImage} alt={itemName} fill className="object-contain p-4 sm:p-8" />
          </div>
        </div>
      )}
    </>
  )
}
