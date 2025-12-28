package com.nexus.feed.backend.DTO;

/**
 * Response DTO for checking if a user has reported a post.
 */
public record ReportStatusResponse(
    boolean hasReported
) {}
