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
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { ProfilePictureUpload } from './ProfilePictureUpload'
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
  const [profilePictureUrl, setProfilePictureUrl] = useState<string | undefined>(
    profile.profilePictureUrl
  )

  useEffect(() => {
    if (isOpen) {
      setBio(profile.bio || '')
      setProfilePictureUrl(profile.profilePictureUrl)
    }
  }, [isOpen, profile])

  const handleSave = () => {
    onSave({
      bio: bio.trim() || undefined,
      profilePictureUrl: profilePictureUrl,
    })
    onClose()
  }

  const handleCancel = () => {
    setBio(profile.bio || '')
    setProfilePictureUrl(profile.profilePictureUrl)
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
        <div className="grid gap-6 py-4">
          <div className="flex flex-col items-center gap-2">
            <Label>Profile Picture</Label>
            <ProfilePictureUpload
              username={profile.username}
              value={profilePictureUrl}
              onChange={setProfilePictureUrl}
              disabled={isUpdating}
            />
          </div>
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
