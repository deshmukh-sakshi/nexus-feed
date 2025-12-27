import { useState } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { formatDistanceToNow } from 'date-fns'
import ReactMarkdown from 'react-markdown'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import {
  ArrowBigUp,
  ArrowBigDown,
  MessageSquare,
  Edit2,
  Trash2,
  Save,
  X,
  ArrowLeft,
  Flag,
} from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardFooter, CardHeader } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { Textarea } from '@/components/ui/textarea'
import { Input } from '@/components/ui/input'
import { UserAvatar } from '@/components/ui/user-avatar'
import { AuthModal } from '@/components/ui/auth-modal'
import { ConfirmDialog } from '@/components/ui/confirm-dialog'
import { ImageGallery } from '@/components/ui/image-gallery'
import { ImageUpload } from '@/components/ui/image-upload'
import { CommentList } from '@/components/posts/CommentList'
import { PostDetailSkeleton } from '@/components/posts/PostDetailSkeleton'
import { ReportModal } from '@/components/posts/ReportModal'
import { cn, formatNumber } from '@/lib/utils'
import { generateTagColor, getTagTextColor } from '@/lib/tag-colors'
import { useAuthStore } from '@/stores/authStore'
import { usePostWithComments } from '@/hooks/usePosts'
import { useComments } from '@/hooks/useComments'
import { useReport } from '@/hooks/useReport'
import { postsApi, votesApi } from '@/lib/api-client'
import { getErrorMessage } from '@/types/errors'
import type { PostUpdateRequest } from '@/types'

