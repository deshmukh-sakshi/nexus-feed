import { useState, useMemo, useEffect, useRef } from 'react'
import { Link } from 'react-router-dom'
import { useAdminReports, useDeletePost, useDeleteComment } from '@/hooks/useAdmin'
import { ArrowUpDown, Filter, ChevronLeft, ChevronRight, Trash2, Eye, MoreVertical, MessageSquare, FileText } from 'lucide-react'
import { AdminMobileCard } from '@/components/admin/AdminMobileCard'
import type { AdminReport, ReportReason } from '@/types'
import { REPORT_REASONS } from '@/types'

type SortField = 'contentTitle' | 'reporterUsername' | 'reason' | 'createdAt'
type SortOrder = 'asc' | 'desc'
type TypeFilter = '' | 'POST' | 'COMMENT'

export const AdminReports = () => {
  const [page, setPage] = useState(0)
  const [reasonFilter, setReasonFilter] = useState<ReportReason | ''>('')
  const [typeFilter, setTypeFilter] = useState<TypeFilter>('')
  const { data, isLoading, error } = useAdminReports(page, 20, reasonFilter || undefined, typeFilter || undefined)
  const deletePost = useDeletePost()
  const deleteComment = useDeleteComment()
  const [sortField, setSortField] = useState<SortField>('createdAt')
  const [sortOrder, setSortOrder] = useState<SortOrder>('desc')
  const [deleteConfirm, setDeleteConfirm] = useState<string | null>(null)
  const [openDropdown, setOpenDropdown] = useState<string | null>(null)
  const dropdownRef = useRef<HTMLDivElement>(null)

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setOpenDropdown(null)
        setDeleteConfirm(null)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const handleDelete = (report: AdminReport) => {
    if (report.reportableType === 'POST') {
      deletePost.mutate(report.reportableId)
    } else {
      deleteComment.mutate(report.reportableId)
    }
    setDeleteConfirm(null)
    setOpenDropdown(null)
  }

  const handleSort = (field: SortField) => {
    if (sortField === field) {
      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc')
    } else {
      setSortField(field)
      setSortOrder('desc')
    }
  }

  const getReasonIcon = (reason: ReportReason) => {
    const found = REPORT_REASONS.find(r => r.value === reason)
    return found?.icon || '❓'
  }

  const sortedReports = useMemo(() => {
    if (!data?.content) return []

    return [...data.content].sort((a: AdminReport, b: AdminReport) => {
      let comparison = 0
      switch (sortField) {
        case 'contentTitle':
          comparison = a.contentTitle.localeCompare(b.contentTitle)
          break
        case 'reporterUsername':
          comparison = a.reporterUsername.localeCompare(b.reporterUsername)
          break
        case 'reason':
          comparison = a.reason.localeCompare(b.reason)
          break
        case 'createdAt':
          comparison = new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
          break
      }
      return sortOrder === 'asc' ? comparison : -comparison
    })
  }, [data?.content, sortField, sortOrder])

  // Map reportableId to report for mobile card delete handling
  const reportsByReportableId = useMemo(() => {
    const map = new Map<string, AdminReport>()
    sortedReports.forEach(report => map.set(report.reportableId, report))
    return map
  }, [sortedReports])

  const handleMobileDelete = (reportableId: string) => {
    const report = reportsByReportableId.get(reportableId)
    if (report) {
      handleDelete(report)
    }
  }

  const getViewLink = (report: AdminReport) => {
    if (report.reportableType === 'POST') {
      return `/admin/posts?search=${encodeURIComponent(report.contentTitle)}`
    }
    return `/admin/comments?search=${encodeURIComponent(report.contentTitle)}`
  }

  const getTypeIcon = (type: 'POST' | 'COMMENT') => {
    return type === 'POST' ? <FileText className="h-4 w-4" /> : <MessageSquare className="h-4 w-4" />
  }

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
        <h1 className="text-3xl font-bold text-black">Manage Reports</h1>
        <div className="px-6 py-4 bg-red-300 border-4 border-black font-bold shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
          Error loading reports: {error instanceof Error ? error.message : 'Unknown error'}
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6 mx-4 md:mx-8 lg:mx-12">
      <h1 className="text-3xl font-bold text-black">Manage Reports</h1>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row flex-wrap gap-3 sm:gap-4 sm:items-center">
        <div className="flex items-center gap-2">
          <Filter className="h-4 w-4 text-gray-600" />
          <label className="font-bold text-sm">Type:</label>
          <select
            value={typeFilter}
            onChange={(e) => {
              setTypeFilter(e.target.value as TypeFilter)
              setPage(0)
            }}
            className="flex-1 sm:flex-none px-3 py-2 border-3 border-black font-semibold text-sm bg-white focus:outline-none focus:ring-2 focus:ring-cyan-400"
          >
            <option value="">All types</option>
            <option value="POST">📄 Posts</option>
            <option value="COMMENT">💬 Comments</option>
          </select>
        </div>
        <div className="flex items-center gap-2">
          <label className="font-bold text-sm">Reason:</label>
          <select
            value={reasonFilter}
            onChange={(e) => {
              setReasonFilter(e.target.value as ReportReason | '')
              setPage(0)
            }}
            className="flex-1 sm:flex-none px-3 py-2 border-3 border-black font-semibold text-sm bg-white focus:outline-none focus:ring-2 focus:ring-cyan-400"
          >
            <option value="">All reasons</option>
            {REPORT_REASONS.map((reason) => (
              <option key={reason.value} value={reason.value}>
                {reason.icon} {reason.label}
              </option>
            ))}
          </select>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <label className="font-bold text-sm">Sort by:</label>
          <select
            value={sortField}
            onChange={(e) => setSortField(e.target.value as SortField)}
            className="flex-1 sm:flex-none px-3 py-2 border-3 border-black font-semibold text-sm bg-white focus:outline-none focus:ring-2 focus:ring-cyan-400"
          >
            <option value="createdAt">Date</option>
            <option value="contentTitle">Content Title</option>
            <option value="reporterUsername">Reporter</option>
            <option value="reason">Reason</option>
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

      {/* Desktop Table View */}
      <div className="hidden lg:block bg-white border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
        <table className="w-full">
          <thead className="bg-cyan-400 border-b-2 border-black">
            <tr>
              <th className="px-4 py-3 text-left font-semibold text-sm">Type</th>
              <th className="px-4 py-3 text-left font-semibold text-sm">
                <SortButton field="contentTitle">Content</SortButton>
              </th>
              <th className="px-4 py-3 text-left font-semibold text-sm">
                <SortButton field="reporterUsername">Reporter</SortButton>
              </th>
              <th className="px-4 py-3 text-left font-semibold text-sm">
                <SortButton field="reason">Reason</SortButton>
              </th>
              <th className="px-4 py-3 text-left font-semibold text-sm">Details</th>
              <th className="px-4 py-3 text-left font-semibold text-sm">
                <SortButton field="createdAt">Date</SortButton>
              </th>
              <th className="px-4 py-3 text-left font-semibold text-sm">Actions</th>
            </tr>
          </thead>
          <tbody>
            {sortedReports.length > 0 ? (
              sortedReports.map((report: AdminReport, idx: number) => (
                <tr key={report.id} className={idx % 2 === 0 ? 'bg-yellow-50' : 'bg-white'}>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-1 border-2 border-black font-semibold text-xs inline-flex items-center gap-1 ${
                      report.reportableType === 'POST' ? 'bg-blue-200' : 'bg-purple-200'
                    }`}>
                      {getTypeIcon(report.reportableType)}
                      {report.reportableType}
                    </span>
                  </td>
                  <td className="px-4 py-3 font-bold max-w-xs">
                    <Link 
                      to={getViewLink(report)} 
                      className="text-blue-600 hover:underline line-clamp-2"
                    >
                      {report.contentTitle}
                    </Link>
                    {report.contentPreview && (
                      <p className="text-xs text-gray-500 mt-1 line-clamp-1">{report.contentPreview}</p>
                    )}
                  </td>
                  <td className="px-4 py-3">
                    <Link 
                      to={`/admin/users?search=${report.reporterUsername}`} 
                      className="text-blue-600 hover:underline font-semibold"
                    >
                      {report.reporterUsername}
                    </Link>
                  </td>
                  <td className="px-4 py-3">
                    <span className="px-2 py-1 bg-orange-200 border-2 border-black font-semibold text-sm inline-flex items-center gap-1">
                      <span>{getReasonIcon(report.reason)}</span>
                      <span>{report.reasonDisplayName}</span>
                    </span>
                  </td>
                  <td className="px-4 py-3 text-sm max-w-xs">
                    {report.additionalDetails ? (
                      <span className="line-clamp-2 text-gray-600" title={report.additionalDetails}>
                        {report.additionalDetails}
                      </span>
                    ) : (
                      <span className="text-gray-400 italic">No details</span>
                    )}
                  </td>
                  <td className="px-4 py-3 text-sm">{new Date(report.createdAt).toLocaleDateString()}</td>
                  <td className="px-4 py-3">
                    <div className="relative" ref={openDropdown === report.id ? dropdownRef : null}>
                      <button
                        onClick={() => setOpenDropdown(openDropdown === report.id ? null : report.id)}
                        className="p-2 bg-gray-200 border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] hover:bg-gray-300 active:translate-x-0.5 active:translate-y-0.5 active:shadow-none transition-all"
                      >
                        <MoreVertical className="h-4 w-4" />
                      </button>
                      
                      {openDropdown === report.id && (
                        <div className="absolute right-0 top-full mt-1 z-50 bg-white border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] min-w-[180px]">
                          <Link
                            to={getViewLink(report)}
                            className="flex items-center gap-2 px-4 py-2 hover:bg-blue-100 font-semibold text-sm border-b border-gray-200"
                            onClick={() => setOpenDropdown(null)}
                          >
                            <Eye className="h-4 w-4" />
                            View {report.reportableType === 'POST' ? 'Post' : 'Comment'}
                          </Link>
                          {deleteConfirm === report.reportableId ? (
                            <div className="p-2 bg-red-50">
                              <p className="text-xs font-semibold mb-2 text-red-700">
                                Delete this {report.reportableType.toLowerCase()}?
                              </p>
                              <div className="flex gap-2">
                                <button
                                  onClick={() => handleDelete(report)}
                                  className="flex-1 px-2 py-1 bg-red-500 text-white border-2 border-black font-bold text-xs hover:bg-red-600"
                                >
                                  Yes
                                </button>
                                <button
                                  onClick={() => setDeleteConfirm(null)}
                                  className="flex-1 px-2 py-1 bg-gray-300 border-2 border-black font-bold text-xs hover:bg-gray-400"
                                >
                                  No
                                </button>
                              </div>
                            </div>
                          ) : (
                            <button
                              onClick={() => setDeleteConfirm(report.reportableId)}
                              className="flex items-center gap-2 px-4 py-2 hover:bg-red-100 font-semibold text-sm w-full text-left text-red-600"
                            >
                              <Trash2 className="h-4 w-4" />
                              Delete {report.reportableType === 'POST' ? 'Post' : 'Comment'}
                            </button>
                          )}
                        </div>
                      )}
                    </div>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={7} className="px-4 py-8 text-center text-gray-500">No reports found</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Mobile Card View */}
      <div className="lg:hidden space-y-4">
        {sortedReports.length > 0 ? (
          sortedReports.map((report: AdminReport) => (
            <AdminMobileCard
              key={report.id}
              id={report.reportableId}
              header={
                <div className="mb-3">
                  <div className="flex items-center gap-2 mb-2">
                    <span className={`px-2 py-1 border-2 border-black font-semibold text-xs inline-flex items-center gap-1 ${
                      report.reportableType === 'POST' ? 'bg-blue-200' : 'bg-purple-200'
                    }`}>
                      {getTypeIcon(report.reportableType)}
                      {report.reportableType}
                    </span>
                  </div>
                  <Link 
                    to={getViewLink(report)}
                    className="font-bold text-lg line-clamp-2 text-blue-600 hover:underline"
                  >
                    {report.contentTitle}
                  </Link>
                  {report.contentPreview && (
                    <p className="text-sm text-gray-500 mt-1 line-clamp-2">{report.contentPreview}</p>
                  )}
                  <div className="flex items-center gap-2 mt-1 flex-wrap">
                    <span className="text-sm text-gray-600">reported by</span>
                    <Link 
                      to={`/admin/users?search=${report.reporterUsername}`} 
                      className="text-blue-600 hover:underline font-semibold text-sm"
                    >
                      {report.reporterUsername}
                    </Link>
                    <span className="text-sm text-gray-500">• {new Date(report.createdAt).toLocaleDateString()}</span>
                  </div>
                  <div className="mt-2">
                    <span className="px-2 py-1 bg-orange-200 border-2 border-black font-semibold text-sm inline-flex items-center gap-1">
                      <span>{getReasonIcon(report.reason)}</span>
                      <span>{report.reasonDisplayName}</span>
                    </span>
                  </div>
                  {report.additionalDetails && (
                    <p className="mt-2 text-sm text-gray-600 line-clamp-3">{report.additionalDetails}</p>
                  )}
                </div>
              }
              stats={[]}
              viewLink={getViewLink(report)}
              deleteConfirm={deleteConfirm}
              onDeleteClick={setDeleteConfirm}
              onDeleteConfirm={handleMobileDelete}
              onDeleteCancel={() => setDeleteConfirm(null)}
              deleteLabel={`Delete ${report.reportableType === 'POST' ? 'Post' : 'Comment'}`}
            />
          ))
        ) : (
          <div className="bg-white border-2 border-black p-8 text-center text-gray-500 shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
            No reports found
          </div>
        )}
      </div>

      <div className="text-sm font-semibold text-gray-600">
        Showing {sortedReports.length} of {data?.page?.totalElements || 0} reports
      </div>

      {/* Pagination Controls */}
      {data?.page && data.page.totalPages > 1 && (
        <div className="flex items-center justify-center gap-2">
          <button
            onClick={() => setPage(p => Math.max(0, p - 1))}
            disabled={page === 0}
            className="flex items-center gap-1 px-2 py-1 text-sm bg-cyan-400 border-2 border-black font-semibold shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] hover:bg-cyan-500 active:translate-x-0.5 active:translate-y-0.5 active:shadow-none transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:bg-cyan-400"
          >
            <ChevronLeft className="h-3 w-3" />
            Prev
          </button>
          <span className="px-2 py-1 text-sm bg-white border-2 border-black font-semibold">
            {page + 1} / {data.page.totalPages}
          </span>
          <button
            onClick={() => setPage(p => Math.min(data.page.totalPages - 1, p + 1))}
            disabled={page >= data.page.totalPages - 1}
            className="flex items-center gap-1 px-2 py-1 text-sm bg-cyan-400 border-2 border-black font-semibold shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] hover:bg-cyan-500 active:translate-x-0.5 active:translate-y-0.5 active:shadow-none transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:bg-cyan-400"
          >
            Next
            <ChevronRight className="h-3 w-3" />
          </button>
        </div>
      )}
    </div>
  )
}
