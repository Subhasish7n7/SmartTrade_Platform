export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  name: string
  email: string
  password: string
  phone_no?: string
}

export interface AuthResponse {
  token: string
}

export interface CurrentUser {
  id: number
  name: string
  email: string
  phoneNo?: string
}