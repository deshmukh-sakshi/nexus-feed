package com.nexus.feed.backend.Repository;

import com.nexus.feed.backend.Entity.Comment;
import com.nexus.feed.backend.Entity.Post;
import com.nexus.feed.backend.Entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByPostAndParentCommentIsNullOrderByCreatedAtDesc(Post post);
    
    @Query(value = "SELECT c.id FROM comments c " +
           "LEFT JOIN (" +
           "  SELECT votable_id, " +
           "         SUM(CASE WHEN vote_value = 'UPVOTE' THEN 1 ELSE 0 END) as upvotes, " +
           "         SUM(CASE WHEN vote_value = 'DOWNVOTE' THEN 1 ELSE 0 END) as downvotes " +
           "  FROM votes " +
           "  WHERE votable_type = 'COMMENT' " +
           "  GROUP BY votable_id" +
           ") v ON c.id = v.votable_id " +
           "WHERE c.post_id = :postId AND c.parent_comment_id IS NULL " +
           "ORDER BY (COALESCE(v.upvotes, 0) - COALESCE(v.downvotes, 0)) / " +
           "POWER((EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - c.created_at))/3600 + 2), 1.5) DESC",
           nativeQuery = true)
    java.util.List<UUID> findTopLevelCommentIdsByPostOrderByHotScore(@Param("postId") UUID postId);
    
    @Query(value = "SELECT c.id FROM comments c " +
           "LEFT JOIN (" +
           "  SELECT votable_id, " +
           "         SUM(CASE WHEN vote_value = 'UPVOTE' THEN 1 ELSE 0 END) as upvotes, " +
           "         SUM(CASE WHEN vote_value = 'DOWNVOTE' THEN 1 ELSE 0 END) as downvotes " +
           "  FROM votes " +
           "  WHERE votable_type = 'COMMENT' " +
           "  GROUP BY votable_id" +
           ") v ON c.id = v.votable_id " +
           "WHERE c.parent_comment_id = :parentCommentId " +
           "ORDER BY (COALESCE(v.upvotes, 0) - COALESCE(v.downvotes, 0)) / " +
           "POWER((EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - c.created_at))/3600 + 2), 1.5) DESC",
           nativeQuery = true)
    java.util.List<UUID> findReplyIdsByParentCommentOrderByHotScore(@Param("parentCommentId") UUID parentCommentId);
    
    List<Comment> findByParentCommentOrderByCreatedAtAsc(Comment parentComment);
    
    Page<Comment> findByUserOrderByCreatedAtDesc(Users user, Pageable pageable);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post = :post")
    long countByPost(Post post);
    
    @Query("SELECT c.post.id as postId, COUNT(c) as count FROM Comment c WHERE c.post.id IN :postIds GROUP BY c.post.id")
    java.util.List<CommentCount> countByPostIds(@Param("postIds") java.util.List<UUID> postIds);
    
    void deleteByPost(Post post);
    
    interface CommentCount {
        UUID getPostId();
        Long getCount();
    }
}