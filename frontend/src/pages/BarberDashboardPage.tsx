import { useQuery } from '@tanstack/react-query'
import { Calendar, Clock, User, Scissors, CheckCircle, TrendingUp, Award } from 'lucide-react'
import { bookingService } from '../services/bookingService'
import { format, parseISO, isToday, isTomorrow } from 'date-fns'
import { ptBR } from 'date-fns/locale'
import { useState } from 'react'

export default function BarberDashboardPage() {
  const [viewMode, setViewMode] = useState<'today' | 'upcoming' | 'all'>('today')

  const { data: allBookings, isLoading } = useQuery({
    queryKey: ['barber-all-bookings'],
    queryFn: bookingService.getBarberBookings,
  })

  const { data: upcomingBookings } = useQuery({
    queryKey: ['barber-upcoming-bookings'],
    queryFn: bookingService.getBarberUpcomingBookings,
  })

  if (isLoading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Carregando sua agenda...</p>
        </div>
      </div>
    )
  }

  // Calculate statistics
  const stats = {
    today: allBookings?.filter(b => {
      const bookingDate = parseISO(b.bookingDate)
      return isToday(bookingDate) && b.status !== 'CANCELLED'
    }).length || 0,
    upcoming: upcomingBookings?.length || 0,
    completed: allBookings?.filter(b => b.status === 'COMPLETED').length || 0,
    total: allBookings?.length || 0,
    revenue: allBookings?.filter(b => b.status === 'COMPLETED').reduce((sum, b) => sum + b.service.price, 0) || 0,
  }

  // Filter bookings based on view mode
  const getFilteredBookings = () => {
    if (!allBookings) return []
    
    switch (viewMode) {
      case 'today':
        return allBookings.filter(b => {
          const bookingDate = parseISO(b.bookingDate)
          return isToday(bookingDate) && b.status !== 'CANCELLED'
        }).sort((a, b) => {
          const timeA = a.bookingTime.split(':').map(Number)
          const timeB = b.bookingTime.split(':').map(Number)
          return (timeA[0] * 60 + timeA[1]) - (timeB[0] * 60 + timeB[1])
        })
      case 'upcoming':
        return upcomingBookings || []
      default:
        return allBookings
    }
  }

  const filteredBookings = getFilteredBookings()

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-4xl font-bold mb-2 text-gray-900">Minha Agenda</h1>
            <p className="text-gray-600">Gerencie seus agendamentos e serviços</p>
          </div>
          <div className="flex items-center space-x-2 bg-primary-100 px-4 py-2 rounded-lg">
            <Scissors className="w-6 h-6 text-primary-600" />
            <span className="font-semibold text-primary-600">Barbeiro</span>
          </div>
        </div>
      </div>

      {/* Statistics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <div className="bg-gradient-to-br from-primary-500 to-primary-600 text-white rounded-xl p-6 shadow-lg">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-primary-100 text-sm font-medium mb-1">Hoje</p>
              <p className="text-3xl font-bold">{stats.today}</p>
              <p className="text-primary-100 text-xs mt-1">agendamentos</p>
            </div>
            <Calendar className="w-12 h-12 opacity-20" />
          </div>
        </div>

        <div className="bg-gradient-to-br from-green-500 to-green-600 text-white rounded-xl p-6 shadow-lg">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-green-100 text-sm font-medium mb-1">Próximos</p>
              <p className="text-3xl font-bold">{stats.upcoming}</p>
              <p className="text-green-100 text-xs mt-1">agendamentos</p>
            </div>
            <TrendingUp className="w-12 h-12 opacity-20" />
          </div>
        </div>

        <div className="bg-gradient-to-br from-blue-500 to-blue-600 text-white rounded-xl p-6 shadow-lg">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-blue-100 text-sm font-medium mb-1">Concluídos</p>
              <p className="text-3xl font-bold">{stats.completed}</p>
              <p className="text-blue-100 text-xs mt-1">serviços</p>
            </div>
            <CheckCircle className="w-12 h-12 opacity-20" />
          </div>
        </div>

        <div className="bg-gradient-to-br from-purple-500 to-purple-600 text-white rounded-xl p-6 shadow-lg">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-purple-100 text-sm font-medium mb-1">Receita</p>
              <p className="text-2xl font-bold">R$ {stats.revenue.toFixed(2)}</p>
              <p className="text-purple-100 text-xs mt-1">total</p>
            </div>
            <Award className="w-12 h-12 opacity-20" />
          </div>
        </div>
      </div>

      {/* View Mode Tabs */}
      <div className="card mb-6">
        <div className="flex space-x-2 border-b border-gray-200">
          <button
            onClick={() => setViewMode('today')}
            className={`px-6 py-3 font-medium transition ${
              viewMode === 'today'
                ? 'text-primary-600 border-b-2 border-primary-600'
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            Hoje ({stats.today})
          </button>
          <button
            onClick={() => setViewMode('upcoming')}
            className={`px-6 py-3 font-medium transition ${
              viewMode === 'upcoming'
                ? 'text-primary-600 border-b-2 border-primary-600'
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            Próximos ({stats.upcoming})
          </button>
          <button
            onClick={() => setViewMode('all')}
            className={`px-6 py-3 font-medium transition ${
              viewMode === 'all'
                ? 'text-primary-600 border-b-2 border-primary-600'
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            Todos ({stats.total})
          </button>
        </div>
      </div>

      {/* Bookings List */}
      <div>
        {filteredBookings.length > 0 ? (
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {filteredBookings.map((booking) => {
              const bookingDate = parseISO(booking.bookingDate)
              const isTodayBooking = isToday(bookingDate)
              const isTomorrowBooking = isTomorrow(bookingDate)

              return (
                <div
                  key={booking.id}
                  className={`card transition-all hover:shadow-xl ${
                    isTodayBooking ? 'border-2 border-primary-500 bg-primary-50' : ''
                  }`}
                >
                  {/* Header */}
                  <div className="flex items-start justify-between mb-4">
                    <div className="flex-1">
                      <h3 className="font-bold text-lg text-gray-900 mb-1">
                        {booking.service.name}
                      </h3>
                      <p className="text-sm text-gray-600">{booking.service.description}</p>
                    </div>
                    <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
                      booking.status === 'CONFIRMED' ? 'bg-green-100 text-green-800' :
                      booking.status === 'PENDING' ? 'bg-yellow-100 text-yellow-800' :
                      booking.status === 'CANCELLED' ? 'bg-red-100 text-red-800' :
                      'bg-blue-100 text-blue-800'
                    }`}>
                      {booking.status === 'CONFIRMED' ? 'Confirmado' :
                       booking.status === 'PENDING' ? 'Pendente' :
                       booking.status === 'CANCELLED' ? 'Cancelado' :
                       'Concluído'}
                    </span>
                  </div>

                  {/* Date & Time */}
                  <div className="space-y-2 mb-4">
                    <div className="flex items-center text-gray-700">
                      <Calendar className={`w-4 h-4 mr-2 ${isTodayBooking ? 'text-primary-600' : 'text-gray-400'}`} />
                      <span className={`font-medium ${isTodayBooking ? 'text-primary-600' : ''}`}>
                        {isTodayBooking ? 'Hoje' :
                         isTomorrowBooking ? 'Amanhã' :
                         format(bookingDate, "dd 'de' MMMM", { locale: ptBR })}
                      </span>
                    </div>
                    <div className="flex items-center text-gray-700">
                      <Clock className="w-4 h-4 mr-2 text-gray-400" />
                      <span className="font-semibold">{booking.bookingTime}</span>
                    </div>
                    <div className="flex items-center text-gray-700">
                      <User className="w-4 h-4 mr-2 text-gray-400" />
                      <span className="text-sm">Cliente: {booking.userId.substring(0, 8)}...</span>
                    </div>
                  </div>

                  {/* Service Details */}
                  <div className="border-t border-gray-200 pt-4">
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="text-xs text-gray-500 mb-1">Duração</p>
                        <p className="font-medium">{booking.service.durationMinutes} min</p>
                      </div>
                      <div className="text-right">
                        <p className="text-xs text-gray-500 mb-1">Valor</p>
                        <p className="text-lg font-bold text-primary-600">
                          R$ {booking.service.price.toFixed(2)}
                        </p>
                      </div>
                    </div>
                  </div>

                  {/* Notes */}
                  {booking.notes && (
                    <div className="mt-4 p-3 bg-gray-50 rounded-lg">
                      <p className="text-xs font-semibold text-gray-600 mb-1">Observações:</p>
                      <p className="text-sm text-gray-700">{booking.notes}</p>
                    </div>
                  )}

                  {/* Actions */}
                  {booking.status === 'PENDING' && (
                    <div className="mt-4 pt-4 border-t border-gray-200">
                      <button className="w-full btn btn-primary text-sm py-2">
                        Confirmar Agendamento
                      </button>
                    </div>
                  )}
                </div>
              )
            })}
          </div>
        ) : (
          <div className="card text-center py-12">
            <Scissors className="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <p className="text-gray-600 text-lg mb-2">
              {viewMode === 'today' && 'Nenhum agendamento para hoje'}
              {viewMode === 'upcoming' && 'Nenhum agendamento próximo'}
              {viewMode === 'all' && 'Nenhum agendamento encontrado'}
            </p>
            <p className="text-gray-400 text-sm">Sua agenda está livre!</p>
          </div>
        )}
      </div>
    </div>
  )
}
