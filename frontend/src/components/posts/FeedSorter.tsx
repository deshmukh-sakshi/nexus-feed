import { Clock, TrendingUp, Flame } from 'lucide-react'
import { cn } from '@/lib/utils'
import type { SortOption } from '@/stores/sortStore'

interface FeedSorterProps {
  sortOption: SortOption
  onSortChange: (option: SortOption) => void
  isLoading?: boolean
}

const sortOptions: { value: SortOption; label: string; icon: React.ReactNode }[] = [
  { value: 'new', label: 'New', icon: <Clock className="size-4" /> },
  { value: 'best', label: 'Best', icon: <TrendingUp className="size-4" /> },
  { value: 'hot', label: 'Hot', icon: <Flame className="size-4" /> },
]

export const FeedSorter = ({ sortOption, onSortChange, isLoading }: FeedSorterProps) => {
  return (
    <div className="flex gap-1">
      {sortOptions.map((option) => (
        <button
          key={option.value}
          onClick={() => onSortChange(option.value)}
          disabled={isLoading}
          className={cn(
            'flex items-center gap-1.5 px-3 py-1.5 text-sm font-bold border-2 border-black transition-all',
            'shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] dark:shadow-[2px_2px_0px_0px_rgba(255,255,255,1)]',
            'active:translate-x-0.5 active:translate-y-0.5 active:shadow-none',
            'disabled:opacity-50 disabled:cursor-not-allowed',
            sortOption === option.value
              ? 'bg-primary text-primary-foreground'
              : 'bg-background text-foreground hover:bg-accent'
          )}
        >
          {option.icon}
          {option.label}
        </button>
      ))}
    </div>
  )
}
