package com.nexus.feed.backend.Admin.DTO;

import java.time.Instant;
import java.util.UUID;

public record AdminUserResponse(
    UUID id,
    String username,
    String email,
    String role,
    Long karma,
    String profilePictureUrl,
    Instant createdAt,
    int postCount,
    int commentCount
) {}
