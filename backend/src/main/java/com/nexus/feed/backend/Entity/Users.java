package com.nexus.feed.backend.Entity;

import com.nexus.feed.backend.Auth.Entity.AppUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"posts", "comments", "userBadges"})
@EqualsAndHashCode(exclude = {"posts", "comments", "userBadges"})
@Entity
@Table(name = "users", 
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "username")
    },
    indexes = {
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_app_user", columnList = "app_user_id")
    }
)
public class Users {
    @Id
    @GeneratedValue
    private UUID id;

    @Size(max = 50)
    private String username;

    private String bio;

    @Size(max = 255)
    private String profilePictureUrl;

    private Instant createdAt;
    private Instant updatedAt;

    @OneToOne
    @JoinColumn(name = "app_user_id", unique = true, nullable = false)
    private AppUser appUser;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserBadge> userBadges = new ArrayList<>();

    @Column(nullable = false)
    private Long karma = 0L;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
