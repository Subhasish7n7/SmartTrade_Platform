import { AnimatedPage, Section } from "@/components/marketplace/animations/section"
import { BackButton } from "@/components/marketplace/back-button"
import { ActionButtons } from "@/components/marketplace/item-detail/action-buttons"
import { ItemGallery } from "@/components/marketplace/item-detail/item-gallery"
import { MobileActionBar } from "@/components/marketplace/item-detail/mobile-action-bar"
import { PricingCard } from "@/components/marketplace/item-detail/pricing-card"
import { SellerCard } from "@/components/marketplace/item-detail/seller-card"
import { SimilarItems } from "@/components/marketplace/item-detail/similar-items"
import { Badge } from "@/components/ui/badge"
import { getCurrentUser } from "@/lib/api/auth/auth.server"
import { getItemById } from "@/lib/api/items/server"
import { Clock } from "lucide-react"
import { notFound } from "next/navigation";

interface Props {
  params: Promise<{
    id: string
  }>
}

export default async function ItemDetailPage({ params }: Props) {
  const { id } = await params
  const item = await getItemById(Number(id)).catch(() => null);
  const user = await getCurrentUser().catch(() => null);
  if (!item) notFound();

  

  return (
    <main className="pt-20 pb-32 lg:pb-16">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <AnimatedPage>
          <Section>
            <BackButton />
          </Section>
          <div className="grid grid-cols-1 lg:grid-cols-5 gap-8">
            {/* LEFT */}
            <div className="lg:col-span-3 space-y-6">
              <Section>
                <ItemGallery images={item.imageUrls} itemName={item.itemName} />
              </Section>
              {/* ITEM INFO */}
              <Section>
                <div className="content-surface p-6">
                  <div>
                    <div className="flex flex-wrap gap-2 mb-3">
                      {item.forTrade && item.forSale ? (
                        <Badge className="bg-gradient-to-r from-primary to-info text-primary-foreground border-none">Trade / Sale</Badge>
                      ) : item.forTrade ? (
                        <Badge className="bg-primary/90 text-primary-foreground border-none">Trade Only</Badge>
                      ) : item.forSale ? (
                        <Badge className="bg-info/90 text-info-foreground border-none">For Sale</Badge>
                      ) : null}

                      <Badge variant="outline">{item.condition}</Badge>
                    </div>

                    <h1 className="text-3xl font-bold">{item.itemName}</h1>
                  </div>

                  <div className="flex flex-wrap items-center gap-4 text-sm text-muted-foreground">
                    <span className="flex items-center gap-1">
                      <Clock className="h-4 w-4" />
                      {item.createdAt}
                    </span>
                  </div>

                  <div>
                    <h3 className="font-medium mb-2">Description</h3>

                    <p className="text-muted-foreground leading-relaxed">{item.description}</p>
                  </div>

                  <div className="flex flex-wrap gap-2">
                    {(item.labels ?? []).map((label) => (
                      <Badge key={label} variant="secondary">
                        {label}
                      </Badge>
                    ))}
                  </div>
                </div>
              </Section>
              <Section>
                <SellerCard seller={item.seller} />
              </Section>
              <Section>
                <SimilarItems />
              </Section>
            </div>

            {/* RIGHT */}
            <div className="lg:col-span-2">
              <div className="sticky top-24 space-y-4">
                <Section>
                  <PricingCard item={item} />
                </Section>
                <Section>
                  <ActionButtons
                  item={item}
                  userId={user?.id ?? null}
                  />
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
