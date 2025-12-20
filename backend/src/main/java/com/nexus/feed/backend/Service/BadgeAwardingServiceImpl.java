package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.Email.Service.EmailService;
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
    private final VoteRepository voteRepository;
    private final CommentRepository commentRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final BadgeService badgeService;
    private final EmailService emailService;

    // Badge name constants
    private static final String BADGE_FIRST_POST = "First Post";
    private static final String BADGE_STORYTELLER = "Storyteller";
    private static final String BADGE_PROLIFIC_POSTER = "Prolific Poster";
    private static final String BADGE_FIRST_COMMENT = "First Comment";
    private static final String BADGE_CONVERSATIONALIST = "Conversationalist";
    private static final String BADGE_COMMENTATOR = "Commentator";
    private static final String BADGE_GETTING_STARTED = "Getting Started";
    private static final String BADGE_RISING_STAR = "Rising Star";
    private static final String BADGE_POPULAR = "Popular";
    private static final String BADGE_SUPERSTAR = "Superstar";
    private static final String BADGE_FIRST_VOTE = "First Vote";
    private static final String BADGE_ACTIVE_VOTER = "Active Voter";
    private static final String BADGE_NEWCOMER = "Newcomer";
    private static final String BADGE_REGULAR = "Regular";
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

        // Storyteller badge (5+ posts)
        if (postCount >= 5) {
            tryAwardBadge(userId, BADGE_STORYTELLER);
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

        // First Comment badge
        if (commentCount >= 1) {
            tryAwardBadge(userId, BADGE_FIRST_COMMENT);
        }

        // Conversationalist badge (10+ comments)
        if (commentCount >= 10) {
            tryAwardBadge(userId, BADGE_CONVERSATIONALIST);
        }

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

        // Getting Started badge (10+ karma)
        if (karma >= 10) {
            tryAwardBadge(userId, BADGE_GETTING_STARTED);
        }

        // Rising Star badge (50+ karma)
        if (karma >= 50) {
            tryAwardBadge(userId, BADGE_RISING_STAR);
        }

        // Popular badge (100+ karma)
        if (karma >= 100) {
            tryAwardBadge(userId, BADGE_POPULAR);
        }

        // Superstar badge (500+ karma)
        if (karma >= 500) {
            tryAwardBadge(userId, BADGE_SUPERSTAR);
        }
    }

    @Override
    public void checkAccountAgeBadges(UUID userId) {
        Users user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getCreatedAt() == null) return;

        long daysSinceCreation = ChronoUnit.DAYS.between(user.getCreatedAt(), Instant.now());

        // Newcomer badge (7+ days)
        if (daysSinceCreation >= 7) {
            tryAwardBadge(userId, BADGE_NEWCOMER);
        }

        // Regular badge (30+ days)
        if (daysSinceCreation >= 30) {
            tryAwardBadge(userId, BADGE_REGULAR);
        }

        // Veteran badge (1+ year = 365 days)
        if (daysSinceCreation >= 365) {
            tryAwardBadge(userId, BADGE_VETERAN);
        }
    }

    @Override
    public void checkVoteBadges(UUID userId) {
        long voteCount = voteRepository.countByUserId(userId);

        // First Vote badge
        if (voteCount >= 1) {
            tryAwardBadge(userId, BADGE_FIRST_VOTE);
        }

        // Active Voter badge (20+ votes)
        if (voteCount >= 20) {
            tryAwardBadge(userId, BADGE_ACTIVE_VOTER);
        }
    }

    @Override
    public void checkAllBadges(UUID userId) {
        checkPostBadges(userId);
        checkCommentBadges(userId);
        checkKarmaBadges(userId);
        checkAccountAgeBadges(userId);
        checkVoteBadges(userId);
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
            
            // Send email notification (async)
            sendBadgeEmailNotification(userId, badge);
        } catch (Exception e) {
            log.error("Failed to award badge '{}' to user {}: {}", badgeName, userId, e.getMessage());
        }
    }
    
    private void sendBadgeEmailNotification(UUID userId, Badge badge) {
        try {
            Users user = userRepository.findById(userId).orElse(null);
            if (user == null || user.getAppUser() == null) return;
            
            String email = user.getAppUser().getEmail();
            String username = user.getUsername();
            
            emailService.sendBadgeAwardedEmail(
                email, 
                username, 
                badge.getName(), 
                badge.getDescription(), 
                badge.getIconUrl()
            );
        } catch (Exception e) {
            log.error("Failed to send badge email for user {}: {}", userId, e.getMessage());
        }
    }
}
