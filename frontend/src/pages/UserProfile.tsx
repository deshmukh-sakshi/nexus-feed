import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Loader2, ArrowLeft, Edit2 } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { UserAvatar } from '@/components/ui/user-avatar'
import { PostList } from '@/components/posts/PostList'
import { EditProfileDialog } from '@/components/profile/EditProfileDialog'
import { BadgeList } from '@/components/profile/BadgeList'
import { postsApi } from '@/lib/api-client'
import { useAuthStore } from '@/stores/authStore'
import { useUserProfile } from '@/hooks/useUserProfile'
import { formatDistanceToNow } from 'date-fns'
import { formatNumber } from '@/lib/utils'

export const UserProfile = () => {
  const { username } = useParams<{ username: string }>()
  const navigate = useNavigate()
  const { user: currentUser } = useAuthStore()
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false)

  const {
    profile,
    isLoading: profileLoading,
    error: profileError,
    updateProfile,
    isUpdating,
  } = useUserProfile(username)

  const { data: postsData, isLoading: postsLoading } = useQuery({
    queryKey: ['userPosts', profile?.id],
    queryFn: () => postsApi.getUserPosts(profile!.id, 0, 20),
    enabled: !!profile?.id,
    retry: false,
  })

  const posts = postsData?.content ?? []
  const isOwnProfile = currentUser?.userId === profile?.id

  const handleSaveProfile = (data: { bio?: string; profilePictureUrl?: string }) => {
    if (profile) {
      updateProfile(profile.id, data)
    }
  }

  if (profileLoading) {
    return (
      <div className="flex justify-center items-center py-12">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    )
  }

  if (profileError || !profile) {
    return (
      <div className="flex flex-col items-center justify-center py-12 space-y-4">
        <p className="text-destructive">User not found.</p>
        <Button 
          onClick={() => navigate('/')}
          className="bg-gray-300 text-black hover:bg-gray-400 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold"
        >
          Go Back Home
        </Button>
      </div>
    )
  }

  return (
    <div className="w-full max-w-4xl mx-auto space-y-4">
      <Button
        size="sm"
        onClick={() => navigate(-1)}
        className="mb-4 bg-gray-300 text-black hover:bg-gray-400 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold"
      >
        <ArrowLeft className="mr-2 h-4 w-4" />
        Back
      </Button>

      <Card>
        <CardHeader>
          <div className="flex flex-col sm:flex-row items-center sm:items-start gap-4">
            <UserAvatar 
              username={profile.username} 
              profileImageUrl={profile.profilePictureUrl}
              size="lg" 
            />
            <div className="flex-1 text-center sm:text-left">
              <div className="flex flex-col sm:flex-row items-center sm:items-center gap-2 sm:gap-3">
                <h1 className="text-2xl font-bold">u/{profile.username}</h1>
                {isOwnProfile && (
                  <Button
                    size="sm"
                    onClick={() => setIsEditDialogOpen(true)}
                    className="bg-yellow-400 text-black hover:bg-yellow-500 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold"
                  >
                    <Edit2 className="mr-2 h-4 w-4" />
                    Edit Profile
                  </Button>
                )}
              </div>
              <p className="text-sm text-muted-foreground mt-1">
                Member since {formatDistanceToNow(new Date(profile.createdAt))} ago
              </p>
              <BadgeList userId={profile.id} />
              {profile.bio && (
                <p className="mt-3 text-muted-foreground">{profile.bio}</p>
              )}
            </div>
          </div>
        </CardHeader>

        <Separator />

        <CardContent className="pt-6">
          <div className="grid grid-cols-2 gap-4 text-center">
            <div>
              <div className="text-2xl font-bold">{formatNumber(profile.karma)}</div>
              <div className="text-sm text-muted-foreground">Karma</div>
            </div>
            <div>
              <div className="text-2xl font-bold">{formatNumber(posts.length)}</div>
              <div className="text-sm text-muted-foreground">Posts</div>
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="space-y-4">
        <h2 className="text-xl font-semibold">Posts by u/{profile.username}</h2>
        {postsLoading ? (
          <div className="flex justify-center py-8">
            <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
          </div>
        ) : (
          <PostList posts={posts} />
        )}
      </div>

      {isOwnProfile && profile && (
        <EditProfileDialog
          isOpen={isEditDialogOpen}
          onClose={() => setIsEditDialogOpen(false)}
          profile={profile}
          onSave={handleSaveProfile}
          isUpdating={isUpdating}
        />
      )}
    </div>
  )
}
