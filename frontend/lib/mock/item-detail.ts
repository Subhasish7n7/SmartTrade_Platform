import { ItemResponse } from "@/lib/types/api/item"

export const demoItem: ItemResponse = {
  itemId: 1,
  itemName: "ASUS ROG Zephyrus G14 Gaming Laptop",
  description:
    "Powerful gaming laptop with RTX 4060, Ryzen 9, 16GB RAM and 1TB SSD. Excellent condition with minimal wear.",
  imageUrls: ["https://images.unsplash.com/photo-1603302576837-37561b2e2302?w=600&h=600&fit=crop",],
  category: "Gaming Laptops",
  labels: [
    "RTX 4060",
    "Ryzen 9",
    "Gaming",
  ],
  userPrice: 1150,
  generatedPrice: 1280,
  newPrice: 1599,
  condition: "minimal-wear",
  forTrade: true,
  forSale: true,
  createdAt: "2 days ago",
  available: true,
  latitude: 81276123,
  longitude: 928137619,
  city: "new york",
  state: "New York",
  locality: "manhattan",
  seller: {
    id: 1,
    name: "TechTrader_Mike",
    trustScore: 96,
    totalListings: 234,
    successfulTrades: 228,
  },
}