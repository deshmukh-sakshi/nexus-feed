import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { generateTagColor, getTagTextColor } from '@/lib/tag-colors'
import { formatDistanceToNow } from 'date-fns'
import { ArrowBigUp, ArrowBigDown, MessageSquare, Share2 } from 'lucide-react'
import { toast } from 'sonner'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { UserAvatar } from '@/components/ui/user-avatar'
import { AuthModal } from '@/components/ui/auth-modal'
import { ImageGallery } from '@/components/ui/image-gallery'
import { PostSkeleton } from '@/components/posts/PostSkeleton'
import { cn, formatNumber } from '@/lib/utils'
import { useAuthStore } from '@/stores/authStore'
import { useVotePost } from '@/hooks/useVotePost'
import type { Post } from '@/types'

interface PostCardProps {
  post: Post & { isLoading?: boolean }
}

export const PostCard = ({ post }: PostCardProps) => {
  const navigate = useNavigate()
  const { isAuthenticated } = useAuthStore()
  const { votePost } = useVotePost()
  const [showAuthModal, setShowAuthModal] = useState(false)
  const [isCardPressed, setIsCardPressed] = useState(false)

  // Show skeleton if post is loading
  if (post.isLoading) {
    return <PostSkeleton />
  }

  const score = post.upvotes - post.downvotes
  // Only show edited if there's more than 1 second difference (to handle timestamp precision issues)
  const isEdited = Math.abs(new Date(post.updatedAt).getTime() - new Date(post.createdAt).getTime()) > 1000

  const handleVote = (voteValue: 'UPVOTE' | 'DOWNVOTE') => {
    if (!isAuthenticated) {
      setShowAuthModal(true)
      return
    }
    votePost(post.id, voteValue)
  }

  const handleCardClick = (e: React.MouseEvent) => {
    // Don't navigate if clicking on interactive elements
    const target = e.target as HTMLElement
    if (target.closest('a, button, [role="button"]')) {
      return
    }
    navigate(`/post/${post.id}`)
  }

  const handleMouseDown = (e: React.MouseEvent) => {
    const target = e.target as HTMLElement
    // Only set pressed if not clicking on interactive elements
    if (!target.closest('a, button, [role="button"]')) {
      setIsCardPressed(true)
    }
  }

  const handleMouseUp = () => {
    setIsCardPressed(false)
  }

  const handleMouseLeave = () => {
    setIsCardPressed(false)
  }

  return (
    <>
    <Card 
        onClick={handleCardClick}
        onMouseDown={handleMouseDown}
        onMouseUp={handleMouseUp}
        onMouseLeave={handleMouseLeave}
        className={cn(
          "flex flex-col overflow-hidden border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none bg-yellow-50 py-0 gap-0 cursor-pointer transition-all hover:bg-yellow-100",
          isCardPressed && "translate-x-[4px] translate-y-[4px] shadow-none"
        )}
    >
      {/* Content Area */}
      <div className="post-content px-4 py-3 pb-0">
        {/* Header */}
        <div className="flex items-center gap-2 text-xs text-muted-foreground mb-2 group">
           <Link 
              to={`/user/${post.username}`} 
              className="group-hover:opacity-80"
           >
              <UserAvatar username={post.username} profileImageUrl={post.profilePictureUrl} size="sm" className="h-5 w-5 border border-black" />
           </Link>
           <Link 
              to={`/user/${post.username}`} 
              className="font-medium hover:underline hover:text-foreground transition-colors"
           >
              u/{post.username}
           </Link>
           <span>•</span>
           <span>{formatDistanceToNow(new Date(post.createdAt))} ago</span>
           {isEdited && <span className="italic">(edited)</span>}
        </div>

        {/* Body */}
        <h3 className="text-2xl font-bold mb-1 leading-tight">{post.title}</h3>

        

        {post.body && (
          <div className="mb-0.5">
            <p className="text-sm text-foreground/90 line-clamp-3 font-medium leading-tight">{post.body}</p>
          </div>
        )}

        {post.tags && post.tags.length > 0 && (
          <div className="flex flex-wrap gap-1.5 mt-2">
            {post.tags.map((tag) => (
              <Link
                key={tag}
                to={`/search?tag=${encodeURIComponent(tag)}`}
                className="px-2 py-0.5 text-xs font-bold border border-black hover:opacity-80 transition-opacity cursor-pointer"
                style={{ 
                  backgroundColor: generateTagColor(tag),
                  color: getTagTextColor(tag)
                }}
                onClick={(e) => e.stopPropagation()}
              >
                #{tag}
              </Link>
            ))}
          </div>
        )}
        
        {post.imageUrls && post.imageUrls.length > 0 && (
          <div className="mt-3 mb-2">
            <ImageGallery
              images={post.imageUrls}
              title={post.title}
              height={400}
              onImageClick={(e) => e.stopPropagation()}
            />
          </div>
        )}
      </div>

      {/* Footer */}
      <div className="px-4 py-2 flex items-center gap-2">
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
              <span className="text-sm font-bold mx-2 min-w-[1.5rem] text-center">{formatNumber(score)}</span>
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
              {formatNumber(post.commentCount)} Comments
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
