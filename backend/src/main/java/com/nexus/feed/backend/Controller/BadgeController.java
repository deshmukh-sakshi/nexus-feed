package com.nexus.feed.backend.Controller;

import com.nexus.feed.backend.DTO.BadgeResponse;
import com.nexus.feed.backend.Service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BadgeController {

    private final BadgeService badgeService;

    @GetMapping
    public ResponseEntity<List<BadgeResponse>> getAllBadges() {
        List<BadgeResponse> badges = badgeService.getAllBadges();
        return ResponseEntity.ok(badges);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BadgeResponse> getBadgeById(@PathVariable Integer id) {
        try {
            BadgeResponse badge = badgeService.getBadgeById(id);
            return ResponseEntity.ok(badge);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<BadgeResponse> getBadgeByName(@PathVariable String name) {
        try {
            BadgeResponse badge = badgeService.getBadgeByName(name);
            return ResponseEntity.ok(badge);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BadgeResponse>> getUserBadges(@PathVariable UUID userId) {
        try {
            List<BadgeResponse> badges = badgeService.getUserBadges(userId);
            return ResponseEntity.ok(badges);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/award")
    public ResponseEntity<Void> awardBadgeToUser(
            @RequestParam UUID userId,
            @RequestParam Integer badgeId) {
        try {
            badgeService.awardBadgeToUser(userId, badgeId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}