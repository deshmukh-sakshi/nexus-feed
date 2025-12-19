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
    onMutate: async (userId: string) => {
      await queryClient.cancelQueries({ queryKey: ['admin', 'users'] })
      const previousData = queryClient.getQueriesData({ queryKey: ['admin', 'users'] })
      
      queryClient.setQueriesData(
        { queryKey: ['admin', 'users'] },
        (old: import('@/types').PageResponse<import('@/types').AdminUser> | undefined) => {
          if (!old) return old
          return {
            ...old,
            content: old.content.filter((user) => user.id !== userId),
          }
        }
      )
      return { previousData }
    },
    onError: (error, _userId, context) => {
      if (context?.previousData) {
        context.previousData.forEach(([queryKey, data]) => {
          queryClient.setQueryData(queryKey, data)
        })
      }
      toast.error(getErrorMessage(error))
    },
    onSuccess: () => {
      toast.success('User deleted')
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'stats'] })
    },
  })
}

export const useDeletePost = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: adminApi.deletePost,
    onMutate: async (postId: string) => {
      await queryClient.cancelQueries({ queryKey: ['admin', 'posts'] })
      const previousData = queryClient.getQueriesData({ queryKey: ['admin', 'posts'] })
      
      queryClient.setQueriesData(
        { queryKey: ['admin', 'posts'] },
        (old: import('@/types').PageResponse<import('@/types').AdminPost> | undefined) => {
          if (!old) return old
          return {
            ...old,
            content: old.content.filter((post) => post.id !== postId),
          }
        }
      )
      return { previousData }
    },
    onError: (error, _postId, context) => {
      if (context?.previousData) {
        context.previousData.forEach(([queryKey, data]) => {
          queryClient.setQueryData(queryKey, data)
        })
      }
      toast.error(getErrorMessage(error))
    },
    onSuccess: () => {
      toast.success('Post deleted')
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'stats'] })
      queryClient.invalidateQueries({ queryKey: ['posts'] })
    },
  })
}

export const useDeleteComment = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: adminApi.deleteComment,
    onMutate: async (commentId: string) => {
      await queryClient.cancelQueries({ queryKey: ['admin', 'comments'] })
      const previousData = queryClient.getQueriesData({ queryKey: ['admin', 'comments'] })
      
      queryClient.setQueriesData(
        { queryKey: ['admin', 'comments'] },
        (old: import('@/types').PageResponse<import('@/types').AdminComment> | undefined) => {
          if (!old) return old
          return {
            ...old,
            content: old.content.filter((comment) => comment.id !== commentId),
          }
        }
      )
      return { previousData }
    },
    onError: (error, _commentId, context) => {
      if (context?.previousData) {
        context.previousData.forEach(([queryKey, data]) => {
          queryClient.setQueryData(queryKey, data)
        })
      }
      toast.error(getErrorMessage(error))
    },
    onSuccess: () => {
      toast.success('Comment deleted')
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'stats'] })
      queryClient.invalidateQueries({ queryKey: ['comments'] })
    },
  })
}
