package com.nexus.feed.backend.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"post"})
@EqualsAndHashCode(exclude = {"post"})
@Entity
@Table(name = "post_images", indexes = {
    @Index(name = "idx_post_image_post_id", columnList = "post_id")
})
public class PostImage {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "post_id")
    private Post post;

    @NotBlank
    @Size(max = 2048)
    private String imageUrl;

    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
