import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { User, Mail, Phone, Shield } from 'lucide-react'
import { userService, UpdateUserData } from '../services/userService'
import { useAuthStore } from '../store/authStore'

export default function ProfilePage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { logout } = useAuthStore()

  const { data: user, isLoading } = useQuery({
    queryKey: ['currentUser'],
    queryFn: userService.getMe,
  })

  const { register, handleSubmit, formState: { errors } } = useForm<UpdateUserData>({
    values: user ? {
      fullName: user.fullName,
      phone: user.phone || '',
    } : undefined,
  })

  const updateMutation = useMutation({
    mutationFn: userService.updateMe,
    onSuccess: () => {
      toast.success('Perfil atualizado com sucesso!')
      queryClient.invalidateQueries({ queryKey: ['currentUser'] })
    },
    onError: () => {
      toast.error('Erro ao atualizar perfil')
    },
  })

  const deleteMutation = useMutation({
    mutationFn: userService.deleteMe,
    onSuccess: () => {
      toast.success('Conta deletada com sucesso')
      logout()
      navigate('/')
    },
    onError: () => {
      toast.error('Erro ao deletar conta')
    },
  })

  const onSubmit = (data: UpdateUserData) => {
    updateMutation.mutate(data)
  }

  const handleDeleteAccount = () => {
    if (confirm('Tem certeza que deseja deletar sua conta? Esta ação não pode ser desfeita.')) {
      if (confirm('Confirme novamente: Deletar minha conta permanentemente?')) {
        deleteMutation.mutate()
      }
    }
  }

  if (isLoading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center py-20">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
        </div>
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-2xl mx-auto">
        <h1 className="text-3xl font-bold mb-8">Meu Perfil</h1>

        {/* Profile Info Card */}
        <div className="card mb-6">
          <div className="flex items-center space-x-4 mb-6">
            <div className="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center">
              <User className="w-8 h-8 text-primary-600" />
            </div>
            <div>
              <h2 className="text-2xl font-bold">{user?.fullName}</h2>
              <p className="text-gray-600">{user?.email}</p>
            </div>
          </div>

          <div className="grid md:grid-cols-2 gap-4">
            <div className="flex items-center space-x-3 p-3 bg-gray-50 rounded-lg">
              <Mail className="w-5 h-5 text-gray-600" />
              <div>
                <p className="text-sm text-gray-600">Email</p>
                <p className="font-medium">{user?.email}</p>
              </div>
            </div>

            <div className="flex items-center space-x-3 p-3 bg-gray-50 rounded-lg">
              <Phone className="w-5 h-5 text-gray-600" />
              <div>
                <p className="text-sm text-gray-600">Telefone</p>
                <p className="font-medium">{user?.phone || 'Não informado'}</p>
              </div>
            </div>

            <div className="flex items-center space-x-3 p-3 bg-gray-50 rounded-lg">
              <Shield className="w-5 h-5 text-gray-600" />
              <div>
                <p className="text-sm text-gray-600">Função</p>
                <p className="font-medium">{user?.role}</p>
              </div>
            </div>

            <div className="flex items-center space-x-3 p-3 bg-gray-50 rounded-lg">
              <Mail className="w-5 h-5 text-gray-600" />
              <div>
                <p className="text-sm text-gray-600">Email Verificado</p>
                <p className={`font-medium ${user?.emailVerified ? 'text-green-600' : 'text-yellow-600'}`}>
                  {user?.emailVerified ? 'Sim' : 'Não'}
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Edit Form */}
        <div className="card mb-6">
          <h3 className="text-xl font-bold mb-4">Editar Informações</h3>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-2">Nome Completo</label>
              <input
                type="text"
                className="input"
                {...register('fullName', { required: 'Nome é obrigatório' })}
              />
              {errors.fullName && (
                <p className="text-red-500 text-sm mt-1">{errors.fullName.message}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium mb-2">Telefone</label>
              <input
                type="tel"
                className="input"
                placeholder="(11) 98765-4321"
                {...register('phone')}
              />
            </div>

            <button
              type="submit"
              className="btn btn-primary w-full"
              disabled={updateMutation.isPending}
            >
              {updateMutation.isPending ? 'Salvando...' : 'Salvar Alterações'}
            </button>
          </form>
        </div>

        {/* Danger Zone */}
        <div className="card border-red-200 bg-red-50">
          <h3 className="text-xl font-bold text-red-600 mb-4">Zona de Perigo</h3>
          <p className="text-gray-600 mb-4">
            Uma vez deletada, sua conta não poderá ser recuperada.
          </p>
          <button
            onClick={handleDeleteAccount}
            className="btn btn-danger"
            disabled={deleteMutation.isPending}
          >
            {deleteMutation.isPending ? 'Deletando...' : 'Deletar Conta'}
          </button>
        </div>
      </div>
    </div>
  )
}



