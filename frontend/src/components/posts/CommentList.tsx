import { CommentItem } from './CommentItem'
import type { Comment } from '@/types'

interface CommentListProps {
  comments: Comment[]
  postId: string
}

// Build a tree structure from flat comment list
const buildCommentTree = (comments: Comment[]): Comment[] => {
  const commentMap = new Map<string, Comment>()
  const rootComments: Comment[] = []

  // First pass: create a map of all comments
  comments.forEach((comment) => {
    commentMap.set(comment.id, { ...comment, replies: [] })
  })

  // Second pass: build the tree
  comments.forEach((comment) => {
    const commentWithReplies = commentMap.get(comment.id)!
    
    if (comment.parentCommentId) {
      const parent = commentMap.get(comment.parentCommentId)
      if (parent) {
        parent.replies = parent.replies || []
        parent.replies.push(commentWithReplies)
      } else {
        // Parent not found, treat as root
        rootComments.push(commentWithReplies)
      }
    } else {
      rootComments.push(commentWithReplies)
    }
  })

  // Sort by creation time (newest first for root level)
  rootComments.sort(
    (a, b) =>
      new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  )

  return rootComments
}

export const CommentList = ({ comments, postId }: CommentListProps) => {
  if (!Array.isArray(comments) || comments.length === 0) {
    return (
      <div className="text-center py-8">
        <p className="text-muted-foreground">
          No comments yet. Be the first to comment!
        </p>
      </div>
    )
  }

  const commentTree = buildCommentTree(comments)

  return (
    <div className="space-y-4">
      {commentTree.map((comment) => (
        <CommentItem key={comment.id} comment={comment} postId={postId} />
      ))}
    </div>
  )
}
