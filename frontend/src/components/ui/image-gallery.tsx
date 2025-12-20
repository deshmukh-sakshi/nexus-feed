import { useState, useEffect } from 'react'
import { ChevronLeft, ChevronRight, Loader2 } from 'lucide-react'
import { cn } from '@/lib/utils'
import { getOptimizedImageUrl, isImageCached } from '@/lib/cloudinary'
import { ImageLightbox } from './image-lightbox'

interface ImageGalleryProps {
  images: string[]
  title?: string
  height?: number
  className?: string
  onImageClick?: (e: React.MouseEvent) => void // For custom click handling (e.g., stopPropagation in PostCard)
}

export const ImageGallery = ({ 
  images, 
  title,
  height = 400,
  className,
  onImageClick,
}: ImageGalleryProps) => {
  const [currentIndex, setCurrentIndex] = useState(0)
  const [imageLoading, setImageLoading] = useState(true)
  const [lightboxOpen, setLightboxOpen] = useState(false)

  // Reset loading state when image index changes
  useEffect(() => {
    const url = getOptimizedImageUrl(images[currentIndex], {
      width: 1920,
      quality: 'auto:best',
      crop: 'limit',
      dpr: 2,
    })
    setImageLoading(!isImageCached(url))
  }, [currentIndex, images])

  if (!images || images.length === 0) return null

  const currentImageUrl = getOptimizedImageUrl(images[currentIndex], {
    width: 1920,
    quality: 'auto:best',
    crop: 'limit',
    dpr: 2,
  })

  const handleContainerClick = (e: React.MouseEvent) => {
    onImageClick?.(e)
    setLightboxOpen(true)
  }

  return (
    <>
      <div className={cn("relative", className)}>
        <div 
          className="relative w-full bg-neutral-200 dark:bg-neutral-900 rounded-xl border border-neutral-300 dark:border-black overflow-hidden cursor-pointer"
          style={{ height }}
          onClick={handleContainerClick}
        >
          {/* Blurred background */}
          <img
            src={currentImageUrl}
            alt=""
            className="absolute inset-0 w-full h-full object-cover blur-2xl scale-110 opacity-50 dark:opacity-30"
          />
          
          {/* Loading spinner */}
          {imageLoading && (
            <div className="absolute inset-0 flex items-center justify-center z-10">
              <Loader2 className="h-8 w-8 animate-spin text-black/60" />
            </div>
          )}
          
          {/* Main image */}
          <img
            src={currentImageUrl}
            alt={title || 'Image'}
            className={cn(
              "relative w-full h-full object-contain drop-shadow-md transition-opacity duration-200",
              imageLoading ? "opacity-0" : "opacity-100"
            )}
            onLoad={() => setImageLoading(false)}
          />
          
          {/* Navigation arrows */}
          {images.length > 1 && (
            <>
              {currentIndex > 0 && (
                <button
                  onClick={(e) => {
                    e.stopPropagation()
                    setCurrentIndex(prev => prev - 1)
                  }}
                  className="absolute left-3 top-1/2 -translate-y-1/2 z-20 w-10 h-10 rounded-full bg-black/60 hover:bg-black/80 text-white flex items-center justify-center transition-all cursor-pointer"
                >
                  <ChevronLeft className="h-6 w-6" />
                </button>
              )}
              
              {currentIndex < images.length - 1 && (
                <button
                  onClick={(e) => {
                    e.stopPropagation()
                    setCurrentIndex(prev => prev + 1)
                  }}
                  className="absolute right-3 top-1/2 -translate-y-1/2 z-20 w-10 h-10 rounded-full bg-black/60 hover:bg-black/80 text-white flex items-center justify-center transition-all cursor-pointer"
                >
                  <ChevronRight className="h-6 w-6" />
                </button>
              )}
              
              {/* Dot indicators */}
              <div className="absolute bottom-3 left-1/2 -translate-x-1/2 z-20 flex items-center gap-1.5 bg-black/50 px-2.5 py-1.5 rounded-full">
                {(() => {
                  const total = images.length
                  const current = currentIndex
                  const maxDots = 5
                  
                  let startIdx = Math.max(0, current - Math.floor(maxDots / 2))
                  let endIdx = startIdx + maxDots
                  
                  if (endIdx > total) {
                    endIdx = total
                    startIdx = Math.max(0, endIdx - maxDots)
                  }
                  
                  const dots = []
                  
                  if (startIdx > 0) {
                    dots.push(
                      <span key="left-ellipsis" className="w-1 h-1 rounded-full bg-white/40" />
                    )
                  }
                  
                  for (let i = startIdx; i < endIdx; i++) {
                    const isActive = i === current
                    const distanceFromCurrent = Math.abs(i - current)
                    
                    dots.push(
                      <button
                        key={i}
                        onClick={(e) => {
                          e.stopPropagation()
                          setCurrentIndex(i)
                        }}
                        className={cn(
                          "rounded-full transition-all cursor-pointer",
                          isActive 
                            ? "w-2 h-2 bg-white" 
                            : distanceFromCurrent === 1
                              ? "w-1.5 h-1.5 bg-white/70 hover:bg-white/90"
                              : "w-1 h-1 bg-white/50 hover:bg-white/70"
                        )}
                      />
                    )
                  }
                  
                  if (endIdx < total) {
                    dots.push(
                      <span key="right-ellipsis" className="w-1 h-1 rounded-full bg-white/40" />
                    )
                  }
                  
                  return dots
                })()}
              </div>
            </>
          )}
        </div>
      </div>

      <ImageLightbox
        images={images}
        initialIndex={currentIndex}
        isOpen={lightboxOpen}
        onClose={() => setLightboxOpen(false)}
        title={title}
      />
    </>
  )
}
