package com.nexus.feed.backend.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "votes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "votable_id", "votable_type"})
})
public class Vote {
    @EmbeddedId
    private VoteId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VotableType votableType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoteValue voteValue;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class VoteId implements Serializable {
        private UUID userId;
        private UUID votableId;
    }

    public enum VotableType { POST, COMMENT }
    public enum VoteValue { UPVOTE, DOWNVOTE }
}
