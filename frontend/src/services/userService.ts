import api from '../lib/api'

export interface User {
  id: string
  email: string
  fullName: string
  phone?: string
  role: string
  emailVerified: boolean
  createdAt: string
}

export interface UpdateUserData {
  fullName?: string
  phone?: string
}

export const userService = {
  getMe: async (): Promise<User> => {
    const response = await api.get('/users/me')
    return response.data
  },

  updateMe: async (data: UpdateUserData): Promise<User> => {
    const response = await api.put('/users/me', data)
    return response.data
  },

  deleteMe: async (): Promise<void> => {
    await api.delete('/users/me')
  },
}



