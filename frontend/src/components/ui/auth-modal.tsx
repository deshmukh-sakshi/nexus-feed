import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'

interface AuthModalProps {
  isOpen: boolean
  onClose: () => void
  message?: string
}

export const AuthModal = ({
  isOpen,
  onClose,
  message = 'You need to be logged in to perform this action.',
}: AuthModalProps) => {
  const navigate = useNavigate()
  const [isClosing, setIsClosing] = useState(false)

  const handleLoginClick = () => {
    setIsClosing(true)
    onClose()
    navigate('/login')
  }

  const handleRegisterClick = () => {
    setIsClosing(true)
    onClose()
    navigate('/register')
  }

  const handleStayLoggedOut = () => {
    setIsClosing(true)
    onClose()
  }

  return (
    <Dialog open={isOpen && !isClosing} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Authentication Required</DialogTitle>
          <DialogDescription>{message}</DialogDescription>
        </DialogHeader>
        <DialogFooter className="flex-col sm:flex-col gap-2">
          <Button onClick={handleRegisterClick} className="w-full">
            Sign Up
          </Button>
          <Button onClick={handleLoginClick} variant="outline" className="w-full">
            Log In
          </Button>
          <Button
            onClick={handleStayLoggedOut}
            variant="ghost"
            className="w-full"
          >
            Stay Logged Out
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
