package com.nexus.feed.backend.Config;

import com.nexus.feed.backend.Repository.BadgeRepository;
import com.nexus.feed.backend.Service.BadgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BadgeInitializer implements CommandLineRunner {

    private final BadgeRepository badgeRepository;
    private final BadgeService badgeService;

    @Override
    public void run(String... args) {
        initializeBadges();
    }

    private void initializeBadges() {
        createBadgeIfNotExists("First Post", "Awarded for creating your first post", "🎉");
        createBadgeIfNotExists("Prolific Poster", "Awarded for creating 10 or more posts", "✍️");
        createBadgeIfNotExists("Commentator", "Awarded for making 50 or more comments", "💬");
        createBadgeIfNotExists("Rising Star", "Awarded for reaching 100 karma", "⭐");
        createBadgeIfNotExists("Popular", "Awarded for reaching 1000 karma", "🌟");
        createBadgeIfNotExists("Veteran", "Awarded for being a member for 1 year", "🏆");
        
        log.info("Badge initialization complete");
    }

    private void createBadgeIfNotExists(String name, String description, String iconUrl) {
        if (!badgeRepository.existsByName(name)) {
            badgeService.createBadge(name, description, iconUrl);
            log.info("Created badge: {}", name);
        }
    }
}
