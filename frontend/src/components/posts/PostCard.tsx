import { useState } from 'react'
import { Link } from 'react-router-dom'
import { formatDistanceToNow } from 'date-fns'
import { ArrowBigUp, ArrowBigDown, MessageSquare, ExternalLink } from 'lucide-react'
import { Card, CardContent, CardFooter, CardHeader } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { UserAvatar } from '@/components/ui/user-avatar'
import { AuthModal } from '@/components/ui/auth-modal'
import { cn } from '@/lib/utils'
import { useAuthStore } from '@/stores/authStore'
import { usePosts } from '@/hooks/usePosts'
import type { Post } from '@/types'

interface PostCardProps {
  post: Post
}

export const PostCard = ({ post }: PostCardProps) => {
  const { isAuthenticated } = useAuthStore()
  const { votePost } = usePosts()
  const [showAuthModal, setShowAuthModal] = useState(false)

  const score = post.upvotes - post.downvotes
  const isEdited = post.createdAt !== post.updatedAt

  const handleVote = (voteValue: 'UPVOTE' | 'DOWNVOTE') => {
    if (!isAuthenticated) {
      setShowAuthModal(true)
      return
    }
    votePost(post.id, voteValue)
  }

  return (
    <>
      <Card className="overflow-hidden border-2 border-border shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none">
        <CardHeader className="pb-3">
        <div className="flex items-start gap-3">
          <div className="flex flex-col items-center gap-1 pt-1">
            <Button
              variant="ghost"
              size="sm"
              className={cn(
                'h-8 w-8 p-0 rounded-none',
                post.userVote === 'UPVOTE' && 'text-orange-500 border-orange-500 bg-orange-100 dark:bg-orange-900/20'
              )}
              onClick={() => handleVote('UPVOTE')}
            >
              <ArrowBigUp className="h-5 w-5" />
            </Button>
            <span className="text-sm font-bold">{score}</span>
            <Button
              variant="ghost"
              size="sm"
              className={cn(
                'h-8 w-8 p-0 rounded-none',
                post.userVote === 'DOWNVOTE' && 'text-blue-500 border-blue-500 bg-blue-100 dark:bg-blue-900/20'
              )}
              onClick={() => handleVote('DOWNVOTE')}
            >
              <ArrowBigDown className="h-5 w-5" />
            </Button>
          </div>

          <div className="flex-1 space-y-2">
            <div className="flex items-center gap-2">
              <Link to={`/user/${post.username}`} className="hover:opacity-80">
                <UserAvatar username={post.username} size="sm" className="border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)]" />
              </Link>
              <div className="flex items-center gap-2 text-xs text-muted-foreground font-mono">
                <Link
                  to={`/user/${post.username}`}
                  className="hover:underline font-bold text-foreground"
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

            <Link to={`/post/${post.id}`}>
              <h3 className="text-xl font-black hover:underline decoration-2 decoration-primary">{post.title}</h3>
            </Link>

            {post.url && (
              <a
                href={post.url}
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center gap-1 text-xs text-blue-600 font-bold hover:underline decoration-2"
              >
                <ExternalLink className="h-3 w-3" />
                {(() => {
                  try {
                    return new URL(post.url).hostname
                  } catch {
                    return post.url
                  }
                })()}
              </a>
            )}
          </div>
        </div>
      </CardHeader>

      {post.body && (
        <CardContent className="pb-3">
          <p className="text-sm text-foreground font-medium line-clamp-3">{post.body}</p>
        </CardContent>
      )}

      {post.imageUrls && post.imageUrls.length > 0 && (
        <CardContent className="pb-3">
          <img
            src={post.imageUrls[0]}
            alt={post.title}
            className="w-full max-h-96 object-cover border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]"
          />
        </CardContent>
      )}

      <CardFooter className="pt-0 flex gap-2 border-t-2 border-border mt-2 pt-3">
        <Link to={`/post/${post.id}`}>
          <Button variant="ghost" size="sm" className="rounded-none font-bold">
            <MessageSquare className="mr-2 h-4 w-4" />
            {post.commentCount} {post.commentCount === 1 ? 'Comment' : 'Comments'}
          </Button>
        </Link>
        <div className="text-xs text-muted-foreground flex items-center font-mono ml-auto">
          <ArrowBigUp className="h-4 w-4 text-orange-500" />
          <span className="ml-1 font-bold">{post.upvotes}</span>
          <ArrowBigDown className="h-4 w-4 ml-2 text-blue-500" />
          <span className="ml-1 font-bold">{post.downvotes}</span>
        </div>
      </CardFooter>
    </Card>

    <AuthModal
      isOpen={showAuthModal}
      onClose={() => setShowAuthModal(false)}
      message="You need to be logged in to vote on posts."
    />
  </>
  )
}
