package com.nexus.feed.backend.Auth.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GoogleCompleteRequest(
    @NotBlank(message = "Temp token is required")
    String tempToken,

    @NotBlank(message = "Username is required")
    @Size(max = 50, message = "Username must be less than 50 characters")
    String username
) {}
