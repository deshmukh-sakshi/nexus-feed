package com.nexus.feed.backend.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a user report against a post or comment.
 * Each user can only report a specific item once (enforced by unique constraint).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reports",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_report_reportable_reporter", columnNames = {"reportable_id", "reportable_type", "reporter_id"})
    },
    indexes = {
        @Index(name = "idx_report_reportable", columnList = "reportable_id, reportable_type"),
        @Index(name = "idx_report_created_at", columnList = "created_at"),
        @Index(name = "idx_report_reason", columnList = "reason")
    }
)
public class Report {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "reportable_id", nullable = false)
    private UUID reportableId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reportable_type", nullable = false)
    private ReportableType reportableType;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Users reporter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportReason reason;

    @Column(name = "additional_details", length = 2048)
    private String additionalDetails;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public enum ReportableType {
        POST, COMMENT
    }
}
