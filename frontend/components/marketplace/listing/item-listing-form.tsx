"use client"

import Link from "next/link"
import { useMemo, useState } from "react"
import { motion } from "framer-motion"
import {
  ArrowLeftRight,
  Check,
  CircleDollarSign,
  FileText,
  Info,
  MapPin,
  PackagePlus,
  Plus,
  Sparkles,
  Tag,
  X,
} from "lucide-react"
import { useRouter } from "next/navigation"

import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { createItem } from "@/lib/api/items/client"
import { ITEM_CATEGORIES, ITEM_CONDITIONS, type ItemCategory, type ItemCondition } from "@/lib/types/api/item"

import { ImageUploadGrid } from "./image-upload-grid"
import { ListingPreviewCard } from "./listing-preview-card"
import { LocationPicker } from "./location-picker"

export function ItemListingForm() {
  const router = useRouter()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [labelInput, setLabelInput] = useState("")
  const [published, setPublished] = useState(false)

  const [form, setForm] = useState({
    itemName: "",
    userPrice: "",
    category: "" as ItemCategory | "",
    condition: "" as ItemCondition | "",
    customCategory: "",
    description: "",
    labels: [] as string[],
    imageUrls: [] as string[],
    latitude: undefined as number | undefined,
    longitude: undefined as number | undefined,
    city: "",
    state: "",
    locality: "",
    forTrade: true,
    forSale: false,
  })

  const price = Number(form.userPrice) || 0
  const descriptionLength = form.description.length
  const validMode = form.forSale || form.forTrade
  const canPublish = Boolean(
    form.itemName.trim() &&
      form.category &&
      form.condition &&
      form.imageUrls.length &&
      price > 0 &&
      validMode &&
      (form.category !== "OTHER" || form.customCategory.trim()),
  )

  const progress = useMemo(() => {
    let done = 0
    if (form.imageUrls.length) done++
    if (form.itemName.trim() && form.category && form.condition) done++
    if (price > 0 && validMode) done++
    if (form.description.trim()) done++
    return Math.round((done / 4) * 100)
  }, [form.category, form.condition, form.description, form.imageUrls.length, form.itemName, price, validMode])

  function setField<K extends keyof typeof form>(key: K, value: (typeof form)[K]) {
    setForm((current) => ({ ...current, [key]: value }))
    setError(null)
    setPublished(false)
  }

  function addTag() {
    const tag = labelInput.trim().replace(/^#/, "")
    if (!tag || form.labels.includes(tag) || form.labels.length >= 8) return

    setField("labels", [...form.labels, tag])
    setLabelInput("")
  }

  function removeTag(tag: string) {
    setField("labels", form.labels.filter((label) => label !== tag))
  }

  async function handleSubmit() {
    setError(null)
    setPublished(false)

    if (!form.itemName.trim()) return setError("Add an item name.")
    if (!form.category) return setError("Choose a category.")
    if (form.category === "OTHER" && !form.customCategory.trim()) return setError("Describe the custom category.")
    if (!form.condition) return setError("Choose the item's condition.")
    if (!form.imageUrls.length) return setError("Add at least one photo.")
    if (!price || price <= 0) return setError("Enter a valid item value.")
    if (!validMode) return setError("Choose whether this item is available for sale, trade, or both.")

    try {
      setLoading(true)
      await createItem({
        itemName: form.itemName.trim(),
        userPrice: price,
        description: form.description.trim(),
        category: form.category,
        condition: form.condition,
        customCategory: form.category === "OTHER" ? form.customCategory.trim() : undefined,
        labels: form.labels,
        imageUrls: form.imageUrls,
        latitude: form.latitude,
        longitude: form.longitude,
        city: form.city || undefined,
        state: form.state || undefined,
        locality: form.locality || undefined,
        forTrade: form.forTrade,
        forSale: form.forSale,
      })
      setPublished(true)
    } catch (err) {
      setError(err instanceof Error ? err.message : "We couldn't publish the listing.")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_390px]">
      <div className="min-w-0 space-y-5">
        <FormSection
          eyebrow="01"
          icon={<PackagePlus className="h-4 w-4" />}
          title="Show it well"
          description="A strong first photo does more for a listing than a wall of text."
        >
          <ImageUploadGrid value={form.imageUrls} onChange={(urls) => setField("imageUrls", urls)} />
        </FormSection>

        <FormSection
          eyebrow="02"
          icon={<Tag className="h-4 w-4" />}
          title="Describe the item"
          description="Keep the structured details precise so traders can compare listings quickly."
        >
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="sm:col-span-2">
              <FieldLabel label="Item name" hint="Use the recognizable product name." />
              <Input value={form.itemName} onChange={(e) => setField("itemName", e.target.value)} placeholder="e.g. Sony WH-1000XM5" className="mt-2 h-11 rounded-xl" maxLength={100} />
            </div>

            <div>
              <FieldLabel label="Category" />
              <Select value={form.category} onValueChange={(value) => setField("category", value as ItemCategory)}>
                <SelectTrigger className="mt-2 h-11 rounded-xl">
                  <SelectValue placeholder="Select category" />
                </SelectTrigger>
                <SelectContent>
                  {ITEM_CATEGORIES.map((category) => (
                    <SelectItem key={category} value={category}>{formatEnum(category)}</SelectItem>
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
                    <SelectItem key={condition} value={condition}>{formatEnum(condition)}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {form.category === "OTHER" && (
              <div className="sm:col-span-2 rounded-2xl border border-[var(--premium-gold)]/20 bg-[var(--premium-gold)]/5 p-4">
                <FieldLabel label="Custom category" hint="Required when using Other." />
                <Input value={form.customCategory} onChange={(e) => setField("customCategory", e.target.value)} placeholder="e.g. Studio Equipment" className="mt-2 h-11 rounded-xl" maxLength={60} />
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
                <Input type="number" min={1} value={form.userPrice} onChange={(e) => setField("userPrice", e.target.value)} placeholder="0" className="h-11 rounded-xl pl-8" />
              </div>
              <div className="mt-3 rounded-2xl border border-border/40 bg-white/[0.018] p-3">
                <div className="flex items-center gap-2 text-xs font-medium"><Sparkles className="h-3.5 w-3.5 text-primary" />Smart value note</div>
                <p className="mt-1.5 text-xs leading-5 text-muted-foreground">TradeVault will generate an estimated market value after the listing is processed. Your value remains the amount you control.</p>
              </div>
            </div>

            <div>
              <FieldLabel label="Listing mode" hint="At least one option must be enabled." />
              <div className="mt-2 grid gap-2 sm:grid-cols-3 xl:grid-cols-1 2xl:grid-cols-3">
                <ModeButton active={form.forTrade && !form.forSale} icon={<ArrowLeftRight className="h-4 w-4" />} title="Trade" description="Receive offers" onClick={() => { setField("forTrade", true); setField("forSale", false) }} />
                <ModeButton active={form.forSale && !form.forTrade} icon={<CircleDollarSign className="h-4 w-4" />} title="Sale" description="Sell directly" tone="blue" onClick={() => { setField("forTrade", false); setField("forSale", true) }} />
                <ModeButton active={form.forSale && form.forTrade} icon={<Sparkles className="h-4 w-4" />} title="Both" description="Maximum reach" tone="gold" onClick={() => { setField("forTrade", true); setField("forSale", true) }} />
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
            <Textarea value={form.description} onChange={(e) => setField("description", e.target.value)} placeholder="Tell traders about the item's condition, included accessories, purchase history, defects, or anything else relevant…" rows={7} maxLength={1200} className="resize-none rounded-2xl leading-6" />
            <div className="mt-2 flex justify-end text-[11px] text-muted-foreground">{descriptionLength}/1200</div>
          </div>

          <div className="mt-4">
            <FieldLabel label="Labels" hint="Optional — up to 8 searchable labels." />
            <div className="mt-2 flex gap-2">
              <Input value={labelInput} onChange={(e) => setLabelInput(e.target.value)} onKeyDown={(e) => { if (e.key === "Enter") { e.preventDefault(); addTag() } }} placeholder="e.g. gaming" className="h-10 rounded-xl" />
              <Button type="button" variant="surface" className="h-10 rounded-xl px-4" onClick={addTag} disabled={form.labels.length >= 8}>
                <Plus className="mr-1.5 h-4 w-4" />Add
              </Button>
            </div>
            {form.labels.length > 0 && (
              <div className="mt-3 flex flex-wrap gap-2">
                {form.labels.map((label) => (
                  <Badge key={label} variant="secondary" className="gap-1 border border-border/40 bg-white/[0.03] py-1.5 pl-2.5 pr-1.5">
                    #{label}
                    <button type="button" onClick={() => removeTag(label)} className="ml-0.5 rounded-full p-0.5 hover:bg-background/40"><X className="h-3 w-3" /></button>
                  </Badge>
                ))}
              </div>
            )}
          </div>
        </FormSection>

        <LocationPicker
          value={{ latitude: form.latitude, longitude: form.longitude, city: form.city, state: form.state, locality: form.locality }}
          onChange={(location) => setForm((current) => ({ ...current, ...location }))}
        />
      </div>

      <aside className="min-w-0 lg:sticky lg:top-24 lg:self-start">
        <div className="space-y-4">
          <ListingPreviewCard form={form} />

          <div className="content-surface relative overflow-hidden p-5">
            <div className="pointer-events-none absolute right-[-30px] top-[-45px] h-32 w-32 rounded-full bg-primary/7 blur-3xl" />
            <div className="relative">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-xs font-semibold uppercase tracking-[0.15em] text-muted-foreground">Readiness</p>
                  <p className="mt-1 text-sm font-medium">{progress === 100 ? "Ready to publish" : "A few details left"}</p>
                </div>
                <div className="text-xl font-bold text-primary">{progress}%</div>
              </div>
              <div className="mt-3 h-1.5 overflow-hidden rounded-full bg-white/[0.06]"><div className="h-full rounded-full bg-gradient-to-r from-primary via-[var(--trust-blue)] to-[var(--negotiation-violet)] transition-all duration-300" style={{ width: `${progress}%` }} /></div>
            </div>

            <div className="relative mt-4 space-y-2 text-xs text-muted-foreground">
              <ChecklistItem done={Boolean(form.imageUrls.length)} text="Add at least one photo" />
              <ChecklistItem done={Boolean(form.itemName.trim() && form.category && form.condition && (form.category !== "OTHER" || form.customCategory.trim()))} text="Complete item details" />
              <ChecklistItem done={price > 0 && validMode} text="Set value and listing mode" />
              <ChecklistItem done={Boolean(form.description.trim())} text="Write a useful description" optional />
            </div>

            {error && <div className="mt-4 rounded-xl border border-destructive/20 bg-destructive/5 p-3 text-xs leading-5 text-destructive">{error}</div>}
            {published && (
              <div className="mt-4 rounded-xl border border-emerald-400/20 bg-emerald-400/5 p-3 text-xs leading-5 text-emerald-300">
                Listing published successfully. You can manage it from your inventory.
              </div>
            )}

            <Button className="relative mt-5 h-11 w-full rounded-xl cta-primary" onClick={handleSubmit} disabled={loading || !canPublish}>
              {loading ? "Publishing listing…" : published ? "Published" : "Publish listing"}
              {!loading && !published && <PackagePlus className="ml-2 h-4 w-4" />}
              {published && <Check className="ml-2 h-4 w-4" />}
            </Button>

            {published && (
              <Button asChild variant="surface" className="mt-2 h-10 w-full rounded-xl">
                <Link href="/inventory">Open inventory</Link>
              </Button>
            )}

            {!validMode && <p className="mt-3 text-center text-[11px] text-amber-300">Choose Trade, Sale, or Both before publishing.</p>}
          </div>

          <div className="rounded-2xl border border-border/40 bg-white/[0.015] px-4 py-3 text-xs leading-5 text-muted-foreground">
            <div className="flex gap-2"><Info className="mt-0.5 h-3.5 w-3.5 shrink-0 text-[var(--chat-cyan)]" /><span>You control the listing details. Ownership is resolved by your authenticated account on the backend.</span></div>
          </div>
        </div>
      </aside>
    </div>
  )
}

function FormSection({ eyebrow, icon, title, description, children }: { eyebrow: string; icon: React.ReactNode; title: string; description: string; children: React.ReactNode }) {
  return (
    <motion.section initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} className="content-surface overflow-hidden p-0">
      <div className="border-b border-border/40 px-5 py-4 sm:px-6">
        <div className="flex items-start gap-3">
          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl border border-primary/10 bg-primary/5 text-primary">{icon}</div>
          <div className="min-w-0">
            <div className="flex items-center gap-2"><span className="text-[10px] font-bold tracking-[0.18em] text-muted-foreground">{eyebrow}</span><h2 className="font-semibold tracking-tight">{title}</h2></div>
            <p className="mt-1 text-xs leading-5 text-muted-foreground">{description}</p>
          </div>
        </div>
      </div>
      <div className="p-5 sm:p-6">{children}</div>
    </motion.section>
  )
}

function FieldLabel({ label, hint }: { label: string; hint?: string }) {
  return <div className="flex items-baseline justify-between gap-3"><span className="text-sm font-medium">{label}</span>{hint && <span className="text-[11px] text-muted-foreground">{hint}</span>}</div>
}

function ModeButton({ active, icon, title, description, tone = "violet", onClick }: { active: boolean; icon: React.ReactNode; title: string; description: string; tone?: "violet" | "blue" | "gold"; onClick: () => void }) {
  const toneClass = { violet: "text-[var(--negotiation-violet)] border-[var(--negotiation-violet)]/30 bg-[var(--negotiation-violet)]/7", blue: "text-[var(--trust-blue)] border-[var(--trust-blue)]/30 bg-[var(--trust-blue)]/7", gold: "text-[var(--premium-gold)] border-[var(--premium-gold)]/30 bg-[var(--premium-gold)]/7" }[tone]
  return (
    <button type="button" onClick={onClick} className={`flex items-center gap-3 rounded-2xl border p-3 text-left transition ${active ? toneClass : "border-border/45 bg-white/[0.015] hover:border-border/75 hover:bg-white/[0.03]"}`}>
      <div className={`flex h-9 w-9 items-center justify-center rounded-xl border ${active ? "border-current/20 bg-black/10" : "border-border/45 bg-background/30 text-muted-foreground"}`}>{icon}</div>
      <div className="min-w-0"><p className={`text-sm font-medium ${active ? "text-foreground" : "text-muted-foreground"}`}>{title}</p><p className="text-[11px] text-muted-foreground">{description}</p></div>
      {active && <Check className="ml-auto h-4 w-4 shrink-0" />}
    </button>
  )
}

function ChecklistItem({ done, text, optional }: { done: boolean; text: string; optional?: boolean }) {
  return <div className="flex items-center gap-2"><span className={`flex h-4 w-4 items-center justify-center rounded-full border ${done ? "border-primary/30 bg-primary/10 text-primary" : "border-border/50 bg-white/[0.02] text-transparent"}`}><Check className="h-2.5 w-2.5" /></span><span className={done ? "text-foreground" : ""}>{text}{optional && !done ? " · optional" : ""}</span></div>
}

function formatEnum(value: string) {
  return value.toLowerCase().replaceAll("_", " ").replace(/\b\w/g, (letter) => letter.toUpperCase())
}
