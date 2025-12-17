
import { useNavigate } from 'react-router-dom'
import { ArrowLeft, ShieldCheck, Database, Cookie, UserCheck } from 'lucide-react'
import { Button } from '@/components/ui/button'

export const Privacy = () => {
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

      <div className="bg-purple-50 dark:bg-gray-900 border-2 border-black shadow-[8px_8px_0px_0px_rgba(0,0,0,1)] dark:shadow-[8px_8px_0px_0px_rgba(255,255,255,1)] p-8">
        <div className="flex items-center gap-4 mb-8 border-b-4 border-black pb-4">
          <div className="p-3 bg-purple-400 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
            <ShieldCheck className="w-8 h-8 text-white" />
          </div>
          <div>
            <h1 className="text-4xl font-black uppercase tracking-tight">Privacy Policy</h1>
            <p className="font-bold text-muted-foreground mt-1">Last updated: December 17, 2025</p>
          </div>
        </div>

        <div className="grid gap-6">
          <div className="bg-blue-100 dark:bg-blue-900/30 p-6 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] transition-all">
            <div className="flex items-start gap-4">
              <div className="p-2 bg-blue-400 border-2 border-black rounded-none">
                <Database className="w-6 h-6 text-white" />
              </div>
              <div>
                <h2 className="text-xl font-black uppercase mb-2">1. Usage of Data</h2>
                <p className="font-medium">We only collect the absolute minimum data required. This includes your username, email (for login), and the content you post. We don't sell your data to third parties.</p>
              </div>
            </div>
          </div>

          <div className="bg-pink-100 dark:bg-pink-900/30 p-6 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] transition-all">
            <div className="flex items-start gap-4">
              <div className="p-2 bg-pink-400 border-2 border-black rounded-none">
                <Cookie className="w-6 h-6 text-white" />
              </div>
              <div>
                <h2 className="text-xl font-black uppercase mb-2">2. Cookies & Storage</h2>
                <p className="font-medium">We use local storage to keep you logged in. No creepy tracking pixels, no analytics spying on you, and definitely no third-party ad cookies.</p>
              </div>
            </div>
          </div>

          <div className="bg-emerald-100 dark:bg-emerald-900/30 p-6 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] transition-all">
            <div className="flex items-start gap-4">
              <div className="p-2 bg-emerald-400 border-2 border-black rounded-none">
                <UserCheck className="w-6 h-6 text-white" />
              </div>
              <div>
                <h2 className="text-xl font-black uppercase mb-2">3. Your Rights</h2>
                <p className="font-medium">You own your content. You can delete your posts and your account at any time. If you leave, we don't keep a shadow profile of you.</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
