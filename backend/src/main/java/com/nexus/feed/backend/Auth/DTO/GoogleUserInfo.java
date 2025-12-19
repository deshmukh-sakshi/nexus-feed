package com.nexus.feed.backend.Auth.DTO;

public record GoogleUserInfo(
    String email,
    String name,
    String pictureUrl,
    String providerId
) {}
