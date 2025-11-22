import { useState } from 'react'
import { Link } from 'react-router-dom'
import { formatDistanceToNow } from 'date-fns'
import { ArrowBigUp, ArrowBigDown, MessageSquare, ExternalLink, Share2 } from 'lucide-react'
import { toast } from 'sonner'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { UserAvatar } from '@/components/ui/user-avatar'
import { AuthModal } from '@/components/ui/auth-modal'
import { PostSkeleton } from '@/components/posts/PostSkeleton'
import { cn } from '@/lib/utils'
import { useAuthStore } from '@/stores/authStore'
import { usePosts } from '@/hooks/usePosts'
import type { Post } from '@/types'

interface PostCardProps {
  post: Post & { isLoading?: boolean }
}

export const PostCard = ({ post }: PostCardProps) => {
  const { isAuthenticated } = useAuthStore()
  const { votePost } = usePosts()
  const [showAuthModal, setShowAuthModal] = useState(false)

  // Show skeleton if post is loading
  if (post.isLoading) {
    return <PostSkeleton />
  }

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
    <Card 
        className="flex flex-col overflow-hidden border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none bg-yellow-50"
    >
      {/* Content Area */}
      <div className="post-content px-4 py-3 pb-0">
        {/* Header */}
        <div className="flex items-center gap-2 text-xs text-muted-foreground mb-0.5 group">
           <Link 
              to={`/user/${post.username}`} 
              className="group-hover:opacity-80"
           >
              <UserAvatar username={post.username} size="sm" className="h-5 w-5 border border-black" />
           </Link>
           <Link 
              to={`/user/${post.username}`} 
              className="font-bold text-foreground group-hover:underline"
           >
              u/{post.username}
           </Link>
           <span>•</span>
           <span>{formatDistanceToNow(new Date(post.createdAt))} ago</span>
           {isEdited && <span className="italic">(edited)</span>}
        </div>

        {/* Body */}
        <h3 className="text-lg font-bold mb-0.5 leading-none">{post.title}</h3>

        {post.url && (
            <a
              href={post.url}
              target="_blank"
              rel="noopener noreferrer"
              className="flex items-center gap-1 text-xs text-blue-600 font-bold hover:underline mb-1 w-fit"
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

        {post.body && (
          <div className="mb-0.5">
            <p className="text-sm text-foreground/90 line-clamp-3 font-medium leading-tight">{post.body}</p>
          </div>
        )}
        
        {post.imageUrls && post.imageUrls.length > 0 && (
          <div className="mb-1 mt-2">
            <img
              src={post.imageUrls[0]}
              alt={post.title}
              className="w-full max-h-[500px] object-cover border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)]"
            />
          </div>
        )}
      </div>

      {/* Footer */}
      <div className="px-4 py-3 flex items-center gap-2">
          {/* Vote Pill */}
          <div 
            className="flex items-center bg-pink-200 border-2 border-black rounded-full h-10 px-3 shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] dark:shadow-[2px_2px_0px_0px_rgba(255,255,255,1)]"
          >
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
              <span className="text-sm font-bold mx-2 min-w-[1.5rem] text-center">{score}</span>
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

          {/* Comment Button */}
          <Link to={`/post/${post.id}`}>
            <Button 
                size="sm" 
                className="h-10 px-3 text-xs font-bold border-2 border-black bg-cyan-300 text-black hover:bg-cyan-400 shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] dark:shadow-[2px_2px_0px_0px_rgba(255,255,255,1)] rounded-full gap-2 transition-all active:translate-x-[2px] active:translate-y-[2px] active:shadow-none"
            >
              <MessageSquare className="h-4 w-4" />
              {post.commentCount} Comments
            </Button>
          </Link>
          
          {/* Share Button */}
          <Button 
              size="sm" 
              className="h-10 px-3 text-xs font-bold border-2 border-black bg-lime-300 text-black hover:bg-lime-400 shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] dark:shadow-[2px_2px_0px_0px_rgba(255,255,255,1)] rounded-full gap-2 transition-all active:translate-x-[2px] active:translate-y-[2px] active:shadow-none"
              onClick={async () => {
                  try {
                    await navigator.clipboard.writeText(`${window.location.origin}/post/${post.id}`);
                    toast.success("Link copied to clipboard!");
                  } catch (err) {
                    console.error("Failed to copy:", err);
                    toast.error("Failed to copy link");
                  }
              }}
          >
            <Share2 className="h-4 w-4" />
            Share
          </Button>
      </div>
    </Card>

    <AuthModal
      isOpen={showAuthModal}
      onClose={() => setShowAuthModal(false)}
      message="You need to be logged in to vote on posts."
    />
  </>
  )
}
