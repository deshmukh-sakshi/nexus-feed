package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.DTO.VoteRequest;
import com.nexus.feed.backend.Entity.Comment;
import com.nexus.feed.backend.Entity.Post;
import com.nexus.feed.backend.Entity.Users;
import com.nexus.feed.backend.Entity.Vote;
import com.nexus.feed.backend.Exception.ResourceNotFoundException;
import com.nexus.feed.backend.Repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VoteServiceImpl Unit Tests")
class VoteServiceImplTest {

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KarmaService karmaService;

    @Mock
    private BadgeAwardingService badgeAwardingService;

    @InjectMocks
    private VoteServiceImpl voteService;

    private UUID userId;
    private UUID postId;
    private UUID commentId;
    private UUID authorId;
    private Users user;
    private Users author;
    private Post post;
    private Comment comment;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        postId = UUID.randomUUID();
        commentId = UUID.randomUUID();
        authorId = UUID.randomUUID();

        user = new Users();
        user.setId(userId);
        user.setUsername("tester");

        author = new Users();
        author.setId(authorId);
        author.setUsername("author");

        post = new Post();
        post.setId(postId);
        post.setTitle("Test Post");
        post.setUser(author);

        comment = new Comment();
        comment.setId(commentId);
        comment.setBody("Test Comment");
        comment.setUser(author);
    }

    @Test
    @DisplayName("Should create new upvote on post successfully")
    void shouldCreateNewUpvoteOnPostSuccessfully() {
        // Given
        VoteRequest request = new VoteRequest();
        request.setVotableId(postId);
        request.setVotableType(Vote.VotableType.POST);
        request.setVoteValue(Vote.VoteValue.UPVOTE);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(postRepository.existsById(postId)).thenReturn(true);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(voteRepository.findById(any(Vote.VoteId.class))).thenReturn(Optional.empty());
        when(voteRepository.save(any(Vote.class))).thenReturn(new Vote());

        // When
        voteService.vote(userId, request);

        // Then
        verify(voteRepository).save(any(Vote.class));
        verify(karmaService).updateKarmaForVote(authorId, userId, 1);
    }

    @Test
    @DisplayName("Should create new downvote on post successfully")
    void shouldCreateNewDownvoteOnPostSuccessfully() {
        // Given
        VoteRequest request = new VoteRequest();
        request.setVotableId(postId);
        request.setVotableType(Vote.VotableType.POST);
        request.setVoteValue(Vote.VoteValue.DOWNVOTE);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(postRepository.existsById(postId)).thenReturn(true);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(voteRepository.findById(any(Vote.VoteId.class))).thenReturn(Optional.empty());
        when(voteRepository.save(any(Vote.class))).thenReturn(new Vote());

        // When
        voteService.vote(userId, request);

        // Then
        verify(voteRepository).save(any(Vote.class));
        verify(karmaService).updateKarmaForVote(authorId, userId, -1);
    }

    @Test
    @DisplayName("Should toggle off existing vote when same vote value")
    void shouldToggleOffExistingVote() {
        // Given
        VoteRequest request = new VoteRequest();
        request.setVotableId(postId);
        request.setVotableType(Vote.VotableType.POST);
        request.setVoteValue(Vote.VoteValue.UPVOTE);

        Vote existingVote = new Vote();
        existingVote.setId(new Vote.VoteId(userId, postId));
        existingVote.setVotableType(Vote.VotableType.POST);
        existingVote.setVoteValue(Vote.VoteValue.UPVOTE);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(postRepository.existsById(postId)).thenReturn(true);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(voteRepository.findById(any(Vote.VoteId.class))).thenReturn(Optional.of(existingVote));

        // When
        voteService.vote(userId, request);

        // Then
        verify(voteRepository).delete(existingVote);
        verify(karmaService).updateKarmaForVote(authorId, userId, -1);
    }

    @Test
    @DisplayName("Should flip vote when different vote value")
    void shouldFlipVoteWhenDifferentValue() {
        // Given
        VoteRequest request = new VoteRequest();
        request.setVotableId(postId);
        request.setVotableType(Vote.VotableType.POST);
        request.setVoteValue(Vote.VoteValue.UPVOTE);

        Vote existingVote = new Vote();
        existingVote.setId(new Vote.VoteId(userId, postId));
        existingVote.setVotableType(Vote.VotableType.POST);
        existingVote.setVoteValue(Vote.VoteValue.DOWNVOTE);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(postRepository.existsById(postId)).thenReturn(true);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(voteRepository.findById(any(Vote.VoteId.class))).thenReturn(Optional.of(existingVote));
        when(voteRepository.save(any(Vote.class))).thenReturn(existingVote);

        // When
        voteService.vote(userId, request);

        // Then
        verify(voteRepository).save(any(Vote.class));
        verify(karmaService).updateKarmaForVote(authorId, userId, 2);
    }

    @Test
    @DisplayName("Should vote on comment successfully")
    void shouldVoteOnCommentSuccessfully() {
        // Given
        VoteRequest request = new VoteRequest();
        request.setVotableId(commentId);
        request.setVotableType(Vote.VotableType.COMMENT);
        request.setVoteValue(Vote.VoteValue.UPVOTE);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(commentRepository.existsById(commentId)).thenReturn(true);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(voteRepository.findById(any(Vote.VoteId.class))).thenReturn(Optional.empty());
        when(voteRepository.save(any(Vote.class))).thenReturn(new Vote());

        // When
        voteService.vote(userId, request);

        // Then
        verify(voteRepository).save(any(Vote.class));
        verify(karmaService).updateKarmaForVote(authorId, userId, 1);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        VoteRequest request = new VoteRequest();
        request.setVotableId(postId);
        request.setVotableType(Vote.VotableType.POST);
        request.setVoteValue(Vote.VoteValue.UPVOTE);

        when(userRepository.existsById(userId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> voteService.vote(userId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when post not found")
    void shouldThrowExceptionWhenPostNotFound() {
        // Given
        VoteRequest request = new VoteRequest();
        request.setVotableId(postId);
        request.setVotableType(Vote.VotableType.POST);
        request.setVoteValue(Vote.VoteValue.UPVOTE);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(postRepository.existsById(postId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> voteService.vote(userId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Post");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when comment not found")
    void shouldThrowExceptionWhenCommentNotFound() {
        // Given
        VoteRequest request = new VoteRequest();
        request.setVotableId(commentId);
        request.setVotableType(Vote.VotableType.COMMENT);
        request.setVoteValue(Vote.VoteValue.UPVOTE);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(commentRepository.existsById(commentId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> voteService.vote(userId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Comment");
    }

    @Test
    @DisplayName("Should remove vote successfully")
    void shouldRemoveVoteSuccessfully() {
        // Given
        Vote.VoteId voteId = new Vote.VoteId(userId, postId);
        doNothing().when(voteRepository).deleteById(voteId);

        // When
        voteService.removeVote(userId, postId, Vote.VotableType.POST);

        // Then
        verify(voteRepository).deleteById(voteId);
    }

    @Test
    @DisplayName("Should get upvote count")
    void shouldGetUpvoteCount() {
        // Given
        when(voteRepository.countByVotableIdAndVotableTypeAndVoteValue(
                postId, Vote.VotableType.POST, Vote.VoteValue.UPVOTE)).thenReturn(10L);

        // When
        long count = voteService.getUpvoteCount(postId, Vote.VotableType.POST);

        // Then
        assertThat(count).isEqualTo(10L);
    }

    @Test
    @DisplayName("Should get downvote count")
    void shouldGetDownvoteCount() {
        // Given
        when(voteRepository.countByVotableIdAndVotableTypeAndVoteValue(
                postId, Vote.VotableType.POST, Vote.VoteValue.DOWNVOTE)).thenReturn(3L);

        // When
        long count = voteService.getDownvoteCount(postId, Vote.VotableType.POST);

        // Then
        assertThat(count).isEqualTo(3L);
    }

    @Test
    @DisplayName("Should get user vote")
    void shouldGetUserVote() {
        // Given
        Vote vote = new Vote();
        vote.setVoteValue(Vote.VoteValue.UPVOTE);

        when(voteRepository.findByUserIdAndVotableIdAndVotableType(userId, postId, Vote.VotableType.POST))
                .thenReturn(Optional.of(vote));

        // When
        Vote.VoteValue result = voteService.getUserVote(userId, postId, Vote.VotableType.POST);

        // Then
        assertThat(result).isEqualTo(Vote.VoteValue.UPVOTE);
    }

    @Test
    @DisplayName("Should return null when user has not voted")
    void shouldReturnNullWhenUserHasNotVoted() {
        // Given
        when(voteRepository.findByUserIdAndVotableIdAndVotableType(userId, postId, Vote.VotableType.POST))
                .thenReturn(Optional.empty());

        // When
        Vote.VoteValue result = voteService.getUserVote(userId, postId, Vote.VotableType.POST);

        // Then
        assertThat(result).isNull();
    }
}
