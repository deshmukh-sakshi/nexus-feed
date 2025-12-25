import { PostList } from '@/components/posts/PostList'
import { FeedSorter } from '@/components/posts/FeedSorter'
import { usePosts } from '@/hooks/usePosts'
import { useSortStore } from '@/stores/sortStore'
import { Loader2, RefreshCw } from 'lucide-react'
import { Button } from '@/components/ui/button'

const sortLabels = {
  new: 'Latest posts from the community',
  best: 'Top voted posts of all time',
  hot: 'Trending posts right now',
}

export const Home = () => {
  const { sortOption, setSortOption } = useSortStore()
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
  } = usePosts(4, sortOption)

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
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h1 className="text-2xl sm:text-4xl font-bold">Home Feed</h1>
            <p className="text-muted-foreground">{sortLabels[sortOption]}</p>
          </div>
          <FeedSorter 
            sortOption={sortOption} 
            onSortChange={setSortOption}
            isLoading={isLoading || isRefetching}
          />
        </div>
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
