import { Clock, TrendingUp, Flame } from 'lucide-react'
import { Button } from '@/components/ui/button'
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
        <Button
          key={option.value}
          onClick={() => onSortChange(option.value)}
          disabled={isLoading}
          variant={sortOption === option.value ? 'default' : 'outline'}
          size="sm"
        >
          {option.icon}
          {option.label}
        </Button>
      ))}
    </div>
  )
}
