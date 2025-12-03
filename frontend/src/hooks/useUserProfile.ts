import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { usersApi } from '@/lib/api-client'
import { useAuthStore } from '@/stores/authStore'
import { getErrorMessage } from '@/types/errors'
import type { UserProfile } from '@/types'

interface UserUpdateRequest {
  bio?: string
  profilePictureUrl?: string
}

export const useUserProfile = (username?: string) => {
  const queryClient = useQueryClient()
  const { user: currentUser, updateProfilePicture } = useAuthStore()

  const profileQuery = useQuery({
    queryKey: ['user', username],
    queryFn: () => usersApi.getUserByUsername(username!),
    enabled: !!username,
    retry: false,
  })

  const updateProfileMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: UserUpdateRequest }) =>
      usersApi.updateUser(id, data),
    onMutate: async ({ data }) => {
      // Cancel any outgoing refetches
      await queryClient.cancelQueries({ queryKey: ['user', username] })

      // Snapshot the previous value
      const previousProfile = queryClient.getQueryData<UserProfile>(['user', username])

      // Optimistically update to the new value
      if (previousProfile) {
        queryClient.setQueryData<UserProfile>(['user', username], {
          ...previousProfile,
          ...data,
          updatedAt: new Date().toISOString(),
        })
      }

      // Return a context object with the snapshotted value
      return { previousProfile }
    },
    onError: (error, _variables, context) => {
      // If the mutation fails, use the context returned from onMutate to roll back
      if (context?.previousProfile) {
        queryClient.setQueryData(['user', username], context.previousProfile)
      }
      toast.error(getErrorMessage(error))
    },
    onSuccess: (updatedProfile, { id, data }) => {
      // Update auth store if this is the current user's profile
      if (currentUser?.userId === id && data.profilePictureUrl !== undefined) {
        updateProfilePicture(data.profilePictureUrl)
      }
      toast.success('Profile updated successfully!')
    },
    onSettled: () => {
      // Always refetch after error or success to ensure we have the latest data
      queryClient.invalidateQueries({ queryKey: ['user', username] })
    },
  })

  return {
    profile: profileQuery.data,
    isLoading: profileQuery.isLoading,
    error: profileQuery.error,
    refetch: profileQuery.refetch,
    updateProfile: (id: string, data: UserUpdateRequest) =>
      updateProfileMutation.mutate({ id, data }),
    isUpdating: updateProfileMutation.isPending,
  }
}
