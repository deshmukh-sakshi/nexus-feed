import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { votesApi } from '@/lib/api-client'
import { getErrorMessage } from '@/types/errors'
import type { Post } from '@/types'

export const useVotePost = () => {
  const queryClient = useQueryClient()

  const votePostMutation = useMutation({
    mutationFn: ({ id, voteValue }: { id: string; voteValue: 'UPVOTE' | 'DOWNVOTE' }) =>
      votesApi.votePost(id, voteValue),
    onMutate: async ({ id, voteValue }) => {
      // Cancel any outgoing refetches (so they don't overwrite our optimistic update)
      await queryClient.cancelQueries({ queryKey: ['posts'] })
      await queryClient.cancelQueries({ queryKey: ['post', id] })
      await queryClient.cancelQueries({ queryKey: ['userPosts'] })
      await queryClient.cancelQueries({ queryKey: ['postWithComments', id] })

      // Snapshot the previous values
      const previousPosts4 = queryClient.getQueryData(['posts', 4])
      const previousPost = queryClient.getQueryData(['post', id])
      const previousUserPosts = new Map()
      const previousPostDetail = queryClient.getQueryData(['postWithComments', id])

      // Helper to update a single post based on its CURRENT cache state
      const updatePostOptimistically = (post: Post) => {
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

      // Update Infinite Query Cache (main feed) - handle all page sizes
      queryClient.setQueriesData({ queryKey: ['posts'] }, (old: unknown) => {
        if (!old) return old
        const typedOld = old as { pages: { content: Post[]; [key: string]: unknown }[] }
        if (!typedOld.pages) return old
        return {
          ...typedOld,
          pages: typedOld.pages.map((page) => ({
            ...page,
            content: page.content.map(updatePostOptimistically),
          })),
        }
      })

      // Update Single Post Cache
      queryClient.setQueryData(['post', id], (old: unknown) => {
        if (!old) return old
        return updatePostOptimistically(old as Post)
      })

      // Update User Posts Cache (profile page)
      queryClient.getQueriesData({ queryKey: ['userPosts'] }).forEach(([key, data]) => {
        if (data) {
          previousUserPosts.set(JSON.stringify(key), data)
          queryClient.setQueryData(key, (old: unknown) => {
            if (!old) return old
            const typedOld = old as { content: Post[]; [key: string]: unknown }
            return {
              ...typedOld,
              content: typedOld.content.map(updatePostOptimistically),
            }
          })
        }
      })

      // Update Post Detail Cache (post detail page)
      queryClient.setQueryData(['postWithComments', id], (old: unknown) => {
        if (!old) return old
        const postDetail = old as { post: Post; comments: unknown[] }
        return {
          ...postDetail,
          post: updatePostOptimistically(postDetail.post),
        }
      })

      return { previousPosts4, previousPost, previousUserPosts, previousPostDetail }
    },
    onError: (err, variables, context) => {
      if (context?.previousPosts4) {
        queryClient.setQueryData(['posts', 4], context.previousPosts4)
      }
      if (context?.previousPost) {
        queryClient.setQueryData(['post', variables.id], context.previousPost)
      }
      if (context?.previousUserPosts) {
        context.previousUserPosts.forEach((data: unknown, key: string) => {
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
  }
}
