import { useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { toast } from 'sonner'

export const useSessionExpiry = () => {
  const navigate = useNavigate()
  const location = useLocation()

  useEffect(() => {
    const handleSessionExpired = (event: CustomEvent<{ message: string }>) => {
      // Don't redirect if already on login page
      if (location.pathname === '/login') return

      // Navigate first, then show toast after navigation
      navigate('/login', { replace: true })
      
      // Small delay to ensure navigation completes before showing toast
      setTimeout(() => {
        toast.error(event.detail.message)
      }, 100)
    }

    window.addEventListener('auth:session-expired', handleSessionExpired as EventListener)
    
    return () => {
      window.removeEventListener('auth:session-expired', handleSessionExpired as EventListener)
    }
  }, [navigate, location.pathname])
}
