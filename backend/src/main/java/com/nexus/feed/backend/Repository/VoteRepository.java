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
    
    @Query("SELECT v.id.votableId as votableId, v.voteValue as voteValue, COUNT(v) as count " +
           "FROM Vote v WHERE v.id.votableId IN :votableIds AND v.votableType = :votableType " +
           "GROUP BY v.id.votableId, v.voteValue")
    java.util.List<VoteCount> countByVotableIdsAndVotableType(
        @Param("votableIds") java.util.List<UUID> votableIds,
        @Param("votableType") Vote.VotableType votableType
    );
    
    @Query("SELECT v FROM Vote v WHERE v.id.userId = :userId AND v.id.votableId IN :votableIds AND v.votableType = :votableType")
    java.util.List<Vote> findByUserIdAndVotableIdsAndVotableType(
        @Param("userId") UUID userId,
        @Param("votableIds") java.util.List<UUID> votableIds,
        @Param("votableType") Vote.VotableType votableType
    );
    
    void deleteByIdVotableIdAndVotableType(UUID votableId, Vote.VotableType votableType);
    
    void deleteByIdVotableId(UUID votableId);
    
    void deleteByIdUserId(UUID userId);
    
    interface VoteCount {
        UUID getVotableId();
        Vote.VoteValue getVoteValue();
        Long getCount();
    }

    @Query("SELECT COALESCE(SUM(CASE WHEN v.voteValue = 'UPVOTE' THEN 1 ELSE -1 END), 0) " +
           "FROM Vote v JOIN Post p ON v.id.votableId = p.id " +
           "WHERE p.user.id = :userId AND v.votableType = 'POST' AND v.id.userId != :userId")
    long calculatePostKarma(@Param("userId") UUID userId);

    @Query("SELECT COALESCE(SUM(CASE WHEN v.voteValue = 'UPVOTE' THEN 1 ELSE -1 END), 0) " +
           "FROM Vote v JOIN Comment c ON v.id.votableId = c.id " +
           "WHERE c.user.id = :userId AND v.votableType = 'COMMENT' AND v.id.userId != :userId")
    long calculateCommentKarma(@Param("userId") UUID userId);
}