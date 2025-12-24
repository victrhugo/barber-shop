import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { useMutation } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { Scissors } from 'lucide-react'
import { authService, RegisterData } from '../services/authService'
import { useAuthStore } from '../store/authStore'

export default function RegisterPage() {
  const navigate = useNavigate()
  const { setAuth } = useAuthStore()
  const { register, handleSubmit, formState: { errors }, watch } = useForm<RegisterData & { confirmPassword: string }>()

  const registerMutation = useMutation({
    mutationFn: authService.register,
    onSuccess: (data) => {
      setAuth(data.token, {
        userId: data.userId,
        email: data.email,
        fullName: data.fullName,
        role: data.role,
        emailVerified: data.emailVerified,
      })
      toast.success('Cadastro realizado! Verifique seu email.')
      navigate('/dashboard')
    },
    onError: (error: any) => {
      console.error('Erro no cadastro:', error)
      const errorMessage = error.response?.data?.error || 
                          error.response?.data?.message ||
                          error.message || 
                          'Erro ao cadastrar. Verifique os dados e tente novamente.'
      toast.error(errorMessage)
    },
  })

  const onSubmit = (data: RegisterData & { confirmPassword: string }) => {
    const { confirmPassword, ...registerData } = data
    registerMutation.mutate(registerData)
  }

  const password = watch('password')

  return (
    <div className="min-h-[80vh] flex items-center justify-center py-12 px-4">
      <div className="max-w-md w-full">
        <div className="text-center mb-8">
          <Scissors className="w-12 h-12 text-primary-600 mx-auto mb-4" />
          <h2 className="text-3xl font-bold">Criar Conta</h2>
          <p className="text-gray-600 mt-2">
            Cadastre-se para começar a agendar
          </p>
        </div>

        <div className="card">
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            <div>
              <label className="block text-sm font-medium mb-2">Nome Completo</label>
              <input
                type="text"
                className="input"
                placeholder="João Silva"
                {...register('fullName', { required: 'Nome é obrigatório' })}
              />
              {errors.fullName && (
                <p className="text-red-500 text-sm mt-1">{errors.fullName.message}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium mb-2">Email</label>
              <input
                type="email"
                className="input"
                placeholder="seu@email.com"
                {...register('email', { required: 'Email é obrigatório' })}
              />
              {errors.email && (
                <p className="text-red-500 text-sm mt-1">{errors.email.message}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium mb-2">Telefone (Opcional)</label>
              <input
                type="tel"
                className="input"
                placeholder="(11) 98765-4321"
                {...register('phone')}
              />
            </div>

            <div>
              <label className="block text-sm font-medium mb-2">Senha</label>
              <input
                type="password"
                className="input"
                placeholder="••••••••"
                {...register('password', { 
                  required: 'Senha é obrigatória',
                  minLength: { value: 6, message: 'Senha deve ter no mínimo 6 caracteres' }
                })}
              />
              {errors.password && (
                <p className="text-red-500 text-sm mt-1">{errors.password.message}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium mb-2">Confirmar Senha</label>
              <input
                type="password"
                className="input"
                placeholder="••••••••"
                {...register('confirmPassword', { 
                  required: 'Confirme sua senha',
                  validate: value => value === password || 'As senhas não coincidem'
                })}
              />
              {errors.confirmPassword && (
                <p className="text-red-500 text-sm mt-1">{errors.confirmPassword.message}</p>
              )}
            </div>

            <button
              type="submit"
              className="btn btn-primary w-full"
              disabled={registerMutation.isPending}
            >
              {registerMutation.isPending ? 'Cadastrando...' : 'Cadastrar'}
            </button>
          </form>

          <div className="mt-6 text-center">
            <p className="text-gray-600">
              Já tem uma conta?{' '}
              <Link to="/login" className="text-primary-600 hover:text-primary-700 font-medium">
                Entrar
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

