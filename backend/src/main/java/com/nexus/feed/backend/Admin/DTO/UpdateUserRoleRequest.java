package com.nexus.feed.backend.Admin.DTO;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRoleRequest(
    @NotBlank(message = "Role is required")
    String role
) {}
