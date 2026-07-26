import { TradeBuilderClient } from "@/components/marketplace/trade/trade-builder-client"
import { getTradeableItems } from "@/lib/api/trades/server"
import { requireUser } from "@/lib/auth/require-user"
import { redirect } from "next/navigation"

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
  if (user.id === Number(userId)) {
    redirect(`/item/${itemId}?error=self-trade`)
  }

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
  return <TradeBuilderClient receiverId={Number(userId)} selectedItemId={Number(itemId)} myItems={myItems} theirItems={theirItems} />
}
