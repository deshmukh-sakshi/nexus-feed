package com.nexus.feed.backend.Auth.DTO;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
    @NotBlank(message = "ID token is required")
    String idToken
) {}
