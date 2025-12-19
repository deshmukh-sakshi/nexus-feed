import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ReactQueryDevtools } from '@tanstack/react-query-devtools'
import { Toaster } from '@/components/ui/sonner'
import { Layout } from '@/components/layout/Layout'
import { Home } from '@/pages/Home'
import { Login } from '@/pages/Login'
import { Register } from '@/pages/Register'
import { PostDetail } from '@/pages/PostDetail'
import { UserProfile } from '@/pages/UserProfile'
import { CreatePost } from '@/pages/CreatePost'
import { Search } from '@/pages/Search'
import { AdminLayout, AdminDashboard, AdminUsers, AdminPosts, AdminComments } from '@/pages/admin'
import { Privacy } from '@/pages/Privacy'
import { Terms } from '@/pages/Terms'
import { Guidelines } from '@/pages/Guidelines'
import { useSessionExpiry } from '@/hooks/useSessionExpiry'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: false,
      staleTime: 5 * 60 * 1000,
    },
  },
})

// Wrapper component to use hooks that require Router context
const AppRoutes = () => {
  useSessionExpiry()
  
  return (
    <Routes>
      {/* Admin routes - standalone layout without sidebars */}
      <Route path="/admin" element={<AdminLayout />}>
        <Route index element={<AdminDashboard />} />
        <Route path="users" element={<AdminUsers />} />
        <Route path="posts" element={<AdminPosts />} />
        <Route path="comments" element={<AdminComments />} />
      </Route>
      
      {/* Main app routes */}
      <Route element={<Layout />}>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/post/:id" element={<PostDetail />} />
        <Route path="/user/:username" element={<UserProfile />} />
        <Route path="/create-post" element={<CreatePost />} />
        <Route path="/search" element={<Search />} />
        <Route path="/privacy" element={<Privacy />} />
        <Route path="/terms" element={<Terms />} />
        <Route path="/guidelines" element={<Guidelines />} />
      </Route>
    </Routes>
  )
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AppRoutes />
        <Toaster />
      </BrowserRouter>
      <ReactQueryDevtools initialIsOpen={false} />
    </QueryClientProvider>
  )
}

export default App
