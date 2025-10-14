package Entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "votes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "votable_id", "votable_type"})
})
public class Votes {
    public enum VotableType {
        POST,
        COMMENT
    }

    public enum VoteValue {
        UPVOTE,
        DOWNVOTE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "votable_id", nullable = false)
    private UUID votableId;

    @Enumerated(EnumType.STRING)
    @Column(name = "votable_type", nullable = false)
    private VotableType votableType;
    @Enumerated(EnumType.STRING)
    @Column(name = "vote_value", nullable = false)
    private VoteValue voteValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;
}
