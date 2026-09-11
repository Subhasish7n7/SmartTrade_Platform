import { notFound } from "next/navigation"

import { AnimatedPage, Section } from "@/components/marketplace/animations/section"
import { BackButton } from "@/components/marketplace/back-button"
import { ItemListingForm } from "@/components/marketplace/listing/item-listing-form"
import { getItemById } from "@/lib/api/items/server"

interface Props {
  searchParams: Promise<{
    editId?: string
  }>
}

export default async function ItemListingPage({
  searchParams,
}: Props) {
  const { editId } = await searchParams

  let initialItem

  if (editId) {
    const itemId = Number(editId)

    if (!Number.isInteger(itemId) || itemId <= 0) {
      notFound()
    }

    try {
      initialItem = await getItemById(itemId)
    } catch {
      notFound()
    }
  }

  const isEdit = Boolean(initialItem)

  return (
    <main className="min-h-screen overflow-hidden bg-background pt-20 pb-24">
      <div className="pointer-events-none fixed inset-x-0 top-0 h-[520px] bg-[radial-gradient(circle_at_15%_8%,rgba(59,130,246,.10),transparent_32%),radial-gradient(circle_at_85%_15%,rgba(168,85,247,.08),transparent_30%),radial-gradient(circle_at_50%_0%,rgba(16,185,129,.05),transparent_36%)]" />

      <div className="relative mx-auto max-w-[1500px] px-4 sm:px-6 lg:px-8">
        <AnimatedPage>
          <Section>
            <BackButton />
          </Section>

          <Section>
            <div className="trade-inbox-hero relative overflow-hidden rounded-[30px] border border-border/50 px-5 py-6 sm:px-7 sm:py-7">
              <div className="pointer-events-none absolute right-[-70px] top-[-90px] h-64 w-64 rounded-full bg-[var(--premium-gold)]/7 blur-3xl" />
              <div className="pointer-events-none absolute bottom-[-90px] left-[35%] h-56 w-56 rounded-full bg-[var(--trust-blue)]/6 blur-3xl" />

              <div className="relative flex flex-col gap-5 lg:flex-row lg:items-end lg:justify-between">
                <div>
                  <div className="mb-2 flex items-center gap-2 text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground">
                    <span className="h-1.5 w-1.5 rounded-full bg-primary shadow-[0_0_12px_rgba(45,212,191,.6)]" />

                    {isEdit
                      ? "Listing editor"
                      : "Marketplace listing"}
                  </div>

                  <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">
                    {isEdit
                      ? "Refine your listing."
                      : "List something worth trading."}
                  </h1>

                  <p className="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground sm:text-base">
                    {isEdit
                      ? "Update the details, photos, value, and terms of your existing listing."
                      : "Give traders the details they need to understand your item, judge its value, and decide whether to buy or make an offer."}
                  </p>
                </div>

                <div className="hidden items-center gap-2 text-xs text-muted-foreground lg:flex">
                  <StepDot
                    index="01"
                    label="Details"
                    active
                  />

                  <span className="h-px w-7 bg-border/60" />

                  <StepDot
                    index="02"
                    label="Value"
                    active
                  />

                  <span className="h-px w-7 bg-border/60" />

                  <StepDot
                    index="03"
                    label={isEdit ? "Save" : "Publish"}
                  />
                </div>
              </div>
            </div>
          </Section>

          <Section>
            <ItemListingForm initialItem={initialItem} />
          </Section>
        </AnimatedPage>
      </div>
    </main>
  )
}

function StepDot({
  index,
  label,
  active = false,
}: {
  index: string
  label: string
  active?: boolean
}) {
  return (
    <div className="flex items-center gap-1.5">
      <span
        className={`flex h-6 min-w-6 items-center justify-center rounded-full border px-1.5 text-[10px] font-bold ${
          active
            ? "border-primary/30 bg-primary/10 text-primary"
            : "border-border/50 bg-white/[0.02] text-muted-foreground"
        }`}
      >
        {index}
      </span>

      <span>{label}</span>
    </div>
  )
}