package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.Entity.Badge;
import com.nexus.feed.backend.Entity.Users;
import com.nexus.feed.backend.Repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BadgeAwardingServiceImpl implements BadgeAwardingService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final BadgeService badgeService;

    // Badge name constants
    private static final String BADGE_FIRST_POST = "First Post";
    private static final String BADGE_PROLIFIC_POSTER = "Prolific Poster";
    private static final String BADGE_COMMENTATOR = "Commentator";
    private static final String BADGE_RISING_STAR = "Rising Star";
    private static final String BADGE_POPULAR = "Popular";
    private static final String BADGE_VETERAN = "Veteran";

    @Override
    public void checkPostBadges(UUID userId) {
        Users user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        long postCount = postRepository.countByUser(user);

        // First Post badge
        if (postCount >= 1) {
            tryAwardBadge(userId, BADGE_FIRST_POST);
        }

        // Prolific Poster badge (10+ posts)
        if (postCount >= 10) {
            tryAwardBadge(userId, BADGE_PROLIFIC_POSTER);
        }
    }

    @Override
    public void checkCommentBadges(UUID userId) {
        Users user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        long commentCount = commentRepository.countByUser(user);

        // Commentator badge (50+ comments)
        if (commentCount >= 50) {
            tryAwardBadge(userId, BADGE_COMMENTATOR);
        }
    }

    @Override
    public void checkKarmaBadges(UUID userId) {
        Users user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        long karma = user.getKarma();

        // Rising Star badge (100+ karma)
        if (karma >= 100) {
            tryAwardBadge(userId, BADGE_RISING_STAR);
        }

        // Popular badge (1000+ karma)
        if (karma >= 1000) {
            tryAwardBadge(userId, BADGE_POPULAR);
        }
    }

    @Override
    public void checkAccountAgeBadges(UUID userId) {
        Users user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getCreatedAt() == null) return;

        long daysSinceCreation = ChronoUnit.DAYS.between(user.getCreatedAt(), Instant.now());

        // Veteran badge (1+ year = 365 days)
        if (daysSinceCreation >= 365) {
            tryAwardBadge(userId, BADGE_VETERAN);
        }
    }

    @Override
    public void checkAllBadges(UUID userId) {
        checkPostBadges(userId);
        checkCommentBadges(userId);
        checkKarmaBadges(userId);
        checkAccountAgeBadges(userId);
    }

    private void tryAwardBadge(UUID userId, String badgeName) {
        try {
            Badge badge = badgeRepository.findByName(badgeName).orElse(null);
            if (badge == null) {
                log.warn("Badge not found: {}", badgeName);
                return;
            }

            // Check if user already has this badge
            if (userBadgeRepository.existsByIdUserIdAndIdBadgeId(userId, badge.getId())) {
                return; // Already has badge
            }

            badgeService.awardBadgeToUser(userId, badge.getId());
            log.info("Badge '{}' awarded to user {}", badgeName, userId);
        } catch (Exception e) {
            log.error("Failed to award badge '{}' to user {}: {}", badgeName, userId, e.getMessage());
        }
    }
}
