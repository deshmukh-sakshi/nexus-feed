import { useState } from 'react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import type { GoogleAuthResponse } from '@/types'

interface UsernamePromptModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  googleData: GoogleAuthResponse | null
  onSubmit: (username: string) => void
  onUsernameChange?: () => void
  isLoading?: boolean
  error?: string | null
}

export function UsernamePromptModal({
  open,
  onOpenChange,
  googleData,
  onSubmit,
  onUsernameChange,
  isLoading,
  error,
}: UsernamePromptModalProps) {
  const [username, setUsername] = useState('')

  const handleUsernameChange = (value: string) => {
    setUsername(value)
    onUsernameChange?.()
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const trimmed = username.trim()
    if (trimmed) {
      onSubmit(trimmed)
    }
  }

  const isValid = username.trim().length > 0 && username.trim().length <= 50

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent showCloseButton={false}>
        <DialogHeader>
          <DialogTitle>Choose your username</DialogTitle>
          <DialogDescription>
            Welcome! Pick a username to complete your registration.
          </DialogDescription>
        </DialogHeader>

        {googleData && (
          <div className="flex items-center gap-3 p-3 bg-muted/50 border-2 border-border">
            {googleData.pictureUrl && (
              <img
                src={googleData.pictureUrl}
                alt=""
                referrerPolicy="no-referrer"
                className="w-10 h-10 rounded-full border-2 border-border"
              />
            )}
            <div className="flex-1 min-w-0">
              <p className="font-medium truncate">{googleData.suggestedName}</p>
              <p className="text-sm text-muted-foreground truncate">{googleData.email}</p>
            </div>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="username">Username</Label>
            <Input
              id="username"
              value={username}
              onChange={(e) => handleUsernameChange(e.target.value)}
              placeholder="Enter username"
              maxLength={50}
              disabled={isLoading}
              autoFocus
            />
            {error && <p className="text-sm text-destructive">{error}</p>}
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={isLoading}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={!isValid || isLoading}>
              {isLoading ? 'Creating...' : 'Continue'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
