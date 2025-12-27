package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.Admin.DTO.AdminReportResponse;
import com.nexus.feed.backend.Entity.Report;
import com.nexus.feed.backend.Entity.ReportReason;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for managing post reports.
 */
public interface ReportService {
    
    /**
     * Creates a new report for a post.
     *
     * @param postId the ID of the post being reported
     * @param reporterId the ID of the user submitting the report
     * @param reason the reason for the report
     * @param additionalDetails optional additional details (max 500 chars)
     * @return the created Report entity
     * @throws DuplicateReportException if the user has already reported this post
     * @throws ResourceNotFoundException if the post or reporter is not found
     */
    Report createReport(UUID postId, UUID reporterId, ReportReason reason, String additionalDetails);
    
    /**
     * Checks if a user has already reported a specific post.
     *
     * @param postId the ID of the post
     * @param reporterId the ID of the user
     * @return true if the user has already reported the post, false otherwise
     */
    boolean hasUserReportedPost(UUID postId, UUID reporterId);
    
    /**
     * Gets the total number of reports for a specific post.
     *
     * @param postId the ID of the post
     * @return the count of reports for the post
     */
    long getReportCountForPost(UUID postId);
    
    /**
     * Retrieves all reports with pagination, ordered by newest first.
     *
     * @param pageable pagination parameters
     * @return a page of AdminReportResponse DTOs
     */
    Page<AdminReportResponse> getAllReports(Pageable pageable);
    
    /**
     * Retrieves reports filtered by reason with pagination.
     *
     * @param reason the report reason to filter by
     * @param pageable pagination parameters
     * @return a page of AdminReportResponse DTOs matching the reason
     */
    Page<AdminReportResponse> getReportsByReason(ReportReason reason, Pageable pageable);
}
