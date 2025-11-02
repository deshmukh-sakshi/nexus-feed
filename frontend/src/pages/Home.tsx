import { PostList } from '@/components/posts/PostList'
import { usePosts } from '@/hooks/usePosts'
import { Loader2, RefreshCw } from 'lucide-react'
import { Button } from '@/components/ui/button'

export const Home = () => {
  const { posts, isLoading, error, refetch } = usePosts()

  if (isLoading) {
    return (
      <div className="flex justify-center items-center py-12">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center py-12 space-y-4">
        <p className="text-destructive">Failed to load posts. Please try again later.</p>
        <Button onClick={() => refetch()} variant="outline">
          <RefreshCw className="mr-2 h-4 w-4" />
          Reload
        </Button>
      </div>
    )
  }

  return (
    <div className="w-full max-w-4xl mx-auto">
      <div className="mb-6">
        <h1 className="text-3xl font-bold">Home Feed</h1>
        <p className="text-muted-foreground">Latest posts from the community</p>
      </div>
      <PostList posts={posts} />
    </div>
  )
}
