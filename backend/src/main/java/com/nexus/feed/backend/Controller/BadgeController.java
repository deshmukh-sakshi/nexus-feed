package com.nexus.feed.backend.Controller;

import com.nexus.feed.backend.DTO.BadgeResponse;
import com.nexus.feed.backend.Service.BadgeAwardingService;
import com.nexus.feed.backend.Service.BadgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;
    private final BadgeAwardingService badgeAwardingService;

    @GetMapping
    public ResponseEntity<List<BadgeResponse>> getAllBadges() {
        List<BadgeResponse> badges = badgeService.getAllBadges();
        return ResponseEntity.ok(badges);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BadgeResponse> getBadgeById(@PathVariable Integer id) {
        BadgeResponse badge = badgeService.getBadgeById(id);
        return ResponseEntity.ok(badge);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<BadgeResponse> getBadgeByName(@PathVariable String name) {
        BadgeResponse badge = badgeService.getBadgeByName(name);
        return ResponseEntity.ok(badge);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BadgeResponse>> getUserBadges(@PathVariable UUID userId) {
        // Automatically check and award any earned badges before returning
        badgeAwardingService.checkAllBadges(userId);
        List<BadgeResponse> badges = badgeService.getUserBadges(userId);
        return ResponseEntity.ok(badges);
    }

    @PostMapping("/award")
    public ResponseEntity<Void> awardBadgeToUser(
            @RequestParam UUID userId,
            @RequestParam Integer badgeId) {
        log.info("Manual badge award: userId={}, badgeId={}", userId, badgeId);
        badgeService.awardBadgeToUser(userId, badgeId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/check/{userId}")
    public ResponseEntity<List<BadgeResponse>> checkAndAwardBadges(@PathVariable UUID userId) {
        log.debug("Checking badges for user: {}", userId);
        badgeAwardingService.checkAllBadges(userId);
        List<BadgeResponse> badges = badgeService.getUserBadges(userId);
        return ResponseEntity.ok(badges);
    }
}