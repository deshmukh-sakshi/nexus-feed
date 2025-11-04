package com.nexus.feed.backend.Repository;

import com.nexus.feed.backend.Entity.Comment;
import com.nexus.feed.backend.Entity.Post;
import com.nexus.feed.backend.Entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByPostAndParentCommentIsNullOrderByCreatedAtDesc(Post post);
    
    List<Comment> findByParentCommentOrderByCreatedAtAsc(Comment parentComment);
    
    Page<Comment> findByUserOrderByCreatedAtDesc(Users user, Pageable pageable);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post = :post")
    long countByPost(Post post);
    
    void deleteByPost(Post post);
}