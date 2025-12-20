import { useState, useMemo } from 'react'
import { Link } from 'react-router-dom'
import { useAdminComments, useDeleteComment } from '@/hooks/useAdmin'
import { Trash2, ExternalLink, ArrowUpDown, Search } from 'lucide-react'
import type { AdminComment } from '@/types'

type SortField = 'body' | 'username' | 'postTitle' | 'votes' | 'createdAt'
type SortOrder = 'asc' | 'desc'

export const AdminComments = () => {
  const [page, setPage] = useState(0)
  const { data, isLoading, error } = useAdminComments(page, 20)
  const deleteComment = useDeleteComment()
  const [deleteConfirm, setDeleteConfirm] = useState<string | null>(null)
  const [sortField, setSortField] = useState<SortField>('createdAt')
  const [sortOrder, setSortOrder] = useState<SortOrder>('desc')
  const [searchTerm, setSearchTerm] = useState('')

  const handleDelete = (commentId: string) => {
    deleteComment.mutate(commentId)
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

  const filteredAndSortedComments = useMemo(() => {
    if (!data?.content) return []
    
    let filtered = data.content.filter((comment: AdminComment) => {
      const search = searchTerm.toLowerCase()
      return (
        comment.body.toLowerCase().includes(search) ||
        comment.username.toLowerCase().includes(search) ||
        comment.postTitle.toLowerCase().includes(search)
      )
    })

    return filtered.sort((a: AdminComment, b: AdminComment) => {
      let comparison = 0
      switch (sortField) {
        case 'body':
          comparison = a.body.localeCompare(b.body)
          break
        case 'username':
          comparison = a.username.localeCompare(b.username)
          break
        case 'postTitle':
          comparison = a.postTitle.localeCompare(b.postTitle)
          break
        case 'votes':
          comparison = (a.upvotes - a.downvotes) - (b.upvotes - b.downvotes)
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
        <h1 className="text-3xl font-bold text-black">Manage Comments</h1>
        <div className="px-6 py-4 bg-red-300 border-4 border-black font-bold shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
          Error loading comments. Make sure you have admin privileges.
        </div>
      </div>
    )
  }


  return (
    <div className="space-y-6 mx-4 md:mx-8 lg:mx-12">
      <h1 className="text-3xl font-bold text-black">Manage Comments</h1>

      {/* Filters */}
      <div className="flex flex-wrap gap-4 items-center">
        <div className="relative flex-1 min-w-[200px] max-w-md">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-500" />
          <input
            type="text"
            placeholder="Search by comment, author or post..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border-3 border-black font-semibold text-sm bg-white focus:outline-none focus:ring-2 focus:ring-green-400"
          />
        </div>
        <div className="flex items-center gap-2">
          <label className="font-bold text-sm">Sort by:</label>
          <select
            value={sortField}
            onChange={(e) => setSortField(e.target.value as SortField)}
            className="px-3 py-2 border-3 border-black font-semibold text-sm bg-white focus:outline-none focus:ring-2 focus:ring-green-400"
          >
            <option value="createdAt">Date</option>
            <option value="body">Comment</option>
            <option value="username">Author</option>
            <option value="postTitle">Post</option>
            <option value="votes">Votes</option>
          </select>
          <select
            value={sortOrder}
            onChange={(e) => setSortOrder(e.target.value as SortOrder)}
            className="px-3 py-2 border-3 border-black font-semibold text-sm bg-white focus:outline-none focus:ring-2 focus:ring-green-400"
          >
            <option value="desc">Descending</option>
            <option value="asc">Ascending</option>
          </select>
        </div>
      </div>

      <div className="bg-white border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] overflow-hidden">
        <table className="w-full">
          <thead className="bg-green-400 border-b-2 border-black">
            <tr>
              <th className="px-4 py-3 text-left font-semibold text-sm">
                <SortButton field="body">Comment</SortButton>
              </th>
              <th className="px-4 py-3 text-left font-semibold text-sm">
                <SortButton field="username">Author</SortButton>
              </th>
              <th className="px-4 py-3 text-left font-semibold text-sm">
                <SortButton field="postTitle">Post</SortButton>
              </th>
              <th className="px-4 py-3 text-left font-semibold text-sm">
                <SortButton field="votes">Votes</SortButton>
              </th>
              <th className="px-4 py-3 text-left font-semibold text-sm">
                <SortButton field="createdAt">Created</SortButton>
              </th>
              <th className="px-4 py-3 text-left font-semibold text-sm">Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredAndSortedComments.length > 0 ? (
              filteredAndSortedComments.map((comment: AdminComment, idx: number) => (
                <tr key={comment.id} className={idx % 2 === 0 ? 'bg-yellow-50' : 'bg-white'}>
                  <td className="px-4 py-3 max-w-xs truncate">{comment.body}</td>
                  <td className="px-4 py-3 font-bold">
                    <Link to={`/admin/users?search=${comment.username}`} className="text-blue-600 hover:underline">
                      {comment.username}
                    </Link>
                  </td>
                  <td className="px-4 py-3 max-w-xs truncate text-sm">{comment.postTitle}</td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-1 border-2 border-black font-bold text-sm ${comment.upvotes - comment.downvotes >= 0 ? 'bg-green-300' : 'bg-red-300'}`}>
                      {comment.upvotes - comment.downvotes}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-sm">{new Date(comment.createdAt).toLocaleDateString()}</td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2">
                      <Link to={`/post/${comment.postId}`} target="_blank" className="p-2 bg-blue-400 border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] hover:shadow-none hover:translate-x-0.5 hover:translate-y-0.5 transition-all">
                        <ExternalLink className="h-4 w-4" />
                      </Link>
                      {deleteConfirm === comment.id ? (
                        <div className="flex gap-2">
                          <button onClick={() => handleDelete(comment.id)} className="px-3 py-1 bg-red-500 text-white border-2 border-black font-bold text-sm shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] hover:shadow-none hover:translate-x-0.5 hover:translate-y-0.5 transition-all">Confirm</button>
                          <button onClick={() => setDeleteConfirm(null)} className="px-3 py-1 bg-gray-300 border-2 border-black font-bold text-sm shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] hover:shadow-none hover:translate-x-0.5 hover:translate-y-0.5 transition-all">Cancel</button>
                        </div>
                      ) : (
                        <button onClick={() => setDeleteConfirm(comment.id)} className="p-2 bg-red-400 border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] hover:shadow-none hover:translate-x-0.5 hover:translate-y-0.5 transition-all">
                          <Trash2 className="h-4 w-4" />
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={6} className="px-4 py-8 text-center text-gray-500">No comments found</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <div className="text-sm font-semibold text-gray-600">
        Showing {filteredAndSortedComments.length} of {data?.content.length || 0} comments
      </div>
    </div>
  )
}
