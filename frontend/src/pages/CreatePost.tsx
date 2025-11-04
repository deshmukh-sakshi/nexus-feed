import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Label } from '@/components/ui/label'
import { usePosts } from '@/hooks/usePosts'
import { useAuthStore } from '@/stores/authStore'
import { useEffect } from 'react'

export const CreatePost = () => {
  const navigate = useNavigate()
  const { isAuthenticated } = useAuthStore()
  const { createPost, isCreating } = usePosts()

  const [title, setTitle] = useState('')
  const [url, setUrl] = useState('')
  const [body, setBody] = useState('')

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
      url: url.trim() || undefined,
      body: body.trim() || undefined,
    })

    // Reset form
    setTitle('')
    setUrl('')
    setBody('')
    
    // Navigate to home after a short delay to show success toast
    setTimeout(() => {
      navigate('/')
    }, 500)
  }

  if (!isAuthenticated) {
    return null
  }

  return (
    <div className="w-full max-w-4xl mx-auto space-y-4">
      <Button
        variant="ghost"
        size="sm"
        onClick={() => navigate(-1)}
        className="mb-4"
      >
        <ArrowLeft className="mr-2 h-4 w-4" />
        Back
      </Button>

      <Card>
        <CardHeader>
          <CardTitle>Create a Post</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="title">
                Title <span className="text-destructive">*</span>
              </Label>
              <Input
                id="title"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="An interesting title for your post"
                required
                maxLength={300}
              />
              <p className="text-xs text-muted-foreground">
                {title.length}/300 characters
              </p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="url">URL (optional)</Label>
              <Input
                id="url"
                type="url"
                value={url}
                onChange={(e) => setUrl(e.target.value)}
                placeholder="https://example.com"
              />
              <p className="text-xs text-muted-foreground">
                Add a link to share relevant content
              </p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="body">Body (optional)</Label>
              <Textarea
                id="body"
                value={body}
                onChange={(e) => setBody(e.target.value)}
                placeholder="Write your thoughts here... (Markdown supported)"
                className="min-h-[200px]"
              />
              <p className="text-xs text-muted-foreground">
                Supports Markdown formatting
              </p>
            </div>

            <div className="flex gap-2">
              <Button type="submit" disabled={!title.trim() || isCreating}>
                {isCreating ? 'Creating...' : 'Create Post'}
              </Button>
              <Button
                type="button"
                variant="outline"
                onClick={() => navigate(-1)}
                disabled={isCreating}
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
