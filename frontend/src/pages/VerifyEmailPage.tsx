import { useEffect } from 'react'
import { useSearchParams, Link } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { CheckCircle, XCircle } from 'lucide-react'
import { authService } from '../services/authService'

export default function VerifyEmailPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')

  const verifyMutation = useMutation({
    mutationFn: () => authService.verifyEmail(token!),
  })

  useEffect(() => {
    if (token) {
      verifyMutation.mutate()
    }
  }, [token])

  return (
    <div className="min-h-[80vh] flex items-center justify-center py-12 px-4">
      <div className="max-w-md w-full text-center">
        <div className="card">
          {verifyMutation.isPending && (
            <>
              <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-primary-600 mx-auto mb-4"></div>
              <h2 className="text-2xl font-bold mb-2">Verificando...</h2>
              <p className="text-gray-600">Aguarde enquanto verificamos seu email</p>
            </>
          )}

          {verifyMutation.isSuccess && (
            <>
              <CheckCircle className="w-16 h-16 text-green-500 mx-auto mb-4" />
              <h2 className="text-2xl font-bold mb-2">Email Verificado!</h2>
              <p className="text-gray-600 mb-6">
                Seu email foi verificado com sucesso. Você já pode fazer login.
              </p>
              <Link to="/login" className="btn btn-primary">
                Fazer Login
              </Link>
            </>
          )}

          {verifyMutation.isError && (
            <>
              <XCircle className="w-16 h-16 text-red-500 mx-auto mb-4" />
              <h2 className="text-2xl font-bold mb-2">Erro na Verificação</h2>
              <p className="text-gray-600 mb-6">
                O link de verificação é inválido ou expirou.
              </p>
              <Link to="/login" className="btn btn-secondary">
                Voltar ao Login
              </Link>
            </>
          )}
        </div>
      </div>
    </div>
  )
}



