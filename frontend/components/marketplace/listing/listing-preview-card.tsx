"use client"

import Image from "next/image"
import { ArrowLeftRight, CircleDollarSign, MapPin, Sparkles } from "lucide-react"

import { Badge } from "@/components/ui/badge"

interface Props {
  form: {
    itemName: string
    userPrice: string
    description: string
    category: string
    condition: string
    imageUrls: string[]
    city: string
    state: string
    locality: string
    forTrade: boolean
    forSale: boolean
    customCategory?: string
  }
}

export function ListingPreviewCard({ form }: Props) {
  const image = form.imageUrls[0]
  const location = [form.locality, form.city].filter(Boolean).join(", ")

  return (
    <section className="trade-card overflow-hidden rounded-[28px]">
      <div className="trade-card-accent" />
      <div className="relative overflow-hidden p-4.5 sm:p-5">
        <div className="pointer-events-none absolute -right-20 -top-24 h-52 w-52 rounded-full bg-[var(--trust-blue)]/7 blur-3xl" />
        <div className="relative flex items-center justify-between">
          <div><p className="text-[10px] font-semibold uppercase tracking-[0.18em] text-muted-foreground">Live preview</p><h2 className="mt-1 font-semibold">How traders will see it</h2></div>
          <Badge variant="outline" className="border-primary/15 bg-primary/5 text-primary">Draft</Badge>
        </div>

        <div className="relative mt-4 overflow-hidden rounded-2xl border border-border/40 bg-background/35">
          <div className="relative aspect-[1.06] overflow-hidden bg-muted/20">
            {image ? <Image src={image} alt="Listing preview" fill className="object-cover transition-transform duration-500 hover:scale-[1.02]" /> : <div className="absolute inset-0 flex flex-col items-center justify-center text-center text-muted-foreground"><Sparkles className="h-6 w-6" /><span className="mt-2 text-xs">Add a photo to preview</span></div>}
            <div className="absolute inset-x-0 bottom-0 bg-gradient-to-t from-background/90 to-transparent px-3 pb-3 pt-10">
              <div className="flex flex-wrap gap-1.5">
                {form.forTrade && form.forSale ? <Badge className="border-none bg-gradient-to-r from-primary to-info">Trade / Sale</Badge> : form.forTrade ? <Badge className="border-none bg-primary/90">Trade</Badge> : form.forSale ? <Badge className="border-none bg-info/90">For Sale</Badge> : null}
                {form.condition && <Badge variant="outline" className="border-white/10 bg-background/60 backdrop-blur-sm">{formatEnum(form.condition)}</Badge>}
              </div>
            </div>
          </div>

          <div className="p-4">
            <h3 className="line-clamp-2 text-lg font-semibold tracking-tight">{form.itemName || "Your item name"}</h3>
            <div className="mt-1 text-xs text-muted-foreground">{form.customCategory || (form.category && formatEnum(form.category)) || "Category"}</div>

            <div className="mt-4 flex items-end justify-between gap-3 border-t border-border/35 pt-3">
              <div><p className="text-[10px] uppercase tracking-wider text-muted-foreground">Listed value</p><p className="mt-0.5 text-xl font-bold">₹{form.userPrice ? Number(form.userPrice).toLocaleString() : "0"}</p></div>
              <div className="text-right"><p className="text-[10px] uppercase tracking-wider text-muted-foreground">AI estimate</p><p className="mt-0.5 inline-flex items-center gap-1 text-sm font-semibold text-muted-foreground"><Sparkles className="h-3 w-3 text-primary" />After publish</p></div>
            </div>

            {location && <div className="mt-3 inline-flex max-w-full items-center gap-1.5 text-xs text-muted-foreground"><MapPin className="h-3.5 w-3.5 shrink-0 text-[var(--chat-cyan)]" /><span className="truncate">{location}</span>{form.state && <span className="truncate">, {form.state}</span>}</div>}

            <p className="mt-3 line-clamp-4 text-xs leading-5 text-muted-foreground">{form.description || "Your description will appear here. Use it to tell traders about condition, accessories, defects, or purchase details."}</p>

            <div className="mt-4 grid grid-cols-2 gap-2">
              <div className="trade-stat"><div className="flex items-center gap-1.5 text-[11px] text-muted-foreground"><ArrowLeftRight className="h-3.5 w-3.5 text-[var(--negotiation-violet)]" />Trade ready</div><p className="mt-1 font-medium">{form.forTrade ? "Yes" : "No"}</p></div>
              <div className="trade-stat"><div className="flex items-center gap-1.5 text-[11px] text-muted-foreground"><CircleDollarSign className="h-3.5 w-3.5 text-[var(--trust-blue)]" />Direct sale</div><p className="mt-1 font-medium">{form.forSale ? "Yes" : "No"}</p></div>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}

function formatEnum(value: string) {
  return value.toLowerCase().replaceAll("_", " ").replace(/\b\w/g, (letter) => letter.toUpperCase())
}
