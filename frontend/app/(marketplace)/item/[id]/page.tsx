import { CalendarDays, Clock, MapPin, Package, Tag } from "lucide-react"
import { notFound } from "next/navigation"
import type { ReactNode } from "react"

import { AnimatedPage, Section } from "@/components/marketplace/animations/section"
import { BackButton } from "@/components/marketplace/back-button"
import { ActionButtons } from "@/components/marketplace/item-detail/action-buttons"
import { ExpandableContentSection } from "@/components/marketplace/item-detail/expandable-content-section"
import { ItemGallery } from "@/components/marketplace/item-detail/item-gallery"
import { MobileActionBar } from "@/components/marketplace/item-detail/mobile-action-bar"
import { PricingCard } from "@/components/marketplace/item-detail/pricing-card"
import { SellerCard } from "@/components/marketplace/item-detail/seller-card"
import { SimilarItems } from "@/components/marketplace/item-detail/similar-items"
import { Badge } from "@/components/ui/badge"

import { getCurrentUser } from "@/lib/api/auth/auth.server"
import { getItemById } from "@/lib/api/items/server"
import { getSimilarItems } from "@/lib/api/items/similar.server"

interface Props {
  params: Promise<{
    id: string
  }>
}

export default async function ItemDetailPage({ params }: Props) {
  const { id } = await params

  const item = await getItemById(Number(id)).catch(() => null)

if (!item) notFound()

const [user, similarItems] = await Promise.all([
  getCurrentUser().catch(() => null),

  getSimilarItems(item, 3).catch((error) => {
    console.error("Failed to load similar listings:", error)
    return []
  }),
])

  const categoryLabel = item.category === "OTHER" ? item.customCategory || "Other" : formatEnum(item.category)
  const conditionLabel = formatEnum(item.condition)
  const listingType =
    item.forTrade && item.forSale ? "Trade / Sale" : item.forTrade ? "Trade only" : item.forSale ? "For sale" : "Unavailable"
  const locationLabel = [item.locality, item.city, item.state].filter(Boolean).join(", ")
  const createdAt = new Intl.DateTimeFormat("en-IN", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(item.createdAt))

  // The backend groups these strings by priority. The product page keeps one
  // simple Specification section rather than exposing three nested UI layers.
  const specifications = [
    ...(item.primarySpecifications ?? []),
    ...(item.secondarySpecifications ?? []),
    ...(item.tertiarySpecifications ?? []),
  ]
    .map((value) => value.trim())
    .filter(Boolean)

  return (
    <main className="pb-32 pt-20 lg:pb-16">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <AnimatedPage>
          <Section>
            <BackButton />
          </Section>

          <div className="grid grid-cols-1 gap-8 lg:grid-cols-5">
            {/* LEFT */}
            <div className="space-y-6 lg:col-span-3">
              <Section>
                <ItemGallery images={item.images} itemName={item.itemName} />
              </Section>

              <Section>
                <div className="content-surface overflow-hidden rounded-[28px] p-5 sm:p-6">
                  <div>
                    <div className="mb-4 flex flex-wrap gap-2">
                      {item.forTrade && item.forSale && (
                        <Badge className="border-none bg-gradient-to-r from-primary to-info text-primary-foreground">Trade / Sale</Badge>
                      )}
                      {item.forTrade && !item.forSale && (
                        <Badge className="border-none bg-primary/90 text-primary-foreground">Trade Only</Badge>
                      )}
                      {item.forSale && !item.forTrade && <Badge className="border-none bg-info/90 text-info-foreground">For Sale</Badge>}
                      <Badge variant="outline">{conditionLabel}</Badge>
                      {!item.available && (
                        <Badge variant="outline" className="border-amber-400/20 text-amber-300">
                          Currently unavailable
                        </Badge>
                      )}
                    </div>

                    <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">{item.itemName}</h1>
                    <div className="mt-4 flex flex-wrap items-center gap-x-5 gap-y-2 text-sm text-muted-foreground">
                      <span className="flex items-center gap-1.5">
                        <CalendarDays className="h-4 w-4" />
                        Listed {createdAt}
                      </span>
                      {locationLabel && (
                        <span className="flex items-center gap-1.5">
                          <MapPin className="h-4 w-4" />
                          {locationLabel}
                        </span>
                      )}
                    </div>
                  </div>

                  <div className="mt-5 grid gap-2 sm:grid-cols-4">
                    <InfoTile icon={<Package className="h-4 w-4" />} label="Category" value={categoryLabel} tone="blue" />
                    <InfoTile icon={<Tag className="h-4 w-4" />} label="Condition" value={conditionLabel} tone="violet" />
                    <InfoTile icon={<Clock className="h-4 w-4" />} label="Listing" value={listingType} tone="gold" />
                    <InfoTile
                      icon={<Package className="h-4 w-4" />}
                      label="Availability"
                      value={item.available ? "Available" : "Unavailable"}
                      tone={item.available ? "success" : "warning"}
                    />
                  </div>

                  <ExpandableContentSection title="Description">
                    {item.description?.trim() ? (
                      <p className="whitespace-pre-line">{item.description}</p>
                    ) : (
                      <p className="italic">No additional description was provided.</p>
                    )}
                  </ExpandableContentSection>

                  <ExpandableContentSection title="Specifications" className="mt-6">
                    {specifications.length > 0 ? (
                      <ul className="list-disc space-y-1 pl-5">
                        {specifications.map((specification, index) => (
                          <li key={`${index}-${specification}`}>{specification}</li>
                        ))}
                      </ul>
                    ) : (
                      <p className="italic">No specifications were provided.</p>
                    )}
                  </ExpandableContentSection>

                  {item.labels?.length > 0 && (
                    <div className="mt-7">
                      <h2 className="mb-3 font-semibold">Tags</h2>
                      <div className="flex flex-wrap gap-2">
                        {item.labels.map((label) => (
                          <Badge key={label} variant="secondary" className="border border-white/[0.06] bg-white/[0.03]">
                            #{label}
                          </Badge>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              </Section>

              <Section>
                <SellerCard seller={item.seller} />
              </Section>

              <Section>
                <SimilarItems items={similarItems} />
              </Section>
            </div>

            {/* RIGHT */}
            <div className="lg:col-span-2">
              <div className="sticky top-24 space-y-4">
                <Section>
                  <PricingCard item={item} />
                </Section>
                <Section>
                  <ActionButtons item={item} userId={user?.id ?? null} />
                </Section>
                <Section>
                  <MobileActionBar price={item.userPrice} itemId={item.itemId} />
                </Section>
              </div>
            </div>
          </div>
        </AnimatedPage>
      </div>
    </main>
  )
}

function InfoTile({
  icon,
  label,
  value,
  tone,
}: {
  icon: ReactNode
  label: string
  value: string
  tone: "blue" | "violet" | "gold" | "success" | "warning"
}) {
  const toneClass = {
    blue: "marketplace-attribute-blue",
    violet: "marketplace-attribute-violet",
    gold: "marketplace-attribute-gold",
    success: "marketplace-attribute-success",
    warning: "marketplace-attribute-warning",
  }[tone]

  return (
    <div className={`marketplace-attribute ${toneClass}`}>
      <span className="marketplace-attribute-icon">{icon}</span>
      <div className="marketplace-attribute-copy">
        <p className="marketplace-attribute-label">{label}</p>
        <p className="marketplace-attribute-value">{value}</p>
      </div>
    </div>
  )
}

function formatEnum(value: string) {
  return value
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (letter) => letter.toUpperCase())
}
