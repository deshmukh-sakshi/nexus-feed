import { useState } from 'react'
import { useAdminUsers, useUpdateUserRole, useDeleteUser } from '@/hooks/useAdmin'
import { Trash2, ChevronLeft, ChevronRight } from 'lucide-react'
import type { AdminUser } from '@/types'

export const AdminUsers = () => {
  const [page, setPage] = useState(0)
  const { data, isLoading, error } = useAdminUsers(page)
  const updateRole = useUpdateUserRole()
  const deleteUser = useDeleteUser()
  const [deleteConfirm, setDeleteConfirm] = useState<string | null>(null)

  const handleRoleChange = (user: AdminUser, newRole: string) => {
    updateRole.mutate({ userId: user.id, role: newRole })
  }

  const handleDelete = (userId: string) => {
    deleteUser.mutate(userId)
    setDeleteConfirm(null)
  }

  if (isLoading) {
    return (
      <div className="flex justify-center p-8 mx-4 md:mx-8 lg:mx-12">
        <div className="px-6 py-3 bg-yellow-300 border-4 border-black font-bold text-lg shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
          Loading...
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="space-y-6 mx-4 md:mx-8 lg:mx-12">
        <h1 className="text-2xl font-black text-black">Manage Users</h1>
        <div className="px-6 py-4 bg-red-300 border-4 border-black font-bold shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
          Error loading users. Make sure you have admin privileges.
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6 mx-4 md:mx-8 lg:mx-12">
      <h1 className="text-2xl font-black text-black">Manage Users</h1>

      <div className="bg-white border-4 border-black shadow-[8px_8px_0px_0px_rgba(0,0,0,1)] overflow-hidden">
        <table className="w-full">
          <thead className="bg-purple-400 border-b-4 border-black">
            <tr>
              <th className="px-4 py-3 text-left font-bold text-sm">Username</th>
              <th className="px-4 py-3 text-left font-bold text-sm">Email</th>
              <th className="px-4 py-3 text-left font-bold text-sm">Role</th>
              <th className="px-4 py-3 text-left font-bold text-sm">Karma</th>
              <th className="px-4 py-3 text-left font-bold text-sm">Posts</th>
              <th className="px-4 py-3 text-left font-bold text-sm">Comments</th>
              <th className="px-4 py-3 text-left font-bold text-sm">Actions</th>
            </tr>
          </thead>
          <tbody>
            {data?.content && data.content.length > 0 ? (
              data.content.map((user, idx) => (
                <tr key={user.id} className={idx % 2 === 0 ? 'bg-yellow-50' : 'bg-white'}>
                  <td className="px-4 py-3 font-semibold text-sm">{user.username}</td>
                  <td className="px-4 py-3 text-sm">{user.email}</td>
                  <td className="px-4 py-3">
                    <select
                      value={user.role}
                      onChange={(e) => handleRoleChange(user, e.target.value)}
                      className="px-2 py-1 border-2 border-black font-semibold text-sm bg-white focus:outline-none focus:ring-2 focus:ring-cyan-400"
                    >
                      <option value="USER">User</option>
                      <option value="ADMIN">Admin</option>
                    </select>
                  </td>
                  <td className="px-4 py-3">
                    <span className="px-2 py-1 bg-green-300 border-2 border-black font-semibold text-xs">
                      {user.karma}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-sm">{user.postCount}</td>
                  <td className="px-4 py-3 text-sm">{user.commentCount}</td>
                  <td className="px-4 py-3">
                    {deleteConfirm === user.id ? (
                      <div className="flex gap-2">
                        <button
                          onClick={() => handleDelete(user.id)}
                          className="px-2 py-1 bg-red-500 text-white border-2 border-black font-semibold text-xs"
                        >
                          Confirm
                        </button>
                        <button
                          onClick={() => setDeleteConfirm(null)}
                          className="px-2 py-1 bg-gray-300 border-2 border-black font-semibold text-xs"
                        >
                          Cancel
                        </button>
                      </div>
                    ) : (
                      <button
                        onClick={() => setDeleteConfirm(user.id)}
                        className="p-2 bg-red-400 border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] hover:shadow-none hover:translate-x-0.5 hover:translate-y-0.5 transition-all"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    )}
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={7} className="px-4 py-8 text-center text-gray-500">
                  No users found
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      <div className="flex items-center justify-between">
        <button
          disabled={page === 0}
          onClick={() => setPage(page - 1)}
          className="flex items-center gap-2 px-4 py-2 bg-white border-2 border-black font-semibold text-sm shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:shadow-none hover:translate-x-1 hover:translate-y-1 transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] disabled:hover:translate-x-0 disabled:hover:translate-y-0"
        >
          <ChevronLeft className="h-4 w-4" />
          Previous
        </button>
        <span className="px-4 py-2 bg-cyan-400 border-2 border-black font-semibold text-sm">
          Page {page + 1} of {data?.page.totalPages || 1}
        </span>
        <button
          disabled={page >= (data?.page.totalPages || 1) - 1}
          onClick={() => setPage(page + 1)}
          className="flex items-center gap-2 px-4 py-2 bg-white border-2 border-black font-semibold text-sm shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:shadow-none hover:translate-x-1 hover:translate-y-1 transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] disabled:hover:translate-x-0 disabled:hover:translate-y-0"
        >
          Next
          <ChevronRight className="h-4 w-4" />
        </button>
      </div>
    </div>
  )
}
