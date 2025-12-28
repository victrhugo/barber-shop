import api from '../lib/api'

export interface RegisterData {
  email: string
  password: string
  fullName: string
  phone?: string
}

export interface LoginData {
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  userId: string
  email: string
  fullName: string
  role: string
  emailVerified: boolean
}

export const authService = {
  register: async (data: RegisterData): Promise<AuthResponse> => {
    const response = await api.post('/auth/register', data)
    return response.data
  },

  login: async (data: LoginData): Promise<AuthResponse> => {
    const response = await api.post('/auth/login', data)
    return response.data
  },

  verifyEmail: async (token: string): Promise<void> => {
    await api.get(`/auth/verify/${token}`)
  },

  resendVerification: async (email: string): Promise<void> => {
    await api.post('/auth/resend-verification', { email })
  },

  createBarber: async (data: {
    email: string
    password: string
    fullName: string
    phone?: string
    bio?: string
    specialties?: string[]
    role?: string // Optional: "BARBER" or "ADMIN"
  }): Promise<any> => {
    const response = await api.post('/auth/admin/barbers', data)
    return response.data
  },
}



