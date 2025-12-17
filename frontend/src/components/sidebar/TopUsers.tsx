import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { Crown } from 'lucide-react'
import { usersApi } from '@/lib/api-client'
import { Skeleton } from '@/components/ui/skeleton'
import { UserAvatar } from '@/components/ui/user-avatar'
import { formatNumber } from '@/lib/utils'
import { RankBadge } from './RankBadge'

interface TopUsersProps {
  limit?: number
}

export const TopUsers = ({ limit = 5 }: TopUsersProps) => {
  const { data: users = [], isLoading } = useQuery({
    queryKey: ['topUsers', limit],
    queryFn: () => usersApi.getTopUsers(limit),
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnWindowFocus: false,
    refetchOnMount: false,
    refetchOnReconnect: false,
  })

  return (
    <div className="p-4 border-2 border-black bg-lime-100 dark:bg-gray-900 shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)]">
      <div className="flex items-center gap-2 mb-4">
        <Crown className="h-5 w-5 text-lime-600" />
        <h2 className="font-bold text-base">Top Contributors</h2>
      </div>

      {isLoading ? (
        <div className="space-y-2">
          {[...Array(5)].map((_, i) => (
            <div
              key={i}
              className="w-full flex items-center gap-2 px-3 py-2 border-2 border-black bg-white shadow-[2px_2px_0px_0px_rgba(0,0,0,1)]"
            >
              <Skeleton className="h-4 w-4" />
              <Skeleton className="h-8 w-8 rounded-full" />
              <Skeleton className="h-4 flex-1" />
              <Skeleton className="h-4 w-12" />
            </div>
          ))}
        </div>
      ) : users.length > 0 ? (
        <div className="space-y-2">
          {users.map((user, index) => (
            <Link
              key={user.id}
              to={`/user/${user.username}`}
              className="group w-full flex items-center gap-3 px-3 py-3 text-sm font-bold border-2 border-black bg-white dark:bg-gray-800 shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:shadow-[6px_6px_0px_0px_rgba(0,0,0,1)] hover:bg-lime-50 dark:hover:bg-gray-700 active:shadow-none active:translate-x-[4px] active:translate-y-[4px] transition-all cursor-pointer"
            >
              <RankBadge rank={index + 1} />
              
              <UserAvatar
                username={user.username}
                profileImageUrl={user.profilePictureUrl}
                size="sm"
                className="h-9 w-9 border-2 border-black flex-shrink-0 shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] group-hover:rotate-6 transition-transform"
              />
              
              <div className="flex flex-col min-w-0 flex-1">
                <span className="font-black truncate text-base">{user.username}</span>
              </div>
              
              <div className="flex flex-col items-end flex-shrink-0 ml-auto">
                <span className="text-xs font-bold text-black dark:text-white">
                  {formatNumber(user.karma ?? 0)}
                </span>
                <span className="text-[10px] uppercase font-black text-black/40 dark:text-white/40">
                  KARMA
                </span>
              </div>
            </Link>
          ))}
        </div>
      ) : (
        <p className="text-sm text-muted-foreground">
          No users yet
        </p>
      )}
    </div>
  )
}
