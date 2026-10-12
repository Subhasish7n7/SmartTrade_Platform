import { TradeBuilderClient } from "@/components/marketplace/trade/trade-builder-client"
import {
  getTradeOfferHistory,
  getTradeableItems,
} from "@/lib/api/trades/server"
import { requireUser } from "@/lib/auth/require-user"
import type { TradeOfferResponse } from "@/lib/types/api/trade"
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

  const otherUserId = Number(resolvedParams.otherUserId)
  const itemId = resolvedSearchParams.itemId
  const tradeId = resolvedSearchParams.tradeId

  const currentPath = itemId
    ? `/trades/create/${otherUserId}?itemId=${itemId}`
    : tradeId
      ? `/trades/create/${otherUserId}?tradeId=${tradeId}`
      : `/trades/create/${otherUserId}`

  const user = await requireUser(currentPath)

  if (itemId && user.id === otherUserId) {
    redirect(`/item/${itemId}?error=self-trade`)
  }

  const [myItems, theirItems] = await Promise.all([
    getTradeableItems(user.id).catch((error) => {
      console.error("Could not load your tradeable items", error)
      return []
    }),
    getTradeableItems(otherUserId).catch((error) => {
      console.error("Could not load the other trader's items", error)
      return []
    }),
  ])

  let initialOffer: TradeOfferResponse | null = null

  if (tradeId) {
    try {
      const history = await getTradeOfferHistory(Number(tradeId))
      initialOffer = history.at(-1) ?? null
    } catch (error) {
      console.error("Could not load the latest trade offer", error)
    }
  }

  return (
    <TradeBuilderClient
      receiverId={otherUserId}
      currentUserId={user.id}
      selectedItemId={itemId ? Number(itemId) : undefined}
      tradeId={tradeId ? Number(tradeId) : undefined}
      isCounterOffer={Boolean(tradeId)}
      myItems={myItems}
      theirItems={theirItems}
      initialOffer={initialOffer}
    />
  )
}