"use client";

import Image from "next/image";
import { useRef, useState } from "react";
import { ImagePlus, Loader2, Trash2, UserRound } from "lucide-react";

import { Button } from "@/components/ui/button";
import {
  deleteImageByToken,
  uploadImage,
  type CloudinaryUploadResult,
} from "@/lib/api/cloudinary";

type Props = {
  currentAvatarUrl: string | null;
  fallbackName?: string;
  uploadedAvatar: CloudinaryUploadResult | null;
  onUploaded: (avatar: CloudinaryUploadResult) => void;
  onClearUploaded: () => void;
  onRemoveCurrent: () => void;
  disabled?: boolean;
};

export function AvatarUploadField({
  currentAvatarUrl,
  fallbackName,
  uploadedAvatar,
  onUploaded,
  onClearUploaded,
  onRemoveCurrent,
  disabled = false,
}: Props) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const previewUrl = uploadedAvatar?.secureUrl ?? currentAvatarUrl;

  const initials =
    fallbackName
      ?.trim()
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part.charAt(0).toUpperCase())
      .join("") ?? "";

  async function chooseFile(file?: File) {
    if (!file) return;

    setError(null);
    setUploading(true);

    try {
      const result = await uploadImage(file, "avatar");

      if (uploadedAvatar?.deleteToken) {
        await deleteImageByToken(uploadedAvatar.deleteToken);
      }

      onUploaded(result);
    } catch (uploadError) {
      setError(
        uploadError instanceof Error
          ? uploadError.message
          : "Avatar upload failed. Please try again.",
      );
    } finally {
      setUploading(false);

      if (inputRef.current) {
        inputRef.current.value = "";
      }
    }
  }

  async function clearAvatar() {
    setError(null);

    if (uploadedAvatar) {
      try {
        await deleteImageByToken(uploadedAvatar.deleteToken);
        onClearUploaded();
      } catch (removeError) {
        setError(
          removeError instanceof Error
            ? removeError.message
            : "The uploaded photo could not be removed. Please try again.",
        );
      }

      return;
    }

    if (currentAvatarUrl) {
      onRemoveCurrent();
    }
  }

  return (
    <section className="flex flex-col gap-4 rounded-2xl border border-border/60 bg-background/25 p-4 sm:flex-row sm:items-center">
      <div className="relative flex h-20 w-20 shrink-0 items-center justify-center overflow-hidden rounded-full border border-border/70 bg-muted/40">
        {previewUrl ? (
          <Image
            src={previewUrl}
            alt="Profile avatar preview"
            fill
            sizes="80px"
            className="object-cover"
          />
        ) : initials ? (
          <span
            aria-label={`${fallbackName} initials`}
            className="text-xl font-semibold tracking-wide text-primary"
          >
            {initials}
          </span>
        ) : (
          <UserRound className="h-8 w-8 text-muted-foreground" />
        )}

        {uploading && (
          <div className="absolute inset-0 flex items-center justify-center bg-background/75">
            <Loader2 className="h-5 w-5 animate-spin text-primary" />
          </div>
        )}
      </div>

      <div className="min-w-0 flex-1">
        <p className="text-sm font-semibold">Profile photo</p>

        <p className="mt-1 text-xs leading-5 text-muted-foreground">
          JPEG, PNG, or WebP. The image is resized before upload.
        </p>

        <input
          ref={inputRef}
          type="file"
          accept="image/jpeg,image/png,image/webp"
          className="hidden"
          disabled={disabled || uploading}
          onChange={(event) => void chooseFile(event.target.files?.[0])}
        />

        <div className="mt-3 flex flex-wrap gap-2">
          <Button
            type="button"
            size="sm"
            className="cta-primary rounded-xl"
            disabled={disabled || uploading}
            onClick={() => inputRef.current?.click()}
          >
            {uploading ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Uploading…
              </>
            ) : (
              <>
                <ImagePlus className="mr-2 h-4 w-4" />
                {previewUrl ? "Change photo" : "Upload photo"}
              </>
            )}
          </Button>

          {previewUrl && (
            <Button
              type="button"
              size="sm"
              variant="outline"
              className="rounded-xl"
              disabled={disabled || uploading}
              onClick={() => void clearAvatar()}
            >
              <Trash2 className="mr-2 h-4 w-4" />
              Remove
            </Button>
          )}
        </div>

        {error && (
          <p role="alert" className="mt-2 text-xs text-destructive">
            {error}
          </p>
        )}
      </div>
    </section>
  );
}