export const PostDetail = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { user, isAuthenticated } = useAuthStore()
  const queryClient = useQueryClient()
  const { data: postDetail, isLoading: postLoading, error: postError } = usePostWithComments(id!)
  
  const post = postDetail?.post
  const comments = postDetail?.comments || []

  const [commentBody, setCommentBody] = useState('')
  const [isEditing, setIsEditing] = useState(false)
  const [editTitle, setEditTitle] = useState('')
  const [editBody, setEditBody] = useState('')
  const [editImageUrls, setEditImageUrls] = useState<string[]>([])
  const [editTags, setEditTags] = useState<string[]>([])
  const [tagInput, setTagInput] = useState('')
  const [isUploadingImages, setIsUploadingImages] = useState(false)
  const [showAuthModal, setShowAuthModal] = useState(false)
  const [showDeleteDialog, setShowDeleteDialog] = useState(false)
  const [showReportModal, setShowReportModal] = useState(false)

  // Report hook - only enabled when authenticated
  const { hasReported, isLoadingStatus: isLoadingReportStatus } = useReport(id!)

  // Mutations
  const votePostMutation = useMutation({
    mutationFn: ({ voteValue }: { voteValue: 'UPVOTE' | 'DOWNVOTE' }) =>
      votesApi.votePost(id!, voteValue),
    onMutate: async ({ voteValue }) => {
      await queryClient.cancelQueries({ queryKey: ['postWithComments', id] })
      
      const previousData = queryClient.getQueryData(['postWithComments', id])
      
      queryClient.setQueryData(['postWithComments', id], (old: unknown) => {
        if (!old) return old
        const postDetail = old as { post: typeof post; comments: typeof comments }
        if (!postDetail.post) return old
        
        const currentPost = postDetail.post
        const isSameVote = currentPost.userVote === voteValue
        const newVote = isSameVote ? null : voteValue
        
        let upvotes = currentPost.upvotes
        let downvotes = currentPost.downvotes
        
        if (currentPost.userVote === 'UPVOTE') upvotes--
        if (currentPost.userVote === 'DOWNVOTE') downvotes--
        
        if (newVote === 'UPVOTE') upvotes++
        if (newVote === 'DOWNVOTE') downvotes++
        
        return {
          ...postDetail,
          post: {
            ...currentPost,
            userVote: newVote,
            upvotes,
            downvotes,
          },
        }
      })
      
      return { previousData }
    },
    onError: (error, _variables, context) => {
      if (context?.previousData) {
        queryClient.setQueryData(['postWithComments', id], context.previousData)
      }
      toast.error(getErrorMessage(error))
    },
  })

  const updatePostMutation = useMutation({
    mutationFn: (data: PostUpdateRequest) => postsApi.updatePost(id!, data),
    onMutate: async (data) => {
      await queryClient.cancelQueries({ queryKey: ['postWithComments', id] })
      await queryClient.cancelQueries({ queryKey: ['posts'] })
      
      const previousData = queryClient.getQueryData(['postWithComments', id])
      
      const now = new Date().toISOString()
      
      // Optimistically update the post in detail view
      queryClient.setQueryData(['postWithComments', id], (old: unknown) => {
        if (!old) return old
        const postDetail = old as { post: typeof post; comments: typeof comments }
        if (!postDetail.post) return old
        return {
          ...postDetail,
          post: {
            ...postDetail.post,
            title: data.title || postDetail.post.title,
            body: data.body !== undefined ? data.body : postDetail.post.body,
            imageUrls: data.imageUrls !== undefined ? data.imageUrls : postDetail.post.imageUrls,
            tags: data.tags !== undefined ? data.tags : postDetail.post.tags,
            updatedAt: now,
          },
        }
      })
      
      // Also update in all posts list queries
      queryClient.setQueriesData({ queryKey: ['posts'] }, (old: unknown) => {
        if (!old) return old
        const typedOld = old as { pages: { content: { id: string; [key: string]: unknown }[]; [key: string]: unknown }[] }
        return {
          ...typedOld,
          pages: typedOld.pages.map((page) => ({
            ...page,
            content: page.content.map((p) => {
              if (p.id !== id) return p
              return {
                ...p,
                title: data.title || p.title,
                body: data.body !== undefined ? data.body : p.body,
                imageUrls: data.imageUrls !== undefined ? data.imageUrls : p.imageUrls,
                tags: data.tags !== undefined ? data.tags : p.tags,
                updatedAt: now,
              }
            }),
          })),
        }
      })
      
      return { previousData }
    },
    onSuccess: (updatedPost) => {
      // Silently replace optimistic data with real server response
      queryClient.setQueryData(['postWithComments', id], (old: unknown) => {
        if (!old) return old
        const postDetail = old as { post: typeof post; comments: typeof comments }
        return {
          ...postDetail,
          post: updatedPost,
        }
      })
      
      queryClient.setQueriesData({ queryKey: ['posts'] }, (old: unknown) => {
        if (!old) return old
        const typedOld = old as { pages: { content: { id: string; [key: string]: unknown }[]; [key: string]: unknown }[] }
        return {
          ...typedOld,
          pages: typedOld.pages.map((page) => ({
            ...page,
            content: page.content.map((p) => 
              p.id === id ? updatedPost : p
            ),
          })),
        }
      })
      
      toast.success('Post updated!')
    },
    onError: (error, _variables, context) => {
      if (context?.previousData) {
        queryClient.setQueryData(['postWithComments', id], context.previousData)
      }
      toast.error(getErrorMessage(error))
    },
  })

  const deletePostMutation = useMutation({
    mutationFn: () => postsApi.deletePost(id!),
    onMutate: async () => {
      // Cancel any outgoing refetches for all page sizes
      await queryClient.cancelQueries({ queryKey: ['posts'] })
      
      // Snapshot previous values for all page sizes
      const previousPosts4 = queryClient.getQueryData(['posts', 4])
      const previousPosts10 = queryClient.getQueryData(['posts', 10])
      
      // Optimistically remove post from all cached queries
      queryClient.setQueriesData({ queryKey: ['posts'] }, (old: unknown) => {
        if (!old) return old
        const typedOld = old as { pages: { content: { id: string; [key: string]: unknown }[]; [key: string]: unknown }[] }
        return {
          ...typedOld,
          pages: typedOld.pages.map((page) => ({
            ...page,
            content: page.content.filter((p) => p.id !== id),
          })),
        }
      })
      
      // Show loading toast immediately
      toast.loading('Deleting post...', { id: 'delete-post' })
      
      // Navigate after optimistic update
      navigate('/')
      
      return { previousPosts4, previousPosts10 }
    },
    onSuccess: () => {
      // Refetch to get fresh data
      queryClient.invalidateQueries({ queryKey: ['posts'] })
      toast.dismiss('delete-post')
      toast.success('Post deleted!')
    },
    onError: (error, _variables, context) => {
      // Restore previous posts on error
      if (context?.previousPosts4) {
        queryClient.setQueryData(['posts', 4], context.previousPosts4)
      }
      if (context?.previousPosts10) {
        queryClient.setQueryData(['posts', 10], context.previousPosts10)
      }
      toast.dismiss('delete-post')
      toast.error(getErrorMessage(error))
    },
  })

  // Use the useComments hook for optimistic updates
  const { createComment: createCommentWithOptimisticUpdate } = useComments(id!)

  if (postLoading) {
    return <PostDetailSkeleton navigate={navigate} />
  }

  if (postError || !post) {
    return (
      <div className="flex flex-col items-center justify-center py-12 space-y-4">
        <p className="text-destructive">Failed to load post. It may not exist.</p>
        <Button 
          onClick={() => navigate('/')} 
          className="bg-gray-300 text-black hover:bg-gray-400 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold"
        >
          Go Back Home
        </Button>
      </div>
    )
  }

  const score = post.upvotes - post.downvotes
  const isOwner = user?.userId === post.userId
  // Only show edited if there's more than 1 second difference (to handle timestamp precision issues)
  const isEdited = Math.abs(new Date(post.updatedAt).getTime() - new Date(post.createdAt).getTime()) > 1000

  const handleVote = (voteValue: 'UPVOTE' | 'DOWNVOTE') => {
    if (!isAuthenticated) {
      setShowAuthModal(true)
      return
    }
    votePostMutation.mutate({ voteValue })
  }

  const handleSubmitComment = () => {
    if (!isAuthenticated) {
      setShowAuthModal(true)
      return
    }
    if (commentBody.trim()) {
      createCommentWithOptimisticUpdate({ body: commentBody.trim() })
      setCommentBody('')
    }
  }

  const handleStartEdit = () => {
    setEditTitle(post.title)
    setEditBody(post.body || '')
    setEditImageUrls(post.imageUrls || [])
    setEditTags(post.tags || [])
    setTagInput('')
    setIsEditing(true)
  }

  const handleSaveEdit = () => {
    if (editTitle.trim() && !isUploadingImages) {
      updatePostMutation.mutate({
        title: editTitle.trim(),
        body: editBody.trim() || undefined,
        imageUrls: editImageUrls,
        tags: editTags,
      })
      setIsEditing(false)
    }
  }

  const handleCancelEdit = () => {
    setIsEditing(false)
    setEditTitle('')
    setEditBody('')
    setEditImageUrls([])
    setEditTags([])
    setTagInput('')
  }

  const handleDelete = () => {
    setShowDeleteDialog(true)
  }

  const confirmDelete = () => {
    deletePostMutation.mutate()
  }

  return (
    <div className="w-full max-w-4xl mx-auto space-y-4">
      <Button
        size="sm"
        onClick={(e) => {
          e.preventDefault()
          e.stopPropagation()
          navigate(-1)
        }}
        className="mb-4 bg-gray-300 text-black hover:bg-gray-400 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold"
      >
        <ArrowLeft className="mr-2 h-4 w-4" />
        Back
      </Button>

      <Card className="bg-yellow-50">
        <CardHeader>
          <div className="flex items-start gap-3">
            {/* Vote Bar */}
            <div className="flex flex-col items-center gap-2 bg-pink-200 border-2 border-black rounded-full px-2 py-2 shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] dark:shadow-[2px_2px_0px_0px_rgba(255,255,255,1)]">
              <Button
                size="icon"
                className={cn(
                  'h-7 w-7 rounded-full transition-all shadow-[1px_1px_0px_0px_rgba(0,0,0,1)]',
                  post.userVote === 'UPVOTE' 
                    ? 'bg-orange-400 text-black hover:bg-orange-500' 
                    : 'bg-white text-black hover:bg-orange-300'
                )}
                onClick={() => handleVote('UPVOTE')}
              >
                <ArrowBigUp className={cn("h-5 w-5", post.userVote === 'UPVOTE' && "fill-current")} />
              </Button>
              <span className="text-sm font-bold">{formatNumber(score)}</span>
              <Button
                size="icon"
                className={cn(
                  'h-7 w-7 rounded-full transition-all shadow-[1px_1px_0px_0px_rgba(0,0,0,1)]',
                  post.userVote === 'DOWNVOTE' 
                    ? 'bg-blue-400 text-black hover:bg-blue-500' 
                    : 'bg-white text-black hover:bg-blue-300'
                )}
                onClick={() => handleVote('DOWNVOTE')}
              >
                <ArrowBigDown className={cn("h-5 w-5", post.userVote === 'DOWNVOTE' && "fill-current")} />
              </Button>
            </div>

            <div className="flex-1 space-y-3">
              <div className="flex items-center gap-2 group">
                <Link to={`/user/${post.username}`} className="group-hover:opacity-80">
                  <UserAvatar username={post.username} profileImageUrl={post.profilePictureUrl} size="sm" />
                </Link>
                <div className="flex items-center gap-2 text-xs text-muted-foreground">
                  <Link
                    to={`/user/${post.username}`}
                    className="group-hover:underline font-medium"
                  >
                    u/{post.username}
                  </Link>
                  <span>•</span>
                  <span>{formatDistanceToNow(new Date(post.createdAt))} ago</span>
                  {isEdited && <span className="italic">(edited)</span>}
                </div>
              </div>

              {isEditing ? (
                <div className="space-y-3">
                  <Input
                    type="text"
                    value={editTitle}
                    onChange={(e) => setEditTitle(e.target.value)}
                    className="text-2xl font-bold"
                    placeholder="Post title"
                  />
                  <Textarea
                    value={editBody}
                    onChange={(e) => setEditBody(e.target.value)}
                    className="min-h-[200px] resize-y"
                    placeholder="Post body (Markdown supported)"
                  />
                </div>
              ) : (
                <>
                  <h1 className="text-2xl font-bold">{post.title}</h1>
                </>
              )}
            </div>
          </div>
        </CardHeader>

        {isEditing && (
          <CardContent>
            <div className="space-y-3">
              {/* Tag editing */}
              <div className="space-y-2">
                <label className="text-sm font-medium">Tags (max 5)</label>
                <div className="flex flex-wrap gap-2 mb-2">
                  {editTags.map((tag) => (
                    <span
                      key={tag}
                      className="px-3 py-1 text-sm font-bold border border-black flex items-center gap-1"
                      style={{
                        backgroundColor: generateTagColor(tag),
                        color: getTagTextColor(tag),
                      }}
                    >
                      #{tag}
                      <button
                        type="button"
                        onClick={() => setEditTags(editTags.filter(t => t !== tag))}
                        className="ml-1 hover:opacity-70"
                      >
                        <X className="h-3 w-3" />
                      </button>
                    </span>
                  ))}
                </div>
                {editTags.length < 5 && (
                  <div className="flex flex-col sm:flex-row gap-2 w-full">
                    <Input
                      type="text"
                      value={tagInput}
                      onChange={(e) => setTagInput(e.target.value.toLowerCase().replace(/[^a-z0-9-]/g, ''))}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter' && tagInput.trim()) {
                          e.preventDefault()
                          const newTag = tagInput.trim()
                          if (newTag && !editTags.includes(newTag) && editTags.length < 5) {
                            setEditTags([...editTags, newTag])
                            setTagInput('')
                          }
                        }
                      }}
                      placeholder="Add a tag..."
                      className="flex-1 w-full"
                      maxLength={50}
                    />
                    <Button
                      type="button"
                      onClick={() => {
                        const newTag = tagInput.trim()
                        if (newTag && !editTags.includes(newTag) && editTags.length < 5) {
                          setEditTags([...editTags, newTag])
                          setTagInput('')
                        }
                      }}
                      disabled={!tagInput.trim() || editTags.includes(tagInput.trim())}
                      className="w-full sm:w-auto bg-blue-400 text-black hover:bg-blue-500 border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] rounded-none font-bold disabled:opacity-50"
                    >
                      Add
                    </Button>
                  </div>
                )}
              </div>
              <ImageUpload
                value={editImageUrls}
                onChange={setEditImageUrls}
                maxSizeMB={5}
                disabled={updatePostMutation.isPending}
                onUploadingChange={setIsUploadingImages}
              />
              <div className="flex flex-col sm:flex-row gap-2 w-full">
                <Button 
                  onClick={handleSaveEdit}
                  disabled={!editTitle.trim() || isUploadingImages}
                  className="w-full sm:w-auto bg-green-400 text-black hover:bg-green-500 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold disabled:opacity-50"
                >
                  <Save className="mr-2 h-4 w-4" />
                  {isUploadingImages ? 'Uploading...' : 'Save'}
                </Button>
                <Button 
                  onClick={handleCancelEdit}
                  className="w-full sm:w-auto bg-gray-300 text-black hover:bg-gray-400 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold"
                >
                  <X className="mr-2 h-4 w-4" />
                  Cancel
                </Button>
              </div>
            </div>
          </CardContent>
        )}

        {!isEditing && post.body && (
          <CardContent>
            <div className="prose dark:prose-invert max-w-none">
              <ReactMarkdown>{post.body}</ReactMarkdown>
            </div>
          </CardContent>
        )}

        {!isEditing && post.tags && post.tags.length > 0 && (
          <CardContent className="pt-0">
            <div className="flex flex-wrap gap-2">
              {post.tags.map((tag) => (
                <Link
                  key={tag}
                  to={`/search?tag=${encodeURIComponent(tag)}`}
                  className="px-3 py-1 text-sm font-bold border border-black hover:opacity-80 transition-opacity cursor-pointer"
                  style={{
                    backgroundColor: generateTagColor(tag),
                    color: getTagTextColor(tag),
                  }}
                >
                  #{tag}
                </Link>
              ))}
            </div>
          </CardContent>
        )}

        {!isEditing && post.imageUrls && post.imageUrls.length > 0 && (
          <CardContent>
            <ImageGallery
              images={post.imageUrls}
              title={post.title}
              height={500}
            />
          </CardContent>
        )}

        <CardFooter className="flex gap-2 flex-wrap">
          <div className="flex items-center gap-1 text-sm text-muted-foreground">
            <MessageSquare className="h-4 w-4" />
            <span>{formatNumber(post.commentCount)} comments</span>
          </div>

          {/* Report button - only show for authenticated users who don't own the post */}
          {isAuthenticated && !isOwner && !isEditing && (
            <Button
              size="sm"
              onClick={() => setShowReportModal(true)}
              disabled={hasReported || isLoadingReportStatus}
              className={cn(
                'w-auto border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold',
                hasReported
                  ? 'bg-gray-300 text-gray-600 cursor-not-allowed'
                  : 'bg-orange-400 text-black hover:bg-orange-500'
              )}
            >
              <Flag className="mr-2 h-4 w-4" />
              {hasReported ? 'Reported' : 'Report'}
            </Button>
          )}

          {isOwner && !isEditing && (
            <div className="ml-auto w-full sm:w-auto flex flex-col sm:flex-row gap-2">
              <Button 
                size="sm" 
                onClick={handleStartEdit}
                className="w-full sm:w-auto bg-yellow-400 text-black hover:bg-yellow-500 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold"
              >
                <Edit2 className="mr-2 h-4 w-4" />
                Edit
              </Button>
              <Button
                size="sm"
                onClick={handleDelete}
                className="w-full sm:w-auto bg-red-400 text-black hover:bg-red-500 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold"
              >
                <Trash2 className="mr-2 h-4 w-4" />
                Delete
              </Button>
            </div>
          )}
        </CardFooter>
      </Card>

      <Card className="bg-yellow-50">
        <CardHeader>
          <h2 className="text-xl font-semibold">Comments</h2>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="space-y-2">
              <Textarea
                value={commentBody}
                onChange={(e) => setCommentBody(e.target.value)}
                className="min-h-[100px] resize-y"
                placeholder="What are your thoughts? (Markdown supported)"
              />
              <Button 
                onClick={handleSubmitComment} 
                disabled={!commentBody.trim()}
                className="bg-blue-400 text-black hover:bg-blue-500 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold disabled:opacity-50"
              >
                Comment
              </Button>
            </div>

            <Separator />

            <CommentList comments={comments} postId={post.id} />
          </div>
        </CardContent>
      </Card>

      <AuthModal
        isOpen={showAuthModal}
        onClose={() => setShowAuthModal(false)}
        message="You need to be logged in to vote or comment."
      />

      <ConfirmDialog
        isOpen={showDeleteDialog}
        onClose={() => setShowDeleteDialog(false)}
        onConfirm={confirmDelete}
        title="Delete Post"
        description="Are you sure you want to delete this post? This action cannot be undone."
        confirmText="Delete"
        cancelText="Cancel"
      />

      <ReportModal
        isOpen={showReportModal}
        onClose={() => setShowReportModal(false)}
        postId={id!}
      />
    </div>
  )
}
