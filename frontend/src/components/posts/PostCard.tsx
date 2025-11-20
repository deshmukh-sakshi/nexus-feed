import { useState } from 'react'
import { Link } from 'react-router-dom'
import { formatDistanceToNow } from 'date-fns'
import { ArrowBigUp, ArrowBigDown, MessageSquare, ExternalLink, Share2 } from 'lucide-react'
import { toast } from 'sonner'
import { Card } from '@/components/ui/card'
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
    <Card 
        className="flex flex-col overflow-hidden border-2 border-border shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none bg-card transition-all duration-100 has-[.post-content:hover]:translate-x-[2px] has-[.post-content:hover]:translate-y-[2px] has-[.post-content:hover]:shadow-[2px_2px_0px_0px_rgba(0,0,0,1)]"
    >
      {/* Content Area - Triggers Card Animation */}
      <div 
        className="post-content px-4 py-3 cursor-pointer"
        onClick={() => window.location.href = `/post/${post.id}`}
      >
        {/* Header */}
        <div className="flex items-center gap-2 text-xs text-muted-foreground mb-0.5">
           {/* User Link - Stop Propagation */}
           <Link 
              to={`/user/${post.username}`} 
              className="flex items-center gap-1 hover:bg-muted rounded p-1 -ml-1 transition-colors"
              onClick={(e) => e.stopPropagation()}
           >
              <UserAvatar username={post.username} size="sm" className="h-5 w-5 border border-black" />
              <span className="font-bold text-foreground hover:underline">u/{post.username}</span>
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
              onClick={(e) => e.stopPropagation()}
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
          <div className="mb-1">
            <img
              src={post.imageUrls[0]}
              alt={post.title}
              className="w-full max-h-[500px] object-cover border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)]"
            />
          </div>
        )}
      </div>

      {/* Footer - Static relative to hover trigger */}
      <div className="px-4 pb-3 flex items-center gap-2">
          {/* Vote Pill */}
          <div 
            className="flex items-center bg-white border-2 border-black rounded-full h-10 px-2"
            onClick={(e) => e.stopPropagation()}
          >
              <Button
                variant="ghost"
                size="icon"
                className={cn(
                  'h-8 w-8 rounded-full hover:bg-neutral-200 text-muted-foreground hover:text-foreground transition-colors',
                  post.userVote === 'UPVOTE' && 'text-orange-600 bg-orange-100 hover:bg-orange-200 hover:text-orange-700'
                )}
                onClick={(e) => {
                  e.stopPropagation();
                  handleVote('UPVOTE');
                }}
              >
                <ArrowBigUp className={cn("h-6 w-6", post.userVote === 'UPVOTE' && "fill-current")} />
              </Button>
              <span className="text-sm font-bold mx-2 min-w-[1.5rem] text-center">{score}</span>
              <Button
                variant="ghost"
                size="icon"
                className={cn(
                  'h-8 w-8 rounded-full hover:bg-neutral-200 text-muted-foreground hover:text-foreground transition-colors',
                  post.userVote === 'DOWNVOTE' && 'text-blue-600 bg-blue-100 hover:bg-blue-200 hover:text-blue-700'
                )}
                onClick={(e) => {
                  e.stopPropagation();
                  handleVote('DOWNVOTE');
                }}
              >
                <ArrowBigDown className={cn("h-6 w-6", post.userVote === 'DOWNVOTE' && "fill-current")} />
              </Button>
          </div>

          {/* Comment Button */}
          <Button 
              variant="ghost" 
              size="sm" 
              className="h-10 px-3 text-xs font-bold border-2 border-black bg-white text-foreground hover:bg-accent rounded-full gap-2 transition-colors"
              onClick={(e) => {
                  e.stopPropagation();
                  window.location.href = `/post/${post.id}`;
              }}
          >
            <MessageSquare className="h-4 w-4" />
            {post.commentCount} Comments
          </Button>
          
          {/* Share Button */}
          <Button 
              variant="ghost" 
              size="sm" 
              className="h-10 px-3 text-xs font-bold border-2 border-black bg-white text-foreground hover:bg-accent rounded-full gap-2 transition-colors"
              onClick={async (e) => {
                  e.stopPropagation();
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
