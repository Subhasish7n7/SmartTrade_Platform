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

export default async function ItemListingPage({ searchParams }: Props) {
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
      <div className="pointer-events-none fixed inset-x-0 top-0 h-[420px] bg-[radial-gradient(circle_at_15%_8%,rgba(59,130,246,.10),transparent_31%),radial-gradient(circle_at_84%_12%,rgba(168,85,247,.08),transparent_29%),radial-gradient(circle_at_50%_0%,rgba(16,185,129,.05),transparent_38%)]" />

      <div className="relative mx-auto max-w-[1500px] px-4 sm:px-6 lg:px-8">
        <AnimatedPage>
          <Section>
            <div className="mb-4 flex items-center justify-between gap-4">
              <BackButton />

              <div className="hidden items-center gap-2 text-xs text-muted-foreground sm:flex">
                <StepDot index="01" label="Details" active />
                <span className="h-px w-5 bg-border/60" />
                <StepDot index="02" label="Value" active />
                <span className="h-px w-5 bg-border/60" />
                <StepDot index="03" label={isEdit ? "Save" : "Publish"} active />
              </div>
            </div>
          </Section>

          <Section>
            <div className="marketplace-page-intro px-5 py-5 sm:px-6 sm:py-6">
              <div className="relative flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
                <div className="min-w-0">
                  <div className="flex flex-wrap items-center gap-2 text-[10px] font-bold uppercase tracking-[0.18em] text-muted-foreground">
                    <span className="inline-flex items-center gap-2 rounded-full border border-primary/15 bg-primary/[0.06] px-2.5 py-1 text-primary">
                      <span className="h-1.5 w-1.5 rounded-full bg-primary shadow-[0_0_12px_rgba(45,212,191,.65)]" />
                      {isEdit ? "Listing editor" : "Marketplace listing"}
                    </span>

                    {isEdit && (
                      <span className="rounded-full border border-[var(--negotiation-violet)]/20 bg-[var(--negotiation-violet)]/[0.06] px-2.5 py-1 text-[var(--negotiation-violet)]">
                        Editing #{initialItem?.itemId}
                      </span>
                    )}
                  </div>

                  <h1 className="mt-3 text-2xl font-bold tracking-[-0.025em] sm:text-[2rem]">
                    {isEdit ? "Refine your listing." : "List something worth trading."}
                  </h1>

                  <p className="mt-1.5 max-w-2xl text-xs leading-5 text-muted-foreground sm:text-sm">
                    {isEdit
                      ? "Update the details, photos, value, and terms of your existing listing."
                      : "Add the details traders need to understand your item, value, and terms."}
                  </p>
                </div>

                <div className="flex items-center gap-2 self-start text-[10px] text-muted-foreground lg:self-auto">
                  <span className="rounded-full border border-border/60 bg-white/[0.025] px-2.5 py-1.5">3 steps</span>
                  <span className="rounded-full border border-[var(--trust-blue)]/15 bg-[var(--trust-blue)]/[0.05] px-2.5 py-1.5 text-[var(--trust-blue)]">
                    Faster listing flow
                  </span>
                </div>
              </div>
            </div>
          </Section>

          <Section>
            <ItemListingForm key={initialItem?.itemId ?? "new"} initialItem={initialItem} />
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
            ? "border-primary/30 bg-primary/10 text-primary shadow-[0_0_18px_rgba(45,212,191,.08)]"
            : "border-border/50 bg-white/[0.02] text-muted-foreground"
        }`}
      >
        {index}
      </span>
      <span>{label}</span>
    </div>
  )
}
