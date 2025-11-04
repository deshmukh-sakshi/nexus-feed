import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { cn } from '@/lib/utils'

interface UserAvatarProps {
  username: string
  profileImageUrl?: string
  className?: string
  size?: 'sm' | 'md' | 'lg'
}

// Generate consistent color based on username
const generateAvatarGradient = (username: string): string => {
  // Create a hash from the username
  let hash = 0
  for (let i = 0; i < username.length; i++) {
    hash = username.charCodeAt(i) + ((hash << 5) - hash)
    hash = hash & hash // Convert to 32-bit integer
  }

  // Generate two colors for gradient
  const hue1 = Math.abs(hash % 360)
  const hue2 = (hue1 + 60) % 360

  return `linear-gradient(135deg, hsl(${hue1}, 65%, 55%), hsl(${hue2}, 65%, 45%))`
}

// Extract initials from username
const getInitials = (username: string): string => {
  if (!username) return '?'
  
  // Remove any special characters and split by common separators
  const cleaned = username.replace(/[^a-zA-Z0-9\s-_]/g, '')
  const parts = cleaned.split(/[\s-_]+/).filter(Boolean)
  
  if (parts.length === 0) {
    return username.charAt(0).toUpperCase()
  }
  
  if (parts.length === 1) {
    return parts[0].charAt(0).toUpperCase()
  }
  
  // Take first letter of first two parts
  return (parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase()
}

const sizeClasses = {
  sm: 'h-8 w-8 text-xs',
  md: 'h-10 w-10 text-sm',
  lg: 'h-16 w-16 text-xl',
}

export const UserAvatar = ({
  username,
  profileImageUrl,
  className,
  size = 'md',
}: UserAvatarProps) => {
  const initials = getInitials(username)
  const gradient = generateAvatarGradient(username)

  return (
    <Avatar className={cn(sizeClasses[size], className)}>
      {profileImageUrl && <AvatarImage src={profileImageUrl} alt={username} />}
      <AvatarFallback
        style={{ background: gradient }}
        className="text-white font-semibold"
      >
        {initials}
      </AvatarFallback>
    </Avatar>
  )
}
