import api from '../lib/api'

export interface Service {
  id: string
  name: string
  description: string
  durationMinutes: number
  price: number
  active: boolean
}

export interface Barber {
  id: string
  userId: string
  fullName: string
  specialties?: string[]
  bio?: string
  rating: number
  active: boolean
}

export interface Booking {
  id: string
  userId: string
  barberId?: string
  barberName?: string
  barber?: Barber
  service: Service
  bookingDate: string
  bookingTime: string
  status: string
  notes?: string
  createdAt: string
}

export interface CreateBookingData {
  serviceId: string
  bookingDate: string
  bookingTime: string
  barberId?: string
  notes?: string
}

export const bookingService = {
  // Services
  getServices: async (): Promise<Service[]> => {
    try {
      const response = await api.get('/services')
      return response.data || []
    } catch (error: any) {
      console.error('Erro ao buscar serviços:', error)
      throw error
    }
  },

  getServiceById: async (id: string): Promise<Service> => {
    const response = await api.get(`/services/${id}`)
    return response.data
  },

  // Bookings
  createBooking: async (data: CreateBookingData): Promise<Booking> => {
    const response = await api.post('/bookings', data)
    return response.data
  },

  getMyBookings: async (): Promise<Booking[]> => {
    const response = await api.get('/bookings/my-bookings')
    return response.data
  },

  getUpcomingBookings: async (): Promise<Booking[]> => {
    const response = await api.get('/bookings/upcoming')
    return response.data
  },

  cancelBooking: async (id: string): Promise<Booking> => {
    const response = await api.put(`/bookings/${id}/cancel`)
    return response.data
  },

  deleteBooking: async (id: string): Promise<void> => {
    await api.delete(`/bookings/${id}`)
  },

  // Barbers
  getBarbers: async (): Promise<Barber[]> => {
    const response = await api.get('/barbers')
    return response.data
  },

  // Barber endpoints
  getBarberBookings: async (): Promise<Booking[]> => {
    const response = await api.get('/bookings/barber/my-bookings')
    return response.data
  },

  getBarberUpcomingBookings: async (): Promise<Booking[]> => {
    const response = await api.get('/bookings/barber/upcoming')
    return response.data
  },

  // Admin endpoints
  getAllBookings: async (): Promise<Booking[]> => {
    const response = await api.get('/bookings/admin/all')
    return response.data
  },
}

