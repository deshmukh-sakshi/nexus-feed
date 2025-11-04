package com.nexus.feed.backend.Repository;

import com.nexus.feed.backend.Entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Vote.VoteId> {
    
    @Query("SELECT v FROM Vote v WHERE v.id.userId = :userId AND v.id.votableId = :votableId AND v.votableType = :votableType")
    Optional<Vote> findByUserIdAndVotableIdAndVotableType(
        @Param("userId") UUID userId, 
        @Param("votableId") UUID votableId, 
        @Param("votableType") Vote.VotableType votableType
    );
    
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.id.votableId = :votableId AND v.votableType = :votableType AND v.voteValue = :voteValue")
    long countByVotableIdAndVotableTypeAndVoteValue(
        @Param("votableId") UUID votableId, 
        @Param("votableType") Vote.VotableType votableType, 
        @Param("voteValue") Vote.VoteValue voteValue
    );
    
    void deleteByIdVotableIdAndVotableType(UUID votableId, Vote.VotableType votableType);
}