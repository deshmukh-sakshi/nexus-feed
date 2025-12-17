import { TopUsers } from './TopUsers'
import { TrendingTags } from './TrendingTags'
import { StickySidebarWrapper } from './StickySidebarWrapper'

export const RightSidebar = () => {
  return (
    <StickySidebarWrapper className="space-y-4">
      <TrendingTags limit={5} />
      <TopUsers limit={5} />
    </StickySidebarWrapper>
  )
}
