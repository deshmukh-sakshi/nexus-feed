export const SiteBranding = () => {
  return (
    <div className="p-4 border-2 border-black bg-teal-200 dark:bg-teal-900 shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)]">
      <div className="flex flex-col items-center text-center">
        <div className="p-3 bg-white border-2 border-black shadow-[3px_3px_0px_0px_rgba(0,0,0,1)] mb-3">
          <img
            src="/logo.svg"
            alt="Nexus Feed"
            className="h-12 w-12"
          />
        </div>
        <h1 className="text-xl font-black mb-2">Nexus Feed</h1>
        <p className="text-sm font-medium text-black/80 dark:text-white/80">
          A Community-Driven News Hub. Discover and discuss the most relevant news and information, as curated by the users themselves.
        </p>
      </div>
    </div>
  )
}
