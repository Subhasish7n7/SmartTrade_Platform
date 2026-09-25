"use client"

import * as React from "react"
import Image from "next/image"
import {
  ChevronLeft,
  ChevronRight,
  Expand,
  X,
} from "lucide-react"

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
      galleryImages.length === 0
        ? 0
        : Math.min(current, galleryImages.length - 1),
    )
  }, [galleryImages.length])

  const currentImage =
    galleryImages[currentImageIndex]?.secureUrl ?? IMAGE_FALLBACK

  function nextImage() {
    if (galleryImages.length < 2) return

    setCurrentImageIndex(
      (previous) => (previous + 1) % galleryImages.length,
    )
  }

  function prevImage() {
    if (galleryImages.length < 2) return

    setCurrentImageIndex(
      (previous) =>
        (previous - 1 + galleryImages.length) % galleryImages.length,
    )
  }

  return (
    <>
      <div className="content-surface space-y-4 rounded-[28px] p-4 sm:p-5">
        <div className="relative aspect-[4/3] overflow-hidden rounded-2xl border bg-muted">
          <Image
            src={currentImage}
            alt={itemName}
            fill
            priority
            className="object-cover"
          />

          {galleryImages.length > 1 && (
            <>
              <button
                type="button"
                onClick={prevImage}
                aria-label="Previous image"
                className="absolute left-4 top-1/2 flex h-10 w-10 -translate-y-1/2 items-center justify-center rounded-full border bg-background/80 backdrop-blur-sm transition hover:bg-background"
              >
                <ChevronLeft className="h-5 w-5" />
              </button>

              <button
                type="button"
                onClick={nextImage}
                aria-label="Next image"
                className="absolute right-4 top-1/2 flex h-10 w-10 -translate-y-1/2 items-center justify-center rounded-full border bg-background/80 backdrop-blur-sm transition hover:bg-background"
              >
                <ChevronRight className="h-5 w-5" />
              </button>
            </>
          )}

          <button
            type="button"
            onClick={() => setIsFullscreen(true)}
            aria-label="Open image fullscreen"
            className="absolute right-4 top-4 flex h-10 w-10 items-center justify-center rounded-full border bg-background/80 backdrop-blur-sm transition hover:bg-background"
          >
            <Expand className="h-5 w-5" />
          </button>

          {galleryImages.length > 0 && (
            <div className="absolute bottom-4 left-4 rounded-full border bg-background/75 px-3 py-1 text-[11px] font-medium backdrop-blur-md">
              {currentImageIndex + 1} / {galleryImages.length}
            </div>
          )}
        </div>

        {galleryImages.length > 0 && (
          <div className="flex gap-3 overflow-x-auto pb-1">
            {galleryImages.map((image, index) => (
              <button
                key={image.id}
                type="button"
                onClick={() => setCurrentImageIndex(index)}
                aria-label={`View ${itemName} image ${index + 1}`}
                className={cn(
                  "relative h-20 w-20 shrink-0 overflow-hidden rounded-xl border transition",
                  currentImageIndex === index &&
                    "ring-2 ring-primary ring-offset-2 ring-offset-background",
                )}
              >
                <Image
                  src={image.secureUrl}
                  alt=""
                  fill
                  className="object-cover"
                />
              </button>
            ))}
          </div>
        )}
      </div>

      {isFullscreen && (
        <div className="fixed inset-0 z-50 flex flex-col bg-background">
          <div className="flex items-center justify-between border-b p-4">
            <span className="text-sm text-muted-foreground">
              {galleryImages.length > 0
                ? `${currentImageIndex + 1} / ${galleryImages.length}`
                : "Preview"}
            </span>

            <button
              type="button"
              onClick={() => setIsFullscreen(false)}
              aria-label="Close fullscreen"
            >
              <X className="h-6 w-6" />
            </button>
          </div>

          <div className="relative flex-1">
            <Image
              src={currentImage}
              alt={itemName}
              fill
              className="object-contain"
            />
          </div>
        </div>
      )}
    </>
  )
}