export interface PublicUserProfile {
  id: number
  name: string
  avatarUrl: string | null
  trustScore: number
  totalListings: number
  successfulTrades: number
}

export interface MyUserProfile extends PublicUserProfile {
  email: string
  phoneNo: string | null
}
