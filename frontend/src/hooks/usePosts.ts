import { useInfiniteQuery, useMutation, useQueryClient, useQuery } from '@tanstack/react-query'
import { toast } from 'sonner'
import { postsApi, votesApi } from '@/lib/api-client'
import { getErrorMessage } from '@/types/errors'
import type { PostCreateRequest, PostUpdateRequest } from '@/types'

export const usePosts = (pageSize = 10) => {
  const queryClient = useQueryClient()

  const postsQuery = useInfiniteQuery({
    queryKey: ['posts', pageSize],
    queryFn: ({ pageParam = 0 }) => postsApi.getPosts(pageParam, pageSize),
    getNextPageParam: (lastPage) => {
      if (lastPage.last) return undefined
      return lastPage.number + 1
    },
    initialPageParam: 0,
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

  const updatePostMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: PostUpdateRequest }) =>
      postsApi.updatePost(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['posts'] })
      queryClient.invalidateQueries({ queryKey: ['post'] })
      toast.success('Post updated successfully!')
    },
    onError: (error) => {
      toast.error(getErrorMessage(error))
    },
  })

  const votePostMutation = useMutation({
    mutationFn: ({ id, voteValue }: { id: string; voteValue: 'UPVOTE' | 'DOWNVOTE' }) =>
      votesApi.votePost(id, voteValue),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['posts'] })
      queryClient.invalidateQueries({ queryKey: ['post'] })
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

  const posts = postsQuery.data?.pages.flatMap((page) => page.content) ?? []
  const hasNextPage = postsQuery.hasNextPage
  const isLastPage = postsQuery.data?.pages[postsQuery.data.pages.length - 1]?.last ?? false

  return {
    posts,
    isLoading: postsQuery.isLoading,
    error: postsQuery.error,
    refetch: postsQuery.refetch,
    fetchNextPage: postsQuery.fetchNextPage,
    hasNextPage,
    isLastPage,
    isFetchingNextPage: postsQuery.isFetchingNextPage,
    createPost: (data: PostCreateRequest) => createPostMutation.mutate(data),
    updatePost: (id: string, data: PostUpdateRequest) =>
      updatePostMutation.mutate({ id, data }),
    votePost: (id: string, voteValue: 'UPVOTE' | 'DOWNVOTE') =>
      votePostMutation.mutate({ id, voteValue }),
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
