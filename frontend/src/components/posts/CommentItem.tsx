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
import { ConfirmDialog } from "@/components/ui/confirm-dialog";
import { cn, formatNumber } from "@/lib/utils";
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
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [isCollapsed, setIsCollapsed] = useState(false);

  const score = comment.upvotes - comment.downvotes;
  const isOwner = user?.userId === comment.userId;
  const isEdited = comment.createdAt !== comment.updatedAt;
  const canNest = depth < MAX_DEPTH;
  const hasReplies = comment.replies && comment.replies.length > 0;
  const isTempComment = comment.id.startsWith('temp-'); // Check if comment is still being created

  const handleVote = (voteValue: "UPVOTE" | "DOWNVOTE") => {
    if (!isAuthenticated) {
      setShowAuthModal(true);
      return;
    }
    if (isTempComment) {
      return; // Don't allow voting on temp comments
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

  const handleDelete = () => {
    setShowDeleteDialog(true);
  };

  const confirmDelete = async () => {
    try {
      await deleteComment(comment.id);
    } catch (error) {
      console.error("Failed to delete comment:", error);
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
          <div className="flex items-start gap-2 group">
            <Link to={`/user/${comment.username}`} className="group-hover:opacity-80">
              <UserAvatar username={comment.username} size="sm" />
            </Link>
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2 text-xs text-muted-foreground flex-wrap">
                {hasReplies && (
                  <Button
                    variant="ghost"
                    size="sm"
                    className="h-4 w-4 p-0 hover:bg-gray-300 hover:border-black"
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
                  className="group-hover:underline font-medium"
                >
                  u/{comment.username}
                </Link>
                <span>•</span>
                <span>
                  {isTempComment 
                    ? 'posting...' 
                    : `${formatDistanceToNow(new Date(comment.createdAt))} ago`
                  }
                </span>
                {isEdited && !isTempComment && <span className="italic">(edited)</span>}
                {hasReplies && (
                  <>
                    <span>•</span>
                    <button
                      onClick={toggleCollapse}
                      className="hover:underline text-blue-500"
                    >
                      {isCollapsed
                        ? `[+] ${formatNumber(comment.replies?.length || 0)} replies`
                        : `[-] ${formatNumber(comment.replies?.length || 0)} replies`}
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
                    <Button 
                      size="sm" 
                      onClick={handleSaveEdit}
                      className="bg-green-400 text-black hover:bg-green-500 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold"
                    >
                      <Save className="mr-2 h-4 w-4" />
                      Save
                    </Button>
                    <Button
                      size="sm"
                      onClick={handleCancelEdit}
                      className="bg-gray-300 text-black hover:bg-gray-400 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold"
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
                {/* Vote Pill */}
                <div className={cn(
                  "flex items-center bg-pink-200 border-2 border-black rounded-full h-8 px-3 shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] dark:shadow-[2px_2px_0px_0px_rgba(255,255,255,1)]",
                  isTempComment && "opacity-50"
                )}>
                  <Button
                    size="icon"
                    className={cn(
                      "h-6 w-6 rounded-full transition-all shadow-[1px_1px_0px_0px_rgba(0,0,0,1)]",
                      comment.userVote === "UPVOTE" 
                        ? "bg-orange-400 text-black hover:bg-orange-500" 
                        : "bg-white text-black hover:bg-orange-300"
                    )}
                    onClick={() => handleVote("UPVOTE")}
                    disabled={isTempComment}
                  >
                    <ArrowBigUp className={cn("h-4 w-4", comment.userVote === "UPVOTE" && "fill-current")} />
                  </Button>
                  <span className="text-xs font-bold mx-2 min-w-[1rem] text-center">{formatNumber(score)}</span>
                  <Button
                    size="icon"
                    className={cn(
                      "h-6 w-6 rounded-full transition-all shadow-[1px_1px_0px_0px_rgba(0,0,0,1)]",
                      comment.userVote === "DOWNVOTE" 
                        ? "bg-blue-400 text-black hover:bg-blue-500" 
                        : "bg-white text-black hover:bg-blue-300"
                    )}
                    onClick={() => handleVote("DOWNVOTE")}
                    disabled={isTempComment}
                  >
                    <ArrowBigDown className={cn("h-4 w-4", comment.userVote === "DOWNVOTE" && "fill-current")} />
                  </Button>
                </div>

                {canNest && (
                  <Button
                    size="sm"
                    className="h-8 px-3 bg-cyan-300 text-black hover:bg-cyan-400 border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] dark:shadow-[2px_2px_0px_0px_rgba(255,255,255,1)] rounded-full font-bold transition-all active:translate-x-[2px] active:translate-y-[2px] active:shadow-none disabled:opacity-50 disabled:cursor-not-allowed"
                    onClick={handleReply}
                    disabled={isTempComment}
                    title={isTempComment ? "Please wait for comment to be posted..." : "Reply to this comment"}
                  >
                    <MessageSquare className="mr-1 h-3 w-3" />
                    Reply
                  </Button>
                )}

                {isOwner && !isEditing && !isTempComment && (
                  <>
                    <Button
                      size="sm"
                      className="h-8 px-3 bg-yellow-400 text-black hover:bg-yellow-500 border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] dark:shadow-[2px_2px_0px_0px_rgba(255,255,255,1)] rounded-full font-bold transition-all active:translate-x-[2px] active:translate-y-[2px] active:shadow-none"
                      onClick={() => setIsEditing(true)}
                    >
                      <Edit2 className="mr-1 h-3 w-3" />
                      Edit
                    </Button>
                    <Button
                      size="sm"
                      className="h-8 px-3 bg-red-400 text-black hover:bg-red-500 border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] dark:shadow-[2px_2px_0px_0px_rgba(255,255,255,1)] rounded-full font-bold transition-all active:translate-x-[2px] active:translate-y-[2px] active:shadow-none"
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
                    <Button 
                      size="sm" 
                      onClick={handleSaveReply}
                      className="bg-blue-400 text-black hover:bg-blue-500 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold"
                    >
                      Reply
                    </Button>
                    <Button
                      size="sm"
                      onClick={handleCancelReply}
                      className="bg-gray-300 text-black hover:bg-gray-400 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold"
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

      <ConfirmDialog
        isOpen={showDeleteDialog}
        onClose={() => setShowDeleteDialog(false)}
        onConfirm={confirmDelete}
        title="Delete Comment"
        description="Are you sure you want to delete this comment? This action cannot be undone."
        confirmText="Delete"
        cancelText="Cancel"
      />
    </>
  );
};
