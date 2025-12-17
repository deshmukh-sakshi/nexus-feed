import { useQuery } from '@tanstack/react-query'
import { Award, Trophy } from 'lucide-react'
import { badgesApi } from '@/lib/api-client'
import { useAuthStore } from '@/stores/authStore'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/components/ui/tooltip'

export const UserAchievements = () => {
  const { user, isAuthenticated } = useAuthStore()

  const { data: badges = [], isLoading } = useQuery({
    queryKey: ['userBadges', user?.userId],
    queryFn: () => badgesApi.getUserBadges(user!.userId),
    enabled: isAuthenticated && !!user?.userId,
  })

  if (!isAuthenticated || !user) {
    return null
  }

  return (
    <div className="p-4 border-2 border-black bg-purple-100 dark:bg-gray-900 shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)]">
      <div className="flex items-center gap-2 mb-4">
        <Trophy className="h-5 w-5 text-purple-600" />
        <h2 className="font-bold text-base">Your Achievements</h2>
      </div>

      {isLoading ? (
        <div className="space-y-2">
          {[...Array(2)].map((_, i) => (
            <div
              key={i}
              className="flex items-center gap-2 px-3 py-2 border-2 border-black bg-white shadow-[2px_2px_0px_0px_rgba(0,0,0,1)]"
            >
              <Skeleton className="h-6 w-6 rounded-full" />
              <Skeleton className="h-4 flex-1" />
            </div>
          ))}
        </div>
      ) : badges.length > 0 ? (
        <TooltipProvider>
          <div className="space-y-2">
            {badges.map((badge) => (
              <Tooltip key={badge.id}>
                <TooltipTrigger asChild>
                  <div className="flex items-center gap-3 px-3 py-2 border-2 border-black bg-gradient-to-r from-yellow-100 to-amber-100 shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] cursor-help">
                    <span className="text-lg flex-shrink-0">
                      {badge.iconUrl ? (
                        badge.iconUrl.startsWith('http') ? (
                          <img src={badge.iconUrl} alt="" className="w-6 h-6" />
                        ) : (
                          badge.iconUrl
                        )
                      ) : (
                        <Award className="w-5 h-5 text-amber-600" />
                      )}
                    </span>
                    <span className="font-bold text-sm truncate">{badge.name}</span>
                  </div>
                </TooltipTrigger>
                <TooltipContent side="bottom" className="max-w-[200px] border-2 border-black bg-white text-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)]">
                  <p className="text-xs">{badge.description}</p>
                </TooltipContent>
              </Tooltip>
            ))}
          </div>
        </TooltipProvider>
      ) : (
        <p className="text-sm text-muted-foreground">
          Start posting and engaging to earn your first badge!
        </p>
      )}
    </div>
  )
}
