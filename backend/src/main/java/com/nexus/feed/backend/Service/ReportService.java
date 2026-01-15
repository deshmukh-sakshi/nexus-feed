package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.Admin.DTO.AdminReportResponse;
import com.nexus.feed.backend.Entity.Report;
import com.nexus.feed.backend.Entity.Report.ReportableType;
import com.nexus.feed.backend.Entity.ReportReason;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for managing reports on posts and comments.
 */
public interface ReportService {
    
    /**
     * Creates a new report for a post.
     */
    Report createPostReport(UUID postId, UUID reporterId, ReportReason reason, String additionalDetails);

    /**
     * Creates a new report for a comment.
     */
    Report createCommentReport(UUID commentId, UUID reporterId, ReportReason reason, String additionalDetails);
    
    /**
     * Checks if a user has already reported a specific post.
     */
    boolean hasUserReportedPost(UUID postId, UUID reporterId);

    /**
     * Checks if a user has already reported a specific comment.
     */
    boolean hasUserReportedComment(UUID commentId, UUID reporterId);
    
    /**
     * Gets the total number of reports for a specific post.
     */
    long getReportCountForPost(UUID postId);

    /**
     * Gets the total number of reports for a specific comment.
     */
    long getReportCountForComment(UUID commentId);
    
    /**
     * Retrieves all reports with pagination, ordered by newest first.
     */
    Page<AdminReportResponse> getAllReports(Pageable pageable);

    /**
     * Retrieves reports filtered by type (POST or COMMENT).
     */
    Page<AdminReportResponse> getReportsByType(ReportableType type, Pageable pageable);
    
    /**
     * Retrieves reports filtered by reason with pagination.
     */
    Page<AdminReportResponse> getReportsByReason(ReportReason reason, Pageable pageable);

    /**
     * Retrieves reports filtered by both reason and type.
     */
    Page<AdminReportResponse> getReportsByReasonAndType(ReportReason reason, ReportableType type, Pageable pageable);

    // Legacy method for backward compatibility
    default Report createReport(UUID postId, UUID reporterId, ReportReason reason, String additionalDetails) {
        return createPostReport(postId, reporterId, reason, additionalDetails);
    }
}
