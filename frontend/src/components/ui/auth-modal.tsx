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

  const handleLoginClick = () => {
    onClose()
    navigate('/login')
  }

  const handleRegisterClick = () => {
    onClose()
    navigate('/register')
  }

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-md border-2 border-black shadow-[8px_8px_0px_0px_rgba(0,0,0,1)] dark:shadow-[8px_8px_0px_0px_rgba(255,255,255,1)] rounded-none bg-yellow-50">
        <DialogHeader>
          <DialogTitle className="text-xl font-bold">Authentication Required</DialogTitle>
          <DialogDescription className="text-base">{message}</DialogDescription>
        </DialogHeader>
        <DialogFooter className="flex-col sm:flex-col gap-2">
          <Button 
            onClick={handleLoginClick}
            className="w-full bg-blue-400 text-black hover:bg-blue-500 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold"
          >
            Log In
          </Button>
          <Button 
            onClick={handleRegisterClick}
            className="w-full bg-green-400 text-black hover:bg-green-500 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold"
          >
            Sign Up
          </Button>
          <Button
            onClick={onClose}
            className="w-full bg-gray-300 text-black hover:bg-gray-400 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold"
          >
            Stay Logged Out
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
