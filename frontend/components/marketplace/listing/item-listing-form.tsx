"use client"

import { motion } from "framer-motion"
import { ArrowLeftRight, Check, CircleDollarSign, FileText, Info, PackagePlus, Plus, Sparkles, Tag, X } from "lucide-react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { useMemo, useState } from "react"

import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Textarea } from "@/components/ui/textarea"
import { createItem, updateItem } from "@/lib/api/items/client"
import {
  ITEM_CATEGORIES,
  ITEM_CONDITIONS,
  type ItemCategory,
  type ItemCondition,
  type ItemImageDraft,
  type ItemImageRequest,
  type ItemResponse,
} from "@/lib/types/api/item"
import { ImageUploadGrid } from "./image-upload-grid"
import { ListingPreviewCard } from "./listing-preview-card"
import { LocationPicker } from "./location-picker"

interface Props {
  initialItem?: ItemResponse
}

type FormState = {
  itemName: string
  userPrice: string
  category: ItemCategory | ""
  condition: ItemCondition | ""
  customCategory: string
  description: string
  labels: string[]
  latitude?: number
  longitude?: number
  city: string
  state: string
  locality: string
  clearCoordinates: boolean
  forTrade: boolean
  forSale: boolean
}

function getInitialForm(item?: ItemResponse): FormState {
  return {
    itemName: item?.itemName ?? "",
    userPrice: item ? String(item.userPrice ?? "") : "",
    category: item?.category ?? "",
    condition: item?.condition ?? "",
    customCategory: item?.customCategory ?? "",
    description: item?.description ?? "",
    labels: item?.labels ?? [],
    latitude: item?.latitude ?? undefined,
    longitude: item?.longitude ?? undefined,
    city: item?.city ?? "",
    state: item?.state ?? "",
    locality: item?.locality ?? "",
    clearCoordinates: false,
    forTrade: item?.forTrade ?? true,
    forSale: item?.forSale ?? false,
  }
}
function buildImageRequests(images: ItemImageDraft[]): ItemImageRequest[] {
  return images.map((image, index) => ({
    id: image.id,
    secureUrl: image.secureUrl,
    publicId: image.publicId,
    assetId: image.assetId,
    width: image.width,
    height: image.height,
    format: image.format,
    bytes: image.bytes,
    sortOrder: index,
  }))
}

