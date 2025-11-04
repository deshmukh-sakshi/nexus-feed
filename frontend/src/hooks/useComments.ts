import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { commentsApi, votesApi } from '@/lib/api-client'
import { getErrorMessage } from '@/types/errors'
import type { CommentCreateRequest, CommentUpdateRequest, VoteRequest } from '@/types'

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
      queryClient.invalidateQueries({ queryKey: ['post', postId] })
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
      queryClient.invalidateQueries({ queryKey: ['post', postId] })
      toast.success('Comment deleted successfully!')
    },
    onError: (error) => {
      toast.error(getErrorMessage(error))
    },
  })

  const voteCommentMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: VoteRequest }) =>
      votesApi.voteComment(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['comments', postId] })
    },
    onError: (error) => {
      toast.error(getErrorMessage(error))
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
    deleteComment: (id: string) => deleteCommentMutation.mutate(id),
    voteComment: (id: string, data: VoteRequest) =>
      voteCommentMutation.mutate({ id, data }),
    isCreating: createCommentMutation.isPending,
  }
}
