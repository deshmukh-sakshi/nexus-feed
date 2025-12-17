import { SiteBranding } from './SiteBranding'
import { UserAchievements } from './UserAchievements'

export const LeftSidebar = () => {
  return (
    <aside className="sticky top-20 space-y-4 h-fit">
      <SiteBranding />
      <UserAchievements />
    </aside>
  )
}