export function ItemListingForm({ initialItem }: Props) {
  const router = useRouter()
  const isEdit = Boolean(initialItem)

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [labelInput, setLabelInput] = useState("")
  const [saved, setSaved] = useState(false)
  const [form, setForm] = useState<FormState>(() => getInitialForm(initialItem))

  const [images, setImages] = useState<ItemImageDraft[]>(
    initialItem?.images.map((image) => ({
      ...image,
      deleteToken: null,
    })) ?? [],
  )

  const [removedImageIds, setRemovedImageIds] = useState<number[]>([])

  const price = Number(form.userPrice) || 0
  const descriptionLength = form.description.length
  const validMode = form.forSale || form.forTrade

  const detailsComplete = Boolean(
    form.itemName.trim() && form.category && form.condition && (form.category !== "OTHER" || form.customCategory.trim()),
  )

  const canSubmit = Boolean(detailsComplete && images.length && price > 0 && validMode)

  const progress = useMemo(() => {
    let done = 0

    if (images.length) done++
    if (detailsComplete) done++
    if (price > 0 && validMode) done++
    if (form.description.trim()) done++

    return Math.round((done / 4) * 100)
  }, [detailsComplete, form.description, images.length, price, validMode])

  function setField<K extends keyof FormState>(key: K, value: FormState[K]) {
    setForm((current) => ({ ...current, [key]: value }))
    setError(null)
    setSaved(false)
  }

  function setMode(mode: "trade" | "sale" | "both") {
    setForm((current) => ({
      ...current,
      forTrade: mode === "trade" || mode === "both",
      forSale: mode === "sale" || mode === "both",
    }))
    setError(null)
    setSaved(false)
  }

  function addTag() {
    const tag = labelInput.trim().replace(/^#/, "").replace(/\s+/g, "-")

    if (!tag || form.labels.includes(tag) || form.labels.length >= 8) return

    setField("labels", [...form.labels, tag])
    setLabelInput("")
  }

  function removeTag(tag: string) {
    setField(
      "labels",
      form.labels.filter((label) => label !== tag),
    )
  }

  function handleExistingImageRemoved(imageId: number) {
    setRemovedImageIds((current) => (current.includes(imageId) ? current : [...current, imageId]))

    setError(null)
    setSaved(false)
  }

  function handleImagesChange(nextImages: ItemImageDraft[]) {
    setImages(nextImages)
    setError(null)
    setSaved(false)
  }

  async function handleSubmit() {
    setError(null)
    setSaved(false)

    if (!form.itemName.trim()) return setError("Add an item name.")
    if (!form.category) return setError("Choose a category.")
    if (form.category === "OTHER" && !form.customCategory.trim()) {
      return setError("Describe the custom category.")
    }
    if (!form.condition) return setError("Choose the item's condition.")
    if (!images.length) return setError("Add at least one photo.")
    if (!price || price <= 0) return setError("Enter a valid item value.")
    if (!validMode) {
      return setError("Choose whether this item is available for sale, trade, or both.")
    }

    const imageRequests = buildImageRequests(images)

    const commonPayload = {
      itemName: form.itemName.trim(),
      userPrice: price,
      description: form.description.trim(),
      category: form.category,
      condition: form.condition,
      customCategory: form.category === "OTHER" ? form.customCategory.trim() : undefined,
      labels: form.labels,
      images: imageRequests,
      latitude: form.latitude,
      longitude: form.longitude,
      city: form.city || undefined,
      state: form.state || undefined,
      locality: form.locality || undefined,
      clearCoordinates: isEdit ? form.clearCoordinates : undefined,
      forTrade: form.forTrade,
      forSale: form.forSale,
    }

    try {
      setLoading(true)

      let saved: ItemResponse

      if (initialItem) {
        saved = await updateItem(initialItem.itemId, {
          ...commonPayload,
          removedImageIds,
        })
      } else {
        saved = await createItem(commonPayload)
      }

      setImages(
        saved.images.map((image) => ({
          ...image,
          deleteToken: null,
        })),
      )

      setRemovedImageIds([])
      setSaved(true)

      window.setTimeout(() => {
        router.push("/inventory")
        router.refresh()
      }, 450)
    } catch (err) {
      setError(err instanceof Error ? err.message : isEdit ? "We couldn't save your listing." : "We couldn't publish the listing.")
    } finally {
      setLoading(false)
    }
  }

  const actionLabel = loading
    ? isEdit
      ? "Saving changes…"
      : "Publishing listing…"
    : saved
      ? isEdit
        ? "Changes saved"
        : "Listing published"
      : isEdit
        ? "Save changes"
        : "Publish listing"

  return (
    <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_390px]">
      <div className="min-w-0 space-y-5">
        <FormSection
          eyebrow="01"
          icon={<PackagePlus className="h-4 w-4" />}
          title="Show it well"
          description="Make the first impression count. Your cover photo is what traders notice first."
        >
          <ImageUploadGrid value={images} onChange={handleImagesChange} onExistingImageRemoved={handleExistingImageRemoved} />
        </FormSection>

        <FormSection
          eyebrow="02"
          icon={<Tag className="h-4 w-4" />}
          title="Describe the item"
          description="Keep the structured details precise so traders can compare listings quickly."
        >
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="sm:col-span-2">
              <FieldLabel label="Item name" hint={`${form.itemName.length}/100`} />
              <Input
                value={form.itemName}
                onChange={(event) => setField("itemName", event.target.value)}
                placeholder="e.g. Sony WH-1000XM5"
                className="mt-2 h-11 rounded-xl"
                maxLength={100}
              />
            </div>

            <div>
              <FieldLabel label="Category" />
              <Select value={form.category} onValueChange={(value) => setField("category", value as ItemCategory)}>
                <SelectTrigger className="mt-2 h-11 rounded-xl">
                  <SelectValue placeholder="Select category" />
                </SelectTrigger>
                <SelectContent>
                  {ITEM_CATEGORIES.map((category) => (
                    <SelectItem key={category} value={category}>
                      {formatEnum(category)}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div>
              <FieldLabel label="Condition" />
              <Select value={form.condition} onValueChange={(value) => setField("condition", value as ItemCondition)}>
                <SelectTrigger className="mt-2 h-11 rounded-xl">
                  <SelectValue placeholder="Select condition" />
                </SelectTrigger>
                <SelectContent>
                  {ITEM_CONDITIONS.map((condition) => (
                    <SelectItem key={condition} value={condition}>
                      {formatEnum(condition)}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {form.category === "OTHER" && (
              <div className="sm:col-span-2 rounded-2xl border border-[var(--premium-gold)]/20 bg-[var(--premium-gold)]/[0.045] p-4 shadow-[0_12px_30px_rgba(250,204,21,.03)]">
                <FieldLabel label="Custom category" hint={`${form.customCategory.length}/60`} />
                <Input
                  value={form.customCategory}
                  onChange={(event) => setField("customCategory", event.target.value)}
                  placeholder="e.g. Studio Equipment"
                  className="mt-2 h-11 rounded-xl"
                  maxLength={60}
                />
              </div>
            )}
          </div>
        </FormSection>

        <FormSection
          eyebrow="03"
          icon={<CircleDollarSign className="h-4 w-4" />}
          title="Set your terms"
          description="Decide how you want other traders to approach this listing."
        >
          <div className="grid gap-5 xl:grid-cols-[1fr_1.15fr]">
            <div>
              <FieldLabel label="Expected value" hint="Your asking/trade value in INR." />

              <div className="relative mt-2">
                <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-sm font-semibold text-muted-foreground">₹</span>
                <Input
                  type="number"
                  min={1}
                  step={1}
                  inputMode="numeric"
                  value={form.userPrice}
                  onChange={(event) => setField("userPrice", event.target.value)}
                  placeholder="0"
                  className="h-11 rounded-xl pl-8"
                />
              </div>

              <div className="mt-3 rounded-2xl border border-[var(--trust-blue)]/10 bg-[var(--trust-blue)]/[0.025] p-3.5">
                <div className="flex items-center gap-2 text-xs font-medium">
                  <span className="flex h-6 w-6 items-center justify-center rounded-lg border border-[var(--premium-gold)]/15 bg-[var(--premium-gold)]/[0.05]">
                    <Sparkles className="h-3.5 w-3.5 text-[var(--premium-gold)]" />
                  </span>
                  Smart value note
                </div>
                <p className="mt-2 text-xs leading-5 text-muted-foreground">
                  Smart Trade will generate an estimated market value after the listing is processed. Your value remains the amount you
                  control.
                </p>
              </div>
            </div>

            <div>
              <FieldLabel label="Listing mode" hint="At least one option must be enabled." />

              <div className="mt-2 grid gap-2 sm:grid-cols-3 xl:grid-cols-1 2xl:grid-cols-3">
                <ModeButton
                  active={form.forTrade && !form.forSale}
                  icon={<ArrowLeftRight className="h-4 w-4" />}
                  title="Trade"
                  description="Receive offers"
                  onClick={() => setMode("trade")}
                />

                <ModeButton
                  active={form.forSale && !form.forTrade}
                  icon={<CircleDollarSign className="h-4 w-4" />}
                  title="Sale"
                  description="Sell directly"
                  tone="blue"
                  onClick={() => setMode("sale")}
                />

                <ModeButton
                  active={form.forSale && form.forTrade}
                  icon={<Sparkles className="h-4 w-4" />}
                  title="Both"
                  description="Maximum reach"
                  tone="gold"
                  onClick={() => setMode("both")}
                />
              </div>
            </div>
          </div>
        </FormSection>

        <FormSection
          eyebrow="04"
          icon={<FileText className="h-4 w-4" />}
          title="Add context"
          description="Describe what is included, what condition it is really in, and anything a trader should know."
        >
          <div>
            <Textarea
              value={form.description}
              onChange={(event) => setField("description", event.target.value)}
              placeholder="Tell traders about the item's condition, included accessories, purchase history, defects, or anything else relevant…"
              rows={7}
              maxLength={1200}
              className="resize-none rounded-2xl leading-6"
            />
            <div className="mt-2 flex justify-end text-[11px] text-muted-foreground">{descriptionLength}/1200</div>
          </div>

          <div className="mt-4">
            <FieldLabel label="Labels" hint={`${form.labels.length}/8 searchable labels`} />

            <div className="mt-2 flex gap-2">
              <Input
                value={labelInput}
                onChange={(event) => setLabelInput(event.target.value)}
                onKeyDown={(event) => {
                  if (event.key === "Enter") {
                    event.preventDefault()
                    addTag()
                  }
                }}
                placeholder="e.g. gaming"
                className="h-10 rounded-xl"
              />

              <Button
                type="button"
                variant="surface"
                className="marketplace-action-secondary h-10 rounded-xl px-4 font-semibold"
                onClick={addTag}
                disabled={form.labels.length >= 8}
              >
                <Plus className="mr-1.5 h-4 w-4" />
                Add
              </Button>
            </div>

            {form.labels.length > 0 && (
              <div className="mt-3 flex flex-wrap gap-2">
                {form.labels.map((label) => (
                  <Badge key={label} variant="secondary" className="gap-1 border border-white/[0.06] bg-white/[0.03] py-1.5 pl-2.5 pr-1.5">
                    #{label}
                    <button
                      type="button"
                      onClick={() => removeTag(label)}
                      className="ml-0.5 rounded-full p-0.5 transition hover:bg-background/40"
                      aria-label={`Remove ${label}`}
                    >
                      <X className="h-3 w-3" />
                    </button>
                  </Badge>
                ))}
              </div>
            )}
          </div>
        </FormSection>

        <LocationPicker
          value={{
            latitude: form.latitude,
            longitude: form.longitude,
            city: form.city,
            state: form.state,
            locality: form.locality,
            clearCoordinates: form.clearCoordinates,
          }}
          onChange={(location) =>
            setForm((current) => ({
              ...current,
              ...location,
            }))
          }
        />
      </div>

      <aside className="min-w-0 lg:sticky lg:top-24 lg:self-start">
        <div className="space-y-4">
          <ListingPreviewCard form={form} images={images} isEdit={isEdit} />

          <div className="marketplace-readiness p-5">
            <div className="pointer-events-none absolute right-[-30px] top-[-45px] h-32 w-32 rounded-full bg-primary/7 blur-3xl" />

            <div className="relative">
              <div className="flex items-center justify-between gap-3">
                <div>
                  <p className="text-xs font-semibold uppercase tracking-[0.15em] text-muted-foreground">Readiness</p>
                  <p className="mt-1 text-sm font-medium">
                    {progress === 100
                      ? isEdit
                        ? "Ready to save · listing complete"
                        : "Ready to publish · listing complete"
                      : canSubmit
                        ? "Ready to publish · optional details remain"
                        : "Required information remains"}
                  </p>
                </div>

                <div className="text-xl font-bold text-primary">{progress}%</div>
              </div>

              <div className="marketplace-readiness-bar mt-3">
                <div className="marketplace-readiness-fill" style={{ width: `${progress}%` }} />
              </div>
            </div>

            <div className="relative mt-4 space-y-2 text-xs text-muted-foreground">
              <ChecklistItem done={Boolean(images.length)} text="Add at least one photo" />
              <ChecklistItem done={detailsComplete} text="Complete item details" />
              <ChecklistItem done={price > 0 && validMode} text="Set value and listing mode" />
              <ChecklistItem done={Boolean(form.description.trim())} text="Write a useful description" optional />
            </div>

            {error && (
              <div className="mt-4 rounded-xl border border-destructive/20 bg-destructive/5 p-3 text-xs leading-5 text-destructive">
                {error}
              </div>
            )}

            {saved && (
              <div className="mt-4 rounded-xl border border-emerald-400/20 bg-emerald-400/[0.05] p-3 text-xs leading-5 text-emerald-300">
                {isEdit
                  ? "Your listing changes were saved. Returning to inventory…"
                  : "Listing published successfully. Opening your inventory…"}
              </div>
            )}

            <Button
              className="relative mt-5 h-11 w-full rounded-xl cta-primary"
              onClick={handleSubmit}
              disabled={loading || saved || !canSubmit}
            >
              {actionLabel}
              {saved ? <Check className="ml-2 h-4 w-4" /> : loading ? null : <PackagePlus className="ml-2 h-4 w-4" />}
            </Button>

            {!canSubmit && !loading && !saved && (
              <p className="mt-3 text-center text-[11px] text-muted-foreground">Complete the required fields above to publish.</p>
            )}

            {!validMode && <p className="mt-1 text-center text-[11px] text-amber-300">Choose Trade, Sale, or Both.</p>}

            {isEdit && (
              <Button asChild variant="surface" className="marketplace-action-secondary mt-2 h-10 w-full rounded-xl font-semibold">
                <Link href={`/item/${initialItem?.itemId}`}>
                  <Sparkles className="mr-2 h-3.5 w-3.5" />
                  View current listing
                </Link>
              </Button>
            )}
          </div>

          <div className="rounded-2xl border border-white/[0.06] bg-white/[0.015] px-4 py-3 text-xs leading-5 text-muted-foreground">
            <div className="flex gap-2">
              <Info className="mt-0.5 h-3.5 w-3.5 shrink-0 text-[var(--chat-cyan)]" />
              <span>
                {isEdit
                  ? "Only the listing details you change here are updated. Availability remains a separate inventory control."
                  : "You control the listing details. Ownership is resolved by your authenticated account on the backend."}
              </span>
            </div>
          </div>
        </div>
      </aside>
    </div>
  )
}

function FormSection({
  eyebrow,
  icon,
  title,
  description,
  children,
}: {
  eyebrow: string
  icon: React.ReactNode
  title: string
  description: string
  children: React.ReactNode
}) {
  return (
    <motion.section initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} className="marketplace-form-section">
      <div className="marketplace-form-section-header">
        <div className="flex items-start gap-3">
          <div className="marketplace-section-icon">{icon}</div>

          <div className="min-w-0">
            <div className="flex items-center gap-2">
              <span className="marketplace-section-eyebrow">{eyebrow}</span>
              <h2 className="marketplace-section-title">{title}</h2>
            </div>

            <p className="marketplace-section-copy">{description}</p>
          </div>
        </div>
      </div>

      <div className="marketplace-form-section-body">{children}</div>
    </motion.section>
  )
}

function FieldLabel({ label, hint }: { label: string; hint?: string }) {
  return (
    <div className="flex items-baseline justify-between gap-3">
      <span className="text-sm font-medium">{label}</span>
      {hint && <span className="text-[11px] text-muted-foreground">{hint}</span>}
    </div>
  )
}

function ModeButton({
  active,
  icon,
  title,
  description,
  tone = "violet",
  onClick,
}: {
  active: boolean
  icon: React.ReactNode
  title: string
  description: string
  tone?: "violet" | "blue" | "gold"
  onClick: () => void
}) {
  const toneClass = {
    violet:
      "text-[var(--negotiation-violet)] border-[var(--negotiation-violet)]/30 bg-[var(--negotiation-violet)]/[0.07] shadow-[0_12px_30px_rgba(168,85,247,.06)]",
    blue: "text-[var(--trust-blue)] border-[var(--trust-blue)]/30 bg-[var(--trust-blue)]/[0.07] shadow-[0_12px_30px_rgba(59,130,246,.06)]",
    gold: "text-[var(--premium-gold)] border-[var(--premium-gold)]/30 bg-[var(--premium-gold)]/[0.07] shadow-[0_12px_30px_rgba(250,204,21,.05)]",
  }[tone]

  return (
    <button
      type="button"
      onClick={onClick}
      className={`flex items-center gap-3 rounded-2xl border p-3 text-left transition duration-200 ${
        active ? toneClass : "border-white/[0.055] bg-white/[0.015] hover:border-white/[0.10] hover:bg-white/[0.025]"
      }`}
    >
      <div
        className={`flex h-9 w-9 items-center justify-center rounded-xl border ${
          active ? "border-current/20 bg-black/10" : "border-white/[0.06] bg-background/30 text-muted-foreground"
        }`}
      >
        {icon}
      </div>

      <div className="min-w-0">
        <p className={`text-sm font-medium ${active ? "text-foreground" : "text-muted-foreground"}`}>{title}</p>
        <p className="text-[11px] text-muted-foreground">{description}</p>
      </div>

      {active && <Check className="ml-auto h-4 w-4 shrink-0" />}
    </button>
  )
}

function ChecklistItem({ done, text, optional }: { done: boolean; text: string; optional?: boolean }) {
  return (
    <div className="flex items-center gap-2">
      <span
        className={`flex h-4 w-4 items-center justify-center rounded-full border ${
          done ? "border-primary/30 bg-primary/10 text-primary" : "border-white/[0.07] bg-white/[0.02] text-transparent"
        }`}
      >
        <Check className="h-2.5 w-2.5" />
      </span>

      <span className={done ? "text-foreground" : ""}>
        {text}
        {optional && !done ? " · optional" : ""}
      </span>
    </div>
  )
}

function formatEnum(value: string) {
  return value
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (letter) => letter.toUpperCase())
}
