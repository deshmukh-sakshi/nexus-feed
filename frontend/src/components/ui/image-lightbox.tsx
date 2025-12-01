import { useEffect, useCallback } from 'react'
import { X, ChevronLeft, ChevronRight, Loader2 } from 'lucide-react'
import { cn } from '@/lib/utils'
import { getOptimizedImageUrl } from '@/lib/cloudinary'
import { useState } from 'react'

interface ImageLightboxProps {
  images: string[]
  initialIndex: number
  isOpen: boolean
  onClose: () => void
  title?: string
}

// Helper to check if image is cached
const isImageCached = (src: string): boolean => {
  const img = new Image()
  img.src = src
  return img.complete
}

export const ImageLightbox = ({
  images,
  initialIndex,
  isOpen,
  onClose,
  title,
}: ImageLightboxProps) => {
  const [currentIndex, setCurrentIndex] = useState(initialIndex)
  const [imageLoading, setImageLoading] = useState(false)

  // Get the current image URL
  const currentImageUrl = getOptimizedImageUrl(images[currentIndex] || '', {
    width: 1920,
    quality: 'auto:best',
    crop: 'limit',
    dpr: 2,
  })

  // Reset to initial index when opening and check cache
  useEffect(() => {
    if (isOpen) {
      setCurrentIndex(initialIndex)
      const url = getOptimizedImageUrl(images[initialIndex] || '', {
        width: 1920,
        quality: 'auto:best',
        crop: 'limit',
        dpr: 2,
      })
      // Only show loading if image is not cached
      setImageLoading(!isImageCached(url))
    }
  }, [isOpen, initialIndex, images])

  // Check cache when changing images
  useEffect(() => {
    if (isOpen && images[currentIndex]) {
      setImageLoading(!isImageCached(currentImageUrl))
    }
  }, [currentIndex, currentImageUrl, isOpen, images])

  const handlePrev = useCallback((e: React.MouseEvent) => {
    e.stopPropagation()
    if (currentIndex > 0) {
      setCurrentIndex(prev => prev - 1)
    }
  }, [currentIndex])

  const handleNext = useCallback((e: React.MouseEvent) => {
    e.stopPropagation()
    if (currentIndex < images.length - 1) {
      setCurrentIndex(prev => prev + 1)
    }
  }, [currentIndex, images.length])

  // Keyboard navigation
  useEffect(() => {
    if (!isOpen) return

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        onClose()
      } else if (e.key === 'ArrowLeft' && currentIndex > 0) {
        setCurrentIndex(prev => prev - 1)
      } else if (e.key === 'ArrowRight' && currentIndex < images.length - 1) {
        setCurrentIndex(prev => prev + 1)
      }
    }

    document.addEventListener('keydown', handleKeyDown)
    return () => document.removeEventListener('keydown', handleKeyDown)
  }, [isOpen, onClose, currentIndex, images.length])

  // Prevent body scroll when open
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden'
    } else {
      document.body.style.overflow = ''
    }
    return () => {
      document.body.style.overflow = ''
    }
  }, [isOpen])

  if (!isOpen) return null

  return (
    <div 
      className="fixed inset-0 z-[100] bg-gray-500/70 backdrop-blur-sm flex items-center justify-center"
      onClick={onClose}
    >
      {/* Close button */}
      <button
        onClick={onClose}
        className="absolute top-4 right-4 z-10 w-10 h-10 rounded-full bg-black/50 hover:bg-black/70 text-white flex items-center justify-center transition-colors cursor-pointer"
      >
        <X className="h-6 w-6" />
      </button>

      {/* Left arrow */}
      {currentIndex > 0 && (
        <button
          onClick={handlePrev}
          className="absolute left-4 top-1/2 -translate-y-1/2 z-10 w-12 h-12 rounded-full bg-black/50 hover:bg-black/70 text-white flex items-center justify-center transition-colors cursor-pointer"
        >
          <ChevronLeft className="h-8 w-8" />
        </button>
      )}

      {/* Right arrow */}
      {currentIndex < images.length - 1 && (
        <button
          onClick={handleNext}
          className="absolute right-4 top-1/2 -translate-y-1/2 z-10 w-12 h-12 rounded-full bg-black/50 hover:bg-black/70 text-white flex items-center justify-center transition-colors cursor-pointer"
        >
          <ChevronRight className="h-8 w-8" />
        </button>
      )}

      {/* Image container */}
      <div 
        className="relative max-w-[90vw] max-h-[85vh] flex items-center justify-center"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Loading spinner */}
        {imageLoading && (
          <div className="absolute inset-0 flex items-center justify-center">
            <Loader2 className="h-10 w-10 animate-spin text-white/60" />
          </div>
        )}
        
        <img
          src={currentImageUrl}
          alt={title || 'Image'}
          className={cn(
            "max-w-full max-h-[85vh] object-contain transition-opacity duration-200",
            imageLoading ? "opacity-0" : "opacity-100"
          )}
          onLoad={() => setImageLoading(false)}
        />
      </div>

      {/* Bottom bar with title and indicators */}
      <div 
        className="absolute bottom-4 left-1/2 -translate-x-1/2 bg-neutral-900/90 rounded-lg px-4 py-3 max-w-2xl"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Title */}
        {title && (
          <p className="text-white text-sm mb-2 text-center line-clamp-2">
            {title}
          </p>
        )}
        
        {/* Dot indicators */}
        {images.length > 1 && (
          <div className="flex items-center justify-center gap-2">
            {images.map((_, index) => (
              <button
                key={index}
                onClick={(e) => {
                  e.stopPropagation()
                  setCurrentIndex(index)
                }}
                className={cn(
                  "rounded-full transition-all cursor-pointer",
                  index === currentIndex 
                    ? "w-2.5 h-2.5 bg-white" 
                    : "w-2 h-2 bg-white/50 hover:bg-white/70"
                )}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
