package com.nexus.feed.backend.Admin.DTO;

import com.nexus.feed.backend.Entity.Report.ReportableType;
import com.nexus.feed.backend.Entity.ReportReason;

import java.time.Instant;
import java.util.UUID;

/**
 * Admin response DTO for viewing report details.
 */
public record AdminReportResponse(
    UUID id,
    UUID reportableId,
    ReportableType reportableType,
    String contentTitle,
    String contentPreview,
    UUID reporterId,
    String reporterUsername,
    ReportReason reason,
    String reasonDisplayName,
    String additionalDetails,
    Instant createdAt
) {}
