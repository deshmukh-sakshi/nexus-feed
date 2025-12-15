import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Search as SearchIcon, Hash, TrendingUp, Loader2 } from 'lucide-react'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { PostList } from '@/components/posts/PostList'
import { tagsApi } from '@/lib/api-client'
import { cn } from '@/lib/utils'

export const Search = () => {
  const [searchParams, setSearchParams] = useSearchParams()
  const initialTag = searchParams.get('tag') || ''
  const [selectedTag, setSelectedTag] = useState(initialTag)
  const [searchInput, setSearchInput] = useState(initialTag)

  const { data: trendingTags = [], isLoading: tagsLoading } = useQuery({
    queryKey: ['trendingTags'],
    queryFn: () => tagsApi.getTrendingTags(15),
  })

  const {
    data: postsData,
    isLoading: postsLoading,
    isFetching,
  } = useQuery({
    queryKey: ['postsByTag', selectedTag],
    queryFn: () => tagsApi.getPostsByTag(selectedTag, 0, 20),
    enabled: !!selectedTag,
  })

  const posts = postsData?.content ?? []

  const handleTagClick = (tagName: string) => {
    setSelectedTag(tagName)
    setSearchInput(tagName)
    setSearchParams({ tag: tagName })
  }

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    if (searchInput.trim()) {
      const tag = searchInput.trim().toLowerCase().replace(/^#/, '')
      setSelectedTag(tag)
      setSearchParams({ tag })
    }
  }

  const clearSearch = () => {
    setSelectedTag('')
    setSearchInput('')
    setSearchParams({})
  }

  return (
    <div className="w-full max-w-4xl mx-auto space-y-6">
      <div>
        <h1 className="text-4xl font-bold mb-2">Search by Tags</h1>
        <p className="text-muted-foreground">
          Discover posts by exploring tags
        </p>
      </div>

      {/* Search Input */}
      <form onSubmit={handleSearch} className="flex gap-2">
        <div className="relative flex-1">
          <Hash className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            placeholder="Search for a tag..."
            className="pl-9 border-2 border-black rounded-none h-12 text-lg"
          />
        </div>
        <Button
          type="submit"
          className="h-12 px-6 bg-teal-400 text-black hover:bg-teal-500 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] rounded-none font-bold"
        >
          <SearchIcon className="h-5 w-5 mr-2" />
          Search
        </Button>
      </form>

      {/* Trending Tags */}
      <div className="space-y-3">
        <div className="flex items-center gap-2 text-sm font-medium text-muted-foreground">
          <TrendingUp className="h-4 w-4" />
          Trending Tags
        </div>
        {tagsLoading ? (
          <div className="flex gap-2">
            {[...Array(5)].map((_, i) => (
              <div
                key={i}
                className="h-8 w-20 bg-gray-200 animate-pulse rounded-none"
              />
            ))}
          </div>
        ) : (
          <div className="flex flex-wrap gap-2">
            {trendingTags.map((tag) => (
              <button
                key={tag.id}
                onClick={() => handleTagClick(tag.name)}
                className={cn(
                  'px-3 py-1.5 text-sm font-medium border-2 border-black transition-all',
                  'shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] hover:shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]',
                  'active:translate-x-[2px] active:translate-y-[2px] active:shadow-none',
                  selectedTag === tag.name
                    ? 'bg-teal-400 text-black'
                    : 'bg-white hover:bg-yellow-100'
                )}
              >
                #{tag.name}
                <span className="ml-1 text-xs opacity-70">({tag.postCount})</span>
              </button>
            ))}
          </div>
        )}
      </div>

      {/* Results */}
      {selectedTag && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-semibold">
              Posts tagged with{' '}
              <span className="text-teal-600">#{selectedTag}</span>
            </h2>
            <Button
              size="sm"
              onClick={clearSearch}
              className="bg-red-400 text-black hover:bg-red-500 border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] rounded-none font-bold transition-all active:translate-x-[2px] active:translate-y-[2px] active:shadow-none"
            >
              Clear
            </Button>
          </div>

          {postsLoading || isFetching ? (
            <div className="flex justify-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
            </div>
          ) : posts.length > 0 ? (
            <PostList posts={posts} />
          ) : (
            <div className="text-center py-12 text-muted-foreground">
              <Hash className="h-12 w-12 mx-auto mb-4 opacity-50" />
              <p>No posts found with tag #{selectedTag}</p>
              <p className="text-sm mt-1">Try a different tag or create a post with this tag!</p>
            </div>
          )}
        </div>
      )}

      {!selectedTag && (
        <div className="text-center py-12 text-muted-foreground">
          <SearchIcon className="h-12 w-12 mx-auto mb-4 opacity-50" />
          <p>Select a trending tag or search for one to see posts</p>
        </div>
      )}
    </div>
  )
}
