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
    onMutate: async ({ id, voteValue }) => {
      // Cancel any outgoing refetches (so they don't overwrite our optimistic update)
      await queryClient.cancelQueries({ queryKey: ['posts'] })
      await queryClient.cancelQueries({ queryKey: ['post', id] })

      // Snapshot the previous value
      const previousPosts = queryClient.getQueryData(['posts'])
      const previousPost = queryClient.getQueryData(['post', id])

      // Helper to update a single post based on its CURRENT cache state
      // This ensures that if multiple votes happen rapidly, we calculate based on the *latest* optimistic state
      const updatePostOptimistically = (post: any) => {
        if (!post || post.id !== id) return post

        const isSameVote = post.userVote === voteValue
        const newVote = isSameVote ? null : voteValue

        let upvotes = post.upvotes
        let downvotes = post.downvotes

        // Remove old vote
        if (post.userVote === 'UPVOTE') upvotes--
        if (post.userVote === 'DOWNVOTE') downvotes--

        // Add new vote
        if (newVote === 'UPVOTE') upvotes++
        if (newVote === 'DOWNVOTE') downvotes++

        return {
          ...post,
          userVote: newVote,
          upvotes,
          downvotes,
        }
      }

      // Update Infinite Query Cache
      queryClient.setQueryData(['posts', pageSize], (old: any) => {
        if (!old) return old
        return {
          ...old,
          pages: old.pages.map((page: any) => ({
            ...page,
            content: page.content.map(updatePostOptimistically),
          })),
        }
      })

      // Update Single Post Cache
      queryClient.setQueryData(['post', id], (old: any) => {
        if (!old) return old
        return updatePostOptimistically(old)
      })

      return { previousPosts, previousPost }
    },
    onError: (err, newTodo, context) => {
      if (context?.previousPosts) {
        queryClient.setQueryData(['posts', pageSize], context.previousPosts)
      }
      if (context?.previousPost) {
        queryClient.setQueryData(['post', newTodo.id], context.previousPost)
      }
      toast.error(getErrorMessage(err))
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['posts'] })
      queryClient.invalidateQueries({ queryKey: ['post'] })
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
