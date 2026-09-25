"use client"

import { ArrowLeftRight, CircleDollarSign, MapPin, ShieldCheck, Sparkles } from "lucide-react"
import Image from "next/image"

import { Badge } from "@/components/ui/badge"
import { ItemImageDraft } from "@/lib/types/api/item"

interface Props {
  isEdit?: boolean
  images: ItemImageDraft[]
  form: {
    itemName: string
    userPrice: string
    description: string
    category: string
    condition: string
    city: string
    state: string
    locality: string
    forTrade: boolean
    forSale: boolean
    customCategory?: string
  }
}

export function ListingPreviewCard({ form, isEdit = false, images }: Props) {
  const imageUrl = images[0]?.secureUrl
  const location = [form.locality, form.city].filter(Boolean).join(", ")

  return (
    <section className="trade-card overflow-hidden rounded-[28px] border-white/[0.07] shadow-[0_18px_60px_rgba(0,0,0,.10)]">
      <div className="trade-card-accent" />

      <div className="relative overflow-hidden p-4.5 sm:p-5">
        <div className="pointer-events-none absolute -right-20 -top-24 h-52 w-52 rounded-full bg-[var(--trust-blue)]/8 blur-3xl" />
        <div className="pointer-events-none absolute -left-20 bottom-[-100px] h-48 w-48 rounded-full bg-[var(--negotiation-violet)]/7 blur-3xl" />

        <div className="relative flex items-center justify-between gap-3">
          <div>
            <p className="text-[10px] font-semibold uppercase tracking-[0.18em] text-muted-foreground">Live preview</p>
            <h2 className="mt-1 font-semibold tracking-tight">How traders will see it</h2>
          </div>

          <Badge variant="outline" className="border-primary/15 bg-primary/[0.05] text-primary">
            {isEdit ? "Editing" : "Draft"}
          </Badge>
        </div>

        <div className="relative mt-4 overflow-hidden rounded-2xl border border-white/[0.055] bg-background/35">
          <div className="relative aspect-[1.04] overflow-hidden bg-muted/20">
            {imageUrl ? (
              <Image
                src={imageUrl}
                alt="Listing preview"
                fill
                className="object-cover transition-transform duration-700 hover:scale-[1.025]"
              />
            ) : (
              <div className="absolute inset-0 flex flex-col items-center justify-center bg-[radial-gradient(circle_at_50%_45%,rgba(45,212,191,.08),transparent_32%)] text-center text-muted-foreground">
                <Sparkles className="h-6 w-6 text-primary/80" />
                <span className="mt-2 text-xs">Add a photo to preview</span>
              </div>
            )}

            <div className="absolute inset-0 bg-gradient-to-t from-background/95 via-background/10 to-transparent" />

            <div className="absolute left-3 top-3 rounded-full border border-white/10 bg-black/25 px-2.5 py-1 text-[10px] font-semibold text-white/85 backdrop-blur-md">
              Marketplace preview
            </div>

            <div className="absolute inset-x-0 bottom-0 px-3 pb-3 pt-14">
              <div className="flex flex-wrap gap-1.5">
                {form.forTrade && form.forSale ? (
                  <Badge className="border border-white/10 bg-gradient-to-r from-primary to-info shadow-[0_8px_22px_rgba(45,212,191,.12)]">
                    Trade / Sale
                  </Badge>
                ) : form.forTrade ? (
                  <Badge className="border border-white/10 bg-primary/90">Trade</Badge>
                ) : form.forSale ? (
                  <Badge className="border border-white/10 bg-info/90">For Sale</Badge>
                ) : null}

                {form.condition && (
                  <Badge variant="outline" className="border-white/10 bg-black/35 backdrop-blur-sm">
                    {formatEnum(form.condition)}
                  </Badge>
                )}
              </div>
            </div>
          </div>

          <div className="p-4">
            <div className="flex items-start justify-between gap-3">
              <div className="min-w-0">
                <h3 className="line-clamp-2 text-lg font-semibold tracking-tight">{form.itemName || "Your item name"}</h3>

                <div className="mt-1 text-xs text-muted-foreground">
                  {form.customCategory || (form.category && formatEnum(form.category)) || "Category"}
                </div>
              </div>

              <div className="shrink-0 rounded-xl border border-[var(--premium-gold)]/10 bg-[var(--premium-gold)]/[0.035] px-2.5 py-1.5 text-right">
                <p className="text-[9px] uppercase tracking-[0.14em] text-muted-foreground">Value</p>
                <p className="mt-0.5 text-lg font-bold tracking-tight">₹{form.userPrice ? Number(form.userPrice).toLocaleString() : "0"}</p>
              </div>
            </div>

            {location && (
              <div className="mt-3 inline-flex max-w-full items-center gap-1.5 text-xs text-muted-foreground">
                <MapPin className="h-3.5 w-3.5 shrink-0 text-[var(--chat-cyan)]" />
                <span className="truncate">{location}</span>
                {form.state && <span className="truncate">, {form.state}</span>}
              </div>
            )}

            <p className="mt-3 line-clamp-4 text-xs leading-5 text-muted-foreground">
              {form.description ||
                "Your description will appear here. Tell traders about condition, accessories, defects, purchase details, or anything else relevant."}
            </p>

            <div className="mt-4 grid grid-cols-2 gap-2">
              <div className="trade-stat">
                <div className="flex items-center gap-1.5 text-[11px] text-muted-foreground">
                  <ArrowLeftRight className="h-3.5 w-3.5 text-[var(--negotiation-violet)]" />
                  Trade ready
                </div>
                <p className="mt-1 font-medium">{form.forTrade ? "Yes" : "No"}</p>
              </div>

              <div className="trade-stat">
                <div className="flex items-center gap-1.5 text-[11px] text-muted-foreground">
                  <CircleDollarSign className="h-3.5 w-3.5 text-[var(--trust-blue)]" />
                  Direct sale
                </div>
                <p className="mt-1 font-medium">{form.forSale ? "Yes" : "No"}</p>
              </div>
            </div>

            <div className="mt-3 flex items-center justify-between gap-2 border-t border-white/[0.05] pt-3">
              <span className="inline-flex items-center gap-1.5 text-[10px] text-muted-foreground">
                <ShieldCheck className="h-3.5 w-3.5 text-[var(--trust-blue)]" />
                Trust-aware marketplace
              </span>

              {images.length > 0 && (
                <span className="text-[10px] font-medium text-muted-foreground">
                  {images.length} photo
                  {images.length === 1 ? "" : "s"}
                </span>
              )}
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}

function formatEnum(value: string) {
  return value
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (letter) => letter.toUpperCase())
}
