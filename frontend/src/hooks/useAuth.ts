import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { authApi } from '@/lib/api-client'
import { useAuthStore } from '@/stores/authStore'
import { getErrorMessage } from '@/types/errors'
import type { LoginRequest, RegistrationRequest } from '@/types'

export const useAuth = () => {
  const navigate = useNavigate()
  const { setAuth, logout: logoutStore } = useAuthStore()

  const loginMutation = useMutation({
    mutationFn: authApi.login,
    onSuccess: (data) => {
      setAuth(
        {
          userId: data.userId,
          username: data.username,
          email: data.email,
        },
        data.token
      )
      toast.success('Welcome back!')
      navigate('/')
    },
    onError: (error) => {
      toast.error(getErrorMessage(error))
    },
  })

  const registerMutation = useMutation({
    mutationFn: authApi.register,
    onSuccess: (data) => {
      setAuth(
        {
          userId: data.userId,
          username: data.username,
          email: data.email,
        },
        data.token
      )
      toast.success('Account created successfully!')
      navigate('/')
    },
    onError: (error) => {
      toast.error(getErrorMessage(error))
    },
  })

  const login = (data: LoginRequest) => loginMutation.mutate(data)
  const register = (data: RegistrationRequest) => registerMutation.mutate(data)
  const logout = () => {
    logoutStore()
    toast.success('Logged out successfully')
    navigate('/login')
  }

  return {
    login,
    register,
    logout,
    isLoading: loginMutation.isPending || registerMutation.isPending,
  }
}
