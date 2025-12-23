import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { votesApi } from '@/lib/api-client'
import { getErrorMessage } from '@/types/errors'
import type { Post } from '@/types'

/**
 * Shared optimistic vote logic for posts.
 * Updates all relevant caches: posts list, single post, user posts, and post detail.
 */
export const useOptimisticVote = () => {
  const queryClient = useQueryClient()

  const updatePostOptimistically = (post: Post, id: string, voteValue: 'UPVOTE' | 'DOWNVOTE'): Post => {
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

    return { ...post, userVote: newVote, upvotes, downvotes }
  }

  const votePostMutation = useMutation({
    mutationFn: ({ id, voteValue }: { id: string; voteValue: 'UPVOTE' | 'DOWNVOTE' }) =>
      votesApi.votePost(id, voteValue),
    onMutate: async ({ id, voteValue }) => {
      // Cancel any outgoing refetches
      await queryClient.cancelQueries({ queryKey: ['posts'] })
      await queryClient.cancelQueries({ queryKey: ['post', id] })
      await queryClient.cancelQueries({ queryKey: ['userPosts'] })
      await queryClient.cancelQueries({ queryKey: ['postWithComments', id] })

      // Snapshot previous values
      const previousPostsQueries = queryClient.getQueriesData({ queryKey: ['posts'] })
      const previousPost = queryClient.getQueryData(['post', id])
      const previousUserPosts = new Map<string, unknown>()
      const previousPostDetail = queryClient.getQueryData(['postWithComments', id])

      // Update all posts list queries (handles any page size)
      queryClient.setQueriesData({ queryKey: ['posts'] }, (old: unknown) => {
        if (!old) return old
        const typedOld = old as { pages?: { content: Post[]; [key: string]: unknown }[] }
        if (!typedOld.pages) return old
        return {
          ...typedOld,
          pages: typedOld.pages.map((page) => ({
            ...page,
            content: page.content.map((post) => updatePostOptimistically(post, id, voteValue)),
          })),
        }
      })

      // Update single post cache
      queryClient.setQueryData(['post', id], (old: unknown) => {
        if (!old) return old
        return updatePostOptimistically(old as Post, id, voteValue)
      })

      // Update user posts cache
      queryClient.getQueriesData({ queryKey: ['userPosts'] }).forEach(([key, data]) => {
        if (data) {
          previousUserPosts.set(JSON.stringify(key), data)
          queryClient.setQueryData(key, (old: unknown) => {
            if (!old) return old
            const typedOld = old as { content: Post[]; [key: string]: unknown }
            return {
              ...typedOld,
              content: typedOld.content.map((post) => updatePostOptimistically(post, id, voteValue)),
            }
          })
        }
      })

      // Update post detail cache
      queryClient.setQueryData(['postWithComments', id], (old: unknown) => {
        if (!old) return old
        const postDetail = old as { post: Post; comments: unknown[] }
        return {
          ...postDetail,
          post: updatePostOptimistically(postDetail.post, id, voteValue),
        }
      })

      return { previousPostsQueries, previousPost, previousUserPosts, previousPostDetail }
    },
    onError: (err, variables, context) => {
      // Restore all previous values on error
      if (context?.previousPostsQueries) {
        context.previousPostsQueries.forEach(([queryKey, data]) => {
          queryClient.setQueryData(queryKey, data)
        })
      }
      if (context?.previousPost) {
        queryClient.setQueryData(['post', variables.id], context.previousPost)
      }
      if (context?.previousUserPosts) {
        context.previousUserPosts.forEach((data, key) => {
          queryClient.setQueryData(JSON.parse(key), data)
        })
      }
      if (context?.previousPostDetail) {
        queryClient.setQueryData(['postWithComments', variables.id], context.previousPostDetail)
      }
      toast.error(getErrorMessage(err))
    },
  })

  return {
    votePost: (id: string, voteValue: 'UPVOTE' | 'DOWNVOTE') =>
      votePostMutation.mutate({ id, voteValue }),
    isVoting: votePostMutation.isPending,
  }
}
