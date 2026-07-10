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
import { IMAGE_FALLBACK } from "@/lib/config";

interface Props {
  images: string[]
  itemName: string
}

export function ItemGallery({images, itemName}: Props) {
  const [currentImageIndex, setCurrentImageIndex] =
    React.useState(0)

  const [isFullscreen, setIsFullscreen] =
    React.useState(false)

  const nextImage = () => {
    setCurrentImageIndex(
      (prev) => (prev + 1) % images.length
    )
  }

  const prevImage = () => {
    setCurrentImageIndex(
      (prev) =>
        (prev - 1 + images.length) %
        images.length
    )
  }
  const galleryImages =
  images && images.length > 0
    ? images.filter((img) => img && img.trim().length > 0)
    : [IMAGE_FALLBACK];

const currentImage =
  galleryImages[currentImageIndex] ?? IMAGE_FALLBACK;

  return (
    <>
      <div className="space-y-4 content-surface">
        {/* MAIN IMAGE */}
        <div className="relative aspect-4/3 rounded-2xl overflow-hidden bg-muted border">
          <Image
            src={currentImage}
            alt={itemName}
            fill
            priority
            className="object-cover"
          />

          {/* LEFT */}
          {galleryImages.length > 1 && (
            <button
              onClick={prevImage}
              className="absolute left-4 top-1/2 -translate-y-1/2 h-10 w-10 rounded-full bg-background/80 border flex items-center justify-center"
            >
              <ChevronLeft className="h-5 w-5" />
            </button>
          )}

          {/* RIGHT */}
          {galleryImages.length > 1 && (
            <button
              onClick={nextImage}
              className="absolute right-4 top-1/2 -translate-y-1/2 h-10 w-10 rounded-full bg-background/80 border flex items-center justify-center"
            >
              <ChevronRight className="h-5 w-5" />
            </button>
          )}

          {/* FULLSCREEN */}
          <button
            onClick={() => setIsFullscreen(true)}
            className="absolute top-4 right-4 h-10 w-10 rounded-full bg-background/80 border flex items-center justify-center"
          >
            <Expand className="h-5 w-5" />
          </button>
        </div>

        {/* THUMBNAILS */}
        <div className="flex gap-3 overflow-x-auto">
          {galleryImages.map((img, index) => (
            <button
              key={index}
              onClick={() =>
                setCurrentImageIndex(index)
              }
              className={cn(
                "relative h-20 w-20 rounded-xl overflow-hidden shrink-0 border",
                currentImageIndex === index &&
                  "ring-2 ring-primary"
              )}
            >
              <Image
                src={img}
                alt=""
                fill
                className="object-cover"
              />
            </button>
          ))}
        </div>
      </div>

      {/* FULLSCREEN */}
      {isFullscreen && (
        <div className="fixed inset-0 bg-background z-50 flex flex-col">
          <div className="flex items-center justify-between p-4 border-b">
            <span className="text-sm text-muted-foreground">
              {currentImageIndex + 1} /{" "}
              {galleryImages.length}
            </span>

            <button
              onClick={() =>
                setIsFullscreen(false)
              }
            >
              <X className="h-6 w-6" />
            </button>
          </div>

          <div className="flex-1 relative">
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