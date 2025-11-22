package com.nexus.feed.backend.Repository;

import com.nexus.feed.backend.Auth.Entity.AppUser;
import com.nexus.feed.backend.Auth.Repository.AppUserRepository;
import com.nexus.feed.backend.Entity.Post;
import com.nexus.feed.backend.Entity.Users;
import com.nexus.feed.backend.Entity.Vote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("VoteRepository Tests")
class VoteRepositoryTest {

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    private Users user1;
    private Users user2;
    private Post post1;
    private Post post2;

    @BeforeEach
    void setUp() {
        // Clean up
        voteRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
        appUserRepository.deleteAll();

        // Create users
        AppUser appUser1 = new AppUser();
        appUser1.setEmail("user1@example.com");
        appUser1.setPassword("password");

        user1 = new Users();
        user1.setUsername("user1");
        user1.setCreatedAt(Instant.now());
        user1.setUpdatedAt(Instant.now());
        user1.setAppUser(appUser1);
        appUser1.setUserProfile(user1);

        AppUser appUser2 = new AppUser();
        appUser2.setEmail("user2@example.com");
        appUser2.setPassword("password");

        user2 = new Users();
        user2.setUsername("user2");
        user2.setCreatedAt(Instant.now());
        user2.setUpdatedAt(Instant.now());
        user2.setAppUser(appUser2);
        appUser2.setUserProfile(user2);

        appUserRepository.save(appUser1);
        appUserRepository.save(appUser2);

        // Create posts
        post1 = new Post();
        post1.setTitle("Post 1");
        post1.setBody("Body 1");
        post1.setUser(user1);
        post1.setCreatedAt(Instant.now());
        post1.setUpdatedAt(Instant.now());

        post2 = new Post();
        post2.setTitle("Post 2");
        post2.setBody("Body 2");
        post2.setUser(user2);
        post2.setCreatedAt(Instant.now());
        post2.setUpdatedAt(Instant.now());

        postRepository.save(post1);
        postRepository.save(post2);
    }

    @Test
    @DisplayName("Should save and find vote by user and votable")
    void shouldSaveAndFindVote() {
        // Given
        Vote vote = new Vote();
        Vote.VoteId voteId = new Vote.VoteId(user1.getId(), post1.getId());
        vote.setId(voteId);
        vote.setVotableType(Vote.VotableType.POST);
        vote.setVoteValue(Vote.VoteValue.UPVOTE);

        // When
        voteRepository.save(vote);

        // Then
        Optional<Vote> found = voteRepository.findByUserIdAndVotableIdAndVotableType(
                user1.getId(), post1.getId(), Vote.VotableType.POST);

        assertThat(found).isPresent();
        assertThat(found.get().getVoteValue()).isEqualTo(Vote.VoteValue.UPVOTE);
    }

    @Test
    @DisplayName("Should count votes by votable and vote value")
    void shouldCountVotesByVotableAndVoteValue() {
        // Given
        Vote vote1 = new Vote();
        vote1.setId(new Vote.VoteId(user1.getId(), post1.getId()));
        vote1.setVotableType(Vote.VotableType.POST);
        vote1.setVoteValue(Vote.VoteValue.UPVOTE);

        Vote vote2 = new Vote();
        vote2.setId(new Vote.VoteId(user2.getId(), post1.getId()));
        vote2.setVotableType(Vote.VotableType.POST);
        vote2.setVoteValue(Vote.VoteValue.UPVOTE);

        Vote vote3 = new Vote();
        vote3.setId(new Vote.VoteId(user1.getId(), post2.getId()));
        vote3.setVotableType(Vote.VotableType.POST);
        vote3.setVoteValue(Vote.VoteValue.DOWNVOTE);

        voteRepository.saveAll(Arrays.asList(vote1, vote2, vote3));

        // When
        long upvotes = voteRepository.countByVotableIdAndVotableTypeAndVoteValue(
                post1.getId(), Vote.VotableType.POST, Vote.VoteValue.UPVOTE);
        long downvotes = voteRepository.countByVotableIdAndVotableTypeAndVoteValue(
                post1.getId(), Vote.VotableType.POST, Vote.VoteValue.DOWNVOTE);

        // Then
        assertThat(upvotes).isEqualTo(2);
        assertThat(downvotes).isEqualTo(0);
    }

