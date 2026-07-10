import { AnimatedPage, Section } from "@/components/marketplace/animations/section"
import { BackButton } from "@/components/marketplace/back-button"
import { TradeBuilderClient } from "@/components/marketplace/trade/trade-builder-client"
import { getItemById } from "@/lib/api/items/client"
import { getTradeableItems } from "@/lib/api/trades/server"
import { requireUser } from "@/lib/auth/require-user"
import { mockMyItems, mockTheirItems } from "@/lib/mock/trade"

interface Props {
  params: Promise<{
    userId: string
  }>
  searchParams: Promise<{
    itemId: string
  }>
}
export default async function CreateTradePage({ params, searchParams }: Props) {
  const { userId } = await params
  console.log("params.userId =", userId)
console.log("Number(userId) =", Number(userId))
  const { itemId } = await searchParams
  const currentPath = `/trades/create/${userId}?itemId=${itemId}`
  const user = await requireUser(currentPath)
  console.log("current user id", user)

  const [myItems, theirItems] = await Promise.all([
  getTradeableItems(user.id).catch((err) => {
    console.error(err)
    return []
  }),
  getTradeableItems(Number(userId)).catch((err) => {
    console.error(err)
    return []
  }),
])
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
                Create Trade Offer
              </h1>

              <p className="text-muted-foreground mt-2">Build a fair proposal and start negotiating with the seller.</p>
            </div>
          </Section>
          <div className="trade-section-divider mb-6" />
          <Section>
            <TradeBuilderClient selectedItemId={Number(itemId)} myItems={myItems} theirItems={theirItems} />
          </Section>
        </AnimatedPage>
      </div>
    </main>
  )
}
