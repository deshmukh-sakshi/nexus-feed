import { CommentItem } from "./CommentItem";
import type { Comment } from "@/types";

interface CommentListProps {
  comments: Comment[];
  postId: string;
}

// Handle properly nested comments from backend
const buildCommentTree = (comments: Comment[]): Comment[] => {
  if (!comments || comments.length === 0) {
    return [];
  }

  // The backend now returns fully nested comments with all levels of replies
  // Just sort the root level comments and return them
  return comments.sort(
    (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  );
};

export const CommentList = ({ comments, postId }: CommentListProps) => {
  if (!Array.isArray(comments) || comments.length === 0) {
    return (
      <div className="text-center py-8">
        <p className="text-muted-foreground">
          No comments yet. Be the first to comment!
        </p>
      </div>
    );
  }

  const commentTree = buildCommentTree(comments);

  return (
    <div className="space-y-4">
      {commentTree.map((comment) => (
        <CommentItem key={comment.id} comment={comment} postId={postId} />
      ))}
    </div>
  );
};
