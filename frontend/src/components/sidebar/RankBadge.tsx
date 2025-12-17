
import { Crown, Medal, Award, Star, Zap } from 'lucide-react'

export const RankBadge = ({ rank }: { rank: number }) => {
  // Common container with neo-brutalist styles
  const baseStyles = "relative flex-shrink-0 w-10 h-10 flex items-center justify-center border-2 border-black shadow-[3px_3px_0px_0px_rgba(0,0,0,1)] transition-transform duration-200"
  
  // Superscript number badge style
  const superscriptStyles = "absolute -top-2 -right-2 w-5 h-5 flex items-center justify-center border-2 border-black rounded-full text-[10px] font-black z-20"

  // Icon common props
  const iconProps = {
    strokeWidth: 2,
    className: "w-6 h-6 text-black drop-shadow-sm transition-transform group-hover:scale-110 duration-200"
  }

  if (rank === 1) {
    return (
      <div className={`${baseStyles} bg-yellow-400 rotate-[-4deg] group-hover:rotate-0`}>
        <Crown {...iconProps} className={`${iconProps.className} fill-yellow-100`} />
        <div className={`${superscriptStyles} bg-red-500 text-white`}>1</div>
      </div>
    )
  }

  if (rank === 2) {
    return (
      <div className={`${baseStyles} bg-slate-300 rotate-[3deg] group-hover:rotate-0`}>
        <Medal {...iconProps} className={`${iconProps.className} fill-slate-100`} />
        <div className={`${superscriptStyles} bg-blue-600 text-white`}>2</div>
      </div>
    )
  }

  if (rank === 3) {
    return (
      <div className={`${baseStyles} bg-orange-300 rotate-[-2deg] group-hover:rotate-0`}>
        <Award {...iconProps} className={`${iconProps.className} fill-orange-100`} />
        <div className={`${superscriptStyles} bg-green-600 text-white`}>3</div>
      </div>
    )
  }

  if (rank === 4) {
    return (
      <div className={`${baseStyles} bg-pink-300 rotate-[2deg] group-hover:rotate-0`}>
        <Star {...iconProps} className={`${iconProps.className} fill-pink-100`} />
        <div className={`${superscriptStyles} bg-purple-700 text-white`}>4</div>
      </div>
    )
  }

  if (rank === 5) {
    return (
      <div className={`${baseStyles} bg-emerald-300 rotate-[-2deg] group-hover:rotate-0`}>
        <Zap {...iconProps} className={`${iconProps.className} fill-emerald-100`} />
        <div className={`${superscriptStyles} bg-yellow-600 text-white`}>5</div>
      </div>
    )
  }

  // Fallback
  return (
    <div className={`${baseStyles} bg-white group-hover:rotate-0`}>
      <span className="font-black text-xs text-black/50">#{rank}</span>
    </div>
  )
}
