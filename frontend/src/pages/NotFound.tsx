import { Link } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Home, Search, ArrowLeft } from 'lucide-react'

export const NotFound = () => {
  return (
    <div className="min-h-[60vh] flex items-center justify-center p-8">
      <div className="text-center space-y-6 max-w-md">
        <div className="space-y-2">
          <h1 className="text-8xl font-black text-black">404</h1>
          <div className="h-2 w-32 mx-auto bg-yellow-400 border-2 border-black" />
        </div>
        
        <div className="space-y-2">
          <h2 className="text-2xl font-bold">Page Not Found</h2>
          <p className="text-muted-foreground">
            The page you're looking for doesn't exist or has been moved.
          </p>
        </div>

        <div className="flex flex-col sm:flex-row gap-3 justify-center">
          <Link to="/">
            <Button className="w-full sm:w-auto bg-yellow-400 text-black hover:bg-yellow-500 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] rounded-none font-bold">
              <Home className="mr-2 h-4 w-4" />
              Go Home
            </Button>
          </Link>
          <Link to="/search">
            <Button className="w-full sm:w-auto bg-teal-400 text-black hover:bg-teal-500 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] rounded-none font-bold">
              <Search className="mr-2 h-4 w-4" />
              Search
            </Button>
          </Link>
          <Button
            onClick={() => window.history.back()}
            className="w-full sm:w-auto bg-gray-300 text-black hover:bg-gray-400 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] rounded-none font-bold"
          >
            <ArrowLeft className="mr-2 h-4 w-4" />
            Go Back
          </Button>
        </div>
      </div>
    </div>
  )
}
