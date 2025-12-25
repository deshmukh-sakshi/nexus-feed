
import { useNavigate } from 'react-router-dom'
import { ArrowLeft, CheckCircle2, XCircle, Scale } from 'lucide-react'
import { Button } from '@/components/ui/button'

export const Guidelines = () => {
  const navigate = useNavigate()
  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <Button
        size="sm"
        onClick={() => navigate('/')}
        className="bg-gray-300 text-black hover:bg-gray-400 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold"
      >
        <ArrowLeft className="mr-2 h-4 w-4" />
        Back
      </Button>

      <div className="bg-teal-50 dark:bg-gray-900 border-2 border-black shadow-[8px_8px_0px_0px_rgba(0,0,0,1)] dark:shadow-[8px_8px_0px_0px_rgba(255,255,255,1)] p-4 sm:p-8">
        <div className="flex items-center gap-3 sm:gap-4 mb-6 sm:mb-8 border-b-4 border-black pb-4">
          <div className="p-2 sm:p-3 bg-teal-400 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
            <Scale className="w-6 h-6 sm:w-8 sm:h-8 text-black" />
          </div>
          <div>
            <h1 className="text-2xl sm:text-4xl font-black uppercase tracking-tight">Community Guidelines</h1>
            <p className="font-bold text-muted-foreground mt-1 text-sm sm:text-base">Let's keep this place nice.</p>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 sm:gap-6">
          <div className="bg-green-100 dark:bg-green-900/30 p-4 sm:p-6 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] h-full">
            <div className="flex items-center gap-3 mb-4 border-b-2 border-black/20 pb-2">
              <CheckCircle2 className="w-6 h-6 sm:w-8 sm:h-8 text-green-600 fill-green-200" />
              <h3 className="text-xl sm:text-2xl font-black uppercase text-green-800 dark:text-green-300">Do's</h3>
            </div>
            <ul className="space-y-3 font-bold">
              {[
                "Be respectful and constructive",
                "Share interesting contents",
                "Use proper tags",
                "Report spam",
                "Upvote quality posts"
              ].map((item, i) => (
                <li key={i} className="flex items-start gap-2">
                  <span className="mt-1.5 w-2 h-2 bg-green-500 rounded-full border border-black shrink-0" />
                  <span>{item}</span>
                </li>
              ))}
            </ul>
          </div>

          <div className="bg-red-100 dark:bg-red-900/30 p-4 sm:p-6 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] h-full">
            <div className="flex items-center gap-3 mb-4 border-b-2 border-black/20 pb-2">
              <XCircle className="w-6 h-6 sm:w-8 sm:h-8 text-red-600 fill-red-200" />
              <h3 className="text-xl sm:text-2xl font-black uppercase text-red-800 dark:text-red-300">Don'ts</h3>
            </div>
            <ul className="space-y-3 font-bold">
              {[
                "Harass or bully others",
                "Post NSFW content",
                "Spam the feed",
                "Impersonate team members",
                "Manipulate votes"
              ].map((item, i) => (
                <li key={i} className="flex items-start gap-2">
                  <span className="mt-1.5 w-2 h-2 bg-red-500 rounded-full border border-black shrink-0" />
                  <span>{item}</span>
                </li>
              ))}
            </ul>
          </div>
        </div>

        <div className="mt-6 sm:mt-8 p-3 sm:p-4 bg-yellow-200 border-2 border-black text-center font-bold shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
          <p className="text-sm sm:text-base">Violating these rules may result in a permanent ban. Play nice!</p>
        </div>
      </div>
    </div>
  )
}
