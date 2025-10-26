package com.nexus.feed.backend.DTO;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {
    private UUID id;
    private String title;
    private String url;
    private String body;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    
    private UUID userId;
    private String username;
    
    // Images
    private List<String> imageUrls;
    
    // Engagement metrics (can be calculated)
    private int commentCount;
    private int upvotes;
    private int downvotes;
    
    // Current user's vote (if authenticated)
    private String userVote; // "UPVOTE", "DOWNVOTE", or null
}