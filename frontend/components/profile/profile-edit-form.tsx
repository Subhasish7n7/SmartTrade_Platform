"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import {
  ArrowLeft,
  Loader2,
  Save,
  ShieldCheck,
} from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { AvatarUploadField } from "@/components/profile/AvatarUploadField";
import {
  deleteImageByToken,
  type CloudinaryUploadResult,
} from "@/lib/api/cloudinary";
import { updateMyProfile } from "@/lib/api/profile/client";
import type { MyUserProfile } from "@/lib/types/api/profile";

interface Props {
  initialProfile: MyUserProfile;
}

type AvatarRequest = Pick<
  CloudinaryUploadResult,
  "secureUrl" | "publicId" | "assetId" | "width" | "height" | "format" | "bytes"
>;

export function ProfileEditForm({ initialProfile }: Props) {
  const router = useRouter();

  const [name, setName] = useState(initialProfile.name ?? "");
  const [phoneNo, setPhoneNo] = useState(initialProfile.phoneNo ?? "");
  const [uploadedAvatar, setUploadedAvatar] =
    useState<CloudinaryUploadResult | null>(null);
  const [clearAvatar, setClearAvatar] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    const normalizedName = name.trim();

    if (normalizedName.length < 2) {
      setError("Your name must contain at least 2 characters.");
      return;
    }

    if (normalizedName.length > 100) {
      setError("Your name must be 100 characters or fewer.");
      return;
    }

    setSaving(true);

    try {
      const avatar: AvatarRequest | undefined = uploadedAvatar
        ? {
            secureUrl: uploadedAvatar.secureUrl,
            publicId: uploadedAvatar.publicId,
            assetId: uploadedAvatar.assetId,
            width: uploadedAvatar.width,
            height: uploadedAvatar.height,
            format: uploadedAvatar.format,
            bytes: uploadedAvatar.bytes,
          }
        : undefined;

      await updateMyProfile({
        name: normalizedName,

        // A blank phone number retains the existing backend behavior:
        // omit the field instead of claiming to clear the stored value.
        ...(phoneNo.trim() ? { phoneNo: phoneNo.trim() } : {}),

        ...(avatar ? { avatar } : {}),
        clearAvatar: clearAvatar && !avatar,
      });

      router.push("/profile");
      router.refresh();
    } catch (saveError) {
      // A newly uploaded image that was not saved to the profile is temporary.
      if (uploadedAvatar?.deleteToken) {
        try {
          await deleteImageByToken(uploadedAvatar.deleteToken);
        } catch (cleanupError) {
          console.error(
            "Failed to remove an unsaved avatar upload",
            cleanupError,
          );
        } finally {
          // Do not retain metadata for an upload that may have been deleted.
          setUploadedAvatar(null);
        }
      }

      setError(
        saveError instanceof Error
          ? saveError.message
          : "Profile could not be saved. Please try again.",
      );
    } finally {
      setSaving(false);
    }
  }

  return (
    <main className="mx-auto min-h-screen w-full max-w-4xl px-4 pb-16 pt-20 sm:px-6 lg:px-8">
      <button
        type="button"
        onClick={() => router.push("/profile")}
        className="mb-4 inline-flex items-center gap-2 rounded-lg px-2 py-1.5 text-sm text-muted-foreground transition hover:bg-white/[0.025] hover:text-foreground"
      >
        <ArrowLeft className="h-4 w-4" />
        Back to profile
      </button>

      <section className="content-surface !p-0">
        <header className="border-b border-border/50 px-5 py-5 sm:px-7 sm:py-6">
          <p className="text-[11px] font-semibold uppercase tracking-[0.16em] text-primary">
            Account profile
          </p>

          <h1 className="mt-2 text-2xl font-bold tracking-tight sm:text-3xl">
            Edit your profile
          </h1>

          <p className="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground">
            Keep your trader profile up to date. Your email and trust score
            cannot be changed here.
          </p>
        </header>

        <form onSubmit={handleSubmit} className="space-y-5 p-5 sm:p-7">
          <AvatarUploadField
            currentAvatarUrl={
              clearAvatar ? null : initialProfile.avatarUrl ?? null
            }
            fallbackName={name}
            uploadedAvatar={uploadedAvatar}
            onUploaded={(avatar) => {
              setUploadedAvatar(avatar);
              setClearAvatar(false);
            }}
            onClearUploaded={() => setUploadedAvatar(null)}
            onRemoveCurrent={() => setClearAvatar(true)}
            disabled={saving}
          />

          <div className="grid gap-4 sm:grid-cols-2">
            <label className="block min-w-0 space-y-2">
              <span className="text-sm font-medium">Display name</span>

              <Input
                value={name}
                onChange={(event) => setName(event.target.value)}
                maxLength={100}
                required
                disabled={saving}
                autoComplete="name"
                className="h-11 rounded-xl border-border/70 bg-background/40"
              />

              <span className="block text-xs text-muted-foreground">
                This name is visible to other traders.
              </span>
            </label>

            <label className="block min-w-0 space-y-2">
              <span className="text-sm font-medium">Phone number</span>

              <Input
                value={phoneNo}
                onChange={(event) => setPhoneNo(event.target.value)}
                maxLength={30}
                disabled={saving}
                autoComplete="tel"
                inputMode="tel"
                className="h-11 rounded-xl border-border/70 bg-background/40"
              />

              <span className="block text-xs text-muted-foreground">
                Leave blank to keep the current value unchanged.
              </span>
            </label>
          </div>

          <div className="flex items-start gap-3 rounded-xl border border-border/60 bg-background/25 p-3.5 sm:items-center">
            <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg border border-border/60 bg-background/50 text-muted-foreground">
              <ShieldCheck className="h-4 w-4" />
            </span>

            <div className="min-w-0 flex-1">
              <p className="text-xs text-muted-foreground">Email address</p>
              <p className="mt-1 break-all text-sm font-medium">
                {initialProfile.email}
              </p>
            </div>

            <span className="shrink-0 rounded-full border border-border/60 px-2 py-1 text-[10px] font-medium text-muted-foreground">
              Read-only
            </span>
          </div>

          {error && (
            <p
              role="alert"
              className="rounded-xl border border-destructive/30 bg-destructive/5 p-3 text-sm text-destructive"
            >
              {error}
            </p>
          )}

          <footer className="flex flex-wrap justify-end gap-2.5 border-t border-border/50 pt-4">
            <Button
              type="button"
              variant="outline"
              className="h-10 rounded-xl"
              disabled={saving}
              onClick={() => router.push("/profile")}
            >
              Cancel
            </Button>

            <Button
              type="submit"
              className="cta-primary h-10 min-w-36 rounded-xl px-4"
              disabled={saving}
            >
              {saving ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Saving…
                </>
              ) : (
                <>
                  <Save className="mr-2 h-4 w-4" />
                  Save profile
                </>
              )}
            </Button>
          </footer>
        </form>
      </section>
    </main>
  );
}