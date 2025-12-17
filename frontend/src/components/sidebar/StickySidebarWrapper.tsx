
import { useEffect, useRef } from 'react'

export const StickySidebarWrapper = ({ children, className = "" }: { children: React.ReactNode, className?: string }) => {
  const sidebarRef = useRef<HTMLDivElement>(null)
  
  // Configuration
  const headerOffset = 84 // Header height + padding
  const bottomOffset = 20 // Bottom padding
  
  useEffect(() => {
    const sidebar = sidebarRef.current
    if (!sidebar) return

    let lastScrollY = window.scrollY
    let currentStickyTop = headerOffset // Start at the top position
    
    const handleScroll = () => {
      const scrollY = window.scrollY
      const viewportHeight = window.innerHeight
      const sidebarHeight = sidebar.offsetHeight
      
      const delta = scrollY - lastScrollY
      lastScrollY = scrollY

      // If sidebar is shorter than viewport (plus offsets), just stick to top
      // We calculate "needed space" as height + header offset
      if (sidebarHeight + headerOffset < viewportHeight) {
        sidebar.style.position = 'sticky'
        sidebar.style.top = `${headerOffset}px`
        return
      }

      // Sidebar is taller than viewport. We need the "dual-direction sticky" logic.
      // Calculate limits
      // MaxTop: The default top position (stick to header)
      const maxTop = headerOffset
      
      // MinTop: The position where the bottom of sidebar sticks to bottom of viewport
      // Formula: viewportHeight - sidebarHeight - bottomOffset
      const minTop = viewportHeight - sidebarHeight - bottomOffset

      // Update currentStickyTop based on scroll delta
      // If scrolling DOWN (delta > 0), we want the top to move UP (negative direction)
      // If scrolling UP (delta < 0), we want the top to move DOWN (positive direction)
      currentStickyTop -= delta

      // Clamp the value
      // It cannot go higher than maxTop (84px)
      // It cannot go lower than minTop (negative value)
      if (currentStickyTop > maxTop) currentStickyTop = maxTop
      if (currentStickyTop < minTop) currentStickyTop = minTop

      // Apply
      sidebar.style.position = 'sticky'
      sidebar.style.top = `${currentStickyTop}px`
    }

    // Initial check
    handleScroll()
    
    window.addEventListener('scroll', handleScroll, { passive: true })
    window.addEventListener('resize', handleScroll)
    
    // We observe the sidebar size to re-calculate bounds if content loading changes height
    const resizeObserver = new ResizeObserver(() => handleScroll())
    resizeObserver.observe(sidebar)

    return () => {
      window.removeEventListener('scroll', handleScroll)
      window.removeEventListener('resize', handleScroll)
      resizeObserver.disconnect()
    }
  }, [children])

  return (
    <div 
      ref={sidebarRef} 
      className={className}
      style={{ position: 'sticky', top: headerOffset }} // Default initial style
    >
      {children}
    </div>
  )
}
