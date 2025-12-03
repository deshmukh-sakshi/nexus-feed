import { useCallback, useState, useRef } from 'react'
import { useDropzone } from 'react-dropzone'
import { Camera, Loader2, X } from 'lucide-react'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { UserAvatar } from '@/components/ui/user-avatar'
import { uploadToCloudinary, getOptimizedImageUrl, type UploadProgress } from '@/lib/cloudinary'
import { cn } from '@/lib/utils'

interface ProfilePictureUploadProps {
  username: string
  value?: string
  onChange: (url: string | undefined) => void
  disabled?: boolean
  maxSizeMB?: number
}

export const ProfilePictureUpload = ({
  username,
  value,
  onChange,
  disabled = false,
  maxSizeMB = 2,
}: ProfilePictureUploadProps) => {
  const [uploading, setUploading] = useState(false)
  const [progress, setProgress] = useState(0)
  const [preview, setPreview] = useState<string | null>(null)
  const previewRef = useRef<string | null>(null)

  const onDrop = useCallback(
    async (acceptedFiles: File[]) => {
      const file = acceptedFiles[0]
      if (!file) return

      // Create preview
      const previewUrl = URL.createObjectURL(file)
      previewRef.current = previewUrl
      setPreview(previewUrl)
      setUploading(true)
      setProgress(0)

      try {
        const result = await uploadToCloudinary(file, (p: UploadProgress) => {
          setProgress(p.percent)
        })
        onChange(result.secure_url)
        toast.success('Profile picture updated!')
      } catch (error) {
        toast.error(error instanceof Error ? error.message : 'Upload failed')
        setPreview(null)
      } finally {
        setUploading(false)
        setProgress(0)
        if (previewRef.current) {
          URL.revokeObjectURL(previewRef.current)
          previewRef.current = null
        }
      }
    },
    [onChange]
  )

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'image/*': ['.png', '.jpg', '.jpeg', '.gif', '.webp'],
    },
    maxSize: maxSizeMB * 1024 * 1024,
    maxFiles: 1,
    disabled: disabled || uploading,
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

  const handleRemove = () => {
    onChange(undefined)
    setPreview(null)
  }

  const displayUrl = preview || value

  return (
    <div className="flex flex-col items-center gap-3">
      <div
        {...getRootProps()}
        className={cn(
          'relative cursor-pointer group',
          disabled && 'cursor-not-allowed opacity-50'
        )}
      >
        <input {...getInputProps()} />
        
        {/* Avatar display */}
        <div className="relative">
          {displayUrl ? (
            <div className="relative h-32 w-32 rounded-none border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] overflow-hidden">
              <img
                src={preview || getOptimizedImageUrl(value!, { width: 200, height: 200, crop: 'fill' })}
                alt={username}
                className={cn(
                  'h-full w-full object-cover',
                  uploading && 'opacity-50'
                )}
              />
            </div>
          ) : (
            <UserAvatar username={username} size="lg" className="h-32 w-32 text-3xl" />
          )}

          {/* Upload overlay */}
          {!uploading && (
            <div
              className={cn(
                'absolute inset-0 flex items-center justify-center bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity',
                isDragActive && 'opacity-100'
              )}
            >
              <Camera className="h-8 w-8 text-white" />
            </div>
          )}

          {/* Upload progress */}
          {uploading && (
            <div className="absolute inset-0 flex items-center justify-center bg-black/50">
              <div className="text-center text-white">
                <Loader2 className="h-6 w-6 animate-spin mx-auto mb-1" />
                <span className="text-xs font-bold">{progress}%</span>
              </div>
            </div>
          )}
        </div>
      </div>

      <p className="text-xs text-muted-foreground text-center">
        {isDragActive ? 'Drop image here' : 'Click or drag to upload'}
      </p>

      {value && !uploading && (
        <Button
          type="button"
          size="sm"
          onClick={handleRemove}
          disabled={disabled}
          className="text-xs bg-red-400 text-black hover:bg-red-500 border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] rounded-none font-bold transition-all active:translate-x-[2px] active:translate-y-[2px] active:shadow-none"
        >
          <X className="h-3 w-3 mr-1" />
          Remove
        </Button>
      )}
    </div>
  )
}
