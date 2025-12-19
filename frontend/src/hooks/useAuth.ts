import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { authApi, usersApi } from '@/lib/api-client'
import { useAuthStore } from '@/stores/authStore'
import { getErrorMessage } from '@/types/errors'
import type { LoginRequest, RegistrationRequest, GoogleAuthResponse } from '@/types'

export const useAuth = (redirectTo?: string) => {
  const navigate = useNavigate()
  const { setAuth, updateProfilePicture, logout: logoutStore } = useAuthStore()
  const [pendingGoogleUser, setPendingGoogleUser] = useState<GoogleAuthResponse | null>(null)

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

  const googleLoginMutation = useMutation({
    mutationFn: authApi.googleLogin,
    onSuccess: async (response) => {
      if ('needsUsername' in response && response.needsUsername) {
        // New user or incomplete profile - needs username selection
        setPendingGoogleUser(response)
      } else if ('token' in response) {
        // Returning user with complete profile
        setAuth(
          {
            userId: response.userId,
            username: response.username,
            email: response.email,
          },
          response.token
        )
        // Fetch profile picture after login
        try {
          const profile = await usersApi.getUserByUsername(response.username)
          if (profile.profilePictureUrl) {
            updateProfilePicture(profile.profilePictureUrl)
          }
        } catch {
          // Ignore error, profile picture is optional
        }
        toast.success('Welcome back!')
        navigate(from, { replace: true })
      }
    },
    onError: (error) => {
      toast.error(getErrorMessage(error))
    },
  })

  const completeGoogleMutation = useMutation({
    mutationFn: ({ tempToken, username }: { tempToken: string; username: string }) =>
      authApi.completeGoogleRegistration(tempToken, username),
    onSuccess: (data) => {
      setAuth(
        {
          userId: data.userId,
          username: data.username,
          email: data.email,
          profilePictureUrl: pendingGoogleUser?.pictureUrl,
        },
        data.token
      )
      setPendingGoogleUser(null)
      toast.success('Account created successfully!')
      navigate(from, { replace: true })
    },
  })

  const login = (data: LoginRequest) => loginMutation.mutate(data)
  const register = (data: RegistrationRequest) => registerMutation.mutate(data)
  const googleLogin = (idToken: string) => googleLoginMutation.mutate(idToken)
  const completeGoogleRegistration = (username: string) => {
    if (pendingGoogleUser?.tempToken) {
      completeGoogleMutation.mutate({ tempToken: pendingGoogleUser.tempToken, username })
    }
  }
  const clearGoogleError = () => completeGoogleMutation.reset()
  const cancelGoogleRegistration = () => {
    setPendingGoogleUser(null)
    completeGoogleMutation.reset()
  }
  const logout = () => {
    logoutStore()
    toast.success('Logged out successfully')
    navigate('/login')
  }

  return {
    login,
    register,
    googleLogin,
    completeGoogleRegistration,
    cancelGoogleRegistration,
    clearGoogleError,
    logout,
    isLoading: loginMutation.isPending || registerMutation.isPending,
    isGoogleLoading: googleLoginMutation.isPending || completeGoogleMutation.isPending,
    pendingGoogleUser,
    googleRegistrationError: completeGoogleMutation.error ? getErrorMessage(completeGoogleMutation.error) : null,
  }
}
