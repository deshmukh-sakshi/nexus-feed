import { useCallback, useState } from 'react'
import { useDropzone } from 'react-dropzone'
import { X, Upload, Loader2, ImageIcon } from 'lucide-react'
import { Button } from './button'
import { uploadToCloudinary, getOptimizedImageUrl, type UploadProgress } from '@/lib/cloudinary'
import { toast } from 'sonner'

interface ImageUploadProps {
  value: string[]
  onChange: (urls: string[]) => void
  maxFiles?: number
  maxSizeMB?: number
  disabled?: boolean
}

interface UploadingFile {
  file: File
  preview: string
  progress: number
}

export const ImageUpload = ({
  value = [],
  onChange,
  maxFiles = 20,
  maxSizeMB = 5,
  disabled = false,
}: ImageUploadProps) => {
  const [uploading, setUploading] = useState<UploadingFile[]>([])

  const onDrop = useCallback(
    async (acceptedFiles: File[]) => {
      const remainingSlots = maxFiles - value.length - uploading.length
      if (remainingSlots <= 0) {
        toast.error(`Maximum ${maxFiles} images allowed`)
        return
      }

      const filesToUpload = acceptedFiles.slice(0, remainingSlots)
      
      // Create preview entries
      const newUploading: UploadingFile[] = filesToUpload.map((file) => ({
        file,
        preview: URL.createObjectURL(file),
        progress: 0,
      }))
      
      setUploading((prev) => [...prev, ...newUploading])

      // Upload files one by one
      for (let i = 0; i < filesToUpload.length; i++) {
        const file = filesToUpload[i]
        const uploadingIndex = uploading.length + i

        try {
          const result = await uploadToCloudinary(file, (progress: UploadProgress) => {
            setUploading((prev) =>
              prev.map((u, idx) =>
                idx === uploadingIndex ? { ...u, progress: progress.percent } : u
              )
            )
          })

          // Add to value and remove from uploading
          onChange([...value, result.secure_url])
          setUploading((prev) => prev.filter((_, idx) => idx !== uploadingIndex))
          
          // Cleanup preview URL
          URL.revokeObjectURL(newUploading[i].preview)
        } catch (error) {
          toast.error(error instanceof Error ? error.message : 'Upload failed')
          setUploading((prev) => prev.filter((_, idx) => idx !== uploadingIndex))
          URL.revokeObjectURL(newUploading[i].preview)
        }
      }
    },
    [value, onChange, maxFiles, uploading.length]
  )

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'image/*': ['.png', '.jpg', '.jpeg', '.gif', '.webp'],
    },
    maxSize: maxSizeMB * 1024 * 1024,
    disabled: disabled || value.length + uploading.length >= maxFiles,
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
  }

  const canAddMore = value.length + uploading.length < maxFiles

  return (
    <div className="space-y-3">
      {/* Uploaded images grid */}
      {(value.length > 0 || uploading.length > 0) && (
        <div className="grid grid-cols-2 gap-2">
          {value.map((url, index) => (
            <div
              key={url}
              className="relative aspect-video rounded-md overflow-hidden border-2 border-black bg-muted"
            >
              <img
                src={getOptimizedImageUrl(url, { width: 400, quality: 'auto:good' })}
                alt={`Upload ${index + 1}`}
                className="w-full h-full object-cover"
              />
              <Button
                type="button"
                size="sm"
                onClick={() => removeImage(index)}
                disabled={disabled}
                className="absolute top-1 right-1 h-6 w-6 p-0 bg-red-500 hover:bg-red-600 text-white border-2 border-black rounded-none"
              >
                <X className="h-3 w-3" />
              </Button>
            </div>
          ))}
          
          {/* Uploading previews */}
          {uploading.map((item, index) => (
            <div
              key={item.preview}
              className="relative aspect-video rounded-md overflow-hidden border-2 border-black bg-muted"
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
      )}

      {/* Dropzone */}
      {canAddMore && (
        <div
          {...getRootProps()}
          className={`
            border-2 border-dashed border-black rounded-md p-4 text-center cursor-pointer
            transition-colors hover:bg-muted/50
            ${isDragActive ? 'bg-muted border-primary' : ''}
            ${disabled ? 'opacity-50 cursor-not-allowed' : ''}
          `}
        >
          <input {...getInputProps()} />
          <div className="flex flex-col items-center gap-2 text-muted-foreground">
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
                  PNG, JPG, GIF, WebP up to {maxSizeMB}MB ({value.length}/{maxFiles})
                </p>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
