import { clientApi } from "@/lib/api/configs/client"
import {
  CreateBuyRequest,
  CreateDisputeRequest,
  CreateTradeOfferRequest,
  TradeDetailsResponse,
  TradeDisputeResponse,
  TradeInboxResponse,
  TradeOfferResponse,
} from "@/lib/types/api/trade"

export async function createTradeOffer(payload: CreateTradeOfferRequest) {
  const response = await clientApi.post<TradeOfferResponse>(
    "/trade/offer",
    payload,
  )

  return response.data
}

export async function createBuyOffer(payload: CreateBuyRequest) {
  const response = await clientApi.post<TradeOfferResponse>(
    "/trade/buy",
    payload,
  )

  return response.data
}

export async function getTrade(
  tradeId: number,
  tradeOfferId: number,
) {
  const response = await clientApi.get<TradeDetailsResponse>(
    `/trade/${tradeId}`,
    { params: { tradeOfferId } },
  )

  return response.data
}

export async function getTradeableItems(userId: number) {
  // Backend route is /trade/users/{userId}/tradeable-items.
  const response = await clientApi.get(
    `/trade/users/${userId}/tradeable-items`,
  )

  return response.data
}

export async function acceptTrade(tradeId: number) {
  return clientApi.patch(`/trade/${tradeId}/accept`)
}

export async function confirmTradeCompletion(tradeId: number) {
  return clientApi.patch(`/trade/${tradeId}/confirm-completion`)
}

export async function cancelTrade(tradeId: number) {
  return clientApi.patch(`/trade/${tradeId}/cancel`)
}

export async function openTradeDispute(
  tradeId: number,
  payload: CreateDisputeRequest,
) {
  const response = await clientApi.post<TradeDisputeResponse>(
    `/trade/${tradeId}/dispute`,
    payload,
  )

  return response.data
}

export async function getTradeDispute(tradeId: number) {
  const response = await clientApi.get<TradeDisputeResponse>(
    `/trade/${tradeId}/dispute`,
  )

  return response.data
}

export async function getTradeOfferHistory(tradeId: number) {
  const response = await clientApi.get<TradeOfferResponse[]>(
    `/trade/${tradeId}/offers`,
  )

  return response.data
}

export async function getTradeInbox() {
  const response = await clientApi.get<TradeInboxResponse[]>(
    "/trade/inbox",
  )

  return response.data
}