    @Test
    @DisplayName("Should batch count votes for multiple votables")
    void shouldBatchCountVotesForMultipleVotables() {
        // Given
        Vote vote1 = new Vote();
        vote1.setId(new Vote.VoteId(user1.getId(), post1.getId()));
        vote1.setVotableType(Vote.VotableType.POST);
        vote1.setVoteValue(Vote.VoteValue.UPVOTE);

        Vote vote2 = new Vote();
        vote2.setId(new Vote.VoteId(user2.getId(), post1.getId()));
        vote2.setVotableType(Vote.VotableType.POST);
        vote2.setVoteValue(Vote.VoteValue.UPVOTE);

        Vote vote3 = new Vote();
        vote3.setId(new Vote.VoteId(user1.getId(), post2.getId()));
        vote3.setVotableType(Vote.VotableType.POST);
        vote3.setVoteValue(Vote.VoteValue.DOWNVOTE);

        voteRepository.saveAll(Arrays.asList(vote1, vote2, vote3));

        // When
        List<UUID> postIds = Arrays.asList(post1.getId(), post2.getId());
        List<VoteRepository.VoteCount> voteCounts = voteRepository.countByVotableIdsAndVotableType(
                postIds, Vote.VotableType.POST);

        // Then
        assertThat(voteCounts).hasSize(2);
        
        // Verify post1 has 2 upvotes
        VoteRepository.VoteCount post1Upvotes = voteCounts.stream()
                .filter(vc -> vc.getVotableId().equals(post1.getId()) && 
                             vc.getVoteValue() == Vote.VoteValue.UPVOTE)
                .findFirst()
                .orElse(null);
        assertThat(post1Upvotes).isNotNull();
        assertThat(post1Upvotes.getCount()).isEqualTo(2L);

        // Verify post2 has 1 downvote
        VoteRepository.VoteCount post2Downvotes = voteCounts.stream()
                .filter(vc -> vc.getVotableId().equals(post2.getId()) && 
                             vc.getVoteValue() == Vote.VoteValue.DOWNVOTE)
                .findFirst()
                .orElse(null);
        assertThat(post2Downvotes).isNotNull();
        assertThat(post2Downvotes.getCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should find user votes for multiple votables")
    void shouldFindUserVotesForMultipleVotables() {
        // Given
        Vote vote1 = new Vote();
        vote1.setId(new Vote.VoteId(user1.getId(), post1.getId()));
        vote1.setVotableType(Vote.VotableType.POST);
        vote1.setVoteValue(Vote.VoteValue.UPVOTE);

        Vote vote2 = new Vote();
        vote2.setId(new Vote.VoteId(user1.getId(), post2.getId()));
        vote2.setVotableType(Vote.VotableType.POST);
        vote2.setVoteValue(Vote.VoteValue.DOWNVOTE);

        voteRepository.saveAll(Arrays.asList(vote1, vote2));

        // When
        List<UUID> postIds = Arrays.asList(post1.getId(), post2.getId());
        List<Vote> userVotes = voteRepository.findByUserIdAndVotableIdsAndVotableType(
                user1.getId(), postIds, Vote.VotableType.POST);

        // Then
        assertThat(userVotes).hasSize(2);
        assertThat(userVotes).extracting(v -> v.getId().getVotableId())
                .containsExactlyInAnyOrder(post1.getId(), post2.getId());
    }

    @Test
    @DisplayName("Should delete vote")
    void shouldDeleteVote() {
        // Given
        Vote vote = new Vote();
        Vote.VoteId voteId = new Vote.VoteId(user1.getId(), post1.getId());
        vote.setId(voteId);
        vote.setVotableType(Vote.VotableType.POST);
        vote.setVoteValue(Vote.VoteValue.UPVOTE);
        voteRepository.save(vote);

        // When
        voteRepository.deleteById(voteId);

        // Then
        Optional<Vote> found = voteRepository.findById(voteId);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should handle empty batch query")
    void shouldHandleEmptyBatchQuery() {
        // When
        List<VoteRepository.VoteCount> voteCounts = voteRepository.countByVotableIdsAndVotableType(
                List.of(), Vote.VotableType.POST);

        // Then
        assertThat(voteCounts).isEmpty();
    }
}
