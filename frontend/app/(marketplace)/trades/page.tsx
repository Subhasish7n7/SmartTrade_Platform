import { TradeInboxClient } from "@/components/marketplace/trade/trade-inbox-client"
import { getTradeInbox } from "@/lib/api/trades/server"
import { requireUser } from "@/lib/auth/require-user"
import { mockTrades } from "@/lib/mock/trade"
import { TradeInboxResponse } from "@/lib/types/api/trade"
import { notFound } from "next/navigation"

export default async function TradesPage() {
  const currentPath = "/trades"
  const user = await requireUser(currentPath)
  let trades: TradeInboxResponse[]
  try {
    trades = await getTradeInbox()
  } catch {
    notFound()
  }
  return <TradeInboxClient trades={trades} />
}
