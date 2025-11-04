package com.nexus.feed.backend.DTO;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentUpdateRequest {
    @NotBlank(message = "Comment body is required")
    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    private String body;
}