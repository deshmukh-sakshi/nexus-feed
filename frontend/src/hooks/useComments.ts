import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { commentsApi, votesApi } from '@/lib/api-client'
import { getErrorMessage } from '@/types/errors'
import { useAuthStore } from '@/stores/authStore'
import type { CommentCreateRequest, CommentUpdateRequest, Comment } from '@/types'

export const useComments = (postId: string) => {
  const queryClient = useQueryClient()
  const { user } = useAuthStore()

  const commentsQuery = useQuery({
    queryKey: ['comments', postId],
    queryFn: () => commentsApi.getComments(postId),
    enabled: false, // Disabled - we use postWithComments instead
    retry: false,
  })

  const createCommentMutation = useMutation({
    mutationFn: (data: CommentCreateRequest) =>
      commentsApi.createComment(postId, data),
    onMutate: async (data) => {
      await queryClient.cancelQueries({ queryKey: ['comments', postId] })
      await queryClient.cancelQueries({ queryKey: ['postWithComments', postId] })
      
      const previousComments = queryClient.getQueryData(['comments', postId])
      const previousPostDetail = queryClient.getQueryData(['postWithComments', postId])
      
      // Create optimistic comment with temp ID
      const tempId = `temp-${Date.now()}-${Math.random()}`
      const optimisticComment: Comment = {
        id: tempId,
        body: data.body,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        userId: user?.userId || '',
        username: user?.username || 'You',
        postId: postId,
        parentCommentId: data.parentCommentId || undefined,
        replies: [],
        upvotes: 0,
        downvotes: 0,
        userVote: null,
      }
      
      // Update comments query
      queryClient.setQueryData(['comments', postId], (old: unknown) => {
        if (!old) return [optimisticComment]
        const comments = old as Comment[]
        
        if (data.parentCommentId) {
          // Add as reply
          const addReply = (comment: Comment): Comment => {
            if (comment.id === data.parentCommentId) {
              return {
                ...comment,
                replies: [...(comment.replies || []), optimisticComment],
              }
            }
            if (comment.replies && comment.replies.length > 0) {
              return { ...comment, replies: comment.replies.map(addReply) }
            }
            return comment
          }
          return comments.map(addReply)
        }
        
        return [optimisticComment, ...comments]
      })
      
      // Update postWithComments query
      queryClient.setQueryData(['postWithComments', postId], (old: unknown) => {
        if (!old) return old
        const postDetail = old as { post: { commentCount: number; [key: string]: unknown }; comments: Comment[] }
        return {
          ...postDetail,
          post: { ...postDetail.post, commentCount: postDetail.post.commentCount + 1 },
          comments: data.parentCommentId
            ? postDetail.comments.map((comment: Comment) => {
                const addReply = (c: Comment): Comment => {
                  if (c.id === data.parentCommentId) {
                    return { ...c, replies: [...(c.replies || []), optimisticComment] }
                  }
                  if (c.replies && c.replies.length > 0) {
                    return { ...c, replies: c.replies.map(addReply) }
                  }
                  return c
                }
                return addReply(comment)
              })
            : [optimisticComment, ...postDetail.comments],
        }
      })
      
      return { previousComments, previousPostDetail, tempId, parentCommentId: data.parentCommentId }
    },
    onSuccess: (newComment, _variables, context) => {
      // Replace temp comment with real comment from server
      if (!context) return
      
      const { tempId, parentCommentId } = context
      
      // Update comments query
      queryClient.setQueryData(['comments', postId], (old: unknown) => {
        if (!old) return old
        const comments = old as Comment[]
        
        if (parentCommentId) {
          // Replace temp reply with real reply
          const replaceReply = (comment: Comment): Comment => {
            if (comment.id === parentCommentId) {
              return {
                ...comment,
                replies: (comment.replies || []).map(r => 
                  r.id === tempId ? newComment : r
                ),
              }
            }
            if (comment.replies && comment.replies.length > 0) {
              return { ...comment, replies: comment.replies.map(replaceReply) }
            }
            return comment
          }
          return comments.map(replaceReply)
        }
        
        // Replace temp root comment with real comment
        return comments.map(c => c.id === tempId ? newComment : c)
      })
      
      // Update postWithComments query
      queryClient.setQueryData(['postWithComments', postId], (old: unknown) => {
        if (!old) return old
        const postDetail = old as { post: unknown; comments: Comment[] }
        
        if (parentCommentId) {
          return {
            ...postDetail,
            comments: postDetail.comments.map((comment: Comment) => {
              const replaceReply = (c: Comment): Comment => {
                if (c.id === parentCommentId) {
                  return {
                    ...c,
                    replies: (c.replies || []).map(r => 
                      r.id === tempId ? newComment : r
                    ),
                  }
                }
                if (c.replies && c.replies.length > 0) {
                  return { ...c, replies: c.replies.map(replaceReply) }
                }
                return c
              }
              return replaceReply(comment)
            }),
          }
        }
        
        return {
          ...postDetail,
          comments: postDetail.comments.map(c => c.id === tempId ? newComment : c),
        }
      })
    },
    onError: (error, _variables, context) => {
      if (context?.previousComments) {
        queryClient.setQueryData(['comments', postId], context.previousComments)
      }
      if (context?.previousPostDetail) {
        queryClient.setQueryData(['postWithComments', postId], context.previousPostDetail)
      }
      toast.error(getErrorMessage(error))
    },
  })

  const updateCommentMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: CommentUpdateRequest }) =>
      commentsApi.updateComment(id, data),
    onMutate: async ({ id, data }) => {
      await queryClient.cancelQueries({ queryKey: ['comments', postId] })
      await queryClient.cancelQueries({ queryKey: ['postWithComments', postId] })
      
      const previousComments = queryClient.getQueryData(['comments', postId])
      const previousPostDetail = queryClient.getQueryData(['postWithComments', postId])
      
      const updateComment = (comment: Comment): Comment => {
        if (comment.id === id) {
          return {
            ...comment,
            body: data.body,
            updatedAt: new Date().toISOString(),
          }
        }
        if (comment.replies && comment.replies.length > 0) {
          return { ...comment, replies: comment.replies.map(updateComment) }
        }
        return comment
      }
      
      queryClient.setQueryData(['comments', postId], (old: unknown) => {
        if (!old) return old
        const comments = old as Comment[]
        return comments.map(updateComment)
      })
      
      queryClient.setQueryData(['postWithComments', postId], (old: unknown) => {
        if (!old) return old
        const postDetail = old as { post: unknown; comments: Comment[] }
        return {
          ...postDetail,
          comments: postDetail.comments.map(updateComment),
        }
      })
      
      return { previousComments, previousPostDetail }
    },
    onSuccess: () => {
      toast.success('Comment updated!')
    },
    onError: (error, _variables, context) => {
      if (context?.previousComments) {
        queryClient.setQueryData(['comments', postId], context.previousComments)
      }
      if (context?.previousPostDetail) {
        queryClient.setQueryData(['postWithComments', postId], context.previousPostDetail)
      }
      toast.error(getErrorMessage(error))
    },
  })

  const deleteCommentMutation = useMutation({
    mutationFn: commentsApi.deleteComment,
    onMutate: async (id) => {
      await queryClient.cancelQueries({ queryKey: ['comments', postId] })
      await queryClient.cancelQueries({ queryKey: ['postWithComments', postId] })
      
      const previousComments = queryClient.getQueryData(['comments', postId])
      const previousPostDetail = queryClient.getQueryData(['postWithComments', postId])
      
      const removeComment = (comments: Comment[]): Comment[] => {
        return comments
          .filter((comment) => comment.id !== id)
          .map((comment) => ({
            ...comment,
            replies: comment.replies ? removeComment(comment.replies) : [],
          }))
      }
      
      queryClient.setQueryData(['comments', postId], (old: unknown) => {
        if (!old) return old
        const comments = old as Comment[]
        return removeComment(comments)
      })
      
      queryClient.setQueryData(['postWithComments', postId], (old: unknown) => {
        if (!old) return old
        const postDetail = old as { post: { commentCount: number; [key: string]: unknown }; comments: Comment[] }
        return {
          ...postDetail,
          post: { ...postDetail.post, commentCount: Math.max(0, postDetail.post.commentCount - 1) },
          comments: removeComment(postDetail.comments),
        }
      })
      
      return { previousComments, previousPostDetail }
    },
    onSuccess: () => {
      toast.success('Comment deleted!')
    },
    onError: (error, _variables, context) => {
      if (context?.previousComments) {
        queryClient.setQueryData(['comments', postId], context.previousComments)
      }
      if (context?.previousPostDetail) {
        queryClient.setQueryData(['postWithComments', postId], context.previousPostDetail)
      }
      toast.error(getErrorMessage(error))
    },
  })

  const voteCommentMutation = useMutation({
    mutationFn: ({ id, voteValue }: { id: string; voteValue: 'UPVOTE' | 'DOWNVOTE' }) =>
      votesApi.voteComment(id, voteValue),
    onMutate: async ({ id, voteValue }) => {
      await queryClient.cancelQueries({ queryKey: ['comments', postId] })
      await queryClient.cancelQueries({ queryKey: ['postWithComments', postId] })
      
      const previousComments = queryClient.getQueryData(['comments', postId])
      const previousPostDetail = queryClient.getQueryData(['postWithComments', postId])
      
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
      
      queryClient.setQueryData(['postWithComments', postId], (old: unknown) => {
        if (!old) return old
        const postDetail = old as { post: unknown; comments: Comment[] }
        return {
          ...postDetail,
          comments: postDetail.comments.map(updateCommentVote),
        }
      })
      
      return { previousComments, previousPostDetail }
    },
    onError: (err, _variables, context) => {
      if (context?.previousComments) {
        queryClient.setQueryData(['comments', postId], context.previousComments)
      }
      if (context?.previousPostDetail) {
        queryClient.setQueryData(['postWithComments', postId], context.previousPostDetail)
      }
      toast.error(getErrorMessage(err))
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
