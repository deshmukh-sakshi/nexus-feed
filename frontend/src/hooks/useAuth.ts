import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { authApi, usersApi } from '@/lib/api-client'
import { useAuthStore } from '@/stores/authStore'
import { getErrorMessage } from '@/types/errors'
import type { LoginRequest, RegistrationRequest } from '@/types'

export const useAuth = (redirectTo?: string) => {
  const navigate = useNavigate()
  const { setAuth, updateProfilePicture, logout: logoutStore } = useAuthStore()

  // Use provided redirect path or default to home
  const from = redirectTo || '/'

  const loginMutation = useMutation({
    mutationFn: authApi.login,
    onSuccess: async (data) => {
      setAuth(
        {
          userId: data.userId,
          username: data.username,
          email: data.email,
        },
        data.token
      )
      // Fetch profile picture after login
      try {
        const profile = await usersApi.getUserByUsername(data.username)
        if (profile.profilePictureUrl) {
          updateProfilePicture(profile.profilePictureUrl)
        }
      } catch {
        // Ignore error, profile picture is optional
      }
      toast.success('Welcome back!')
      navigate(from, { replace: true })
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
      // New users don't have profile pictures yet
      toast.success('Account created successfully!')
      navigate(from, { replace: true })
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
