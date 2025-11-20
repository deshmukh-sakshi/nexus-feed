package com.nexus.feed.backend.DTO;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDetailResponse {
    private PostResponse post;
    private List<CommentResponse> comments;
}
