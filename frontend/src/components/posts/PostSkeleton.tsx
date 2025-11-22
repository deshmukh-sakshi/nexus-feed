import { Card } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'

export const PostSkeleton = () => {
  return (
    <Card className="flex flex-col overflow-hidden border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none bg-yellow-50">
      {/* Content Area */}
      <div className="px-4 py-3 space-y-2">
        {/* Header */}
        <div className="flex items-center gap-2">
          <Skeleton className="h-5 w-5 rounded-none bg-muted-foreground/20" />
          <Skeleton className="h-4 w-24 rounded-none bg-muted-foreground/20" />
          <Skeleton className="h-4 w-16 rounded-none bg-muted-foreground/20" />
        </div>

        {/* Title */}
        <Skeleton className="h-6 w-2/3 rounded-none bg-muted-foreground/20" />

        {/* Body Preview */}
        <div className="space-y-1.5">
          <Skeleton className="h-4 w-full rounded-none bg-muted-foreground/20" />
          <Skeleton className="h-4 w-4/5 rounded-none bg-muted-foreground/20" />
        </div>
      </div>

      {/* Footer */}
      <div className="px-4 pb-3 flex items-center gap-2">
        {/* Vote Pill Skeleton */}
        <div className="h-10 w-28 border-2 border-black bg-pink-200 rounded-full flex items-center justify-between px-3 shadow-[2px_2px_0px_0px_rgba(0,0,0,1)]">
             <Skeleton className="h-7 w-7 rounded-full bg-muted-foreground/20" />
             <Skeleton className="h-4 w-8 rounded-none bg-muted-foreground/20" />
             <Skeleton className="h-7 w-7 rounded-full bg-muted-foreground/20" />
        </div>

        {/* Comment Button Skeleton */}
        <div className="h-10 w-32 border-2 border-black bg-cyan-300 rounded-full flex items-center justify-center gap-2 shadow-[2px_2px_0px_0px_rgba(0,0,0,1)]">
             <Skeleton className="h-4 w-20 rounded-none bg-muted-foreground/20" />
        </div>

        {/* Share Button Skeleton */}
        <div className="h-10 w-24 border-2 border-black bg-lime-300 rounded-full flex items-center justify-center gap-2 shadow-[2px_2px_0px_0px_rgba(0,0,0,1)]">
             <Skeleton className="h-4 w-14 rounded-none bg-muted-foreground/20" />
        </div>
      </div>
    </Card>
  )
}
