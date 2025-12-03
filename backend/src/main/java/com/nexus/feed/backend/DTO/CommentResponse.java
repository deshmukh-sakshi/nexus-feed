package com.nexus.feed.backend.DTO;

import lombok.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    private UUID id;
    private String body;
    private Instant createdAt;
    private Instant updatedAt;
    
    // User info
    private UUID userId;
    private String username;
    private String userProfilePictureUrl;
    
    // Post info
    private UUID postId;
    
    // Parent comment (for replies)
    private UUID parentCommentId;
    
    // Nested replies (can be limited to avoid deep nesting)
    private List<CommentResponse> replies;
    
    // Engagement metrics
    private int upvotes;
    private int downvotes;
    private String userVote; // Current user's vote
}