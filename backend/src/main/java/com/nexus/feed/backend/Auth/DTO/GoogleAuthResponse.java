package com.nexus.feed.backend.Auth.DTO;

public record GoogleAuthResponse(
    boolean needsUsername,
    String tempToken,
    String email,
    String suggestedName,
    String pictureUrl
) {}
