import { AnimatedPage, Section } from "@/components/marketplace/animations/section"
import { BackButton } from "@/components/marketplace/back-button"
import { ItemListingForm } from "@/components/marketplace/listing/item-listing-form"

export default function ListItemPage() {
  return (
    <main className="pt-20 pb-24">
      <div className="mx-auto max-w-[1600px] px-4 sm:px-6 lg:px-8">
        <AnimatedPage>
          <Section>
            <BackButton />
          </Section>

          <Section>
            <div className="trade-inbox-hero rounded-2xl px-6 py-5 mb-5 border border-border/50">
              <h1 className="text-3xl font-bold bg-gradient-to-r from-[var(--trust-blue)] via-[var(--negotiation-violet)] to-[var(--premium-gold)] bg-clip-text text-transparent">
                List New Item
              </h1>

              <p className="text-muted-foreground mt-2">Create a listing and start receiving trade offers from nearby users.</p>
            </div>
          </Section>

          <div className="trade-section-divider mb-6" />

          <Section>
            <ItemListingForm />
          </Section>
        </AnimatedPage>
      </div>
    </main>
  )
}
