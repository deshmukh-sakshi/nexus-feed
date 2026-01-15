package com.nexus.feed.backend.Repository;

import com.nexus.feed.backend.Entity.Report;
import com.nexus.feed.backend.Entity.Report.ReportableType;
import com.nexus.feed.backend.Entity.ReportReason;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    
    /**
     * Check if a user has already reported a specific item.
     */
    boolean existsByReportableIdAndReportableTypeAndReporterId(UUID reportableId, ReportableType reportableType, UUID reporterId);
    
    /**
     * Count the number of reports for a specific item.
     */
    long countByReportableIdAndReportableType(UUID reportableId, ReportableType reportableType);

    /**
     * Count reports for a post (convenience method for backward compatibility).
     */
    default long countByPostId(UUID postId) {
        return countByReportableIdAndReportableType(postId, ReportableType.POST);
    }
    
    /**
     * Find all reports ordered by newest first.
     */
    @Query(value = "SELECT r FROM Report r JOIN FETCH r.reporter ORDER BY r.createdAt DESC",
           countQuery = "SELECT COUNT(r) FROM Report r")
    Page<Report> findAllWithDetails(Pageable pageable);
    
    /**
     * Find reports filtered by reason.
     */
    @Query(value = "SELECT r FROM Report r JOIN FETCH r.reporter WHERE r.reason = :reason ORDER BY r.createdAt DESC",
           countQuery = "SELECT COUNT(r) FROM Report r WHERE r.reason = :reason")
    Page<Report> findByReasonWithDetails(@Param("reason") ReportReason reason, Pageable pageable);

    /**
     * Find reports filtered by reportable type.
     */
    @Query(value = "SELECT r FROM Report r JOIN FETCH r.reporter WHERE r.reportableType = :type ORDER BY r.createdAt DESC",
           countQuery = "SELECT COUNT(r) FROM Report r WHERE r.reportableType = :type")
    Page<Report> findByReportableTypeWithDetails(@Param("type") ReportableType type, Pageable pageable);

    /**
     * Find reports filtered by reason and reportable type.
     */
    @Query(value = "SELECT r FROM Report r JOIN FETCH r.reporter WHERE r.reason = :reason AND r.reportableType = :type ORDER BY r.createdAt DESC",
           countQuery = "SELECT COUNT(r) FROM Report r WHERE r.reason = :reason AND r.reportableType = :type")
    Page<Report> findByReasonAndReportableTypeWithDetails(@Param("reason") ReportReason reason, @Param("type") ReportableType type, Pageable pageable);

    /**
     * Delete all reports for a specific item.
     */
    void deleteByReportableIdAndReportableType(UUID reportableId, ReportableType reportableType);
}
