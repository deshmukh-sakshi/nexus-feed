import { Link } from 'react-router-dom'
import { useAdminStats } from '@/hooks/useAdmin'
import { Users, FileText, MessageSquare, ThumbsUp } from 'lucide-react'

export const AdminDashboard = () => {
  const { data: stats, isLoading } = useAdminStats()

  if (isLoading) {
    return (
      <div className="flex justify-center p-8">
        <div className="px-6 py-3 bg-yellow-300 border-2 border-black font-bold text-lg shadow-[2px_2px_0px_0px_rgba(0,0,0,1)]">
          Loading...
        </div>
      </div>
    )
  }

  const statCards = [
    { title: 'Total Users', value: stats?.totalUsers ?? 0, icon: Users, link: '/admin/users', color: 'bg-pink-400' },
    { title: 'Total Posts', value: stats?.totalPosts ?? 0, icon: FileText, link: '/admin/posts', color: 'bg-cyan-400' },
    { title: 'Total Comments', value: stats?.totalComments ?? 0, icon: MessageSquare, link: '/admin/comments', color: 'bg-green-400' },
    { title: 'Total Votes', value: stats?.totalVotes ?? 0, icon: ThumbsUp, link: null, color: 'bg-orange-400' },
  ]

  return (
    <div className="space-y-8 mx-4 md:mx-8 lg:mx-12">
      <h1 className="text-3xl font-bold text-black">Dashboard</h1>
      
      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
        {statCards.map((stat) => (
          <div
            key={stat.title}
            className={`${stat.color} border-2 border-black p-6 shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]`}
          >
            <div className="flex items-center justify-between mb-4">
              <span className="text-sm font-bold uppercase tracking-wide">{stat.title}</span>
              <div className="p-2 bg-white border-2 border-black">
                <stat.icon className="h-5 w-5" />
              </div>
            </div>
            <div className="text-4xl font-black mb-2">{stat.value.toLocaleString()}</div>
            {stat.link && (
              <Link
                to={stat.link}
                className="inline-block mt-2 px-3 py-1 bg-white border-2 border-black font-bold text-sm hover:bg-yellow-200 transition-colors"
              >
                View all →
              </Link>
            )}
          </div>
        ))}
      </div>

      <div className="bg-white border-2 border-black p-6 shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
        <h2 className="text-xl font-black mb-4">Quick Actions</h2>
        <div className="flex flex-wrap gap-4">
          <Link
            to="/admin/users"
            className="px-6 py-3 bg-pink-400 border-2 border-black font-bold shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:bg-pink-500 transition-colors"
          >
            Manage Users
          </Link>
          <Link
            to="/admin/posts"
            className="px-6 py-3 bg-cyan-400 border-2 border-black font-bold shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:bg-cyan-500 transition-colors"
          >
            Manage Posts
          </Link>
          <Link
            to="/admin/comments"
            className="px-6 py-3 bg-green-400 border-2 border-black font-bold shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:bg-green-500 transition-colors"
          >
            Manage Comments
          </Link>
        </div>
      </div>
    </div>
  )
}
