package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.DTO.VoteRequest;
import com.nexus.feed.backend.Entity.Vote;
import java.util.UUID;

public interface VoteService {
    void vote(UUID userId, VoteRequest request);
    void removeVote(UUID userId, UUID votableId, Vote.VotableType votableType);
    long getUpvoteCount(UUID votableId, Vote.VotableType votableType);
    long getDownvoteCount(UUID votableId, Vote.VotableType votableType);
    Vote.VoteValue getUserVote(UUID userId, UUID votableId, Vote.VotableType votableType);
}