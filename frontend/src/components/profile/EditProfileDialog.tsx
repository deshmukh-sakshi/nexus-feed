import { useState, useEffect } from 'react'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import type { UserProfile } from '@/types'

interface EditProfileDialogProps {
  isOpen: boolean
  onClose: () => void
  profile: UserProfile
  onSave: (data: { bio?: string; profilePictureUrl?: string }) => void
  isUpdating: boolean
}

export const EditProfileDialog = ({
  isOpen,
  onClose,
  profile,
  onSave,
  isUpdating,
}: EditProfileDialogProps) => {
  const [bio, setBio] = useState(profile.bio || '')
  const [profilePictureUrl, setProfilePictureUrl] = useState(
    profile.profilePictureUrl || ''
  )

  useEffect(() => {
    if (isOpen) {
      setBio(profile.bio || '')
      setProfilePictureUrl(profile.profilePictureUrl || '')
    }
  }, [isOpen, profile])

  const handleSave = () => {
    onSave({
      bio: bio.trim() || undefined,
      profilePictureUrl: profilePictureUrl.trim() || undefined,
    })
    onClose()
  }

  const handleCancel = () => {
    setBio(profile.bio || '')
    setProfilePictureUrl(profile.profilePictureUrl || '')
    onClose()
  }

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-[525px]">
        <DialogHeader>
          <DialogTitle>Edit Profile</DialogTitle>
          <DialogDescription>
            Make changes to your profile here. Click save when you're done.
          </DialogDescription>
        </DialogHeader>
        <div className="grid gap-4 py-4">
          <div className="grid gap-2">
            <Label htmlFor="bio">Bio</Label>
            <Textarea
              id="bio"
              value={bio}
              onChange={(e) => setBio(e.target.value)}
              placeholder="Tell us about yourself..."
              className="min-h-[100px] resize-y"
              maxLength={500}
            />
            <p className="text-xs text-muted-foreground">
              {bio.length}/500 characters
            </p>
          </div>
          <div className="grid gap-2">
            <Label htmlFor="profilePictureUrl">Profile Picture URL</Label>
            <Input
              id="profilePictureUrl"
              value={profilePictureUrl}
              onChange={(e) => setProfilePictureUrl(e.target.value)}
              placeholder="https://example.com/image.jpg"
              maxLength={255}
            />
            <p className="text-xs text-muted-foreground">
              Enter a URL to your profile picture
            </p>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={handleCancel} disabled={isUpdating}>
            Cancel
          </Button>
          <Button onClick={handleSave} disabled={isUpdating}>
            {isUpdating ? 'Saving...' : 'Save changes'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
