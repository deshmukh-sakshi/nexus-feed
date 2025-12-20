import { Outlet, useLocation } from 'react-router-dom'
import { Navbar } from './Navbar'
import { LeftSidebar, RightSidebar } from '@/components/sidebar'

// Routes where sidebars should be hidden
const NO_SIDEBAR_ROUTES = ['/login', '/register']

export const Layout = () => {
  const location = useLocation()
  const hideSidebars = NO_SIDEBAR_ROUTES.includes(location.pathname)

  return (
    <div className="min-h-screen w-full">
      <Navbar />
      <main className="w-full">
        <div className="container mx-auto px-4 py-6">
          {hideSidebars ? (
            // Full width layout for login/register
            <div className="max-w-4xl mx-auto">
              <Outlet />
            </div>
          ) : (
            // Three-column layout for other pages
            <div className="grid grid-cols-1 lg:grid-cols-[260px_1fr_340px] gap-6">
              {/* Left Sidebar - hidden on mobile/tablet */}
              <div className="hidden lg:block">
                <LeftSidebar />
              </div>

              {/* Main Content */}
              <div className="min-w-0">
                <Outlet />
              </div>

              {/* Right Sidebar - hidden on mobile/tablet */}
              <div className="hidden lg:block">
                <RightSidebar />
              </div>
            </div>
          )}
        </div>
      </main>
    </div>
  )
}
