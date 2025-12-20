import { useState, useMemo, useEffect } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useAdminUsers, useDeleteUser } from '@/hooks/useAdmin'
import { Trash2, ArrowUpDown, Search } from 'lucide-react'
import type { AdminUser } from '@/types'

type SortField = 'username' | 'email' | 'role' | 'karma' | 'postCount' | 'commentCount' | 'createdAt'
type SortOrder = 'asc' | 'desc'

export const AdminUsers = () => {
  const [searchParams] = useSearchParams()
  const [page, setPage] = useState(0)
  const { data, isLoading, error } = useAdminUsers(page, 20)
  const deleteUser = useDeleteUser()
  const [deleteConfirm, setDeleteConfirm] = useState<string | null>(null)
  const [sortField, setSortField] = useState<SortField>('createdAt')
  const [sortOrder, setSortOrder] = useState<SortOrder>('desc')
  const [searchTerm, setSearchTerm] = useState(searchParams.get('search') || '')
  const [roleFilter, setRoleFilter] = useState<string>('all')

  useEffect(() => {
    const search = searchParams.get('search')
    if (search) setSearchTerm(search)
  }, [searchParams])

  const handleDelete = (userId: string) => {
    deleteUser.mutate(userId)
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

  const filteredAndSortedUsers = useMemo(() => {
    if (!data?.content) return []
    
    let filtered = data.content.filter((user: AdminUser) => {
      const search = searchTerm.toLowerCase()
      const matchesSearch = user.username?.toLowerCase().includes(search) || user.email?.toLowerCase().includes(search)
      const matchesRole = roleFilter === 'all' || user.role === roleFilter
      return matchesSearch && matchesRole
    })

    return filtered.sort((a: AdminUser, b: AdminUser) => {
      let comparison = 0
      switch (sortField) {
        case 'username':
          comparison = a.username.localeCompare(b.username)
          break
        case 'email':
          comparison = a.email.localeCompare(b.email)
          break
        case 'role':
          comparison = a.role.localeCompare(b.role)
          break
        case 'karma':
          comparison = a.karma - b.karma
          break
        case 'postCount':
          comparison = a.postCount - b.postCount
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
  }, [data?.content, sortField, sortOrder, searchTerm, roleFilter])

  const SortButton = ({ field, children }: { field: SortField; children: React.ReactNode }) => (
    <button onClick={() => handleSort(field)} className="flex items-center gap-1 hover:text-blue-800">
      {children}
      <ArrowUpDown className={`h-3 w-3 ${sortField === field ? 'text-blue-800' : 'opacity-50'}`} />
    </button>
  )

  if (isLoading) {
    return (
      <div className="flex justify-center p-8 mx-4 md:mx-8 lg:mx-12">
        <div className="px-6 py-3 bg-yellow-300 border-2 border-black font-bold text-lg shadow-[2px_2px_0px_0px_rgba(0,0,0,1)]">
          Loading...
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="space-y-6 mx-4 md:mx-8 lg:mx-12">
        <h1 className="text-3xl font-bold text-black">Manage Users</h1>
        <div className="px-6 py-4 bg-red-300 border-4 border-black font-bold shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
          Error loading users. Make sure you have admin privileges.
        </div>
      </div>
    )
  }


  return (
    <div className="space-y-6 mx-4 md:mx-8 lg:mx-12">
      <h1 className="text-3xl font-bold text-black">Manage Users</h1>

      {/* Filters */}
      <div className="flex flex-wrap gap-4 items-center">
        <div className="relative flex-1 min-w-[200px] max-w-md">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-500" />
          <input
            type="text"
            placeholder="Search by username or email..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border-3 border-black font-semibold text-sm bg-white focus:outline-none focus:ring-2 focus:ring-purple-400"
          />
        </div>
        <div className="flex items-center gap-2">
          <label className="font-bold text-sm">Role:</label>
          <select
            value={roleFilter}
            onChange={(e) => setRoleFilter(e.target.value)}
            className="px-3 py-2 border-3 border-black font-semibold text-sm bg-white focus:outline-none focus:ring-2 focus:ring-purple-400"
          >
            <option value="all">All</option>
            <option value="USER">User</option>
            <option value="ADMIN">Admin</option>
          </select>
        </div>
        <div className="flex items-center gap-2">
          <label className="font-bold text-sm">Sort by:</label>
          <select
            value={sortField}
            onChange={(e) => setSortField(e.target.value as SortField)}
            className="px-3 py-2 border-3 border-black font-semibold text-sm bg-white focus:outline-none focus:ring-2 focus:ring-purple-400"
          >
            <option value="createdAt">Date</option>
            <option value="username">Username</option>
            <option value="email">Email</option>
            <option value="role">Role</option>
            <option value="karma">Karma</option>
            <option value="postCount">Posts</option>
            <option value="commentCount">Comments</option>
          </select>
          <select
            value={sortOrder}
            onChange={(e) => setSortOrder(e.target.value as SortOrder)}
            className="px-3 py-2 border-3 border-black font-semibold text-sm bg-white focus:outline-none focus:ring-2 focus:ring-purple-400"
          >
            <option value="desc">Descending</option>
            <option value="asc">Ascending</option>
          </select>
        </div>
      </div>

      <div className="bg-white border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] overflow-hidden">
        <table className="w-full">
          <thead className="bg-purple-400 border-b-2 border-black">
            <tr>
              <th className="px-4 py-3 text-left font-semibold text-sm">
                <SortButton field="username">Username</SortButton>
              </th>
              <th className="px-4 py-3 text-left font-semibold text-sm">
                <SortButton field="email">Email</SortButton>
              </th>
              <th className="px-4 py-3 text-left font-semibold text-sm">
                <SortButton field="role">Role</SortButton>
              </th>
              <th className="px-4 py-3 text-left font-semibold text-sm">
                <SortButton field="karma">Karma</SortButton>
              </th>
              <th className="px-4 py-3 text-left font-semibold text-sm">
                <SortButton field="postCount">Posts</SortButton>
              </th>
              <th className="px-4 py-3 text-left font-semibold text-sm">
                <SortButton field="commentCount">Comments</SortButton>
              </th>
              <th className="px-4 py-3 text-left font-semibold text-sm">Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredAndSortedUsers.length > 0 ? (
              filteredAndSortedUsers.map((user: AdminUser, idx: number) => (
                <tr key={user.id} className={idx % 2 === 0 ? 'bg-yellow-50' : 'bg-white'}>
                  <td className="px-4 py-3 font-semibold text-sm">
                    <Link to={`/user/${user.username}`} className="text-blue-600 hover:underline">
                      {user.username}
                    </Link>
                  </td>
                  <td className="px-4 py-3 text-sm">{user.email}</td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-1 border-2 border-black font-semibold text-xs ${user.role === 'ADMIN' ? 'bg-purple-300' : 'bg-gray-200'}`}>
                      {user.role}
                    </span>
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
                        <button onClick={() => handleDelete(user.id)} className="px-2 py-1 bg-red-500 text-white border-2 border-black font-semibold text-xs shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] hover:bg-red-600 active:translate-x-0.5 active:translate-y-0.5 active:shadow-none transition-all">Confirm</button>
                        <button onClick={() => setDeleteConfirm(null)} className="px-2 py-1 bg-gray-300 border-2 border-black font-semibold text-xs shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] hover:bg-gray-400 active:translate-x-0.5 active:translate-y-0.5 active:shadow-none transition-all">Cancel</button>
                      </div>
                    ) : (
                      <button onClick={() => setDeleteConfirm(user.id)} className="p-2 bg-red-400 border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] hover:bg-red-500 active:translate-x-0.5 active:translate-y-0.5 active:shadow-none transition-all">
                        <Trash2 className="h-4 w-4" />
                      </button>
                    )}
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={7} className="px-4 py-8 text-center text-gray-500">No users found</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <div className="text-sm font-semibold text-gray-600">
        Showing {filteredAndSortedUsers.length} of {data?.content.length || 0} users
      </div>
    </div>
  )
}
