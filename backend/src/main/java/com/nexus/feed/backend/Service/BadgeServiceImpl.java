package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.DTO.BadgeResponse;
import com.nexus.feed.backend.Entity.*;
import com.nexus.feed.backend.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BadgeServiceImpl implements BadgeService {
    
    private final BadgeRepository badgeRepository;
    private final UserRepository userRepository;
    private final UserBadgeRepository userBadgeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BadgeResponse> getAllBadges() {
        return badgeRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BadgeResponse getBadgeById(Integer id) {
        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Badge not found"));
        return convertToResponse(badge);
    }

    @Override
    @Transactional(readOnly = true)
    public BadgeResponse getBadgeByName(String name) {
        Badge badge = badgeRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Badge not found"));
        return convertToResponse(badge);
    }

    @Override
    public Badge createBadge(String name, String description, String iconUrl) {
        if (badgeRepository.existsByName(name)) {
            throw new RuntimeException("Badge with this name already exists");
        }

        Badge badge = new Badge();
        badge.setName(name);
        badge.setDescription(description);
        badge.setIconUrl(iconUrl);
        
        return badgeRepository.save(badge);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BadgeResponse> getUserBadges(UUID userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return userBadgeRepository.findByUser(user).stream()
                .map(userBadge -> convertToResponse(userBadge.getBadge()))
                .collect(Collectors.toList());
    }

    @Override
    public void awardBadgeToUser(UUID userId, Integer badgeId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new RuntimeException("Badge not found"));

        // Check if user already has this badge
        if (userBadgeRepository.existsByIdUserIdAndIdBadgeId(userId, badgeId)) {
            throw new RuntimeException("User already has this badge");
        }

        UserBadge userBadge = new UserBadge();
        UserBadge.UserBadgeId userBadgeId = new UserBadge.UserBadgeId(userId, badgeId);
        userBadge.setId(userBadgeId);
        userBadge.setUser(user);
        userBadge.setBadge(badge);

        userBadgeRepository.save(userBadge);
    }

    private BadgeResponse convertToResponse(Badge badge) {
        return BadgeResponse.builder()
                .id(badge.getId())
                .name(badge.getName())
                .description(badge.getDescription())
                .iconUrl(badge.getIconUrl())
                .build();
    }
}