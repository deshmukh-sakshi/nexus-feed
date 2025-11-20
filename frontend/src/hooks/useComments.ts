import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { commentsApi, votesApi } from '@/lib/api-client'
import { getErrorMessage } from '@/types/errors'
import type { CommentCreateRequest, CommentUpdateRequest, Comment } from '@/types'

export const useComments = (postId: string) => {
  const queryClient = useQueryClient()

  const commentsQuery = useQuery({
    queryKey: ['comments', postId],
    queryFn: () => commentsApi.getComments(postId),
    enabled: !!postId,
    retry: false,
  })

  const createCommentMutation = useMutation({
    mutationFn: (data: CommentCreateRequest) =>
      commentsApi.createComment(postId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['comments', postId] })
      queryClient.setQueryData(['post', postId], (old: unknown) => {
        if (!old) return old
        const post = old as { commentCount: number; [key: string]: unknown }
        return { ...post, commentCount: post.commentCount + 1 }
      })
      toast.success('Comment posted successfully!')
    },
    onError: (error) => {
      toast.error(getErrorMessage(error))
    },
  })

  const updateCommentMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: CommentUpdateRequest }) =>
      commentsApi.updateComment(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['comments', postId] })
      toast.success('Comment updated successfully!')
    },
    onError: (error) => {
      toast.error(getErrorMessage(error))
    },
  })

  const deleteCommentMutation = useMutation({
    mutationFn: commentsApi.deleteComment,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['comments', postId] })
      queryClient.setQueryData(['post', postId], (old: unknown) => {
        if (!old) return old
        const post = old as { commentCount: number; [key: string]: unknown }
        return { ...post, commentCount: Math.max(0, post.commentCount - 1) }
      })
      toast.success('Comment deleted successfully!')
    },
    onError: (error) => {
      toast.error(getErrorMessage(error))
    },
  })

  const voteCommentMutation = useMutation({
    mutationFn: ({ id, voteValue }: { id: string; voteValue: 'UPVOTE' | 'DOWNVOTE' }) =>
      votesApi.voteComment(id, voteValue),
    onMutate: async ({ id, voteValue }) => {
      await queryClient.cancelQueries({ queryKey: ['comments', postId] })
      
      const previousComments = queryClient.getQueryData(['comments', postId])
      
      const updateCommentVote = (comment: Comment): Comment => {
        if (comment.id === id) {
          const isSameVote = comment.userVote === voteValue
          const newVote = isSameVote ? null : voteValue
          
          let upvotes = comment.upvotes
          let downvotes = comment.downvotes
          
          if (comment.userVote === 'UPVOTE') upvotes--
          if (comment.userVote === 'DOWNVOTE') downvotes--
          
          if (newVote === 'UPVOTE') upvotes++
          if (newVote === 'DOWNVOTE') downvotes++
          
          return { ...comment, userVote: newVote, upvotes, downvotes }
        }
        
        if (comment.replies && comment.replies.length > 0) {
          return { ...comment, replies: comment.replies.map(updateCommentVote) }
        }
        
        return comment
      }
      
      queryClient.setQueryData(['comments', postId], (old: unknown) => {
        if (!old) return old
        const comments = old as Comment[]
        return comments.map(updateCommentVote)
      })
      
      return { previousComments }
    },
    onError: (err, _variables, context) => {
      if (context?.previousComments) {
        queryClient.setQueryData(['comments', postId], context.previousComments)
      }
      toast.error(getErrorMessage(err))
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['comments', postId] })
    },
  })

  return {
    comments: commentsQuery.data ?? [],
    isLoading: commentsQuery.isLoading,
    error: commentsQuery.error,
    refetch: commentsQuery.refetch,
    createComment: (data: CommentCreateRequest) =>
      createCommentMutation.mutate(data),
    updateComment: (id: string, data: CommentUpdateRequest) =>
      updateCommentMutation.mutate({ id, data }),
    deleteComment: (id: string) => deleteCommentMutation.mutateAsync(id),
    voteComment: (id: string, voteValue: 'UPVOTE' | 'DOWNVOTE') =>
      voteCommentMutation.mutate({ id, voteValue }),
    isCreating: createCommentMutation.isPending,
  }
}
