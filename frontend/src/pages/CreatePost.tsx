import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowLeft, Sparkles, PenLine, Image } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Label } from '@/components/ui/label'
import { ImageUpload } from '@/components/ui/image-upload'
import { usePosts } from '@/hooks/usePosts'
import { useAuthStore } from '@/stores/authStore'

export const CreatePost = () => {
  const navigate = useNavigate()
  const { isAuthenticated } = useAuthStore()
  const { createPost, isCreating } = usePosts()

  const [title, setTitle] = useState('')
  const [body, setBody] = useState('')
  const [imageUrls, setImageUrls] = useState<string[]>([])
  const [isUploadingImages, setIsUploadingImages] = useState(false)

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login')
    }
  }, [isAuthenticated, navigate])

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!title.trim()) {
      return
    }

    createPost({
      title: title.trim(),
      body: body.trim() || undefined,
      imageUrls: imageUrls.length > 0 ? imageUrls : undefined,
    })

    // Reset form and navigate immediately
    setTitle('')
    setBody('')
    setImageUrls([])
    navigate('/')
  }

  if (!isAuthenticated) {
    return null
  }

  return (
    <div className="w-full max-w-3xl mx-auto space-y-3">
      <Button
        size="sm"
        onClick={() => navigate(-1)}
        className="bg-pink-200 text-black hover:bg-pink-300 border-2 border-black shadow-[3px_3px_0px_0px_rgba(0,0,0,1)] dark:shadow-[3px_3px_0px_0px_rgba(255,255,255,1)] rounded-full font-bold transition-all active:translate-x-[3px] active:translate-y-[3px] active:shadow-none"
      >
        <ArrowLeft className="mr-2 h-4 w-4" />
        Back
      </Button>

      <Card className="border-2 border-black shadow-[6px_6px_0px_0px_rgba(0,0,0,1)] dark:shadow-[6px_6px_0px_0px_rgba(255,255,255,1)] bg-yellow-50 dark:bg-neutral-900 overflow-hidden">
        <CardHeader className="border-b-2 border-black py-3">
          <CardTitle className="flex items-center gap-2 text-xl">
            <Sparkles className="h-5 w-5 text-orange-600 dark:text-orange-400" />
            Create a Post
          </CardTitle>
        </CardHeader>
        <CardContent className="pt-4 pb-4">
          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Title Field */}
            <div className="space-y-1">
              <Label htmlFor="title" className="flex items-center gap-2 text-sm font-bold">
                <PenLine className="h-4 w-4 text-orange-500" />
                Title <span className="text-destructive">*</span>
              </Label>
              <Input
                id="title"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="An interesting title for your post"
                required
                maxLength={300}
                className="border-2 border-black bg-white dark:bg-neutral-800 focus:ring-2 focus:ring-orange-400"
              />
              <div className="flex justify-end">
                <span className={`text-xs font-medium ${title.length > 250 ? 'text-orange-500' : 'text-muted-foreground'}`}>
                  {title.length}/300
                </span>
              </div>
            </div>

            {/* Body Field */}
            <div className="space-y-1">
              <Label htmlFor="body" className="flex items-center gap-2 text-sm font-bold">
                <PenLine className="h-4 w-4 text-cyan-500" />
                Body <span className="text-muted-foreground font-normal">(optional)</span>
              </Label>
              <Textarea
                id="body"
                value={body}
                onChange={(e) => setBody(e.target.value)}
                placeholder="Write your thoughts here..."
                className="min-h-[100px] border-2 border-black bg-white dark:bg-neutral-800 focus:ring-2 focus:ring-orange-400 resize-none"
              />
            </div>

            {/* Images Field */}
            <div className="space-y-1">
              <Label className="flex items-center gap-2 text-sm font-bold">
                <Image className="h-4 w-4 text-lime-500" />
                Images <span className="text-muted-foreground font-normal">(optional)</span>
              </Label>
              <ImageUpload
                value={imageUrls}
                onChange={setImageUrls}
                maxSizeMB={5}
                disabled={isCreating}
                onUploadingChange={setIsUploadingImages}
              />
            </div>

            {/* Action Buttons */}
            <div className="flex gap-3 pt-3 border-t-2 border-dashed border-black/20">
              <Button 
                type="submit" 
                disabled={!title.trim() || isCreating || isUploadingImages}
                className="flex-1 bg-lime-300 text-black hover:bg-lime-400 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-full font-bold transition-all active:translate-x-[4px] active:translate-y-[4px] active:shadow-none disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <Sparkles className="mr-2 h-4 w-4" />
                {isCreating ? 'Creating...' : isUploadingImages ? 'Uploading Images...' : 'Create Post'}
              </Button>
              <Button
                type="button"
                onClick={() => navigate(-1)}
                disabled={isCreating}
                className="bg-gray-200 text-black hover:bg-gray-300 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-full font-bold px-6 transition-all active:translate-x-[4px] active:translate-y-[4px] active:shadow-none disabled:opacity-50"
              >
                Cancel
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
