
import { Link } from 'react-router-dom'
import { Copyright } from 'lucide-react'

export const SidebarFooter = () => {
  return (
    <div className="flex flex-wrap gap-x-3 gap-y-2 px-2 text-xs font-bold text-gray-700 dark:text-gray-300">
      <Link to="/privacy" className="hover:text-black dark:hover:text-white hover:underline transition-colors">Privacy</Link>
      <span className="opacity-50">•</span>
      <Link to="/terms" className="hover:text-black dark:hover:text-white hover:underline transition-colors">Terms</Link>
      <span className="opacity-50">•</span>
      <Link to="/guidelines" className="hover:text-black dark:hover:text-white hover:underline transition-colors">Guidelines</Link>
      <div className="w-full pt-2 flex items-center gap-1 opacity-70">
        <Copyright className="w-4 h-4" />
        <span>{new Date().getFullYear()} Nexus Feed</span>
      </div>
    </div>
  )
}
