import { Link } from 'react-router-dom'
import { Trash2, ExternalLink } from 'lucide-react'
import type { ReactNode } from 'react'

type StatItem = {
  value: number | string
  label: string
  color: 'green' | 'blue' | 'yellow' | 'red' | 'dynamic'
  dynamicPositive?: boolean
}

type AdminMobileCardProps = {
  id: string
  header: ReactNode
  stats?: StatItem[]
  viewLink?: string
  deleteConfirm: string | null
  onDeleteClick: (id: string) => void
  onDeleteConfirm: (id: string) => void
  onDeleteCancel: () => void
}

const statColorClasses = {
  green: 'bg-green-100',
  blue: 'bg-blue-100',
  yellow: 'bg-yellow-100',
  red: 'bg-red-100',
}

export const AdminMobileCard = ({
  id,
  header,
  stats,
  viewLink,
  deleteConfirm,
  onDeleteClick,
  onDeleteConfirm,
  onDeleteCancel,
}: AdminMobileCardProps) => {
  const isConfirming = deleteConfirm === id

  return (
    <div className="bg-white border-2 border-black p-4 shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
      {header}

      {stats && stats.length > 0 && (
        <div className={`grid grid-cols-${Math.min(stats.length, 3)} gap-2 mb-4 text-center`}>
          {stats.map((stat, idx) => {
            const bgColor = stat.color === 'dynamic'
              ? stat.dynamicPositive ? 'bg-green-100' : 'bg-red-100'
              : statColorClasses[stat.color]
            return (
              <div key={idx} className={`${bgColor} border-2 border-black p-2`}>
                <div className="font-bold text-lg">{stat.value}</div>
                <div className="text-xs text-gray-600">{stat.label}</div>
              </div>
            )
          })}
        </div>
      )}

      <div className="flex justify-end gap-2">
        {viewLink && (
          <Link
            to={viewLink}
            target="_blank"
            className="flex items-center gap-2 px-3 py-2 bg-blue-400 border-2 border-black font-semibold text-sm shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] hover:bg-blue-500 active:translate-x-0.5 active:translate-y-0.5 active:shadow-none transition-all"
          >
            <ExternalLink className="h-4 w-4" />
            View
          </Link>
        )}
        {isConfirming ? (
          <>
            <button
              onClick={() => onDeleteConfirm(id)}
              className="px-3 py-2 bg-red-500 text-white border-2 border-black font-semibold text-sm shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] hover:bg-red-600 active:translate-x-0.5 active:translate-y-0.5 active:shadow-none transition-all"
            >
              {viewLink ? 'Confirm' : 'Confirm Delete'}
            </button>
            <button
              onClick={onDeleteCancel}
              className="px-3 py-2 bg-gray-300 border-2 border-black font-semibold text-sm shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] hover:bg-gray-400 active:translate-x-0.5 active:translate-y-0.5 active:shadow-none transition-all"
            >
              Cancel
            </button>
          </>
        ) : (
          <button
            onClick={() => onDeleteClick(id)}
            className="flex items-center gap-2 px-3 py-2 bg-red-400 border-2 border-black font-semibold text-sm shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] hover:bg-red-500 active:translate-x-0.5 active:translate-y-0.5 active:shadow-none transition-all"
          >
            <Trash2 className="h-4 w-4" />
            Delete
          </button>
        )}
      </div>
    </div>
  )
}
