package com.nexus.feed.backend.Admin.DTO;

import java.time.Instant;
import java.util.UUID;

public record AdminCommentResponse(
    UUID id,
    String body,
    UUID userId,
    String username,
    UUID postId,
    String postTitle,
    int upvotes,
    int downvotes,
    Instant createdAt,
    Instant updatedAt
) {}
