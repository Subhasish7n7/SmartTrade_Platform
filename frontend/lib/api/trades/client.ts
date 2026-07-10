import { clientApi } from "@/lib/api/client"

import { CreateBuyRequest, CreateTradeOfferRequest, TradeOfferResponse } from "@/lib/types/api/trade"

export async function createTradeOffer(payload: CreateTradeOfferRequest) {
  const response = await clientApi.post<TradeOfferResponse>("/trade/offer", payload)

  return response.data
}

export async function createBuyOffer(payload: CreateBuyRequest) {
  const response = await clientApi.post<TradeOfferResponse>("/trade/buy", payload)

  return response.data
}

export async function getTrade(tradeId: number, tradeOfferId: number) {
  const response = await clientApi.get(`/trade/${tradeId}`, { params: { tradeOfferId } })
  return response.data
}
export async function getTradeableItems(userId: number) {
  const response = await clientApi.get(`/users/${userId}/tradeable-items`)
  return response.data
}

export async function acceptTrade(tradeId: number) {
  return clientApi.patch(`/trade/${tradeId}/accept`)
}

export async function cancelTrade(tradeId: number) {
  return clientApi.patch(`/trade/${tradeId}/cancel`)
}

export async function completeTrade(tradeId: number) {
  return clientApi.patch(`/trade/${tradeId}/complete`)
}

export async function getTradeOfferHistory(tradeId: number) {
  const response = await clientApi.get(`/trade/${tradeId}/offers`)

  return response.data
}

export async function getTradeInbox() {
  const response = await clientApi.get("/trade/inbox")

  return response.data
}
