package com.nexus.feed.backend.DTO;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteRequest {
    @NotNull(message = "Vote value is required")
    @Pattern(regexp = "UPVOTE|DOWNVOTE", message = "Vote must be either UPVOTE or DOWNVOTE")
    private String voteValue;
}