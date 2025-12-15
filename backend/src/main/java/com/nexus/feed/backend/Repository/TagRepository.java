package com.nexus.feed.backend.Repository;

import com.nexus.feed.backend.Entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByNameIgnoreCase(String name);
    
    List<Tag> findByNameInIgnoreCase(Set<String> names);
    
    @Query("SELECT t FROM Tag t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY SIZE(t.posts) DESC")
    List<Tag> searchByName(@Param("query") String query);
    
    @Query("SELECT t FROM Tag t ORDER BY SIZE(t.posts) DESC")
    List<Tag> findTopTags();
    
    boolean existsByNameIgnoreCase(String name);
}
