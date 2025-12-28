package com.nexus.feed.backend.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a user report against a post.
 * Each user can only report a post once (enforced by unique constraint).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reports",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_report_post_reporter", columnNames = {"post_id", "reporter_id"})
    },
    indexes = {
        @Index(name = "idx_report_post_id", columnList = "post_id"),
        @Index(name = "idx_report_created_at", columnList = "created_at"),
        @Index(name = "idx_report_reason", columnList = "reason")
    }
)
public class Report {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

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
}
