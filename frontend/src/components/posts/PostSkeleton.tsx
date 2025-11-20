import { Card } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'

export const PostSkeleton = () => {
  return (
    <Card className="flex flex-col overflow-hidden border-2 border-border shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none bg-card">
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
        <div className="h-10 w-28 border-2 border-border bg-card rounded-full flex items-center justify-between px-2">
             <Skeleton className="h-6 w-6 rounded-full bg-muted-foreground/20" />
             <Skeleton className="h-4 w-8 rounded-none bg-muted-foreground/20" />
             <Skeleton className="h-6 w-6 rounded-full bg-muted-foreground/20" />
        </div>

        {/* Comment Button Skeleton */}
        <div className="h-10 w-24 border-2 border-border bg-card rounded-full flex items-center justify-center gap-2">
             <Skeleton className="h-4 w-16 rounded-none bg-muted-foreground/20" />
        </div>

        {/* Share Button Skeleton */}
        <div className="h-10 w-20 border-2 border-border bg-card rounded-full flex items-center justify-center gap-2">
             <Skeleton className="h-4 w-12 rounded-none bg-muted-foreground/20" />
        </div>
      </div>
    </Card>
  )
}
