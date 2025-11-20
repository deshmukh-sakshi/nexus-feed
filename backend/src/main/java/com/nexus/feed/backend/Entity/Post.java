package com.nexus.feed.backend.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"images", "comments"})
@EqualsAndHashCode(exclude = {"images", "comments"})
@Entity
@Table(name = "posts", indexes = {
    @Index(name = "idx_post_user_id", columnList = "user_id"),
    @Index(name = "idx_post_created_at", columnList = "created_at"),
    @Index(name = "idx_post_user_created", columnList = "user_id, created_at")
})
public class Post {
    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank
    @Size(max = 300)
    private String title;

    @Size(max = 2048)
    private String url;

    private String body;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<PostImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<Comment> comments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}