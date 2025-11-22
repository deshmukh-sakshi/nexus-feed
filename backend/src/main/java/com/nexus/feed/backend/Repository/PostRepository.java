package com.nexus.feed.backend.Repository;

import com.nexus.feed.backend.Entity.Post;
import com.nexus.feed.backend.Entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.images WHERE p.user = :user ORDER BY p.createdAt DESC")
    Page<Post> findByUserOrderByCreatedAtDesc(@Param("user") Users user, Pageable pageable);
    
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.images ORDER BY p.createdAt DESC")
    Page<Post> findAllOrderByCreatedAtDesc(Pageable pageable);
    
    @Query(value = "SELECT DISTINCT p.* FROM posts p " +
           "LEFT JOIN users u ON p.user_id = u.id " +
           "LEFT JOIN (" +
           "  SELECT votable_id, " +
           "         SUM(CASE WHEN vote_value = 'UPVOTE' THEN 1 ELSE 0 END) as upvotes, " +
           "         SUM(CASE WHEN vote_value = 'DOWNVOTE' THEN 1 ELSE 0 END) as downvotes " +
           "  FROM votes " +
           "  WHERE votable_type = 'POST' " +
           "  GROUP BY votable_id" +
           ") v ON p.id = v.votable_id " +
           "ORDER BY (COALESCE(v.upvotes, 0) - COALESCE(v.downvotes, 0)) / " +
           "POWER((EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - p.created_at))/3600 + 2), 1.5) DESC",
           countQuery = "SELECT COUNT(DISTINCT p.id) FROM posts p",
           nativeQuery = true)
    Page<Post> findAllOrderByHotScore(Pageable pageable);
    
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.images WHERE p.title LIKE %:keyword% OR p.body LIKE %:keyword% ORDER BY p.createdAt DESC")
    Page<Post> findByTitleContainingOrBodyContainingOrderByCreatedAtDesc(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.images WHERE p.id = :id")
    java.util.Optional<Post> findByIdWithUserAndImages(@Param("id") UUID id);
    
    @Query(value = "SELECT p.id FROM posts p " +
           "LEFT JOIN (" +
           "  SELECT votable_id, " +
           "         SUM(CASE WHEN vote_value = 'UPVOTE' THEN 1 ELSE 0 END) as upvotes, " +
           "         SUM(CASE WHEN vote_value = 'DOWNVOTE' THEN 1 ELSE 0 END) as downvotes " +
           "  FROM votes " +
           "  WHERE votable_type = 'POST' " +
           "  GROUP BY votable_id" +
           ") v ON p.id = v.votable_id " +
           "ORDER BY (COALESCE(v.upvotes, 0) - COALESCE(v.downvotes, 0)) / " +
           "POWER((EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - p.created_at))/3600 + 2), 1.5) DESC " +
           "LIMIT :limit OFFSET :offset",
           nativeQuery = true)
    java.util.List<UUID> findPostIdsByHotScore(@Param("limit") int limit, @Param("offset") int offset);
}