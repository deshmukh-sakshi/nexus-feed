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
@ToString(exclude = {"post", "parentComment", "replies"})
@EqualsAndHashCode(exclude = {"post", "parentComment", "replies"})
@Entity
@Table(name = "comments", indexes = {
    @Index(name = "idx_comment_post_id", columnList = "post_id"),
    @Index(name = "idx_comment_user_id", columnList = "user_id"),
    @Index(name = "idx_comment_parent_id", columnList = "parent_comment_id"),
    @Index(name = "idx_comment_post_created", columnList = "post_id, created_at"),
    @Index(name = "idx_comment_user_created", columnList = "user_id, created_at")
})
public class Comment {
    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank
    private String body;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private Users user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<Comment> replies = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
