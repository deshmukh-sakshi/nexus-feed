import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAdminPosts, useDeletePost } from '@/hooks/useAdmin'
import { Trash2, ExternalLink, ChevronLeft, ChevronRight } from 'lucide-react'

export const AdminPosts = () => {
  const [page, setPage] = useState(0)
  const { data, isLoading } = useAdminPosts(page)
  const deletePost = useDeletePost()
  const [deleteConfirm, setDeleteConfirm] = useState<string | null>(null)

  const handleDelete = (postId: string) => {
    deletePost.mutate(postId)
    setDeleteConfirm(null)
  }

  if (isLoading) {
    return (
      <div className="flex justify-center p-8">
        <div className="px-6 py-3 bg-yellow-300 border-4 border-black font-bold text-lg shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
          Loading...
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6 mx-4 md:mx-8 lg:mx-12">
      <h1 className="text-3xl font-black text-black">Manage Posts</h1>

      <div className="bg-white border-4 border-black shadow-[8px_8px_0px_0px_rgba(0,0,0,1)] overflow-hidden">
        <table className="w-full">
          <thead className="bg-cyan-400 border-b-4 border-black">
            <tr>
              <th className="px-4 py-3 text-left font-black">Title</th>
              <th className="px-4 py-3 text-left font-black">Author</th>
              <th className="px-4 py-3 text-left font-black">Votes</th>
              <th className="px-4 py-3 text-left font-black">Comments</th>
              <th className="px-4 py-3 text-left font-black">Created</th>
              <th className="px-4 py-3 text-left font-black">Actions</th>
            </tr>
          </thead>
          <tbody>
            {data?.content.map((post, idx) => (
              <tr key={post.id} className={idx % 2 === 0 ? 'bg-yellow-50' : 'bg-white'}>
                <td className="px-4 py-3 font-bold max-w-xs truncate">{post.title}</td>
                <td className="px-4 py-3">{post.username}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-1 border-2 border-black font-bold text-sm ${
                    post.upvotes - post.downvotes >= 0 ? 'bg-green-300' : 'bg-red-300'
                  }`}>
                    {post.upvotes - post.downvotes}
                  </span>
                </td>
                <td className="px-4 py-3">{post.commentCount}</td>
                <td className="px-4 py-3 text-sm">{new Date(post.createdAt).toLocaleDateString()}</td>
                <td className="px-4 py-3">
                  <div className="flex gap-2">
                    <Link
                      to={`/post/${post.id}`}
                      target="_blank"
                      className="p-2 bg-blue-400 border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] hover:shadow-none hover:translate-x-0.5 hover:translate-y-0.5 transition-all"
                    >
                      <ExternalLink className="h-4 w-4" />
                    </Link>
                    {deleteConfirm === post.id ? (
                      <div className="flex gap-2">
                        <button
                          onClick={() => handleDelete(post.id)}
                          className="px-3 py-1 bg-red-500 text-white border-2 border-black font-bold text-sm"
                        >
                          Confirm
                        </button>
                        <button
                          onClick={() => setDeleteConfirm(null)}
                          className="px-3 py-1 bg-gray-300 border-2 border-black font-bold text-sm"
                        >
                          Cancel
                        </button>
                      </div>
                    ) : (
                      <button
                        onClick={() => setDeleteConfirm(post.id)}
                        className="p-2 bg-red-400 border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] hover:shadow-none hover:translate-x-0.5 hover:translate-y-0.5 transition-all"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      <div className="flex items-center justify-between">
        <button
          disabled={page === 0}
          onClick={() => setPage(page - 1)}
          className="flex items-center gap-2 px-4 py-2 bg-white border-2 border-black font-bold shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:shadow-none hover:translate-x-1 hover:translate-y-1 transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] disabled:hover:translate-x-0 disabled:hover:translate-y-0"
        >
          <ChevronLeft className="h-4 w-4" />
          Previous
        </button>
        <span className="px-4 py-2 bg-cyan-400 border-2 border-black font-bold">
          Page {page + 1} of {data?.page.totalPages || 1}
        </span>
        <button
          disabled={page >= (data?.page.totalPages || 1) - 1}
          onClick={() => setPage(page + 1)}
          className="flex items-center gap-2 px-4 py-2 bg-white border-2 border-black font-bold shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:shadow-none hover:translate-x-1 hover:translate-y-1 transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] disabled:hover:translate-x-0 disabled:hover:translate-y-0"
        >
          Next
          <ChevronRight className="h-4 w-4" />
        </button>
      </div>
    </div>
  )
}
