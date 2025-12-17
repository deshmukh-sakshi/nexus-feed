import { SiteBranding } from './SiteBranding'
import { UserAchievements } from './UserAchievements'
import { SidebarFooter } from './SidebarFooter'

export const LeftSidebar = () => {
  return (
    <aside className="sticky top-20 space-y-4 h-fit">
      <SiteBranding />
      <UserAchievements />
      <SidebarFooter />
    </aside>
  )
}
