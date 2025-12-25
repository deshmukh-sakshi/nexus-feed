import { Link } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import type { LucideIcon } from 'lucide-react'

type MobileNavItemProps = {
  to?: string
  icon?: LucideIcon
  label: string
  color: 'teal' | 'green' | 'yellow' | 'purple' | 'red' | 'pink' | 'blue'
  onClick?: () => void
}

const colorClasses = {
  teal: 'bg-teal-400 hover:bg-teal-500',
  green: 'bg-green-400 hover:bg-green-500',
  yellow: 'bg-yellow-400 hover:bg-yellow-500',
  purple: 'bg-purple-400 hover:bg-purple-500',
  red: 'bg-red-400 hover:bg-red-500',
  pink: 'bg-pink-400 hover:bg-pink-500',
  blue: 'bg-blue-500 hover:bg-blue-600',
}

export const MobileNavItem = ({ to, icon: Icon, label, color, onClick }: MobileNavItemProps) => {
  const buttonContent = (
    <Button
      onClick={!to ? onClick : undefined}
      className={`w-full ${colorClasses[color]} text-black border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] rounded-none font-bold justify-start`}
    >
      {Icon && <Icon className="mr-2 h-5 w-5" />}
      {label}
    </Button>
  )

  if (to) {
    return (
      <Link to={to} onClick={onClick}>
        {buttonContent}
      </Link>
    )
  }

  return buttonContent
}
