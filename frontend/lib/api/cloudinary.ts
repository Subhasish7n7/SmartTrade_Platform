const CLOUDINARY_CLOUD_NAME =
  process.env.NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME

const ITEM_UPLOAD_PRESET =
  process.env.NEXT_PUBLIC_CLOUDINARY_ITEM_UPLOAD_PRESET

const AVATAR_UPLOAD_PRESET =
  process.env.NEXT_PUBLIC_CLOUDINARY_AVATAR_UPLOAD_PRESET

const MAX_INPUT_FILE_SIZE = 20 * 1024 * 1024
const MAX_OUTPUT_FILE_SIZE = 5 * 1024 * 1024

export type CloudinaryUploadPurpose = "item" | "avatar"

export type CloudinaryUploadResult = {
  secureUrl: string
  publicId: string
  width: number
  height: number
  format: string
  bytes: number
}

const purposeConfig: Record<
  CloudinaryUploadPurpose,
  { preset: string | undefined; maxDimension: number }
> = {
  item: {
    preset: ITEM_UPLOAD_PRESET,
    maxDimension: 1600,
  },
  avatar: {
    preset: AVATAR_UPLOAD_PRESET,
    maxDimension: 1024,
  },
}

function assertConfigured(
  purpose: CloudinaryUploadPurpose,
): { preset: string; maxDimension: number } {
  if (!CLOUDINARY_CLOUD_NAME) {
    throw new Error("Cloudinary cloud name is not configured")
  }

  const config = purposeConfig[purpose]

  if (!config.preset) {
    throw new Error(
      `Cloudinary ${purpose} upload preset is not configured`,
    )
  }

  return {
    preset: config.preset,
    maxDimension: config.maxDimension,
  }
}

function loadImage(file: File): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const objectUrl = URL.createObjectURL(file)
    const image = new Image()

    image.onload = () => {
      URL.revokeObjectURL(objectUrl)
      resolve(image)
    }

    image.onerror = () => {
      URL.revokeObjectURL(objectUrl)
      reject(new Error("Could not read image"))
    }

    image.src = objectUrl
  })
}

async function compressImage(
  file: File,
  maxDimension: number,
): Promise<Blob> {
  if (!file.type.startsWith("image/")) {
    throw new Error("Only image files are supported")
  }

  // Canvas/WebP conversion is deliberately limited to normal web image formats.
  // HEIC/HEIF support is browser-dependent and should not silently fail.
  if (
    !["image/jpeg", "image/png", "image/webp"].includes(
      file.type.toLowerCase(),
    )
  ) {
    throw new Error(
      "Please upload a JPEG, PNG, or WebP image",
    )
  }

  if (file.size > MAX_INPUT_FILE_SIZE) {
    throw new Error("Image must be 20 MB or smaller")
  }

  const image = await loadImage(file)

  const scale = Math.min(
    1,
    maxDimension / image.naturalWidth,
    maxDimension / image.naturalHeight,
  )

  const width = Math.max(
    1,
    Math.round(image.naturalWidth * scale),
  )

  const height = Math.max(
    1,
    Math.round(image.naturalHeight * scale),
  )

  const canvas = document.createElement("canvas")
  canvas.width = width
  canvas.height = height

  const context = canvas.getContext("2d")

  if (!context) {
    throw new Error("Could not initialize image processor")
  }

  context.drawImage(image, 0, 0, width, height)

  let quality = 0.82

  for (let attempt = 0; attempt < 4; attempt++) {
    const blob = await new Promise<Blob | null>((resolve) => {
      canvas.toBlob(
        resolve,
        "image/webp",
        quality,
      )
    })

    if (!blob) {
      throw new Error("Could not compress image")
    }

    if (blob.size <= MAX_OUTPUT_FILE_SIZE || attempt === 3) {
      return blob
    }

    quality -= 0.10
  }

  throw new Error("Could not compress image")
}

export async function uploadImage( file: File, purpose: CloudinaryUploadPurpose,): Promise<CloudinaryUploadResult> {
  const { preset, maxDimension } = assertConfigured(purpose)

  const compressed = await compressImage(
    file,
    maxDimension,
  )

  const formData = new FormData()
  formData.append("file", compressed, "smarttrade.webp")
  formData.append("upload_preset", preset)

  const response = await fetch(
    `https://api.cloudinary.com/v1_1/${CLOUDINARY_CLOUD_NAME}/image/upload`,
    {
      method: "POST",
      body: formData,
    },
  )

  let body: {
    secure_url?: string
    public_id?: string
    width?: number
    height?: number
    format?: string
    bytes?: number
    error?: { message?: string }
  }

  try {
    body = await response.json()
  } catch {
    throw new Error("Cloudinary returned an invalid response")
  }

  if (!response.ok || !body.secure_url || !body.public_id) {
    throw new Error(
      body.error?.message ||
        "Cloudinary image upload failed",
    )
  }

  return {
    secureUrl: body.secure_url,
    publicId: body.public_id,
    width: body.width ?? 0,
    height: body.height ?? 0,
    format: body.format ?? "webp",
    bytes: body.bytes ?? compressed.size,
  }
}