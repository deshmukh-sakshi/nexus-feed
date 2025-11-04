package com.nexus.feed.backend.DTO;

import com.nexus.feed.backend.Entity.Vote;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteRequest {
    @NotNull(message = "Votable ID is required")
    private UUID votableId;
    
    @NotNull(message = "Votable type is required")
    private Vote.VotableType votableType;
    
    @NotNull(message = "Vote value is required")
    private Vote.VoteValue voteValue;
}