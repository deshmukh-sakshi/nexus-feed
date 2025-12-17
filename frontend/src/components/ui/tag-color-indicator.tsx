import { generateTagColor } from '@/lib/tag-colors'
import { cn } from '@/lib/utils'

interface TagColorIndicatorProps {
  tagName: string
  size?: 'sm' | 'md'
  className?: string
}

export const TagColorIndicator = ({
  tagName,
  size = 'sm',
  className,
}: TagColorIndicatorProps) => {
  const color = generateTagColor(tagName)

  const sizeClasses = {
    sm: 'h-2.5 w-2.5',
    md: 'h-3.5 w-3.5',
  }

  return (
    <span
      className={cn('inline-block rounded-full', sizeClasses[size], className)}
      style={{ backgroundColor: color }}
      aria-hidden="true"
    />
  )
}
