package com.nexus.feed.backend.Service;

import java.util.UUID;

/**
 * Service for automatically awarding badges based on user activity.
 */
public interface BadgeAwardingService {
    
    /**
     * Check and award badges after a user creates a post.
     */
    void checkPostBadges(UUID userId);
    
    /**
     * Check and award badges after a user creates a comment.
     */
    void checkCommentBadges(UUID userId);
    
    /**
     * Check and award karma-based badges for a user.
     */
    void checkKarmaBadges(UUID userId);
    
    /**
     * Check and award account age badges for a user.
     */
    void checkAccountAgeBadges(UUID userId);
    
    /**
     * Check and award voting badges for a user.
     */
    void checkVoteBadges(UUID userId);
    
    /**
     * Run all badge checks for a user.
     */
    void checkAllBadges(UUID userId);
}
