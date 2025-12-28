import { useState } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { useQuery, useMutation } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import toast from 'react-hot-toast'
import { Clock, DollarSign } from 'lucide-react'
import { bookingService, CreateBookingData } from '../services/bookingService'

export default function NewBookingPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const selectedService = location.state?.selectedService

  const [selectedServiceId, setSelectedServiceId] = useState(selectedService?.id || '')

  const { data: services } = useQuery({
    queryKey: ['services'],
    queryFn: bookingService.getServices,
  })

  const { data: barbers, isLoading: isLoadingBarbers, error: barbersError } = useQuery({
    queryKey: ['barbers'],
    queryFn: bookingService.getBarbers,
    retry: 2,
    refetchOnWindowFocus: true,
  })

  const { register, handleSubmit, formState: { errors }, watch } = useForm<CreateBookingData>({
    defaultValues: {
      serviceId: selectedServiceId,
    },
  })

  const createMutation = useMutation({
    mutationFn: bookingService.createBooking,
    onSuccess: () => {
      toast.success('Agendamento criado com sucesso!')
      navigate('/bookings')
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.error || 'Erro ao criar agendamento')
    },
  })

  const onSubmit = (data: CreateBookingData) => {
    createMutation.mutate(data)
  }

  const watchServiceId = watch('serviceId')
  const currentService = services?.find(s => s.id === watchServiceId)

  // Generate time slots (9:00 - 18:00, 30min intervals)
  const timeSlots = []
  for (let hour = 9; hour < 18; hour++) {
    timeSlots.push(`${hour.toString().padStart(2, '0')}:00`)
    timeSlots.push(`${hour.toString().padStart(2, '0')}:30`)
  }

  // Get minimum date (tomorrow)
  const tomorrow = new Date()
  tomorrow.setDate(tomorrow.getDate() + 1)
  const minDate = tomorrow.toISOString().split('T')[0]

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-2xl mx-auto">
        <h1 className="text-3xl font-bold mb-8">Novo Agendamento</h1>

        <div className="card">
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            {/* Service Selection */}
            <div>
              <label className="block text-sm font-medium mb-2">Serviço</label>
              <select
                className="input"
                {...register('serviceId', { required: 'Selecione um serviço' })}
                onChange={(e) => setSelectedServiceId(e.target.value)}
              >
                <option value="">Selecione um serviço</option>
                {services?.map((service) => (
                  <option key={service.id} value={service.id}>
                    {service.name} - R$ {service.price.toFixed(2)}
                  </option>
                ))}
              </select>
              {errors.serviceId && (
                <p className="text-red-500 text-sm mt-1">{errors.serviceId.message}</p>
              )}
            </div>

            {/* Service Details */}
            {currentService && (
              <div className="bg-primary-50 border border-primary-200 rounded-lg p-4">
                <h3 className="font-semibold text-lg mb-2">{currentService.name}</h3>
                <p className="text-gray-600 mb-3">{currentService.description}</p>
                <div className="flex items-center space-x-4 text-sm">
                  <span className="flex items-center">
                    <Clock className="w-4 h-4 mr-1 text-primary-600" />
                    {currentService.durationMinutes} min
                  </span>
                  <span className="flex items-center font-semibold text-primary-600">
                    <DollarSign className="w-4 h-4 mr-1" />
                    R$ {currentService.price.toFixed(2)}
                  </span>
                </div>
              </div>
            )}

            {/* Date Selection */}
            <div>
              <label className="block text-sm font-medium mb-2">Data</label>
              <input
                type="date"
                className="input"
                min={minDate}
                {...register('bookingDate', { required: 'Selecione uma data' })}
              />
              {errors.bookingDate && (
                <p className="text-red-500 text-sm mt-1">{errors.bookingDate.message}</p>
              )}
            </div>

            {/* Time Selection */}
            <div>
              <label className="block text-sm font-medium mb-2">Horário</label>
              <select
                className="input"
                {...register('bookingTime', { required: 'Selecione um horário' })}
              >
                <option value="">Selecione um horário</option>
                {timeSlots.map((time) => (
                  <option key={time} value={time}>
                    {time}
                  </option>
                ))}
              </select>
              {errors.bookingTime && (
                <p className="text-red-500 text-sm mt-1">{errors.bookingTime.message}</p>
              )}
            </div>

            {/* Barber Selection */}
            <div>
              <label className="block text-sm font-medium mb-2">
                Escolher Barbeiro
              </label>
              {isLoadingBarbers ? (
                <div className="input text-gray-500">Carregando barbeiros...</div>
              ) : barbersError ? (
                <div className="input text-red-500 border-red-300">
                  Erro ao carregar barbeiros. Você ainda pode agendar e o sistema atribuirá automaticamente.
                </div>
              ) : (
                <>
                  <select
                    className="input"
                    {...register('barberId')}
                  >
                    <option value="">🔄 Atribuir automaticamente</option>
                    {barbers && barbers.length > 0 ? (
                      barbers.map((barber) => (
                        <option key={barber.id} value={barber.id}>
                          ✂️ {barber.fullName || `Barbeiro ${barber.id.substring(0, 8)}`}
                          {barber.specialties && barber.specialties.length > 0 && ` - ${barber.specialties.join(', ')}`}
                        </option>
                      ))
                    ) : (
                      <option value="">Nenhum barbeiro disponível</option>
                    )}
                  </select>
                  <p className="text-sm text-gray-500 mt-1">
                    {barbers && barbers.length > 0 
                      ? "Escolha um barbeiro específico ou deixe em 'Atribuir automaticamente' para o sistema escolher um barbeiro disponível."
                      : "Nenhum barbeiro cadastrado. O sistema tentará atribuir automaticamente quando houver barbeiros disponíveis."}
                  </p>
                </>
              )}
            </div>

            {/* Notes */}
            <div>
              <label className="block text-sm font-medium mb-2">
                Observações (Opcional)
              </label>
              <textarea
                className="input"
                rows={3}
                placeholder="Alguma preferência ou observação especial?"
                {...register('notes')}
              />
            </div>

            {/* Submit Button */}
            <div className="flex space-x-4">
              <button
                type="button"
                onClick={() => navigate(-1)}
                className="btn btn-secondary flex-1"
              >
                Cancelar
              </button>
              <button
                type="submit"
                className="btn btn-primary flex-1"
                disabled={createMutation.isPending}
              >
                {createMutation.isPending ? 'Agendando...' : 'Confirmar Agendamento'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}

