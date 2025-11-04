package com.nexus.feed.backend.Repository;

import com.nexus.feed.backend.Entity.UserBadge;
import com.nexus.feed.backend.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, UserBadge.UserBadgeId> {
    List<UserBadge> findByUser(Users user);
    boolean existsByIdUserIdAndIdBadgeId(UUID userId, Integer badgeId);
}