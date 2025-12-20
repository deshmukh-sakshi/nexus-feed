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
@Entity
@Table(name = "post_images", indexes = {
    @Index(name = "idx_post_image_post_id", columnList = "post_id"),
    @Index(name = "idx_post_image_order", columnList = "post_id, order_index")
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

    @Column(name = "order_index")
    private Integer orderIndex = 0;

    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostImage postImage = (PostImage) o;
        return Objects.equals(imageUrl, postImage.imageUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageUrl);
    }
}
