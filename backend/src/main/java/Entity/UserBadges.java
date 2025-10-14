package Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
class UserBadgeId implements Serializable {
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "badge_id")
    private Integer badgeId;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_badges")
public class UserBadges {

    @EmbeddedId
    private UserBadgeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("badgeId")
    @JoinColumn(name = "badge_id")
    private Badges badge;

    @CreatedDate
    @Column(name = "awarded_at", updatable = false)
    private Instant awardedAt;
}
