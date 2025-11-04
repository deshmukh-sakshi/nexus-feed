import { useState } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { formatDistanceToNow } from 'date-fns'
import ReactMarkdown from 'react-markdown'
import {
  ArrowBigUp,
  ArrowBigDown,
  MessageSquare,
  ExternalLink,
  Loader2,
  Edit2,
  Trash2,
  Save,
  X,
  ArrowLeft,
} from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardFooter, CardHeader } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { Textarea } from '@/components/ui/textarea'
import { Input } from '@/components/ui/input'
import { UserAvatar } from '@/components/ui/user-avatar'
import { AuthModal } from '@/components/ui/auth-modal'
import { CommentList } from '@/components/posts/CommentList'
import { cn } from '@/lib/utils'
import { useAuthStore } from '@/stores/authStore'
import { usePost } from '@/hooks/usePosts'
import { useComments } from '@/hooks/useComments'
import { usePosts } from '@/hooks/usePosts'

export const PostDetail = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { user, isAuthenticated } = useAuthStore()
  const { data: post, isLoading: postLoading, error: postError } = usePost(id!)
  const { comments, isLoading: commentsLoading, createComment } = useComments(id!)
  const { votePost, updatePost, deletePost } = usePosts()

  const [commentBody, setCommentBody] = useState('')
  const [isEditing, setIsEditing] = useState(false)
  const [editTitle, setEditTitle] = useState('')
  const [editBody, setEditBody] = useState('')
  const [showAuthModal, setShowAuthModal] = useState(false)

  if (postLoading) {
    return (
      <div className="flex justify-center items-center py-12">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    )
  }

  if (postError || !post) {
    return (
      <div className="flex flex-col items-center justify-center py-12 space-y-4">
        <p className="text-destructive">Failed to load post. It may not exist.</p>
        <Button onClick={() => navigate('/')} variant="outline">
          Go Back Home
        </Button>
      </div>
    )
  }

  const score = post.upvotes - post.downvotes
  const isOwner = user?.userId === post.userId
  const isEdited = post.createdAt !== post.updatedAt

  const handleVote = (voteValue: 'UPVOTE' | 'DOWNVOTE') => {
    if (!isAuthenticated) {
      setShowAuthModal(true)
      return
    }
    votePost(post.id, voteValue)
  }

  const handleSubmitComment = () => {
    if (!isAuthenticated) {
      setShowAuthModal(true)
      return
    }
    if (commentBody.trim()) {
      createComment({ body: commentBody.trim() })
      setCommentBody('')
    }
  }

  const handleStartEdit = () => {
    setEditTitle(post.title)
    setEditBody(post.body || '')
    setIsEditing(true)
  }

  const handleSaveEdit = () => {
    if (editTitle.trim()) {
      updatePost(post.id, {
        title: editTitle.trim(),
        body: editBody.trim() || undefined,
      })
      setIsEditing(false)
    }
  }

  const handleCancelEdit = () => {
    setIsEditing(false)
    setEditTitle('')
    setEditBody('')
  }

  const handleDelete = () => {
    if (window.confirm('Are you sure you want to delete this post?')) {
      deletePost(post.id)
      navigate('/')
    }
  }

  return (
    <div className="w-full max-w-4xl mx-auto space-y-4">
      <Button
        variant="ghost"
        size="sm"
        onClick={() => navigate(-1)}
        className="mb-4"
      >
        <ArrowLeft className="mr-2 h-4 w-4" />
        Back
      </Button>

      <Card>
        <CardHeader>
          <div className="flex items-start gap-3">
            <div className="flex flex-col items-center gap-1 pt-1">
              <Button
                variant="ghost"
                size="sm"
                className={cn(
                  'h-8 w-8 p-0',
                  post.userVote === 'UPVOTE' && 'text-orange-500'
                )}
                onClick={() => handleVote('UPVOTE')}
              >
                <ArrowBigUp className="h-5 w-5" />
              </Button>
              <span className="text-sm font-semibold">{score}</span>
              <Button
                variant="ghost"
                size="sm"
                className={cn(
                  'h-8 w-8 p-0',
                  post.userVote === 'DOWNVOTE' && 'text-blue-500'
                )}
                onClick={() => handleVote('DOWNVOTE')}
              >
                <ArrowBigDown className="h-5 w-5" />
              </Button>
            </div>

            <div className="flex-1 space-y-3">
              <div className="flex items-center gap-2">
                <Link to={`/user/${post.username}`} className="hover:opacity-80">
                  <UserAvatar username={post.username} size="sm" />
                </Link>
                <div className="flex items-center gap-2 text-xs text-muted-foreground">
                  <Link
                    to={`/user/${post.username}`}
                    className="hover:underline font-medium"
                  >
                    u/{post.username}
                  </Link>
                  <span>•</span>
                  <span>{formatDistanceToNow(new Date(post.createdAt))} ago</span>
                  {isEdited && (
                    <>
                      <span>•</span>
                      <span className="italic">edited</span>
                    </>
                  )}
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
                  <div className="flex gap-2">
                    <Button onClick={handleSaveEdit}>
                      <Save className="mr-2 h-4 w-4" />
                      Save
                    </Button>
                    <Button variant="outline" onClick={handleCancelEdit}>
                      <X className="mr-2 h-4 w-4" />
                      Cancel
                    </Button>
                  </div>
                </div>
              ) : (
                <>
                  <h1 className="text-2xl font-bold">{post.title}</h1>

                  {post.url && (
                    <a
                      href={post.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="flex items-center gap-1 text-sm text-blue-500 hover:underline"
                    >
                      <ExternalLink className="h-4 w-4" />
                      {(() => {
                        try {
                          return new URL(post.url).hostname
                        } catch {
                          return post.url
                        }
                      })()}
                    </a>
                  )}
                </>
              )}
            </div>
          </div>
        </CardHeader>

        {!isEditing && post.body && (
          <CardContent>
            <div className="prose dark:prose-invert max-w-none">
              <ReactMarkdown>{post.body}</ReactMarkdown>
            </div>
          </CardContent>
        )}

        {!isEditing && post.imageUrls && post.imageUrls.length > 0 && (
          <CardContent>
            <img
              src={post.imageUrls[0]}
              alt={post.title}
              className="rounded-md w-full max-h-[600px] object-contain"
            />
          </CardContent>
        )}

        <CardFooter className="flex gap-2 flex-wrap">
          <div className="flex items-center gap-1 text-sm text-muted-foreground">
            <MessageSquare className="h-4 w-4" />
            <span>{post.commentCount} comments</span>
          </div>
          <div className="flex items-center gap-1 text-sm text-muted-foreground">
            <ArrowBigUp className="h-4 w-4 text-orange-500" />
            <span>{post.upvotes}</span>
          </div>
          <div className="flex items-center gap-1 text-sm text-muted-foreground">
            <ArrowBigDown className="h-4 w-4 text-blue-500" />
            <span>{post.downvotes}</span>
          </div>

          {isOwner && !isEditing && (
            <div className="ml-auto flex gap-2">
              <Button variant="outline" size="sm" onClick={handleStartEdit}>
                <Edit2 className="mr-2 h-4 w-4" />
                Edit
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={handleDelete}
                className="text-destructive hover:text-destructive"
              >
                <Trash2 className="mr-2 h-4 w-4" />
                Delete
              </Button>
            </div>
          )}
        </CardFooter>
      </Card>

      <Card>
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
              <Button onClick={handleSubmitComment} disabled={!commentBody.trim()}>
                Comment
              </Button>
            </div>

            <Separator />

            {commentsLoading ? (
              <div className="flex justify-center py-8">
                <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
              </div>
            ) : (
              <CommentList comments={comments} postId={post.id} />
            )}
          </div>
        </CardContent>
      </Card>

      <AuthModal
        isOpen={showAuthModal}
        onClose={() => setShowAuthModal(false)}
        message="You need to be logged in to vote or comment."
      />
    </div>
  )
}
