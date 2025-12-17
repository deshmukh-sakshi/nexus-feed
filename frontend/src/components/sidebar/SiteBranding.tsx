
import { BadgeCheck, Rocket } from 'lucide-react'
import { Link } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { useAuthStore } from '@/stores/authStore'

export const SiteBranding = () => {
  const { isAuthenticated } = useAuthStore()

  return (
    <div className="p-4 border-2 border-black bg-teal-200 dark:bg-teal-900 shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)]">
      <div className="flex flex-col items-center text-center">
        <div className="p-3 bg-white border-2 border-black shadow-[3px_3px_0px_0px_rgba(0,0,0,1)] mb-3">
          <img
            src="/logo.svg"
            alt="Nexus Feed"
            className="h-12 w-12"
          />
        </div>
        <h1 className="text-xl font-black mb-2">Nexus Feed</h1>
        <p className="text-sm font-medium text-black/80 dark:text-white/80 mb-4">
          A Community-Driven News Hub. Discover and discuss the most relevant news and information, as curated by the users themselves.
        </p>

        <div className="w-full space-y-2 mb-4">
          {[
            "100% User Curated",
            "Vote-Based Ranking",
            "No Tracking"
          ].map((text, i) => (
             <div key={i} className="flex items-center gap-2 text-xs font-bold px-2 py-1 bg-white/40 border-2 border-black/5 rounded-sm">
               <BadgeCheck className="w-4 h-4 text-teal-700 dark:text-teal-300" />
               <span>{text}</span>
             </div>
          ))}
        </div>

        {!isAuthenticated && (
          <Button 
            asChild
            className="w-full bg-pink-400 text-black hover:bg-pink-500 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold active:translate-x-[2px] active:translate-y-[2px] active:shadow-none transition-all"
          >
            <Link to="/register">
              <Rocket className="w-4 h-4 mr-2" />
              Join Community
            </Link>
          </Button>
        )}
      </div>
    </div>
  )
}
