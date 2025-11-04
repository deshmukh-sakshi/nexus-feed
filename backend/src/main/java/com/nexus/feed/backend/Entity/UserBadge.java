package com.nexus.feed.backend.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "badge"})
@EqualsAndHashCode(exclude = {"user", "badge"})
@Entity
@Table(name = "user_badges")
public class UserBadge {
    @EmbeddedId // This says "use this composite object as the primary key"
    private UserBadgeId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private Users user;

    @ManyToOne
    @MapsId("badgeId")
    @JoinColumn(name = "badge_id")
    private Badge badge;

    private LocalDateTime awardedAt;

    @PrePersist
    protected void onCreate() {
        awardedAt = LocalDateTime.now();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable // This says "this class can be embedded in other entities"
    public static class UserBadgeId implements Serializable {
        private UUID userId;
        private Integer badgeId;
    }
}