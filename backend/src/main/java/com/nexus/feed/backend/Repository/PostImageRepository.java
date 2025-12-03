package com.nexus.feed.backend.Repository;

import com.nexus.feed.backend.Entity.Post;
import com.nexus.feed.backend.Entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, UUID> {
    List<PostImage> findByPostOrderByCreatedAtAsc(Post post);
    
    @Modifying
    void deleteByPost(Post post);
}