package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.DTO.BadgeResponse;
import com.nexus.feed.backend.Entity.Badge;
import java.util.List;

public interface BadgeService {
    List<BadgeResponse> getAllBadges();
    BadgeResponse getBadgeById(Integer id);
    BadgeResponse getBadgeByName(String name);
    Badge createBadge(String name, String description, String iconUrl);
    List<BadgeResponse> getUserBadges(java.util.UUID userId);
    void awardBadgeToUser(java.util.UUID userId, Integer badgeId);
}