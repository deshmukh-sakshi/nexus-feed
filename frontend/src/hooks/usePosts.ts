import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { postsApi } from '@/lib/api-client'
import { getErrorMessage } from '@/types/errors'
import type { PostCreateRequest, VoteRequest } from '@/types'

export const usePosts = () => {
  const queryClient = useQueryClient()

  const postsQuery = useQuery({
    queryKey: ['posts'],
    queryFn: postsApi.getPosts,
    retry: false,
  })

  const createPostMutation = useMutation({
    mutationFn: postsApi.createPost,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['posts'] })
      toast.success('Post created successfully!')
    },
    onError: (error) => {
      toast.error(getErrorMessage(error))
    },
  })

  const votePostMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: VoteRequest }) =>
      postsApi.votePost(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['posts'] })
    },
    onError: (error) => {
      toast.error(getErrorMessage(error))
    },
  })

  const deletePostMutation = useMutation({
    mutationFn: postsApi.deletePost,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['posts'] })
      toast.success('Post deleted successfully!')
    },
    onError: (error) => {
      toast.error(getErrorMessage(error))
    },
  })

  return {
    posts: Array.isArray(postsQuery.data) ? postsQuery.data : [],
    isLoading: postsQuery.isLoading,
    error: postsQuery.error,
    refetch: postsQuery.refetch,
    createPost: (data: PostCreateRequest) => createPostMutation.mutate(data),
    votePost: (id: string, data: VoteRequest) =>
      votePostMutation.mutate({ id, data }),
    deletePost: (id: string) => deletePostMutation.mutate(id),
    isCreating: createPostMutation.isPending,
  }
}

export const usePost = (id: string) => {
  return useQuery({
    queryKey: ['post', id],
    queryFn: () => postsApi.getPost(id),
    enabled: !!id,
  })
}
