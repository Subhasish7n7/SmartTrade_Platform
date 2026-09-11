import { TradeBuilderClient } from "@/components/marketplace/trade/trade-builder-client"
import { getTradeableItems } from "@/lib/api/trades/server"
import { requireUser } from "@/lib/auth/require-user"
import { redirect } from "next/navigation"

interface Props {
  params: Promise<{
    otherUserId: string
  }>
  searchParams: Promise<{
    itemId?: string
    tradeId?: string
  }>
}

export default async function CreateTradePage({ params, searchParams }: Props) {
  const resolvedParams = await params
  const resolvedSearchParams = await searchParams

  console.log("PARAMS:", resolvedParams)
  console.log("SEARCH PARAMS:", resolvedSearchParams)

  const otherUserId = resolvedParams.otherUserId
  const itemId = resolvedSearchParams.itemId
  const tradeId = resolvedSearchParams.tradeId

  console.log("FINAL:", {
    otherUserId,
    itemId,
    tradeId,
  })
  const currentPath = itemId
    ? `/trades/create/${otherUserId}?itemId=${itemId}`
    : tradeId
      ? `/trades/create/${otherUserId}?tradeId=${tradeId}`
      : `/trades/create/${otherUserId}`

  const user = await requireUser(currentPath)

  // Only redirect to the item when this page was entered
  // from an item page.
  if (itemId && user.id === Number(otherUserId)) {
    redirect(`/item/${itemId}?error=self-trade`)
  }

  const [myItems, theirItems] = await Promise.all([
    getTradeableItems(user.id).catch((err) => {
      console.error(err)
      return []
    }),
    getTradeableItems(Number(otherUserId)).catch((err) => {
      console.error(err)
      return []
    }),
  ])

  return (
    <TradeBuilderClient
      receiverId={Number(otherUserId)}
      selectedItemId={itemId ? Number(itemId) : undefined}
      tradeId={tradeId ? Number(tradeId) : undefined}
      isCounterOffer={!!tradeId}
      myItems={myItems}
      theirItems={theirItems}
    />
  )
}
