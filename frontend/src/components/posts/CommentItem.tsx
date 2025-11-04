import { useState } from "react";
import { Link } from "react-router-dom";
import { formatDistanceToNow } from "date-fns";
import ReactMarkdown from "react-markdown";
import {
  ArrowBigUp,
  ArrowBigDown,
  MessageSquare,
  Edit2,
  Trash2,
  Save,
  X,
  ChevronDown,
  ChevronRight,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { UserAvatar } from "@/components/ui/user-avatar";
import { AuthModal } from "@/components/ui/auth-modal";
import { cn } from "@/lib/utils";
import { useAuthStore } from "@/stores/authStore";
import { useComments } from "@/hooks/useComments";
import type { Comment } from "@/types";

interface CommentItemProps {
  comment: Comment;
  postId: string;
  depth?: number;
}

const MAX_DEPTH = 6;

export const CommentItem = ({
  comment,
  postId,
  depth = 0,
}: CommentItemProps) => {
  const { user, isAuthenticated } = useAuthStore();
  const { voteComment, updateComment, deleteComment, createComment } =
    useComments(postId);
  const [isEditing, setIsEditing] = useState(false);
  const [editBody, setEditBody] = useState(comment.body);
  const [isReplying, setIsReplying] = useState(false);
  const [replyBody, setReplyBody] = useState("");
  const [showAuthModal, setShowAuthModal] = useState(false);
  const [isCollapsed, setIsCollapsed] = useState(false);

  const score = comment.upvotes - comment.downvotes;
  const isOwner = user?.userId === comment.userId;
  const isEdited = comment.createdAt !== comment.updatedAt;
  const canNest = depth < MAX_DEPTH;
  const hasReplies = comment.replies && comment.replies.length > 0;

  const handleVote = (voteValue: "UPVOTE" | "DOWNVOTE") => {
    if (!isAuthenticated) {
      setShowAuthModal(true);
      return;
    }
    voteComment(comment.id, voteValue);
  };

  const handleSaveEdit = () => {
    if (editBody.trim()) {
      updateComment(comment.id, { body: editBody.trim() });
      setIsEditing(false);
    }
  };

  const handleCancelEdit = () => {
    setEditBody(comment.body);
    setIsEditing(false);
  };

  const handleDelete = async () => {
    if (window.confirm("Are you sure you want to delete this comment?")) {
      try {
        await deleteComment(comment.id);
      } catch (error) {
        console.error("Failed to delete comment:", error);
      }
    }
  };

  const toggleCollapse = () => {
    setIsCollapsed(!isCollapsed);
  };

  const handleReply = () => {
    if (!isAuthenticated) {
      setShowAuthModal(true);
      return;
    }
    setIsReplying(true);
  };

  const handleSaveReply = () => {
    if (replyBody.trim()) {
      createComment({
        body: replyBody.trim(),
        parentCommentId: comment.id,
      });
      setReplyBody("");
      setIsReplying(false);
    }
  };

  const handleCancelReply = () => {
    setReplyBody("");
    setIsReplying(false);
  };

  return (
    <>
      <div
        className={cn("border-l-2 border-muted pl-4 py-2", depth > 0 && "ml-4")}
      >
        <div className="space-y-2">
          <div className="flex items-start gap-2">
            <Link to={`/user/${comment.username}`} className="hover:opacity-80">
              <UserAvatar username={comment.username} size="sm" />
            </Link>
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2 text-xs text-muted-foreground flex-wrap">
                {hasReplies && (
                  <Button
                    variant="ghost"
                    size="sm"
                    className="h-4 w-4 p-0 hover:bg-transparent"
                    onClick={toggleCollapse}
                  >
                    {isCollapsed ? (
                      <ChevronRight className="h-3 w-3" />
                    ) : (
                      <ChevronDown className="h-3 w-3" />
                    )}
                  </Button>
                )}
                <Link
                  to={`/user/${comment.username}`}
                  className="hover:underline font-medium"
                >
                  u/{comment.username}
                </Link>
                <span>•</span>
                <span>
                  {formatDistanceToNow(new Date(comment.createdAt))} ago
                </span>
                {isEdited && (
                  <>
                    <span>•</span>
                    <span className="italic">edited</span>
                  </>
                )}
                {hasReplies && (
                  <>
                    <span>•</span>
                    <button
                      onClick={toggleCollapse}
                      className="hover:underline text-blue-500"
                    >
                      {isCollapsed
                        ? `[+] ${comment.replies?.length || 0} replies`
                        : `[-] ${comment.replies?.length || 0} replies`}
                    </button>
                  </>
                )}
              </div>

              {isEditing ? (
                <div className="mt-2 space-y-2">
                  <Textarea
                    value={editBody}
                    onChange={(e) => setEditBody(e.target.value)}
                    className="min-h-[100px] resize-y"
                    placeholder="Edit your comment (Markdown supported)"
                  />
                  <div className="flex gap-2">
                    <Button size="sm" onClick={handleSaveEdit}>
                      <Save className="mr-2 h-4 w-4" />
                      Save
                    </Button>
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={handleCancelEdit}
                    >
                      <X className="mr-2 h-4 w-4" />
                      Cancel
                    </Button>
                  </div>
                </div>
              ) : (
                <div className="mt-2 prose prose-sm dark:prose-invert max-w-none">
                  <ReactMarkdown>{comment.body}</ReactMarkdown>
                </div>
              )}

              <div className="flex items-center gap-2 mt-2">
                <div className="flex items-center gap-1">
                  <Button
                    variant="ghost"
                    size="sm"
                    className={cn(
                      "h-7 px-2",
                      comment.userVote === "UPVOTE" && "text-orange-500"
                    )}
                    onClick={() => handleVote("UPVOTE")}
                  >
                    <ArrowBigUp className="h-4 w-4" />
                  </Button>
                  <span className="text-xs font-semibold">{score}</span>
                  <Button
                    variant="ghost"
                    size="sm"
                    className={cn(
                      "h-7 px-2",
                      comment.userVote === "DOWNVOTE" && "text-blue-500"
                    )}
                    onClick={() => handleVote("DOWNVOTE")}
                  >
                    <ArrowBigDown className="h-4 w-4" />
                  </Button>
                </div>

                {canNest && (
                  <Button
                    variant="ghost"
                    size="sm"
                    className="h-7 px-2"
                    onClick={handleReply}
                  >
                    <MessageSquare className="mr-1 h-3 w-3" />
                    Reply
                  </Button>
                )}

                {isOwner && !isEditing && (
                  <>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="h-7 px-2"
                      onClick={() => setIsEditing(true)}
                    >
                      <Edit2 className="mr-1 h-3 w-3" />
                      Edit
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="h-7 px-2 text-destructive hover:text-destructive"
                      onClick={handleDelete}
                    >
                      <Trash2 className="mr-1 h-3 w-3" />
                      Delete
                    </Button>
                  </>
                )}
              </div>

              {isReplying && (
                <div className="mt-3 space-y-2">
                  <Textarea
                    value={replyBody}
                    onChange={(e) => setReplyBody(e.target.value)}
                    className="min-h-[80px] resize-y"
                    placeholder="Write a reply (Markdown supported)"
                    autoFocus
                  />
                  <div className="flex gap-2">
                    <Button size="sm" onClick={handleSaveReply}>
                      Reply
                    </Button>
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={handleCancelReply}
                    >
                      Cancel
                    </Button>
                  </div>
                </div>
              )}
            </div>
          </div>

          {hasReplies && !isCollapsed && (
            <div className="space-y-2">
              {comment.replies?.map((reply) => (
                <CommentItem
                  key={reply.id}
                  comment={reply}
                  postId={postId}
                  depth={depth + 1}
                />
              ))}
            </div>
          )}
        </div>
      </div>

      <AuthModal
        isOpen={showAuthModal}
        onClose={() => setShowAuthModal(false)}
        message="You need to be logged in to vote or reply to comments."
      />
    </>
  );
};
