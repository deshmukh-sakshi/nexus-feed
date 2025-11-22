import { PostList } from '@/components/posts/PostList'
import { usePosts } from '@/hooks/usePosts'
import { Loader2, RefreshCw } from 'lucide-react'
import { Button } from '@/components/ui/button'

export const Home = () => {
  const {
    posts,
    isLoading,
    isRefetching,
    error,
    refetch,
    fetchNextPage,
    hasNextPage,
    isLastPage,
    isFetchingNextPage,
  } = usePosts()

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center py-12 space-y-4">
        <p className="text-destructive">Failed to load posts. Please try again later.</p>
        <Button 
          onClick={() => refetch()} 
          disabled={isRefetching}
          className="bg-red-400 text-black hover:bg-red-500 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold disabled:opacity-50"
        >
          {isRefetching ? (
            <>
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              Reloading...
            </>
          ) : (
            <>
              <RefreshCw className="mr-2 h-4 w-4" />
              Reload
            </>
          )}
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
      <PostList posts={posts} isLoading={isLoading} />
      
      {posts.length > 0 && (
        <div className="mt-6 flex justify-center">
          {!isLastPage && hasNextPage && (
            <Button
              onClick={() => fetchNextPage()}
              disabled={isFetchingNextPage}
              size="lg"
              className="bg-teal-400 text-black hover:bg-teal-500 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold disabled:opacity-50"
            >
              {isFetchingNextPage ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Loading...
                </>
              ) : (
                'Load More'
              )}
            </Button>
          )}
          
          {isLastPage && (
            <p className="text-muted-foreground text-sm">
              That's the end — no more posts to load
            </p>
          )}
        </div>
      )}
    </div>
  )
}
