import { Navigate, Outlet, Link, useLocation } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'
import { cn } from '@/lib/utils'
import { LayoutDashboard, Users, FileText, MessageSquare, Home, LogOut } from 'lucide-react'
import { useAuth } from '@/hooks/useAuth'

const navItems = [
  { to: '/admin', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/admin/users', label: 'Users', icon: Users },
  { to: '/admin/posts', label: 'Posts', icon: FileText },
  { to: '/admin/comments', label: 'Comments', icon: MessageSquare },
]

export const AdminLayout = () => {
  const { isAdmin, isAuthenticated, user } = useAuthStore()
  const { logout } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) return <Navigate to="/login" replace />
  // TODO: Uncomment for production
  // if (!isAdmin) return <Navigate to="/" replace />

  return (
    <div className="min-h-screen">
      {/* Admin Header */}
      <header className="sticky top-0 z-50 border-b-4 border-black bg-purple-400">
        <div className="container mx-auto px-6 max-w-7xl flex h-16 items-center justify-between">
          <div className="flex items-center gap-4">
            <Link
              to="/"
              className="flex items-center gap-2 px-4 py-2 bg-yellow-300 text-black font-bold border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:shadow-none hover:translate-x-1 hover:translate-y-1 transition-all"
            >
              <Home className="h-5 w-5" />
              Back to Site
            </Link>
            <h1 className="text-2xl font-black text-black">Admin Panel</h1>
          </div>
          <div className="flex items-center gap-4">
            <span className="px-3 py-1 bg-green-300 border-2 border-black font-bold text-sm">
              {user?.username}
            </span>
            <button
              onClick={logout}
              className="flex items-center gap-2 px-4 py-2 bg-red-400 text-black font-bold border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:shadow-none hover:translate-x-1 hover:translate-y-1 transition-all"
            >
              <LogOut className="h-4 w-4" />
              Logout
            </button>
          </div>
        </div>
      </header>

      {/* Admin Navigation Tabs */}
      <nav className="border-b-4 border-black bg-white">
        <div className="container mx-auto px-6 max-w-7xl">
          <div className="flex gap-2 py-3">
            {navItems.map((item) => (
              <Link
                key={item.to}
                to={item.to}
                className={cn(
                  'flex items-center gap-2 px-4 py-2 font-bold border-2 border-black transition-all',
                  location.pathname === item.to
                    ? 'bg-cyan-400 shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]'
                    : 'bg-white hover:bg-gray-100 shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] hover:shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]'
                )}
              >
                <item.icon className="h-4 w-4" />
                {item.label}
              </Link>
            ))}
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="container mx-auto px-6 py-8 max-w-7xl">
        <Outlet />
      </main>
    </div>
  )
}
