import { useCallback, useState, useEffect, useRef } from 'react'
import { useDropzone } from 'react-dropzone'
import { X, Upload, Loader2, ImageIcon, ChevronLeft, ChevronRight, GripVertical, ZoomIn } from 'lucide-react'
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  type DragEndEvent,
} from '@dnd-kit/core'
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  useSortable,
  horizontalListSortingStrategy,
} from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import { Button } from './button'
import { Dialog, DialogContent } from './dialog'
import { uploadToCloudinary, getOptimizedImageUrl, type UploadProgress } from '@/lib/cloudinary'
import { toast } from 'sonner'
import { cn } from '@/lib/utils'

interface ImageUploadProps {
  value: string[]
  onChange: (urls: string[]) => void
  maxFiles?: number
  maxSizeMB?: number
  disabled?: boolean
  onUploadingChange?: (isUploading: boolean) => void
  onEnterPress?: () => void
}

interface UploadingFile {
  id: string
  file: File
  preview: string
  progress: number
}

interface SortableImageProps {
  url: string
  index: number
  onRemove: () => void
  onPreview: () => void
  disabled: boolean
}

const SortableImage = ({ url, index, onRemove, onPreview, disabled }: SortableImageProps) => {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: url })

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  }

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={cn(
        "relative flex-shrink-0 w-[200px] h-[150px] rounded-lg overflow-hidden border-2 border-black bg-muted group",
        isDragging && "opacity-50 z-50"
      )}
    >
      <img
        src={getOptimizedImageUrl(url, { width: 400, quality: 'auto:good' })}
        alt={`Upload ${index + 1}`}
        className="w-full h-full object-cover"
      />
      
      {/* Drag handle */}
      <div
        {...attributes}
        {...listeners}
        className="absolute top-1 left-1 h-6 w-6 flex items-center justify-center bg-black/60 hover:bg-black/80 text-white rounded cursor-grab active:cursor-grabbing"
      >
        <GripVertical className="h-4 w-4" />
      </div>

      {/* Preview button */}
      <Button
        type="button"
        size="sm"
        onClick={onPreview}
        disabled={disabled}
        className="absolute top-1 left-8 h-6 w-6 p-0 bg-black/60 hover:bg-black/80 text-white border-0 rounded"
      >
        <ZoomIn className="h-3 w-3" />
      </Button>

      {/* Remove button */}
      <Button
        type="button"
        size="sm"
        onClick={(e) => {
          e.preventDefault()
          e.stopPropagation()
          onRemove()
        }}
        disabled={disabled}
        className="absolute top-1 right-1 h-6 w-6 p-0 bg-red-500 hover:bg-red-600 text-white border-2 border-black rounded"
      >
        <X className="h-3 w-3" />
      </Button>

      {/* Image number badge */}
      <div className="absolute bottom-1 right-1 bg-black/70 text-white text-xs font-bold px-2 py-0.5 rounded">
        {index + 1}
      </div>
    </div>
  )
}

