package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.DTO.TagResponse;
import com.nexus.feed.backend.DTO.TrendingTagResponse;
import com.nexus.feed.backend.Entity.Post;
import com.nexus.feed.backend.Entity.Tag;
import com.nexus.feed.backend.Repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    public Set<Tag> getOrCreateTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }

        // Normalize tag names (lowercase, trim, remove duplicates)
        Set<String> normalizedNames = tagNames.stream()
                .map(name -> name.trim().toLowerCase())
                .filter(name -> !name.isEmpty() && name.length() <= 50)
                .collect(Collectors.toSet());

        if (normalizedNames.isEmpty()) {
            return new HashSet<>();
        }

        // Find existing tags
        List<Tag> existingTags = tagRepository.findByNameInIgnoreCase(normalizedNames);
        Set<String> existingNames = existingTags.stream()
                .map(tag -> tag.getName().toLowerCase())
                .collect(Collectors.toSet());

        // Create new tags for names that don't exist
        Set<Tag> result = new HashSet<>(existingTags);
        for (String name : normalizedNames) {
            if (!existingNames.contains(name)) {
                Tag newTag = Tag.builder().name(name).build();
                result.add(tagRepository.save(newTag));
                log.info("Created new tag: {}", name);
            }
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> searchTags(String query) {
        if (query == null || query.trim().isEmpty()) {
            log.debug("Empty search query, returning trending tags");
            return getTrendingTags(10);
        }
        
        log.debug("Searching tags with query: {}", query.trim());
        return tagRepository.searchByName(query.trim()).stream()
                .limit(10)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getTrendingTags(int limit) {
        return tagRepository.findTopTags().stream()
                .limit(limit)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private TagResponse convertToResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .postCount(tag.getPosts() != null ? tag.getPosts().size() : 0)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateTrendingScore(Tag tag) {
        if (tag.getPosts() == null || tag.getPosts().isEmpty()) {
            return 0.0;
        }

        Instant now = Instant.now();
        Instant oneDayAgo = now.minus(Duration.ofDays(1));
        Instant sevenDaysAgo = now.minus(Duration.ofDays(7));
        Instant thirtyDaysAgo = now.minus(Duration.ofDays(30));

        double score = 0.0;
        for (Post post : tag.getPosts()) {
            Instant createdAt = post.getCreatedAt();
            if (createdAt == null || createdAt.isBefore(thirtyDaysAgo)) {
                // Posts older than 30 days don't contribute
                continue;
            }
            
            if (createdAt.isAfter(oneDayAgo)) {
                // Posts from last 24 hours: weight 1.0
                score += 1.0;
            } else if (createdAt.isAfter(sevenDaysAgo)) {
                // Posts from 1-7 days: weight 0.5
                score += 0.5;
            } else {
                // Posts from 7-30 days: weight 0.1
                score += 0.1;
            }
        }
        return score;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrendingTagResponse> getTrendingTagsWithScore(int limit) {
        return tagRepository.findAllWithPosts().stream()
                .map(tag -> {
                    double score = calculateTrendingScore(tag);
                    return TrendingTagResponse.builder()
                            .id(tag.getId())
                            .name(tag.getName())
                            .postCount(tag.getPosts() != null ? tag.getPosts().size() : 0)
                            .trendingScore(score)
                            .build();
                })
                .filter(response -> response.getTrendingScore() > 0) // Exclude tags with no recent posts
                .sorted(Comparator.comparingDouble(TrendingTagResponse::getTrendingScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
