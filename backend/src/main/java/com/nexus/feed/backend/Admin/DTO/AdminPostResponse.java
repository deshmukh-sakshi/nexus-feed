package com.nexus.feed.backend.Admin.DTO;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminPostResponse(
    UUID id,
    String title,
    String body,
    UUID userId,
    String username,
    List<String> imageUrls,
    List<String> tags,
    int upvotes,
    int downvotes,
    int commentCount,
    int reportCount,
    Instant createdAt,
    Instant updatedAt
) {}