export const ImageUpload = ({
  value = [],
  onChange,
  maxFiles,
  maxSizeMB = 5,
  disabled = false,
  onUploadingChange,
  onEnterPress,
}: ImageUploadProps) => {
  const [uploading, setUploading] = useState<UploadingFile[]>([])
  const [currentIndex, setCurrentIndex] = useState(0)
  const [previewOpen, setPreviewOpen] = useState(false)

  // Notify parent when uploading state changes
  const isUploading = uploading.length > 0
  const prevIsUploadingRef = useRef(false)
  
  useEffect(() => {
    if (prevIsUploadingRef.current !== isUploading) {
      prevIsUploadingRef.current = isUploading
      onUploadingChange?.(isUploading)
    }
  }, [isUploading, onUploadingChange])
  const [previewIndex, setPreviewIndex] = useState(0)

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8,
      },
    }),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  )

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event

    if (over && active.id !== over.id) {
      const oldIndex = value.indexOf(active.id as string)
      const newIndex = value.indexOf(over.id as string)
      onChange(arrayMove(value, oldIndex, newIndex))
    }
  }

  const onDrop = useCallback(
    async (acceptedFiles: File[]) => {
      if (maxFiles !== undefined) {
        const remainingSlots = maxFiles - value.length - uploading.length
        if (remainingSlots <= 0) {
          toast.error(`Maximum ${maxFiles} images allowed`)
          return
        }
      }

      const filesToUpload = maxFiles !== undefined 
        ? acceptedFiles.slice(0, maxFiles - value.length - uploading.length)
        : acceptedFiles
      
      const newUploading: UploadingFile[] = filesToUpload.map((file, idx) => ({
        id: `uploading-${Date.now()}-${idx}`,
        file,
        preview: URL.createObjectURL(file),
        progress: 0,
      }))
      
      setUploading((prev) => [...prev, ...newUploading])

      // Use array with fixed positions to preserve order
      const uploadedUrls: (string | null)[] = new Array(filesToUpload.length).fill(null)
      let completedCount = 0

      // Upload all files in parallel
      const uploadPromises = filesToUpload.map(async (file, i) => {
        const uploadId = newUploading[i].id

        try {
          const result = await uploadToCloudinary(file, (progress: UploadProgress) => {
            setUploading((prev) =>
              prev.map((u) =>
                u.id === uploadId ? { ...u, progress: progress.percent } : u
              )
            )
          })

          // Store URL at the correct index to preserve order
          uploadedUrls[i] = result.secure_url
          completedCount++
          setUploading((prev) => prev.filter((u) => u.id !== uploadId))
          URL.revokeObjectURL(newUploading[i].preview)
          
          // Only update when all uploads are complete to preserve order
          if (completedCount === filesToUpload.length) {
            const successfulUrls = uploadedUrls.filter((url): url is string => url !== null)
            onChange([...value, ...successfulUrls])
          }
        } catch (error) {
          toast.error(error instanceof Error ? error.message : 'Upload failed')
          completedCount++
          setUploading((prev) => prev.filter((u) => u.id !== uploadId))
          URL.revokeObjectURL(newUploading[i].preview)
          
          // Still update when all uploads are complete (including failed ones)
          if (completedCount === filesToUpload.length) {
            const successfulUrls = uploadedUrls.filter((url): url is string => url !== null)
            if (successfulUrls.length > 0) {
              onChange([...value, ...successfulUrls])
            }
          }
        }
      })

      await Promise.all(uploadPromises)
    },
    [value, onChange, maxFiles, uploading.length]
  )

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'image/*': ['.png', '.jpg', '.jpeg', '.gif', '.webp'],
    },
    maxSize: maxSizeMB * 1024 * 1024,
    disabled: disabled || (maxFiles !== undefined && value.length + uploading.length >= maxFiles),
    onDropRejected: (rejections) => {
      const error = rejections[0]?.errors[0]
      if (error?.code === 'file-too-large') {
        toast.error(`File too large. Maximum size is ${maxSizeMB}MB`)
      } else if (error?.code === 'file-invalid-type') {
        toast.error('Invalid file type. Please upload an image')
      } else {
        toast.error('File rejected')
      }
    },
  })

  const removeImage = (index: number) => {
    onChange(value.filter((_, i) => i !== index))
    if (currentIndex >= value.length - 1 && currentIndex > 0) {
      setCurrentIndex(currentIndex - 1)
    }
  }

  const openPreview = (index: number) => {
    setPreviewIndex(index)
    setPreviewOpen(true)
  }

  const canAddMore = maxFiles === undefined || value.length + uploading.length < maxFiles
  const totalItems = value.length + uploading.length
  const maxVisibleItems = 3
  const canScrollLeft = currentIndex > 0
  const canScrollRight = currentIndex < totalItems - maxVisibleItems

  const scrollLeft = () => {
    setCurrentIndex(Math.max(0, currentIndex - 1))
  }

  const scrollRight = () => {
    setCurrentIndex(Math.min(totalItems - maxVisibleItems, currentIndex + 1))
  }

  return (
    <div className="space-y-3">
      {/* Image slider with navigation */}
      {totalItems > 0 && (
        <div className="relative">
          {/* Navigation arrows - positioned at center of image height (150px / 2 = 75px) */}
          {totalItems > maxVisibleItems && (
            <>
              {canScrollLeft && (
                <button
                  type="button"
                  onClick={scrollLeft}
                  className="absolute left-0 top-[75px] -translate-y-1/2 -translate-x-3 z-10 w-8 h-8 rounded-full bg-black/70 hover:bg-black/90 text-white flex items-center justify-center transition-all shadow-lg"
                >
                  <ChevronLeft className="h-5 w-5" />
                </button>
              )}
              {canScrollRight && (
                <button
                  type="button"
                  onClick={scrollRight}
                  className="absolute right-0 top-[75px] -translate-y-1/2 translate-x-3 z-10 w-8 h-8 rounded-full bg-black/70 hover:bg-black/90 text-white flex items-center justify-center transition-all shadow-lg"
                >
                  <ChevronRight className="h-5 w-5" />
                </button>
              )}
            </>
          )}

          {/* Scrollable container */}
          <div className="overflow-hidden px-1">
            <DndContext
              sensors={sensors}
              collisionDetection={closestCenter}
              onDragEnd={handleDragEnd}
            >
              <SortableContext items={value} strategy={horizontalListSortingStrategy}>
                <div 
                  className="flex gap-3 transition-transform duration-300 ease-out"
                  style={{ transform: `translateX(-${currentIndex * 212}px)` }}
                >
                  {value.map((url, index) => (
                    <SortableImage
                      key={url}
                      url={url}
                      index={index}
                      onRemove={() => removeImage(index)}
                      onPreview={() => openPreview(index)}
                      disabled={disabled}
                    />
                  ))}
                  
                  {/* Uploading previews */}
                  {uploading.map((item, index) => (
                    <div
                      key={item.id}
                      className="relative flex-shrink-0 w-[200px] h-[150px] rounded-lg overflow-hidden border-2 border-black bg-muted"
                    >
                      <img
                        src={item.preview}
                        alt={`Uploading ${index + 1}`}
                        className="w-full h-full object-cover opacity-50"
                      />
                      <div className="absolute inset-0 flex items-center justify-center bg-black/30">
                        <div className="text-center text-white">
                          <Loader2 className="h-6 w-6 animate-spin mx-auto mb-1" />
                          <span className="text-xs font-bold">{item.progress}%</span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </SortableContext>
            </DndContext>
          </div>

          {/* Dot indicators */}
          {totalItems > maxVisibleItems && (
            <div className="flex justify-center gap-1.5 mt-3">
              {Array.from({ length: Math.ceil(totalItems / maxVisibleItems) }).map((_, idx) => (
                <button
                  key={idx}
                  type="button"
                  onClick={() => setCurrentIndex(Math.min(idx * maxVisibleItems, totalItems - maxVisibleItems))}
                  className={cn(
                    "w-2 h-2 rounded-full transition-all",
                    Math.floor(currentIndex / maxVisibleItems) === idx
                      ? "bg-black dark:bg-white"
                      : "bg-black/30 dark:bg-white/30 hover:bg-black/50 dark:hover:bg-white/50"
                  )}
                />
              ))}
            </div>
          )}

          {/* Image count indicator */}
          <div className="text-center text-xs text-muted-foreground mt-2">
            {value.length} image{value.length !== 1 ? 's' : ''} uploaded
            {uploading.length > 0 && ` • ${uploading.length} uploading`}
            {maxFiles && ` • Max ${maxFiles}`}
          </div>
        </div>
      )}

      {/* Dropzone */}
      {canAddMore && (
        <div
          {...getRootProps()}
          onKeyDown={(e) => {
            if (e.key === 'Enter' && !e.shiftKey && onEnterPress) {
              e.preventDefault()
              onEnterPress()
            }
          }}
          tabIndex={0}
          className={cn(
            "border-2 border-dashed border-black rounded-md p-4 text-center cursor-pointer transition-colors hover:bg-muted/50 focus:outline-none focus:ring-2 focus:ring-orange-400 min-h-[100px]",
            isDragActive && "bg-muted border-primary",
            disabled && "opacity-50 cursor-not-allowed"
          )}
        >
          <input {...getInputProps()} />
          <div className="flex flex-col items-center justify-center gap-2 text-muted-foreground h-full min-h-[68px]">
            {isDragActive ? (
              <>
                <ImageIcon className="h-8 w-8" />
                <p className="text-sm font-medium">Drop images here</p>
              </>
            ) : (
              <>
                <Upload className="h-8 w-8" />
                <p className="text-sm font-medium">
                  Drag & drop images or click to browse
                </p>
                <p className="text-xs">
                  PNG, JPG, GIF, WebP up to {maxSizeMB}MB
                </p>
              </>
            )}
          </div>
        </div>
      )}

      {/* Preview Modal */}
      <Dialog open={previewOpen} onOpenChange={setPreviewOpen}>
        <DialogContent className="max-w-4xl p-0 bg-black/95 border-0" showCloseButton={false}>
          <div className="relative">
            {/* Close button */}
            <button
              onClick={() => setPreviewOpen(false)}
              className="absolute top-4 right-4 z-10 w-10 h-10 rounded-full bg-black/60 hover:bg-black/80 text-white flex items-center justify-center transition-all"
            >
              <X className="h-5 w-5" />
            </button>

            {/* Image */}
            {value[previewIndex] && (
              <div className="relative h-[70vh] w-full flex items-center justify-center">
                <img
                  src={getOptimizedImageUrl(value[previewIndex], {
                    width: 1920,
                    quality: 'auto:best',
                    crop: 'limit',
                    dpr: 2,
                  })}
                  alt={`Preview ${previewIndex + 1}`}
                  className="max-w-full max-h-full object-contain"
                />
              </div>
            )}

            {/* Navigation for preview */}
            {value.length > 1 && (
              <>
                {previewIndex > 0 && (
                  <button
                    onClick={() => setPreviewIndex(previewIndex - 1)}
                    className="absolute left-4 top-1/2 -translate-y-1/2 w-12 h-12 rounded-full bg-black/60 hover:bg-black/80 text-white flex items-center justify-center transition-all"
                  >
                    <ChevronLeft className="h-7 w-7" />
                  </button>
                )}
                {previewIndex < value.length - 1 && (
                  <button
                    onClick={() => setPreviewIndex(previewIndex + 1)}
                    className="absolute right-4 top-1/2 -translate-y-1/2 w-12 h-12 rounded-full bg-black/60 hover:bg-black/80 text-white flex items-center justify-center transition-all"
                  >
                    <ChevronRight className="h-7 w-7" />
                  </button>
                )}

                {/* Dot indicators in preview */}
                <div className="absolute bottom-4 left-1/2 -translate-x-1/2 flex items-center gap-2 bg-black/50 px-3 py-2 rounded-full">
                  {value.map((_, idx) => (
                    <button
                      key={idx}
                      onClick={() => setPreviewIndex(idx)}
                      className={cn(
                        "rounded-full transition-all",
                        idx === previewIndex
                          ? "w-2.5 h-2.5 bg-white"
                          : "w-2 h-2 bg-white/50 hover:bg-white/70"
                      )}
                    />
                  ))}
                </div>
              </>
            )}

            {/* Image counter */}
            <div className="absolute top-4 left-4 bg-black/60 text-white text-sm font-bold px-3 py-1.5 rounded-full">
              {previewIndex + 1} / {value.length}
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  )
}
