package com.nexus.feed.backend.DTO;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateRequest {
    @Size(max = 300, message = "Title must not exceed 300 characters")
    private String title;

    @Size(max = 2048, message = "URL must not exceed 2048 characters")
    private String url;

    private String body;

    private List<String> imageUrls;
}