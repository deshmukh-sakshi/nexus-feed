package com.nexus.feed.backend.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "votes", 
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "votable_id", "votable_type"})
    },
    indexes = {
        @Index(name = "idx_vote_votable", columnList = "votable_id, votable_type"),
        @Index(name = "idx_vote_votable_value", columnList = "votable_id, votable_type, vote_value"),
        @Index(name = "idx_vote_user_votable", columnList = "user_id, votable_id, votable_type")
    }
)
public class Vote {
    @EmbeddedId
    private VoteId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "votable_type", nullable = false)
    private VotableType votableType;

    @Enumerated(EnumType.STRING)
    @Column(name = "vote_value", nullable = false)
    private VoteValue voteValue;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class VoteId implements Serializable {
        @Column(name = "user_id")
        private UUID userId;
        
        @Column(name = "votable_id")
        private UUID votableId;
    }

    public enum VotableType { POST, COMMENT }
    public enum VoteValue { UPVOTE, DOWNVOTE }
}
