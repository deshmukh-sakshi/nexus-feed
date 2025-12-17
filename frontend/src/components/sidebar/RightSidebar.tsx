import { TrendingTags } from './TrendingTags'

export const RightSidebar = () => {
  return (
    <aside className="sticky top-20 space-y-4 h-fit">
      <TrendingTags limit={10} />
    </aside>
  )
}
