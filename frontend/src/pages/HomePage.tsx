import { Link } from 'react-router-dom'
import { Calendar, Clock, Star, Scissors } from 'lucide-react'
import { useAuthStore } from '../store/authStore'

export default function HomePage() {
  const { isAuthenticated } = useAuthStore()
  const authenticated = isAuthenticated()

  return (
    <div>
      {/* Hero Section */}
      <section className="bg-gradient-to-r from-primary-600 to-primary-800 text-white py-20">
        <div className="container mx-auto px-4 text-center">
          <h1 className="text-5xl font-bold mb-6">
            Bem-vindo à BarberShop
          </h1>
          <p className="text-xl mb-8 max-w-2xl mx-auto">
            Agende seu corte online e tenha a melhor experiência em cuidados masculinos
          </p>
          <div className="flex justify-center space-x-4">
            {authenticated ? (
              <>
                <Link to="/bookings/new" className="bg-white text-primary-600 px-8 py-3 rounded-lg font-semibold hover:bg-gray-100 transition">
                  Agendar Agora
                </Link>
                <Link to="/services" className="bg-primary-700 text-white px-8 py-3 rounded-lg font-semibold hover:bg-primary-800 transition border-2 border-white">
                  Ver Serviços
                </Link>
              </>
            ) : (
              <>
                <Link to="/register" className="bg-white text-primary-600 px-8 py-3 rounded-lg font-semibold hover:bg-gray-100 transition">
                  Cadastre-se Grátis
                </Link>
                <Link to="/services" className="bg-primary-700 text-white px-8 py-3 rounded-lg font-semibold hover:bg-primary-800 transition border-2 border-white">
                  Ver Serviços
                </Link>
              </>
            )}
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="py-16 bg-white">
        <div className="container mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-12">
            Por que escolher a BarberShop?
          </h2>
          <div className="grid md:grid-cols-3 gap-8">
            <div className="text-center">
              <div className="bg-primary-100 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
                <Calendar className="w-8 h-8 text-primary-600" />
              </div>
              <h3 className="text-xl font-semibold mb-2">Agendamento Fácil</h3>
              <p className="text-gray-600">
                Agende seu horário em poucos cliques, 24/7
              </p>
            </div>
            
            <div className="text-center">
              <div className="bg-primary-100 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
                <Star className="w-8 h-8 text-primary-600" />
              </div>
              <h3 className="text-xl font-semibold mb-2">Profissionais Qualificados</h3>
              <p className="text-gray-600">
                Barbeiros experientes e especializados
              </p>
            </div>
            
            <div className="text-center">
              <div className="bg-primary-100 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
                <Clock className="w-8 h-8 text-primary-600" />
              </div>
              <h3 className="text-xl font-semibold mb-2">Sem Espera</h3>
              <p className="text-gray-600">
                Chegue na hora marcada e seja atendido
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-16 bg-gray-100">
        <div className="container mx-auto px-4 text-center">
          <Scissors className="w-16 h-16 text-primary-600 mx-auto mb-4" />
          <h2 className="text-3xl font-bold mb-4">
            Pronto para transformar seu visual?
          </h2>
          <p className="text-xl text-gray-600 mb-8">
            Agende agora e experimente o melhor serviço de barbearia
          </p>
          {authenticated ? (
            <Link to="/bookings/new" className="btn btn-primary text-lg px-8 py-3 inline-block">
              Agendar Agora
            </Link>
          ) : (
            <Link to="/register" className="btn btn-primary text-lg px-8 py-3 inline-block">
              Começar Agora
            </Link>
          )}
        </div>
      </section>
    </div>
  )
}



