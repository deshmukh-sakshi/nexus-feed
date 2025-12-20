import { useState, useMemo } from 'react'
import { Link } from 'react-router-dom'
import { useAdminPosts, useDeletePost } from '@/hooks/useAdmin'
import { Trash2, ExternalLink, ArrowUpDown, Search } from 'lucide-react'
import type { AdminPost } from '@/types'

type SortField = 'title' | 'username' | 'votes' | 'commentCount' | 'createdAt'
type SortOrder = 'asc' | 'desc'

export const AdminPosts = () => {
  const [page, setPage] = useState(0)
  const { data, isLoading, error } = useAdminPosts(page, 20)
  const deletePost = useDeletePost()
  const [deleteConfirm, setDeleteConfirm] = useState<string | null>(null)
  const [sortField, setSortField] = useState<SortField>('createdAt')
  const [sortOrder, setSortOrder] = useState<SortOrder>('desc')
  const [searchTerm, setSearchTerm] = useState('')

  const handleDelete = (postId: string) => {
    deletePost.mutate(postId)
    setDeleteConfirm(null)
  }

  const handleSort = (field: SortField) => {
    if (sortField === field) {
      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc')
    } else {
      setSortField(field)
      setSortOrder('desc')
    }
  }

  const filteredAndSortedPosts = useMemo(() => {
    if (!data?.content) return []
    
    let filtered = data.content.filter((post: AdminPost) => {
      const search = searchTerm.toLowerCase()
      return (
        post.title.toLowerCase().includes(search) ||
        post.username.toLowerCase().includes(search)
      )
    })

    return filtered.sort((a: AdminPost, b: AdminPost) => {
      let comparison = 0
      switch (sortField) {
        case 'title':
          comparison = a.title.localeCompare(b.title)
          break
        case 'username':
          comparison = a.username.localeCompare(b.username)
          break
        case 'votes':
          comparison = (a.upvotes - a.downvotes) - (b.upvotes - b.downvotes)
          break
        case 'commentCount':
          comparison = a.commentCount - b.commentCount
          break
        case 'createdAt':
          comparison = new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
          break
      }
      return sortOrder === 'asc' ? comparison : -comparison
    })
  }, [data?.content, sortField, sortOrder, searchTerm])

  const SortButton = ({ field, children }: { field: SortField; children: React.ReactNode }) => (
    <button onClick={() => handleSort(field)} className="flex items-center gap-1 hover:text-blue-800">
      {children}
      <ArrowUpDown className={`h-3 w-3 ${sortField === field ? 'text-blue-800' : 'opacity-50'}`} />
    </button>
  )

  if (isLoading) {
    return (
      <div className="flex justify-center p-8">
        <div className="px-6 py-3 bg-yellow-300 border-2 border-black font-bold text-lg shadow-[2px_2px_0px_0px_rgba(0,0,0,1)]">
          Loading...
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="space-y-6 mx-4 md:mx-8 lg:mx-12">
        <h1 className="text-3xl font-bold text-black">Manage Posts</h1>
        <div className="px-6 py-4 bg-red-300 border-4 border-black font-bold shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
          Error loading posts. Make sure you have admin privileges.
        </div>
      </div>
    )
  }


  return (
    <div className="space-y-6 mx-4 md:mx-8 lg:mx-12">
      <h1 className="text-3xl font-bold text-black">Manage Posts</h1>

      {/* Filters */}
      <div className="flex flex-wrap gap-4 items-center">
        <div className="relative flex-1 min-w-[200px] max-w-md">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-500" />
          <input
            type="text"
            placeholder="Search by title or author..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border-3 border-black font-semibold text-sm bg-white focus:outline-none focus:ring-2 focus:ring-cyan-400"
          />
        </div>
        <div className="flex items-center gap-2">
          <label className="font-bold text-sm">Sort by:</label>
          <select
            value={sortField}
            onChange={(e) => setSortField(e.target.value as SortField)}
            className="px-3 py-2 border-3 border-black font-semibold text-sm bg-white focus:outline-none focus:ring-2 focus:ring-cyan-400"
          >
            <option value="createdAt">Date</option>
            <option value="title">Title</option>
            <option value="username">Author</option>
            <option value="votes">Votes</option>
            <option value="commentCount">Comments</option>
          </select>
          <select
            value={sortOrder}
            onChange={(e) => setSortOrder(e.target.value as SortOrder)}
            className="px-3 py-2 border-3 border-black font-semibold text-sm bg-white focus:outline-none focus:ring-2 focus:ring-cyan-400"
          >
            <option value="desc">Descending</option>
            <option value="asc">Ascending</option>
          </select>
        </div>
      </div>

      <div className="bg-white border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] overflow-hidden">
        <table className="w-full">
          <thead className="bg-cyan-400 border-b-2 border-black">
            <tr>
              <th className="px-4 py-3 text-left font-semibold text-sm">
                <SortButton field="title">Title</SortButton>
              </th>
              <th className="px-4 py-3 text-left font-semibold text-sm">
                <SortButton field="username">Author</SortButton>
              </th>
              <th className="px-4 py-3 text-left font-semibold text-sm">
                <SortButton field="votes">Votes</SortButton>
              </th>
              <th className="px-4 py-3 text-left font-semibold text-sm">
                <SortButton field="commentCount">Comments</SortButton>
              </th>
              <th className="px-4 py-3 text-left font-semibold text-sm">
                <SortButton field="createdAt">Created</SortButton>
              </th>
              <th className="px-4 py-3 text-left font-semibold text-sm">Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredAndSortedPosts.length > 0 ? (
              filteredAndSortedPosts.map((post: AdminPost, idx: number) => (
                <tr key={post.id} className={idx % 2 === 0 ? 'bg-yellow-50' : 'bg-white'}>
                  <td className="px-4 py-3 font-bold max-w-xs truncate">{post.title}</td>
                  <td className="px-4 py-3">
                    <Link to={`/admin/users?search=${post.username}`} className="text-blue-600 hover:underline font-semibold">
                      {post.username}
                    </Link>
                  </td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-1 border-2 border-black font-bold text-sm ${post.upvotes - post.downvotes >= 0 ? 'bg-green-300' : 'bg-red-300'}`}>
                      {post.upvotes - post.downvotes}
                    </span>
                  </td>
                  <td className="px-4 py-3">{post.commentCount}</td>
                  <td className="px-4 py-3 text-sm">{new Date(post.createdAt).toLocaleDateString()}</td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2">
                      <Link to={`/post/${post.id}`} target="_blank" className="p-2 bg-blue-400 border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] hover:bg-blue-500 active:translate-x-0.5 active:translate-y-0.5 active:shadow-none transition-all">
                        <ExternalLink className="h-4 w-4" />
                      </Link>
                      {deleteConfirm === post.id ? (
                        <div className="flex gap-2">
                          <button onClick={() => handleDelete(post.id)} className="px-3 py-1 bg-red-500 text-white border-2 border-black font-bold text-sm shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] hover:bg-red-600 active:translate-x-0.5 active:translate-y-0.5 active:shadow-none transition-all">Confirm</button>
                          <button onClick={() => setDeleteConfirm(null)} className="px-3 py-1 bg-gray-300 border-2 border-black font-bold text-sm shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] hover:bg-gray-400 active:translate-x-0.5 active:translate-y-0.5 active:shadow-none transition-all">Cancel</button>
                        </div>
                      ) : (
                        <button onClick={() => setDeleteConfirm(post.id)} className="p-2 bg-red-400 border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] hover:bg-red-500 active:translate-x-0.5 active:translate-y-0.5 active:shadow-none transition-all">
                          <Trash2 className="h-4 w-4" />
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={6} className="px-4 py-8 text-center text-gray-500">No posts found</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <div className="text-sm font-semibold text-gray-600">
        Showing {filteredAndSortedPosts.length} of {data?.content.length || 0} posts
      </div>
    </div>
  )
}
