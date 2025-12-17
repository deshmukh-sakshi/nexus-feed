import { Outlet } from 'react-router-dom'
import { Navbar } from './Navbar'
import { LeftSidebar, RightSidebar } from '@/components/sidebar'

export const Layout = () => {
  return (
    <div className="min-h-screen w-full">
      <Navbar />
      <main className="w-full">
        <div className="container mx-auto px-4 py-6">
          <div className="grid grid-cols-1 lg:grid-cols-[280px_1fr_300px] gap-6">
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
        </div>
      </main>
    </div>
  )
}
