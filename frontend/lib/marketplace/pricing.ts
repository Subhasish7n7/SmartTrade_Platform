export function calculatePriceDiff(
  userPrice: number,
  aiEstimate: number
) {
  return ((userPrice - aiEstimate) / aiEstimate) * 100
}

export function calculateSavings(
  originalPrice: number,
  currentPrice: number
) {
  return originalPrice - currentPrice
}