package com.nexus.feed.backend.Auth.DTO;

import java.util.UUID;

public record AuthResponse(UUID userId, String username, String email, String token) {
}
