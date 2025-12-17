import { SiteBranding } from './SiteBranding'
import { UserAchievements } from './UserAchievements'
import { SidebarFooter } from './SidebarFooter'
import { StickySidebarWrapper } from './StickySidebarWrapper'

export const LeftSidebar = () => {
  return (
    <StickySidebarWrapper className="space-y-4">
      <SiteBranding />
      <UserAchievements />
      <SidebarFooter />
    </StickySidebarWrapper>
  )
}
