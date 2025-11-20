import { Card, CardContent, CardFooter, CardHeader } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { Separator } from '@/components/ui/separator'
import { Button } from '@/components/ui/button'
import { ArrowLeft } from 'lucide-react'

interface PostDetailSkeletonProps {
  navigate: (delta: number) => void
}

export const PostDetailSkeleton = ({ navigate }: PostDetailSkeletonProps) => {
  return (
    <div className="w-full max-w-4xl mx-auto space-y-4">
      {/* Back Button - Functional */}
      <Button
        size="sm"
        onClick={() => navigate(-1)}
        className="mb-4 bg-gray-300 text-black hover:bg-gray-400 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold"
      >
        <ArrowLeft className="mr-2 h-4 w-4" />
        Back
      </Button>

      {/* Post Card Skeleton */}
      <Card className="bg-yellow-50">
        <CardHeader>
          <div className="flex items-start gap-3">
            {/* Vote Bar Skeleton */}
            <div className="flex flex-col items-center gap-2 bg-pink-200 border-2 border-black rounded-full px-2 py-2 shadow-[2px_2px_0px_0px_rgba(0,0,0,1)]">
              <Skeleton className="h-7 w-7 rounded-full bg-muted-foreground/20" />
              <Skeleton className="h-4 w-6 rounded-none bg-muted-foreground/20" />
              <Skeleton className="h-7 w-7 rounded-full bg-muted-foreground/20" />
            </div>

            <div className="flex-1 space-y-3">
              {/* User Info */}
              <div className="flex items-center gap-2">
                <Skeleton className="h-8 w-8 rounded-full bg-muted-foreground/20" />
                <Skeleton className="h-4 w-32 rounded-none bg-muted-foreground/20" />
              </div>

              {/* Title */}
              <Skeleton className="h-8 w-3/4 rounded-none bg-muted-foreground/20" />
            </div>
          </div>
        </CardHeader>

        {/* Body Content Skeleton */}
        <CardContent>
          <div className="space-y-2">
            <Skeleton className="h-4 w-full rounded-none bg-muted-foreground/20" />
            <Skeleton className="h-4 w-full rounded-none bg-muted-foreground/20" />
            <Skeleton className="h-4 w-3/4 rounded-none bg-muted-foreground/20" />
          </div>
        </CardContent>

        <CardFooter>
          <Skeleton className="h-4 w-24 rounded-none bg-muted-foreground/20" />
        </CardFooter>
      </Card>

      {/* Comments Card Skeleton */}
      <Card className="bg-yellow-50">
        <CardHeader>
          <Skeleton className="h-7 w-32 rounded-none bg-muted-foreground/20" />
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {/* Comment Input Skeleton */}
            <div className="space-y-2">
              <Skeleton className="h-24 w-full rounded-none bg-muted-foreground/20" />
              <Skeleton className="h-10 w-24 rounded-none bg-muted-foreground/20" />
            </div>

            <Separator />

            {/* Comment Items Skeleton */}
            <div className="space-y-4">
              {[1, 2].map((i) => (
                <div key={i} className="border-l-2 border-muted pl-4 py-2">
                  <div className="flex items-start gap-2">
                    <Skeleton className="h-8 w-8 rounded-full bg-muted-foreground/20" />
                    <div className="flex-1 space-y-2">
                      <Skeleton className="h-4 w-32 rounded-none bg-muted-foreground/20" />
                      <Skeleton className="h-16 w-full rounded-none bg-muted-foreground/20" />
                      <div className="flex items-center gap-2">
                        <Skeleton className="h-8 w-24 rounded-full bg-muted-foreground/20" />
                        <Skeleton className="h-8 w-16 rounded-full bg-muted-foreground/20" />
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
