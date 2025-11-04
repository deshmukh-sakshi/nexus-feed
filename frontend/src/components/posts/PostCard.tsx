import { Link } from 'react-router-dom'
import { formatDistanceToNow } from 'date-fns'
import { ArrowBigUp, ArrowBigDown, MessageSquare, ExternalLink } from 'lucide-react'
import { Card, CardContent, CardFooter, CardHeader } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { UserAvatar } from '@/components/ui/user-avatar'
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

  const score = post.upvotes - post.downvotes
  const isEdited = post.createdAt !== post.updatedAt

  const handleVote = (voteValue: 'UPVOTE' | 'DOWNVOTE') => {
    if (!isAuthenticated) return
    votePost(post.id, voteValue)
  }

  return (
    <Card className="overflow-hidden">
      <CardHeader className="pb-3">
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
              disabled={!isAuthenticated}
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
              disabled={!isAuthenticated}
            >
              <ArrowBigDown className="h-5 w-5" />
            </Button>
          </div>

          <div className="flex-1 space-y-2">
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

            <Link to={`/post/${post.id}`}>
              <h3 className="text-lg font-semibold hover:underline">{post.title}</h3>
            </Link>

            {post.url && (
              <a
                href={post.url}
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center gap-1 text-xs text-blue-500 hover:underline"
              >
                <ExternalLink className="h-3 w-3" />
                {new URL(post.url).hostname}
              </a>
            )}
          </div>
        </div>
      </CardHeader>

      {post.body && (
        <CardContent className="pb-3">
          <p className="text-sm text-muted-foreground line-clamp-3">{post.body}</p>
        </CardContent>
      )}

      {post.imageUrls && post.imageUrls.length > 0 && (
        <CardContent className="pb-3">
          <img
            src={post.imageUrls[0]}
            alt={post.title}
            className="rounded-md w-full max-h-96 object-cover"
          />
        </CardContent>
      )}

      <CardFooter className="pt-0 flex gap-2">
        <Link to={`/post/${post.id}`}>
          <Button variant="ghost" size="sm">
            <MessageSquare className="mr-2 h-4 w-4" />
            {post.commentCount} {post.commentCount === 1 ? 'Comment' : 'Comments'}
          </Button>
        </Link>
        <div className="text-xs text-muted-foreground flex items-center">
          <ArrowBigUp className="h-4 w-4 text-orange-500" />
          <span className="ml-1">{post.upvotes}</span>
          <ArrowBigDown className="h-4 w-4 ml-2 text-blue-500" />
          <span className="ml-1">{post.downvotes}</span>
        </div>
      </CardFooter>
    </Card>
  )
}
