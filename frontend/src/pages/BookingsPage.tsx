import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { Calendar, Clock, Plus, X } from 'lucide-react'
import toast from 'react-hot-toast'
import { format } from 'date-fns'
import { ptBR } from 'date-fns/locale'
import { bookingService } from '../services/bookingService'

export default function BookingsPage() {
  const queryClient = useQueryClient()

  const { data: bookings, isLoading } = useQuery({
    queryKey: ['myBookings'],
    queryFn: bookingService.getMyBookings,
  })

  const cancelMutation = useMutation({
    mutationFn: bookingService.cancelBooking,
    onSuccess: () => {
      toast.success('Agendamento cancelado com sucesso')
      queryClient.invalidateQueries({ queryKey: ['myBookings'] })
      queryClient.invalidateQueries({ queryKey: ['upcomingBookings'] })
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.error || 'Erro ao cancelar agendamento')
    },
  })

  const handleCancel = (id: string) => {
    if (confirm('Tem certeza que deseja cancelar este agendamento?')) {
      cancelMutation.mutate(id)
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800'
      case 'CONFIRMED':
        return 'bg-green-100 text-green-800'
      case 'CANCELLED':
        return 'bg-red-100 text-red-800'
      case 'COMPLETED':
        return 'bg-blue-100 text-blue-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  const getStatusLabel = (status: string) => {
    switch (status) {
      case 'PENDING':
        return 'Pendente'
      case 'CONFIRMED':
        return 'Confirmado'
      case 'CANCELLED':
        return 'Cancelado'
      case 'COMPLETED':
        return 'Concluído'
      default:
        return status
    }
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl font-bold mb-2">Meus Agendamentos</h1>
          <p className="text-gray-600">Gerencie seus horários marcados</p>
        </div>
        <Link to="/bookings/new" className="btn btn-primary flex items-center space-x-2">
          <Plus className="w-5 h-5" />
          <span>Novo Agendamento</span>
        </Link>
      </div>

      {isLoading ? (
        <div className="text-center py-20">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
        </div>
      ) : bookings && bookings.length > 0 ? (
        <div className="space-y-4">
          {bookings.map((booking) => (
            <div key={booking.id} className="card hover:shadow-lg transition">
              <div className="flex justify-between items-start">
                <div className="flex-1">
                  <div className="flex items-start justify-between mb-3">
                    <h3 className="font-semibold text-xl">{booking.service.name}</h3>
                    <span className={`px-3 py-1 rounded-full text-sm font-medium ${getStatusColor(booking.status)}`}>
                      {getStatusLabel(booking.status)}
                    </span>
                  </div>
                  
                  <p className="text-gray-600 mb-3">{booking.service.description}</p>
                  
                  <div className="flex flex-wrap gap-4 text-sm text-gray-700">
                    <span className="flex items-center">
                      <Calendar className="w-4 h-4 mr-2 text-primary-600" />
                      {format(new Date(booking.bookingDate), "dd 'de' MMMM 'de' yyyy", { locale: ptBR })}
                    </span>
                    <span className="flex items-center">
                      <Clock className="w-4 h-4 mr-2 text-primary-600" />
                      {booking.bookingTime}
                    </span>
                    <span className="font-semibold text-primary-600">
                      R$ {booking.service.price.toFixed(2)}
                    </span>
                  </div>

                  {booking.notes && (
                    <div className="mt-3 p-3 bg-gray-50 rounded-lg">
                      <p className="text-sm text-gray-600">
                        <strong>Observações:</strong> {booking.notes}
                      </p>
                    </div>
                  )}
                </div>

                {(booking.status === 'PENDING' || booking.status === 'CONFIRMED') && (
                  <button
                    onClick={() => handleCancel(booking.id)}
                    className="btn btn-danger ml-4 flex items-center space-x-2"
                    disabled={cancelMutation.isPending}
                  >
                    <X className="w-4 h-4" />
                    <span>Cancelar</span>
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="text-center py-20 card">
          <Calendar className="w-20 h-20 text-gray-300 mx-auto mb-4" />
          <h3 className="text-2xl font-bold mb-2">Nenhum agendamento encontrado</h3>
          <p className="text-gray-600 mb-6">
            Você ainda não fez nenhum agendamento
          </p>
          <Link to="/bookings/new" className="btn btn-primary inline-flex items-center space-x-2">
            <Plus className="w-5 h-5" />
            <span>Fazer Primeiro Agendamento</span>
          </Link>
        </div>
      )}
    </div>
  )
}



