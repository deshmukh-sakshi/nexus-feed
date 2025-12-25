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
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.images LEFT JOIN FETCH p.tags WHERE p.user = :user ORDER BY p.createdAt DESC")
    Page<Post> findByUserOrderByCreatedAtDesc(@Param("user") Users user, Pageable pageable);
    
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.images LEFT JOIN FETCH p.tags ORDER BY p.createdAt DESC")
    Page<Post> findAllOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.images LEFT JOIN FETCH p.tags WHERE p.title LIKE %:keyword% OR p.body LIKE %:keyword% ORDER BY p.createdAt DESC")
    Page<Post> findByTitleContainingOrBodyContainingOrderByCreatedAtDesc(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.images LEFT JOIN FETCH p.tags WHERE p.id = :id")
    java.util.Optional<Post> findByIdWithUserAndImages(@Param("id") UUID id);
    
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.images LEFT JOIN p.tags t WHERE LOWER(t.name) = LOWER(:tagName) ORDER BY p.createdAt DESC")
    Page<Post> findByTagName(@Param("tagName") String tagName, Pageable pageable);
    
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.images LEFT JOIN p.tags t WHERE LOWER(t.name) IN :tagNames ORDER BY p.createdAt DESC")
    Page<Post> findByTagNames(@Param("tagNames") java.util.List<String> tagNames, Pageable pageable);
    
    long countByUser(Users user);
    
    @Query(value = """
        SELECT p.* FROM posts p 
        LEFT JOIN (
            SELECT v.votable_id, 
                   SUM(CASE WHEN v.vote_value = 'UPVOTE' THEN 1 WHEN v.vote_value = 'DOWNVOTE' THEN -1 ELSE 0 END) as net_votes
            FROM votes v WHERE v.votable_type = 'POST' GROUP BY v.votable_id
        ) vc ON vc.votable_id = p.id
        ORDER BY COALESCE(vc.net_votes, 0) DESC, p.created_at DESC
        """, 
        countQuery = "SELECT COUNT(p.id) FROM posts p",
        nativeQuery = true)
    Page<Post> findAllOrderByBest(Pageable pageable);
    
    @Query(value = """
        SELECT p.* FROM posts p 
        LEFT JOIN (
            SELECT v.votable_id, 
                   SUM(CASE WHEN v.vote_value = 'UPVOTE' THEN 1 WHEN v.vote_value = 'DOWNVOTE' THEN -1 ELSE 0 END) as net_votes
            FROM votes v WHERE v.votable_type = 'POST' GROUP BY v.votable_id
        ) vc ON vc.votable_id = p.id
        ORDER BY COALESCE(vc.net_votes, 0) / POWER(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - p.created_at)) / 3600.0 + 2, 1.5) DESC, p.created_at DESC
        """, 
        countQuery = "SELECT COUNT(p.id) FROM posts p",
        nativeQuery = true)
    Page<Post> findAllOrderByHot(Pageable pageable);
}