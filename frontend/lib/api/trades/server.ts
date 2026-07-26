import { serverApi } from "@/lib/api/configs/server"

import { CreateBuyRequest, CreateTradeOfferRequest, TradeOfferResponse } from "@/lib/types/api/trade"

export async function createTradeOffer(payload: CreateTradeOfferRequest) {
  const api = await serverApi()
  const response = await api.post<TradeOfferResponse>("/trade/offer", payload)

  return response.data
}

export async function createBuyOffer(payload: CreateBuyRequest) {
  const api = await serverApi()
  const response = await api.post<TradeOfferResponse>("/trade/buy", payload)

  return response.data
}

export async function getTrade(tradeId: number, tradeOfferId: number) {
  const api = await serverApi()
  const response = await api.get(`/trade/${tradeId}`, { params: { tradeOfferId } })
  return response.data
}
export async function getTradeableItems(userId: number) {
  const api = await serverApi()
  const response = await api.get(`/trade/users/${userId}/tradeable-items`)
  return response.data
}

export async function acceptTrade(tradeId: number) {
  const api = await serverApi()
  return api.patch(`/trade/${tradeId}/accept`)
}

export async function cancelTrade(tradeId: number) {
  const api = await serverApi()
  return api.patch(`/trade/${tradeId}/cancel`)
}

export async function completeTrade(tradeId: number) {
  const api = await serverApi()
  return api.patch(`/trade/${tradeId}/complete`)
}

export async function getTradeOfferHistory(tradeId: number) {
  const api = await serverApi()
  const response = await api.get(`/trade/${tradeId}/offers`)

  return response.data
}

export async function getTradeInbox() {
  const api = await serverApi()
  const response = await api.get("/trade/inbox")

  return response.data
}
