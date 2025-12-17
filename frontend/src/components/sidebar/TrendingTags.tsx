import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { Flame } from 'lucide-react'
import { tagsApi } from '@/lib/api-client'
import { Skeleton } from '@/components/ui/skeleton'
import { generateTagColor, getTagTextColor } from '@/lib/tag-colors'
import { formatNumber } from '@/lib/utils'
import { RankBadge } from './RankBadge'

interface TrendingTagsProps {
  limit?: number
}

export const TrendingTags = ({ limit = 5 }: TrendingTagsProps) => {
  const navigate = useNavigate()

  const { data: tags = [], isLoading } = useQuery({
    queryKey: ['trendingTagsScored', limit],
    queryFn: () => tagsApi.getTrendingTagsScored(limit),
    staleTime: Infinity, // Never refetch automatically
    gcTime: Infinity, // Keep in cache forever (until page refresh)
    refetchOnWindowFocus: false,
    refetchOnMount: false,
    refetchOnReconnect: false,
  })

  const handleTagClick = (tagName: string) => {
    navigate(`/search?tag=${encodeURIComponent(tagName)}`)
  }

  return (
    <div className="p-4 border-2 border-black bg-orange-100 dark:bg-gray-900 shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)]">
      <div className="flex items-center gap-2 mb-4">
        <Flame className="h-5 w-5 text-orange-500" />
        <h2 className="font-bold text-base">Trending Now</h2>
      </div>

      {isLoading ? (
        <div className="space-y-2">
          {[...Array(5)].map((_, i) => (
            <div
              key={i}
              className="w-full flex items-center gap-2 px-3 py-2 border-2 border-black bg-white shadow-[2px_2px_0px_0px_rgba(0,0,0,1)]"
            >
              <Skeleton className="h-4 w-4" />
              <Skeleton className="h-5 w-16" />
              <Skeleton className="h-4 w-12 ml-auto" />
            </div>
          ))}
        </div>
      ) : tags.length > 0 ? (
        <div className="space-y-2">
          {tags.map((tag, index) => (
            <button
              key={tag.id}
              onClick={() => handleTagClick(tag.name)}
              className="group w-full flex items-center gap-3 px-3 py-3 text-sm font-bold border-2 border-black bg-white dark:bg-gray-800 shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:shadow-[6px_6px_0px_0px_rgba(0,0,0,1)] hover:bg-yellow-50 dark:hover:bg-gray-700 active:shadow-none active:translate-x-[4px] active:translate-y-[4px] transition-all cursor-pointer"
            >
              <RankBadge rank={index + 1} />
              
              <div className="flex flex-col items-start gap-1 overflow-hidden min-w-0 flex-1">
                <span
                  className="px-2 py-0.5 text-xs font-black border-2 border-black truncate max-w-full shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] group-hover:rotate-1 transition-transform"
                  style={{
                    backgroundColor: generateTagColor(tag.name),
                    color: getTagTextColor(tag.name),
                  }}
                  title={tag.name}
                >
                  #{tag.name}
                </span>
              </div>
              
              <div className="flex flex-col items-end flex-shrink-0 ml-auto">
                <span className="text-xs font-bold text-black dark:text-white">
                  {formatNumber(tag.postCount)}
                </span>
                <span className="text-[10px] uppercase font-black text-black/40 dark:text-white/40">
                  {tag.postCount === 1 ? 'POST' : 'POSTS'}
                </span>
              </div>
            </button>
          ))}
        </div>
      ) : (
        <p className="text-sm text-muted-foreground">
          No trending topics right now
        </p>
      )}
    </div>
  )
}
