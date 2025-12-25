import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Calendar, Clock, Users, Scissors, TrendingUp, DollarSign, Search, CheckCircle, XCircle, BarChart3, Plus, Edit, Trash2 } from 'lucide-react'
import { bookingService, UpdateBarberData } from '../services/bookingService'
import { authService } from '../services/authService'
import { format, parseISO, startOfWeek, endOfWeek, isWithinInterval } from 'date-fns'
import { ptBR } from 'date-fns/locale'
import { useState } from 'react'
import toast from 'react-hot-toast'

export default function AdminDashboardPage() {
  const [statusFilter, setStatusFilter] = useState<string>('ALL')
  const [dateFilter, setDateFilter] = useState<string>('ALL')
  const [searchTerm, setSearchTerm] = useState('')
  const [showBarberForm, setShowBarberForm] = useState(false)
  const [editingBarber, setEditingBarber] = useState<any>(null)
  const [barberFormData, setBarberFormData] = useState({
    email: '',
    password: '',
    fullName: '',
    phone: '',
    bio: '',
    specialties: '',
  })
  const queryClient = useQueryClient()

  const { data: bookings, isLoading } = useQuery({
    queryKey: ['admin-all-bookings'],
    queryFn: bookingService.getAllBookings,
  })

  const { data: barbers } = useQuery({
    queryKey: ['barbers'],
    queryFn: bookingService.getBarbers,
  })

  const createBarberMutation = useMutation({
    mutationFn: authService.createBarber,
    onSuccess: () => {
      toast.success('Barbeiro cadastrado com sucesso!')
      setShowBarberForm(false)
      setBarberFormData({
        email: '',
        password: '',
        fullName: '',
        phone: '',
        bio: '',
        specialties: '',
      })
      queryClient.invalidateQueries({ queryKey: ['barbers'] })
    },
    onError: (error: any) => {
      const errorMessage = error.response?.data?.error || 
                          error.response?.data?.message ||
                          error.message || 
                          'Erro ao cadastrar barbeiro'
      toast.error(errorMessage)
    },
  })

  const updateBarberMutation = useMutation({
    mutationFn: ({ barberId, data }: { barberId: string; data: UpdateBarberData }) => 
      bookingService.updateBarber(barberId, data),
    onSuccess: () => {
      toast.success('Barbeiro atualizado com sucesso!')
      setEditingBarber(null)
      queryClient.invalidateQueries({ queryKey: ['barbers'] })
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.error || 'Erro ao atualizar barbeiro')
    },
  })

  const deleteBarberMutation = useMutation({
    mutationFn: bookingService.deleteBarber,
    onSuccess: () => {
      toast.success('Barbeiro desativado com sucesso!')
      queryClient.invalidateQueries({ queryKey: ['barbers'] })
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.error || 'Erro ao desativar barbeiro')
    },
  })

  const handleCreateBarber = (e: React.FormEvent) => {
    e.preventDefault()
    const specialties = barberFormData.specialties
      .split(',')
      .map(s => s.trim())
      .filter(s => s.length > 0)
    
    createBarberMutation.mutate({
      email: barberFormData.email,
      password: barberFormData.password,
      fullName: barberFormData.fullName,
      phone: barberFormData.phone || undefined,
      bio: barberFormData.bio || undefined,
      specialties: specialties.length > 0 ? specialties : undefined,
    })
  }

  const handleEditBarber = (barber: any) => {
    setEditingBarber(barber)
    setBarberFormData({
      email: barber.email || '',
      password: '',
      fullName: barber.fullName || '',
      phone: '',
      bio: barber.bio || '',
      specialties: barber.specialties?.join(', ') || '',
    })
    setShowBarberForm(true)
  }

  const handleUpdateBarber = (e: React.FormEvent) => {
    e.preventDefault()
    if (!editingBarber) return

    const specialties = barberFormData.specialties
      .split(',')
      .map(s => s.trim())
      .filter(s => s.length > 0)

    updateBarberMutation.mutate({
      barberId: editingBarber.id,
      data: {
        bio: barberFormData.bio || undefined,
        specialties: specialties.length > 0 ? specialties : undefined,
      },
    })
  }

  const handleDeleteBarber = (barberId: string) => {
    if (confirm('Tem certeza que deseja desativar este barbeiro?')) {
      deleteBarberMutation.mutate(barberId)
    }
  }

  if (isLoading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Carregando dados...</p>
        </div>
      </div>
    )
  }

  // Calculate statistics
  const stats = {
    total: bookings?.length || 0,
    pending: bookings?.filter(b => b.status === 'PENDING').length || 0,
    confirmed: bookings?.filter(b => b.status === 'CONFIRMED').length || 0,
    cancelled: bookings?.filter(b => b.status === 'CANCELLED').length || 0,
    completed: bookings?.filter(b => b.status === 'COMPLETED').length || 0,
    today: bookings?.filter(b => {
      const bookingDate = parseISO(b.bookingDate)
      const today = new Date()
      return bookingDate.toDateString() === today.toDateString()
    }).length || 0,
    thisWeek: bookings?.filter(b => {
      const bookingDate = parseISO(b.bookingDate)
      const weekStart = startOfWeek(new Date(), { locale: ptBR })
      const weekEnd = endOfWeek(new Date(), { locale: ptBR })
      return isWithinInterval(bookingDate, { start: weekStart, end: weekEnd })
    }).length || 0,
    revenue: bookings?.filter(b => b.status !== 'CANCELLED').reduce((sum, b) => sum + b.service.price, 0) || 0,
  }

  // Filter bookings
  const filteredBookings = bookings?.filter(booking => {
    if (statusFilter !== 'ALL' && booking.status !== statusFilter) return false
    if (searchTerm && !booking.service.name.toLowerCase().includes(searchTerm.toLowerCase())) return false
    if (dateFilter === 'TODAY') {
      const bookingDate = parseISO(booking.bookingDate)
      const today = new Date()
      return bookingDate.toDateString() === today.toDateString()
    }
    if (dateFilter === 'WEEK') {
      const bookingDate = parseISO(booking.bookingDate)
      const weekStart = startOfWeek(new Date(), { locale: ptBR })
      const weekEnd = endOfWeek(new Date(), { locale: ptBR })
      return isWithinInterval(bookingDate, { start: weekStart, end: weekEnd })
    }
    return true
  }) || []

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-4xl font-bold mb-2 text-gray-900">Painel Administrativo</h1>
            <p className="text-gray-600">Gerenciamento completo do sistema</p>
          </div>
          <div className="flex items-center space-x-2 bg-primary-100 px-4 py-2 rounded-lg">
            <BarChart3 className="w-6 h-6 text-primary-600" />
            <span className="font-semibold text-primary-600">Admin</span>
          </div>
        </div>
      </div>

      {/* Statistics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div className="bg-gradient-to-br from-blue-500 to-blue-600 text-white rounded-xl p-6 shadow-lg">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-blue-100 text-sm font-medium mb-1">Total de Agendamentos</p>
              <p className="text-3xl font-bold">{stats.total}</p>
            </div>
            <Scissors className="w-12 h-12 opacity-20" />
          </div>
        </div>

        <div className="bg-gradient-to-br from-yellow-500 to-yellow-600 text-white rounded-xl p-6 shadow-lg">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-yellow-100 text-sm font-medium mb-1">Pendentes</p>
              <p className="text-3xl font-bold">{stats.pending}</p>
            </div>
            <Clock className="w-12 h-12 opacity-20" />
          </div>
        </div>

        <div className="bg-gradient-to-br from-green-500 to-green-600 text-white rounded-xl p-6 shadow-lg">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-green-100 text-sm font-medium mb-1">Confirmados</p>
              <p className="text-3xl font-bold">{stats.confirmed}</p>
            </div>
            <CheckCircle className="w-12 h-12 opacity-20" />
          </div>
        </div>

        <div className="bg-gradient-to-br from-purple-500 to-purple-600 text-white rounded-xl p-6 shadow-lg">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-purple-100 text-sm font-medium mb-1">Receita Total</p>
              <p className="text-2xl font-bold">R$ {stats.revenue.toFixed(2)}</p>
            </div>
            <DollarSign className="w-12 h-12 opacity-20" />
          </div>
        </div>
      </div>

      {/* Create Barber Section */}
      <div className="card mb-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-2xl font-bold text-gray-900">Gerenciar Barbeiros</h2>
          <button
            onClick={() => {
              if (editingBarber) {
                setEditingBarber(null)
                setBarberFormData({
                  email: '',
                  password: '',
                  fullName: '',
                  phone: '',
                  bio: '',
                  specialties: '',
                })
              }
              setShowBarberForm(!showBarberForm)
            }}
            className="flex items-center space-x-2 bg-primary-600 text-white px-4 py-2 rounded-lg hover:bg-primary-700 transition"
          >
            <Plus className="w-5 h-5" />
            <span>{showBarberForm ? 'Cancelar' : 'Cadastrar Barbeiro'}</span>
          </button>
        </div>

        {showBarberForm && (
          <form onSubmit={editingBarber ? handleUpdateBarber : handleCreateBarber} className="space-y-4 pt-4 border-t">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Nome Completo *
                </label>
                <input
                  type="text"
                  required
                  value={barberFormData.fullName}
                  onChange={(e) => setBarberFormData({ ...barberFormData, fullName: e.target.value })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                />
              </div>

              {!editingBarber && (
                <>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Email *
                    </label>
                    <input
                      type="email"
                      required
                      value={barberFormData.email}
                      onChange={(e) => setBarberFormData({ ...barberFormData, email: e.target.value })}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Senha *
                    </label>
                    <input
                      type="password"
                      required
                      minLength={6}
                      value={barberFormData.password}
                      onChange={(e) => setBarberFormData({ ...barberFormData, password: e.target.value })}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                    />
                  </div>
                </>
              )}

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Telefone
                </label>
                <input
                  type="tel"
                  value={barberFormData.phone}
                  onChange={(e) => setBarberFormData({ ...barberFormData, phone: e.target.value })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                />
              </div>

              <div className="md:col-span-2">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Especialidades (separadas por vírgula)
                </label>
                <input
                  type="text"
                  value={barberFormData.specialties}
                  onChange={(e) => setBarberFormData({ ...barberFormData, specialties: e.target.value })}
                  placeholder="Ex: Corte masculino, Barba, Sobrancelha"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                />
              </div>

              <div className="md:col-span-2">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Biografia
                </label>
                <textarea
                  value={barberFormData.bio}
                  onChange={(e) => setBarberFormData({ ...barberFormData, bio: e.target.value })}
                  rows={3}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                />
              </div>
            </div>

            <div className="flex justify-end space-x-4">
              <button
                type="button"
                onClick={() => setShowBarberForm(false)}
                className="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition"
              >
                Cancelar
              </button>
              <button
                type="submit"
                disabled={editingBarber ? updateBarberMutation.isPending : createBarberMutation.isPending}
                className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition disabled:opacity-50"
              >
                {editingBarber 
                  ? (updateBarberMutation.isPending ? 'Atualizando...' : 'Atualizar Barbeiro')
                  : (createBarberMutation.isPending ? 'Cadastrando...' : 'Cadastrar Barbeiro')
                }
              </button>
            </div>
          </form>
        )}

        {/* Barbers List */}
        {barbers && barbers.length > 0 && (
          <div className="mt-6 pt-6 border-t">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Barbeiros Cadastrados</h3>
            <div className="space-y-3">
              {barbers.map((barber) => (
                <div
                  key={barber.id}
                  className={`p-4 border rounded-lg flex items-center justify-between ${
                    barber.active ? 'bg-white border-gray-200' : 'bg-gray-50 border-gray-300 opacity-60'
                  }`}
                >
                  <div className="flex-1">
                    <div className="flex items-center space-x-3">
                      <div>
                        <h4 className="font-semibold text-gray-900">
                          {barber.fullName || 'Nome não disponível'}
                        </h4>
                        {barber.email && (
                          <p className="text-sm text-gray-600">{barber.email}</p>
                        )}
                        {barber.bio && (
                          <p className="text-sm text-gray-500 mt-1">{barber.bio}</p>
                        )}
                        {barber.specialties && barber.specialties.length > 0 && (
                          <div className="flex flex-wrap gap-2 mt-2">
                            {barber.specialties.map((specialty, idx) => (
                              <span
                                key={idx}
                                className="px-2 py-1 bg-primary-100 text-primary-700 text-xs rounded"
                              >
                                {specialty}
                              </span>
                            ))}
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                  <div className="flex items-center space-x-2">
                    <button
                      onClick={() => handleEditBarber(barber)}
                      className="p-2 text-primary-600 hover:bg-primary-50 rounded-lg transition"
                      title="Editar"
                    >
                      <Edit className="w-5 h-5" />
                    </button>
                    <button
                      onClick={() => handleDeleteBarber(barber.id)}
                      disabled={deleteBarberMutation.isPending}
                      className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition disabled:opacity-50"
                      title="Desativar"
                    >
                      <Trash2 className="w-5 h-5" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Secondary Stats */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
        <div className="card bg-white border-2 border-gray-200">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 mb-1">Hoje</p>
              <p className="text-2xl font-bold text-primary-600">{stats.today}</p>
            </div>
            <Calendar className="w-8 h-8 text-primary-600" />
          </div>
        </div>

        <div className="card bg-white border-2 border-gray-200">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 mb-1">Esta Semana</p>
              <p className="text-2xl font-bold text-primary-600">{stats.thisWeek}</p>
            </div>
            <TrendingUp className="w-8 h-8 text-primary-600" />
          </div>
        </div>

        <div className="card bg-white border-2 border-gray-200">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 mb-1">Cancelados</p>
              <p className="text-2xl font-bold text-red-600">{stats.cancelled}</p>
            </div>
            <XCircle className="w-8 h-8 text-red-600" />
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="card mb-6">
        <div className="flex flex-col md:flex-row gap-4 items-center">
          <div className="flex-1 w-full">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
              <input
                type="text"
                placeholder="Buscar por serviço..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              />
            </div>
          </div>

          <div className="flex gap-2">
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500"
            >
              <option value="ALL">Todos os Status</option>
              <option value="PENDING">Pendente</option>
              <option value="CONFIRMED">Confirmado</option>
              <option value="CANCELLED">Cancelado</option>
              <option value="COMPLETED">Concluído</option>
            </select>

            <select
              value={dateFilter}
              onChange={(e) => setDateFilter(e.target.value)}
              className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500"
            >
              <option value="ALL">Todas as Datas</option>
              <option value="TODAY">Hoje</option>
              <option value="WEEK">Esta Semana</option>
            </select>
          </div>
        </div>
      </div>

      {/* Bookings Table */}
      <div className="card">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-bold text-gray-900">Agendamentos</h2>
          <span className="text-sm text-gray-600">
            {filteredBookings.length} {filteredBookings.length === 1 ? 'agendamento' : 'agendamentos'}
          </span>
        </div>

        {filteredBookings.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b-2 border-gray-200 bg-gray-50">
                  <th className="text-left p-4 font-semibold text-gray-700">ID</th>
                  <th className="text-left p-4 font-semibold text-gray-700">Serviço</th>
                  <th className="text-left p-4 font-semibold text-gray-700">Barbeiro</th>
                  <th className="text-left p-4 font-semibold text-gray-700">Cliente</th>
                  <th className="text-left p-4 font-semibold text-gray-700">Data</th>
                  <th className="text-left p-4 font-semibold text-gray-700">Horário</th>
                  <th className="text-left p-4 font-semibold text-gray-700">Valor</th>
                  <th className="text-left p-4 font-semibold text-gray-700">Status</th>
                </tr>
              </thead>
              <tbody>
                {filteredBookings.map((booking) => (
                  <tr key={booking.id} className="border-b border-gray-100 hover:bg-gray-50 transition">
                    <td className="p-4">
                      <span className="text-xs font-mono text-gray-500">
                        {booking.id.substring(0, 8)}...
                      </span>
                    </td>
                    <td className="p-4">
                      <div>
                        <div className="font-semibold text-gray-900">{booking.service.name}</div>
                        <div className="text-sm text-gray-500">{booking.service.description}</div>
                      </div>
                    </td>
                    <td className="p-4">
                      {booking.barber ? (
                        <div className="flex items-center">
                          <Users className="w-4 h-4 mr-2 text-gray-400" />
                          <span className="font-medium">{booking.barber.fullName}</span>
                        </div>
                      ) : booking.barberName ? (
                        <span className="font-medium">{booking.barberName}</span>
                      ) : (
                        <span className="text-gray-400 italic">Não atribuído</span>
                      )}
                    </td>
                    <td className="p-4">
                      <span className="text-sm text-gray-600">
                        {booking.userId.substring(0, 8)}...
                      </span>
                    </td>
                    <td className="p-4">
                      <div className="flex items-center text-gray-700">
                        <Calendar className="w-4 h-4 mr-2 text-gray-400" />
                        {format(parseISO(booking.bookingDate), "dd/MM/yyyy", { locale: ptBR })}
                      </div>
                    </td>
                    <td className="p-4">
                      <div className="flex items-center text-gray-700">
                        <Clock className="w-4 h-4 mr-2 text-gray-400" />
                        {booking.bookingTime}
                      </div>
                    </td>
                    <td className="p-4">
                      <span className="font-semibold text-green-600">
                        R$ {booking.service.price.toFixed(2)}
                      </span>
                    </td>
                    <td className="p-4">
                      <span className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold ${
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
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="text-center py-12">
            <Scissors className="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <p className="text-gray-600 text-lg">Nenhum agendamento encontrado</p>
            <p className="text-gray-400 text-sm mt-2">Tente ajustar os filtros</p>
          </div>
        )}
      </div>
    </div>
  )
}
