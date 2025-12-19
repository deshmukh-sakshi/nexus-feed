import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { adminApi } from '@/lib/api-client'
import { getErrorMessage } from '@/types/errors'

export const useAdminStats = () => {
  return useQuery({
    queryKey: ['admin', 'stats'],
    queryFn: adminApi.getStats,
  })
}

export const useAdminUsers = (page = 0, size = 10) => {
  return useQuery({
    queryKey: ['admin', 'users', page, size],
    queryFn: () => adminApi.getUsers(page, size),
  })
}

export const useAdminPosts = (page = 0, size = 10) => {
  return useQuery({
    queryKey: ['admin', 'posts', page, size],
    queryFn: () => adminApi.getPosts(page, size),
  })
}

export const useAdminComments = (page = 0, size = 10) => {
  return useQuery({
    queryKey: ['admin', 'comments', page, size],
    queryFn: () => adminApi.getComments(page, size),
  })
}

export const useUpdateUserRole = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ userId, role }: { userId: string; role: string }) =>
      adminApi.updateUserRole(userId, role),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'users'] })
      toast.success('User role updated')
    },
    onError: (error) => toast.error(getErrorMessage(error)),
  })
}

export const useDeleteUser = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: adminApi.deleteUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin'] })
      toast.success('User deleted')
    },
    onError: (error) => toast.error(getErrorMessage(error)),
  })
}

export const useDeletePost = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: adminApi.deletePost,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin'] })
      toast.success('Post deleted')
    },
    onError: (error) => toast.error(getErrorMessage(error)),
  })
}

export const useDeleteComment = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: adminApi.deleteComment,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin'] })
      toast.success('Comment deleted')
    },
    onError: (error) => toast.error(getErrorMessage(error)),
  })
}
