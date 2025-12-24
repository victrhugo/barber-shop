import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { Clock, DollarSign } from 'lucide-react'
import { bookingService } from '../services/bookingService'
import { useAuthStore } from '../store/authStore'

export default function ServicesPage() {
  const { isAuthenticated } = useAuthStore()

  const { data: services, isLoading, error } = useQuery({
    queryKey: ['services'],
    queryFn: bookingService.getServices,
    retry: 2,
  })

  if (isLoading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center py-20">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Carregando serviços...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center py-20">
          <div className="text-red-500 mb-4">
            <p className="text-xl font-semibold">Erro ao carregar serviços</p>
            <p className="text-sm mt-2">Por favor, tente novamente mais tarde.</p>
          </div>
          <button
            onClick={() => window.location.reload()}
            className="btn btn-primary mt-4"
          >
            Recarregar
          </button>
        </div>
      </div>
    )
  }

  if (!services || services.length === 0) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center py-20">
          <h1 className="text-4xl font-bold mb-4">Nossos Serviços</h1>
          <p className="text-xl text-gray-600 mb-8">
            Nenhum serviço disponível no momento.
          </p>
        </div>
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="text-center mb-12">
        <h1 className="text-4xl font-bold mb-4">Nossos Serviços</h1>
        <p className="text-xl text-gray-600">
          Escolha o serviço ideal para você
        </p>
      </div>

      <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6 max-w-6xl mx-auto">
        {services.map((service) => (
          <div key={service.id} className="card hover:shadow-xl transition">
            <div className="mb-4">
              <h3 className="text-2xl font-bold mb-2">{service.name}</h3>
              <p className="text-gray-600">{service.description}</p>
            </div>

            <div className="space-y-2 mb-6">
              <div className="flex items-center text-gray-700">
                <Clock className="w-5 h-5 mr-2 text-primary-600" />
                <span>{service.durationMinutes} minutos</span>
              </div>
              <div className="flex items-center text-gray-700">
                <DollarSign className="w-5 h-5 mr-2 text-primary-600" />
                <span className="text-2xl font-bold text-primary-600">
                  R$ {service.price.toFixed(2)}
                </span>
              </div>
            </div>

            {isAuthenticated() ? (
              <Link
                to="/bookings/new"
                state={{ selectedService: service }}
                className="btn btn-primary w-full"
              >
                Agendar Agora
              </Link>
            ) : (
              <Link to="/register" className="btn btn-primary w-full">
                Cadastre-se para Agendar
              </Link>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}

