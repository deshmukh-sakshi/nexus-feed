import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { User } from '@/types'

interface AuthState {
  user: User | null
  token: string | null
  isAuthenticated: boolean
  isAdmin: boolean
  setAuth: (user: User, token: string) => void
  updateProfilePicture: (profilePictureUrl: string | undefined) => void
  logout: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      isAdmin: false,
      setAuth: (user, token) => {
        localStorage.setItem('token', token)
        set({ user, token, isAuthenticated: true, isAdmin: user.role === 'ADMIN' })
      },
      updateProfilePicture: (profilePictureUrl) => {
        set((state) => ({
          user: state.user ? { ...state.user, profilePictureUrl } : null,
        }))
      },
      logout: () => {
        localStorage.removeItem('token')
        set({ user: null, token: null, isAuthenticated: false, isAdmin: false })
      },
    }),
    {
      name: 'auth-storage',
    }
  )
)
