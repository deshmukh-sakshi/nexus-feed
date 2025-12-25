package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.Auth.Entity.AppUser;
import com.nexus.feed.backend.Auth.Repository.AppUserRepository;
import com.nexus.feed.backend.Entity.Post;
import com.nexus.feed.backend.Entity.Users;
import com.nexus.feed.backend.Entity.Vote;
import com.nexus.feed.backend.Repository.PostRepository;
import com.nexus.feed.backend.Repository.UserRepository;
import com.nexus.feed.backend.Repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Post Sorting Property Tests")
class PostSortingPropertyTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private VoteRepository voteRepository;

    @BeforeEach
    void setUp() {
        voteRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    private Users createUser(String username) {
        AppUser appUser = new AppUser();
        appUser.setEmail(username + "@example.com");
        appUser.setPassword("password");

        Users user = new Users();
        user.setUsername(username);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user.setAppUser(appUser);
        appUser.setUserProfile(user);

        appUserRepository.save(appUser);
        return user;
    }

    private Post createPost(Users user, String title, Instant createdAt) {
        Post post = new Post();
        post.setTitle(title);
        post.setBody("Body for " + title);
        post.setUser(user);
        post.setCreatedAt(createdAt);
        post.setUpdatedAt(createdAt);
        return postRepository.save(post);
    }

    private void addVote(UUID voterId, UUID postId, Vote.VoteValue voteValue) {
        Vote vote = new Vote();
        vote.setId(new Vote.VoteId(voterId, postId));
        vote.setVotableType(Vote.VotableType.POST);
        vote.setVoteValue(voteValue);
        voteRepository.save(vote);
    }

    @Test
    @DisplayName("New sorting orders posts by createdAt descending")
    void newSortingOrdersByCreatedAtDescending() {
        Users user = createUser("user_new");
        Instant baseTime = Instant.now();

        createPost(user, "Post 1", baseTime.minus(30, ChronoUnit.MINUTES));
        createPost(user, "Post 2", baseTime.minus(20, ChronoUnit.MINUTES));
        createPost(user, "Post 3", baseTime.minus(10, ChronoUnit.MINUTES));

        Page<Post> posts = postRepository.findAllOrderByCreatedAtDesc(PageRequest.of(0, 100));
        List<Post> content = posts.getContent();

        assertThat(content).hasSize(3);
        for (int i = 0; i < content.size() - 1; i++) {
            Instant current = content.get(i).getCreatedAt();
            Instant next = content.get(i + 1).getCreatedAt();
            assertThat(current).isAfterOrEqualTo(next);
        }
    }

    @Test
    @DisplayName("Best sorting orders posts by net votes descending")
    void bestSortingOrdersByNetVotesDescending() {
        Users author = createUser("author_best");
        Instant baseTime = Instant.now();

        Post post1 = createPost(author, "Low votes", baseTime.minus(3, ChronoUnit.HOURS));
        Post post2 = createPost(author, "Medium votes", baseTime.minus(2, ChronoUnit.HOURS));
        Post post3 = createPost(author, "High votes", baseTime.minus(1, ChronoUnit.HOURS));

        Users voter1 = createUser("voter1");
        Users voter2 = createUser("voter2");
        Users voter3 = createUser("voter3");

        addVote(voter1.getId(), post1.getId(), Vote.VoteValue.UPVOTE);

        addVote(voter1.getId(), post2.getId(), Vote.VoteValue.UPVOTE);
        addVote(voter2.getId(), post2.getId(), Vote.VoteValue.UPVOTE);

        addVote(voter1.getId(), post3.getId(), Vote.VoteValue.UPVOTE);
        addVote(voter2.getId(), post3.getId(), Vote.VoteValue.UPVOTE);
        addVote(voter3.getId(), post3.getId(), Vote.VoteValue.UPVOTE);

        Page<Post> posts = postRepository.findAllOrderByBest(PageRequest.of(0, 100));
        List<Post> content = posts.getContent();

        assertThat(content).hasSize(3);
        assertThat(content.get(0).getTitle()).isEqualTo("High votes");
        assertThat(content.get(1).getTitle()).isEqualTo("Medium votes");
        assertThat(content.get(2).getTitle()).isEqualTo("Low votes");
    }

    @Test
    @DisplayName("Best sorting uses createdAt as tiebreaker for equal votes")
    void bestSortingUsesCreatedAtAsTiebreaker() {
        Users author = createUser("author_tie");
        Instant baseTime = Instant.now();

        Post olderPost = createPost(author, "Older post", baseTime.minus(2, ChronoUnit.HOURS));
        Post newerPost = createPost(author, "Newer post", baseTime.minus(1, ChronoUnit.HOURS));

        Users voter = createUser("voter_tie");
        addVote(voter.getId(), olderPost.getId(), Vote.VoteValue.UPVOTE);
        addVote(voter.getId(), newerPost.getId(), Vote.VoteValue.UPVOTE);

        Page<Post> posts = postRepository.findAllOrderByBest(PageRequest.of(0, 100));
        List<Post> content = posts.getContent();

        assertThat(content).hasSize(2);
        assertThat(content.get(0).getTitle()).isEqualTo("Newer post");
        assertThat(content.get(1).getTitle()).isEqualTo("Older post");
    }

    @Test
    @DisplayName("Hot sorting balances votes and recency")
    void hotSortingBalancesVotesAndRecency() {
        Users author = createUser("author_hot");
        Instant now = Instant.now();

        Post recentFewVotes = createPost(author, "Recent few votes", now.minus(1, ChronoUnit.HOURS));
        Post oldManyVotes = createPost(author, "Old many votes", now.minus(48, ChronoUnit.HOURS));

        Users voter1 = createUser("voter_hot1");
        Users voter2 = createUser("voter_hot2");

        addVote(voter1.getId(), recentFewVotes.getId(), Vote.VoteValue.UPVOTE);
        addVote(voter2.getId(), recentFewVotes.getId(), Vote.VoteValue.UPVOTE);

        for (int i = 0; i < 5; i++) {
            Users tempVoter = createUser("temp_voter_" + i);
            addVote(tempVoter.getId(), oldManyVotes.getId(), Vote.VoteValue.UPVOTE);
        }

        Page<Post> posts = postRepository.findAllOrderByHot(PageRequest.of(0, 100));
        List<Post> content = posts.getContent();

        assertThat(content).hasSize(2);
    }

    @Test
    @DisplayName("Hot score decays over time")
    void hotScoreDecaysOverTime() {
        Users author = createUser("author_decay");
        Instant now = Instant.now();

        Post veryOld = createPost(author, "Very old", now.minus(72, ChronoUnit.HOURS));
        Post recent = createPost(author, "Recent", now.minus(1, ChronoUnit.HOURS));

        Users voter = createUser("voter_decay");
        addVote(voter.getId(), veryOld.getId(), Vote.VoteValue.UPVOTE);
        addVote(voter.getId(), recent.getId(), Vote.VoteValue.UPVOTE);

        Page<Post> posts = postRepository.findAllOrderByHot(PageRequest.of(0, 100));
        List<Post> content = posts.getContent();

        assertThat(content).hasSize(2);
        assertThat(content.get(0).getTitle()).isEqualTo("Recent");
    }

    @Test
    @DisplayName("Posts with no votes are handled correctly")
    void postsWithNoVotesHandledCorrectly() {
        Users author = createUser("author_novotes");
        Instant baseTime = Instant.now();

        createPost(author, "No votes 1", baseTime.minus(2, ChronoUnit.HOURS));
        createPost(author, "No votes 2", baseTime.minus(1, ChronoUnit.HOURS));

        Page<Post> bestPosts = postRepository.findAllOrderByBest(PageRequest.of(0, 100));
        Page<Post> hotPosts = postRepository.findAllOrderByHot(PageRequest.of(0, 100));

        assertThat(bestPosts.getContent()).hasSize(2);
        assertThat(hotPosts.getContent()).hasSize(2);
    }
}
