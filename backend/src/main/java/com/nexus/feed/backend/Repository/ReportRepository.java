package com.nexus.feed.backend.Repository;

import com.nexus.feed.backend.Entity.Report;
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
     * Check if a user has already reported a specific post.
     */
    boolean existsByPostIdAndReporterId(UUID postId, UUID reporterId);
    
    /**
     * Count the number of reports for a specific post.
     */
    long countByPostId(UUID postId);
    
    /**
     * Find all reports with post and reporter details, ordered by newest first.
     */
    @Query("SELECT r FROM Report r JOIN FETCH r.post JOIN FETCH r.reporter ORDER BY r.createdAt DESC")
    Page<Report> findAllWithDetails(Pageable pageable);
    
    /**
     * Find reports filtered by reason with post and reporter details.
     */
    @Query("SELECT r FROM Report r JOIN FETCH r.post JOIN FETCH r.reporter WHERE r.reason = :reason ORDER BY r.createdAt DESC")
    Page<Report> findByReasonWithDetails(@Param("reason") ReportReason reason, Pageable pageable);
}
