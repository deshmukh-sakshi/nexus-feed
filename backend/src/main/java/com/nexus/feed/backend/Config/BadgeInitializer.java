package com.nexus.feed.backend.Config;

import com.nexus.feed.backend.Entity.Users;
import com.nexus.feed.backend.Repository.BadgeRepository;
import com.nexus.feed.backend.Repository.UserRepository;
import com.nexus.feed.backend.Service.BadgeAwardingService;
import com.nexus.feed.backend.Service.BadgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BadgeInitializer implements CommandLineRunner {

    private final BadgeRepository badgeRepository;
    private final BadgeService badgeService;
    private final UserRepository userRepository;
    private final BadgeAwardingService badgeAwardingService;

    @Override
    public void run(String... args) {
        initializeBadges();
        checkAccountAgeBadgesForAllUsers();
    }

    private void initializeBadges() {
        // Post badges
        createBadgeIfNotExists("First Post", "Created your first post", "🎉");
        createBadgeIfNotExists("Storyteller", "Created 5 posts", "📝");
        createBadgeIfNotExists("Prolific Poster", "Created 10 posts", "✍️");
        
        // Comment badges
        createBadgeIfNotExists("First Comment", "Made your first comment", "💭");
        createBadgeIfNotExists("Conversationalist", "Made 10 comments", "🗣️");
        createBadgeIfNotExists("Commentator", "Made 50 comments", "💬");
        
        // Karma badges
        createBadgeIfNotExists("Getting Started", "Reached 10 karma", "🌱");
        createBadgeIfNotExists("Rising Star", "Reached 50 karma", "⭐");
        createBadgeIfNotExists("Popular", "Reached 100 karma", "🌟");
        createBadgeIfNotExists("Superstar", "Reached 500 karma", "✨");
        
        // Engagement badges
        createBadgeIfNotExists("First Vote", "Cast your first vote", "👍");
        createBadgeIfNotExists("Active Voter", "Cast 20 votes", "🗳️");
        
        // Account age badges
        createBadgeIfNotExists("Newcomer", "Member for 7 days", "👋");
        createBadgeIfNotExists("Regular", "Member for 30 days", "📅");
        createBadgeIfNotExists("Veteran", "Member for 1 year", "🏆");
        
        log.info("Badge initialization complete");
    }

    @Async
    public void checkAccountAgeBadgesForAllUsers() {
        try {
            List<Users> allUsers = userRepository.findAll();
            log.info("Checking account age badges for {} users", allUsers.size());
            
            for (Users user : allUsers) {
                try {
                    badgeAwardingService.checkAccountAgeBadges(user.getId());
                } catch (Exception e) {
                    log.error("Failed to check account age badges for user {}: {}", user.getId(), e.getMessage());
                }
            }
            
            log.info("Account age badge check complete");
        } catch (Exception e) {
            log.error("Failed to check account age badges: {}", e.getMessage());
        }
    }

    private void createBadgeIfNotExists(String name, String description, String iconUrl) {
        if (!badgeRepository.existsByName(name)) {
            badgeService.createBadge(name, description, iconUrl);
            log.info("Created badge: {}", name);
        }
    }
}
