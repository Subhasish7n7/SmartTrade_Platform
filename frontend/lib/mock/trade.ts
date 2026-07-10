import { TradeDetailsResponse, TradeHistoryResponse, TradeOfferResponse } from "@/lib/types/api/trade"
import { TradeInboxResponse, TradeItemResponse } from "@/lib/types/api/trade" 
const history:TradeHistoryResponse[] =[
    {
      tradeOfferId: 2,
      senderId: 123,
      senderName: "john",
      createdAt: "12:01"
    },
    {
      tradeOfferId: 3,
      senderId: 124,
      senderName: "ben",
      createdAt: "12:02"
    },
    {
      tradeOfferId: 4,
      senderId: 125,
      senderName: "hellen",
      createdAt: "12:03"
    }
  ]
export const mockMyItems:TradeItemResponse[] = [
    {
      imageUrl:"https://images.unsplash.com/photo-1603302576837-37561b2e2302?w=600&h=600&fit=crop",
      itemId: 1,
      name: "ASUS ROG Zephyrus G14 Gaming Laptop",
      userPrice: 83000,
      systemGeneratedPrice:89000,
      condition:"new",
    },
    {
      imageUrl:"https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=600&h=600&fit=crop",
      itemId: 2,
      name: "Sony A7 III Full Frame Mirrorless Camera",
      userPrice: 85000,
      condition:"new",
      systemGeneratedPrice:78000
    },
    {
      imageUrl:"https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=600&h=600&fit=crop",
      itemId: 3,
      name: "iPhone 15 Pro Max 256GB - Natural Titanium",
      userPrice: 76000,
      condition:"new",
      systemGeneratedPrice:54000
    },
    {
      imageUrl:"https://images.unsplash.com/photo-1576435728678-68d0fbf94e91?w=600&h=600&fit=crop",
      itemId: 4,
      name: "Specialized Rockhopper Mountain Bike",
      userPrice: 15000,
      condition:"new",
      systemGeneratedPrice:13500
    },
  ]

export const mockTheirItems:TradeItemResponse[] = [
    {
      imageUrl:"https://images.unsplash.com/photo-1613771404784-3a5686aa2be3?w=600&h=600&fit=crop",
      itemId: 5,
      name: "Limited Edition Pokemon Card Collection",
      userPrice: 2000,
      condition:"new",
      systemGeneratedPrice:2500
    },
    {
      imageUrl:"https://images.unsplash.com/photo-1473968512647-3e447244af8f?w=600&h=600&fit=crop",
      itemId: 6,
      name: "DJI Mini 3 Pro Drone with RC Controller",
      userPrice: 156000,
      condition:"new",
      systemGeneratedPrice:148000
    },
  ]  
export const tradeOffer:TradeOfferResponse={
    tradeOfferId:1,
    senderId:122,
    receiverId:121,
    senderItems:mockMyItems,
    receiverItems:mockTheirItems,
    cashAdjustment:1800,
    status:"NEGOTIATING",
    createdAt:"11:56",
}
export const TradeDetailsDemo:TradeDetailsResponse={
    tradeId:1,
    initiatorId:1234,
    receiverId:5678,
    initiatorName:"libtard",
    receiverName:"republitard",
    status:"NEGOTIATING",
    createdAt:"08:50",
    offer:tradeOffer,
    history:history,
}
export const mockTrades: TradeInboxResponse[] = [
  {
    tradeId: 12,
    otherUserId:123,
    otherUserName: "Alex Morgan",
    status: "NEGOTIATING" as const,
    yourItemCount: 2,
    theirItemCount: 3,
    valueDifference: 1500,
    updatedAt: "2h ago",
    latestOffer: tradeOffer
  },
  {
    tradeId: 15,
    otherUserId:124,
    otherUserName: "Sarah Chen",
    status: "OPEN" as const,
    yourItemCount: 1,
    theirItemCount: 1,
    valueDifference: 0,
    updatedAt: "5h ago",
    latestOffer: tradeOffer
  },
];