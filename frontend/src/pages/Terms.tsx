
import { useNavigate } from 'react-router-dom'
import { ArrowLeft, ScrollText, Handshake, MessageSquareWarning, Siren } from 'lucide-react'
import { Button } from '@/components/ui/button'

export const Terms = () => {
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

      <div className="bg-orange-50 dark:bg-gray-900 border-2 border-black shadow-[8px_8px_0px_0px_rgba(0,0,0,1)] dark:shadow-[8px_8px_0px_0px_rgba(255,255,255,1)] p-8">
        <div className="flex items-center gap-4 mb-8 border-b-4 border-black pb-4">
          <div className="p-3 bg-orange-400 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
            <ScrollText className="w-8 h-8 text-black" />
          </div>
          <div>
            <h1 className="text-4xl font-black uppercase tracking-tight">Terms of Service</h1>
            <p className="font-bold text-muted-foreground mt-1">Effective Date: December 17, 2025</p>
          </div>
        </div>

        <div className="grid gap-6">
          <div className="bg-yellow-100 dark:bg-yellow-900/30 p-6 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] transition-all">
            <div className="flex items-start gap-4">
              <div className="p-2 bg-yellow-400 border-2 border-black rounded-none">
                <Handshake className="w-6 h-6 text-black" />
              </div>
              <div>
                <h2 className="text-xl font-black uppercase mb-2">1. The Agreement</h2>
                <p className="font-medium">By using Nexus Feed, you agree to be a decent human being. We're building a community, not a fight club. Treat others with respect.</p>
              </div>
            </div>
          </div>

          <div className="bg-cyan-100 dark:bg-cyan-900/30 p-6 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] transition-all">
            <div className="flex items-start gap-4">
              <div className="p-2 bg-cyan-400 border-2 border-black rounded-none">
                <MessageSquareWarning className="w-6 h-6 text-black" />
              </div>
              <div>
                <h2 className="text-xl font-black uppercase mb-2">2. Content & Conduct</h2>
                <p className="font-medium">Don't post illegal content, spam, or hate speech. We will ban you faster than you can say "freedom of speech". Keep it clean and relevant.</p>
              </div>
            </div>
          </div>

          <div className="bg-red-100 dark:bg-red-900/30 p-6 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] transition-all">
            <div className="flex items-start gap-4">
              <div className="p-2 bg-red-400 border-2 border-black rounded-none">
                <Siren className="w-6 h-6 text-white" />
              </div>
              <div>
                <h2 className="text-xl font-black uppercase mb-2">3. No Warranties</h2>
                <p className="font-medium">This service is provided "as is". If the server catches fire or your cat walks on your keyboard, we're not responsible for lost data.</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
