package com.nexus.feed.backend.DTO;

import com.nexus.feed.backend.Entity.ReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for submitting a report against a post.
 */
public record ReportRequest(
    @NotNull(message = "Reason is required")
    ReportReason reason,
    
    @Size(max = 500, message = "Additional details cannot exceed 500 characters")
    String additionalDetails
) {}
