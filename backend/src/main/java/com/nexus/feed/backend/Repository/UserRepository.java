package com.nexus.feed.backend.Repository;

import com.nexus.feed.backend.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID> {
    Optional<Users> findByUsername(String username);
    boolean existsByUsername(String username);

    @Modifying
    @Query("UPDATE Users u SET u.karma = u.karma + :delta WHERE u.id = :userId")
    void incrementKarma(@Param("userId") UUID userId, @Param("delta") int delta);

    @Query("SELECT u FROM Users u ORDER BY u.karma DESC")
    List<Users> findTopByKarma(org.springframework.data.domain.Pageable pageable);
}