import { useState, useRef, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { LogOut, User, PlusCircle, Search, Shield, Menu, X } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { UserAvatar } from '@/components/ui/user-avatar'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { useAuthStore } from '@/stores/authStore'
import { useAuth } from '@/hooks/useAuth'

export const Navbar = () => {
  const { user, isAuthenticated, isAdmin } = useAuthStore()
  const { logout } = useAuth()
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false)
  const mobileMenuRef = useRef<HTMLDivElement>(null)

  // Click outside handler to close mobile menu
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (mobileMenuRef.current && !mobileMenuRef.current.contains(event.target as Node)) {
        setIsMobileMenuOpen(false)
      }
    }

    if (isMobileMenuOpen) {
      document.addEventListener('mousedown', handleClickOutside)
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside)
    }
  }, [isMobileMenuOpen])

  // Close mobile menu when navigating
  const handleMobileNavClick = () => {
    setIsMobileMenuOpen(false)
  }

  return (
    <nav className="sticky top-0 z-50 w-full border-b-2 border-black bg-yellow-50 backdrop-blur-sm">
      <div className="container mx-auto px-4 flex h-14 items-center">
        <div className="mr-4 flex">
          <Link 
            to="/" 
            className="mr-6 flex items-center gap-2 px-4 py-2 bg-purple-400 text-black hover:bg-purple-500 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] active:translate-x-[4px] active:translate-y-[4px] active:shadow-none transition-all rounded-none font-bold"
          >
            <img src="/logo.svg" alt="Nexus Feed" className="h-6 w-6" />
            <span className="hidden sm:inline">Nexus Feed</span>
          </Link>
        </div>

        {/* Desktop navigation - hidden on mobile */}
        <div className="hidden sm:flex flex-1 items-center justify-end space-x-2">
          <Link to="/search">
            <Button className="bg-teal-400 text-black hover:bg-teal-500 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold">
              <Search className="mr-2 h-5 w-5" />
              Search
            </Button>
          </Link>

          {isAuthenticated ? (
            <>
              <Link to="/create-post">
                <Button className="bg-green-400 text-black hover:bg-green-500 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold">
                  <PlusCircle className="mr-2 h-5 w-5" />
                  Create Post
                </Button>
              </Link>

              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button className="bg-yellow-400 text-black hover:bg-yellow-500 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold px-2">
                    <UserAvatar 
                      username={user?.username || 'User'} 
                      profileImageUrl={user?.profilePictureUrl}
                      size="sm"
                      className="mr-2 border-2 border-black h-6 w-6"
                    />
                    <span className="max-w-[100px] truncate">{user?.username}</span>
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end">
                  <div className="flex items-center justify-start gap-2 p-2">
                    <div className="flex flex-col space-y-1 leading-none">
                      <p className="font-medium">{user?.username}</p>
                      <p className="w-[200px] truncate text-sm text-muted-foreground">
                        {user?.email}
                      </p>
                    </div>
                  </div>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem asChild>
                    <Link to={`/user/${user?.username}`}>
                      <User className="mr-2 h-4 w-4" />
                      Profile
                    </Link>
                  </DropdownMenuItem>
                  {isAdmin && (
                    <DropdownMenuItem asChild>
                      <Link to="/admin">
                        <Shield className="mr-2 h-4 w-4" />
                        Admin Panel
                      </Link>
                    </DropdownMenuItem>
                  )}
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={logout}>
                    <LogOut className="mr-2 h-4 w-4" />
                    Log out
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </>
          ) : (
            <>
              <Link to="/login">
                <Button className="bg-pink-400 text-black hover:bg-pink-500 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold">
                  Log in
                </Button>
              </Link>
              <Link to="/register">
                <Button className="bg-blue-500 text-black hover:bg-blue-600 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold">
                  Sign up
                </Button>
              </Link>
            </>
          )}
        </div>

        {/* Mobile hamburger button - visible only on mobile */}
        <div className="flex flex-1 items-center justify-end sm:hidden" ref={mobileMenuRef}>
          <Button
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            className="bg-orange-400 text-black hover:bg-orange-500 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] rounded-none font-bold p-2"
            aria-label={isMobileMenuOpen ? 'Close menu' : 'Open menu'}
          >
            {isMobileMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
          </Button>

          {/* Mobile dropdown menu */}
          {isMobileMenuOpen && (
            <div className="absolute top-full right-4 mt-2 w-56 bg-white border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] z-50">
              <div className="flex flex-col p-2 space-y-2">
                <Link to="/search" onClick={handleMobileNavClick}>
                  <Button className="w-full bg-teal-400 text-black hover:bg-teal-500 border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] rounded-none font-bold justify-start">
                    <Search className="mr-2 h-5 w-5" />
                    Search
                  </Button>
                </Link>

                {isAuthenticated ? (
                  <>
                    <Link to="/create-post" onClick={handleMobileNavClick}>
                      <Button className="w-full bg-green-400 text-black hover:bg-green-500 border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] rounded-none font-bold justify-start">
                        <PlusCircle className="mr-2 h-5 w-5" />
                        Create Post
                      </Button>
                    </Link>

                    <Link to={`/user/${user?.username}`} onClick={handleMobileNavClick}>
                      <Button className="w-full bg-yellow-400 text-black hover:bg-yellow-500 border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] rounded-none font-bold justify-start">
                        <User className="mr-2 h-5 w-5" />
                        Profile
                      </Button>
                    </Link>

                    {isAdmin && (
                      <Link to="/admin" onClick={handleMobileNavClick}>
                        <Button className="w-full bg-purple-400 text-black hover:bg-purple-500 border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] rounded-none font-bold justify-start">
                          <Shield className="mr-2 h-5 w-5" />
                          Admin Panel
                        </Button>
                      </Link>
                    )}

                    <Button
                      onClick={() => {
                        logout()
                        handleMobileNavClick()
                      }}
                      className="w-full bg-red-400 text-black hover:bg-red-500 border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] rounded-none font-bold justify-start"
                    >
                      <LogOut className="mr-2 h-5 w-5" />
                      Log out
                    </Button>
                  </>
                ) : (
                  <>
                    <Link to="/login" onClick={handleMobileNavClick}>
                      <Button className="w-full bg-pink-400 text-black hover:bg-pink-500 border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] rounded-none font-bold justify-start">
                        Log in
                      </Button>
                    </Link>
                    <Link to="/register" onClick={handleMobileNavClick}>
                      <Button className="w-full bg-blue-500 text-black hover:bg-blue-600 border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] rounded-none font-bold justify-start">
                        Sign up
                      </Button>
                    </Link>
                  </>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    </nav>
  )
}
