import { useQuery } from '@tanstack/react-query'
import { Award } from 'lucide-react'
import { badgesApi } from '@/lib/api-client'
import { Badge } from '@/components/ui/badge'
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/components/ui/tooltip'

interface BadgeListProps {
  userId: string
}

export const BadgeList = ({ userId }: BadgeListProps) => {
  const { data: badges = [], isLoading } = useQuery({
    queryKey: ['userBadges', userId],
    queryFn: () => badgesApi.getUserBadges(userId),
    enabled: !!userId,
  })

  if (isLoading || badges.length === 0) {
    return null
  }

  return (
    <div className="flex flex-wrap gap-1.5 mt-2">
      <TooltipProvider>
        {badges.map((badge) => (
          <Tooltip key={badge.id}>
            <TooltipTrigger asChild>
              <Badge variant="gold" className="gap-1 cursor-default">
                {badge.iconUrl ? (
                  badge.iconUrl.startsWith('http') ? (
                    <img
                      src={badge.iconUrl}
                      alt={badge.name}
                      className="w-3.5 h-3.5"
                    />
                  ) : (
                    <span className="text-sm leading-none">{badge.iconUrl}</span>
                  )
                ) : (
                  <Award className="w-3.5 h-3.5" />
                )}
                {badge.name}
              </Badge>
            </TooltipTrigger>
            <TooltipContent>
              <p>{badge.description}</p>
            </TooltipContent>
          </Tooltip>
        ))}
      </TooltipProvider>
    </div>
  )
}
