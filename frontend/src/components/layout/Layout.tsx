import { Outlet } from 'react-router-dom'
import { Navbar } from './Navbar'

export const Layout = () => {
  return (
    <div className="min-h-screen w-full bg-background">
      <Navbar />
      <main className="w-full">
        <div className="container mx-auto px-4 py-6">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
