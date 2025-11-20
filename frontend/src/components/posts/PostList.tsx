import { PostCard } from './PostCard'
import { PostSkeleton } from './PostSkeleton'
import type { Post } from '@/types'

interface PostListProps {
  posts: Post[]
  isLoading?: boolean
}

export const PostList = ({ posts, isLoading }: PostListProps) => {
  if (isLoading) {
    return (
      <div className="space-y-4">
        <PostSkeleton />
        <PostSkeleton />
        <PostSkeleton />
      </div>
    )
  }

  if (!Array.isArray(posts) || posts.length === 0) {
    return (
      <div className="text-center py-12">
        <p className="text-muted-foreground font-bold">No posts yet. Be the first to create one!</p>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      {posts.map((post) => (
        <PostCard key={post.id} post={post} />
      ))}
    </div>
  )
}
