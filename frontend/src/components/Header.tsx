import { Link, useNavigate } from 'react-router-dom'
import { Scissors, User, LogOut, Calendar } from 'lucide-react'
import { useAuthStore } from '../store/authStore'

export default function Header() {
  const navigate = useNavigate()
  const { isAuthenticated, user, logout } = useAuthStore()

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  return (
    <header className="bg-white shadow-md">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center space-x-2">
            <Scissors className="w-8 h-8 text-primary-600" />
            <span className="text-2xl font-bold text-gray-900">BarberShop</span>
          </Link>

          {/* Navigation */}
          <nav className="hidden md:flex items-center space-x-6">
            <Link to="/services" className="text-gray-700 hover:text-primary-600 transition">
              Serviços
            </Link>
            {isAuthenticated() && (
              <>
                {user?.role === 'ADMIN' && (
                  <Link to="/admin/dashboard" className="text-gray-700 hover:text-primary-600 transition">
                    Admin
                  </Link>
                )}
                {user?.role === 'BARBER' && (
                  <Link to="/barber/dashboard" className="text-gray-700 hover:text-primary-600 transition">
                    Painel Barbeiro
                  </Link>
                )}
                {user?.role === 'USER' && (
                  <>
                    <Link to="/dashboard" className="text-gray-700 hover:text-primary-600 transition">
                      Dashboard
                    </Link>
                    <Link to="/bookings" className="text-gray-700 hover:text-primary-600 transition">
                      Meus Agendamentos
                    </Link>
                  </>
                )}
              </>
            )}
          </nav>

          {/* User Menu */}
          <div className="flex items-center space-x-4">
            {isAuthenticated() ? (
              <>
                <Link 
                  to="/bookings/new" 
                  className="btn btn-primary flex items-center space-x-2"
                >
                  <Calendar className="w-4 h-4" />
                  <span>Agendar</span>
                </Link>
                
                <div className="relative group">
                  <button className="flex items-center space-x-2 text-gray-700 hover:text-primary-600">
                    <User className="w-5 h-5" />
                    <span className="hidden md:block">{user?.fullName}</span>
                  </button>
                  
                  <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg py-2 invisible group-hover:visible opacity-0 group-hover:opacity-100 transition-all">
                    <Link 
                      to="/profile" 
                      className="block px-4 py-2 text-gray-700 hover:bg-gray-100"
                    >
                      Meu Perfil
                    </Link>
                    <button
                      onClick={handleLogout}
                      className="w-full text-left px-4 py-2 text-red-600 hover:bg-gray-100 flex items-center space-x-2"
                    >
                      <LogOut className="w-4 h-4" />
                      <span>Sair</span>
                    </button>
                  </div>
                </div>
              </>
            ) : (
              <>
                <Link to="/login" className="btn btn-secondary">
                  Entrar
                </Link>
                <Link to="/register" className="btn btn-primary">
                  Cadastrar
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </header>
  )
}



