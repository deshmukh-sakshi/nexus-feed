package com.nexus.feed.backend.DTO;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private String bio;
    private String profilePictureUrl;
    private Instant createdAt;
    private Instant updatedAt;
    private Long karma;
    
    // Note: passwordHash is intentionally excluded for security
}