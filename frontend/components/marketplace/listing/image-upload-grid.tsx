"use client"

import Image from "next/image"
import { useState } from "react"

interface Props {
  value: string[]

  onChange: (
    urls: string[]
  ) => void
}

export function ImageUploadGrid({
  value,
  onChange,
}: Props) {

  const [uploading,
    setUploading] =
    useState(false)

  async function uploadFiles(
    files: FileList | null
  ) {

    if (!files) return

    try {

      setUploading(true)

      const uploaded: string[] = []

      for (const file of Array.from(files)) {

        const formData =
          new FormData()

        formData.append(
          "file",
          file
        )

        formData.append(
          "upload_preset",
          process.env
            .NEXT_PUBLIC_CLOUDINARY_UPLOAD_PRESET!
        )

        const response =
          await fetch(
            `https://api.cloudinary.com/v1_1/${process.env.NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME}/image/upload`,
            {
              method: "POST",
              body: formData,
            }
          )

        const data =
          await response.json()

        uploaded.push(
          data.secure_url
        )
      }

      onChange([
        ...value,
        ...uploaded,
      ])

    } finally {

      setUploading(false)

    }
  }

  return (
    <div>

      <label
        className="
        glass
        rounded-2xl
        border
        border-dashed
        border-border
        h-52
        flex
        items-center
        justify-center
        cursor-pointer
        "
      >

        <input
          type="file"
          multiple
          accept="image/*"
          className="hidden"
          onChange={(e) =>
            uploadFiles(
              e.target.files
            )
          }
        />

        <span className="text-muted-foreground">
          {uploading
            ? "Uploading..."
            : "Click To Upload Photos"}
        </span>

      </label>

      {value.length > 0 && (

        <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mt-4">

          {value.map((image) => (

            <div
              key={image}
              className="relative aspect-square rounded-xl overflow-hidden"
            >

              <Image
                src={image}
                alt="item"
                fill
                className="object-cover"
              />

            </div>

          ))}

        </div>

      )}

    </div>
  )
}