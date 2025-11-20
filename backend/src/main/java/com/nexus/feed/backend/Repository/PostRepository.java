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
    
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.images WHERE p.title LIKE %:keyword% OR p.body LIKE %:keyword% ORDER BY p.createdAt DESC")
    Page<Post> findByTitleContainingOrBodyContainingOrderByCreatedAtDesc(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.images WHERE p.id = :id")
    java.util.Optional<Post> findByIdWithUserAndImages(@Param("id") UUID id);
}