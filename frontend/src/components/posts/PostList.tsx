import { PostCard } from './PostCard'
import type { Post } from '@/types'

interface PostListProps {
  posts: Post[]
}

export const PostList = ({ posts }: PostListProps) => {
  if (!Array.isArray(posts) || posts.length === 0) {
    return (
      <div className="text-center py-12">
        <p className="text-muted-foreground">No posts yet. Be the first to create one!</p>
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
