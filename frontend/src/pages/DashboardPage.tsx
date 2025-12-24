import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Calendar, Clock, Plus } from 'lucide-react'
import { bookingService } from '../services/bookingService'
import { useAuthStore } from '../store/authStore'
import { format } from 'date-fns'
import { ptBR } from 'date-fns/locale'

export default function DashboardPage() {
  const { user } = useAuthStore()

  const { data: upcomingBookings, isLoading } = useQuery({
    queryKey: ['upcomingBookings'],
    queryFn: bookingService.getUpcomingBookings,
  })

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Welcome Section */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold mb-2">
          Olá, {user?.fullName}!
        </h1>
        <p className="text-gray-600">
          Bem-vindo ao seu painel de controle
        </p>
      </div>

      {/* Stats */}
      <div className="grid md:grid-cols-3 gap-6 mb-8">
        <div className="card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-600 text-sm">Próximos Agendamentos</p>
              <p className="text-3xl font-bold text-primary-600">
                {upcomingBookings?.length || 0}
              </p>
            </div>
            <Calendar className="w-12 h-12 text-primary-600 opacity-20" />
          </div>
        </div>

        <div className="card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-600 text-sm">Status da Conta</p>
              <p className="text-lg font-semibold text-green-600">
                {user?.emailVerified ? 'Verificada' : 'Pendente'}
              </p>
            </div>
            <div className={`w-12 h-12 rounded-full ${user?.emailVerified ? 'bg-green-100' : 'bg-yellow-100'}`}></div>
          </div>
        </div>

        <Link to="/bookings/new" className="card hover:shadow-lg transition cursor-pointer bg-primary-600 text-white">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-primary-100 text-sm">Novo Agendamento</p>
              <p className="text-lg font-semibold">Agendar Horário</p>
            </div>
            <Plus className="w-12 h-12 opacity-50" />
          </div>
        </Link>
      </div>

      {/* Upcoming Bookings */}
      <div className="card">
        <h2 className="text-2xl font-bold mb-6">Próximos Agendamentos</h2>
        
        {isLoading ? (
          <div className="text-center py-8">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
          </div>
        ) : upcomingBookings && upcomingBookings.length > 0 ? (
          <div className="space-y-4">
            {upcomingBookings.map((booking) => (
              <div key={booking.id} className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition">
                <div className="flex justify-between items-start">
                  <div className="flex-1">
                    <h3 className="font-semibold text-lg">{booking.service.name}</h3>
                    <p className="text-gray-600">{booking.service.description}</p>
                    <div className="flex items-center space-x-4 mt-2 text-sm text-gray-500">
                      <span className="flex items-center">
                        <Calendar className="w-4 h-4 mr-1" />
                        {format(new Date(booking.bookingDate), "dd 'de' MMMM 'de' yyyy", { locale: ptBR })}
                      </span>
                      <span className="flex items-center">
                        <Clock className="w-4 h-4 mr-1" />
                        {booking.bookingTime}
                      </span>
                    </div>
                  </div>
                  <div className="text-right">
                    <span className="inline-block px-3 py-1 rounded-full text-sm font-medium bg-green-100 text-green-800">
                      {booking.status}
                    </span>
                    <p className="text-2xl font-bold text-primary-600 mt-2">
                      R$ {booking.service.price.toFixed(2)}
                    </p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-12">
            <Calendar className="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <p className="text-gray-600 mb-4">Você não tem agendamentos próximos</p>
            <Link to="/bookings/new" className="btn btn-primary">
              Agendar Agora
            </Link>
          </div>
        )}
      </div>
    </div>
  )
}



