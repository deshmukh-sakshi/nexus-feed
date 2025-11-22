package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.DTO.CommentCreateRequest;
import com.nexus.feed.backend.DTO.CommentResponse;
import com.nexus.feed.backend.DTO.CommentUpdateRequest;
import com.nexus.feed.backend.Entity.Comment;
import com.nexus.feed.backend.Entity.Post;
import com.nexus.feed.backend.Entity.Users;
import com.nexus.feed.backend.Entity.Vote;
import com.nexus.feed.backend.Exception.ResourceNotFoundException;
import com.nexus.feed.backend.Exception.UnauthorizedException;
import com.nexus.feed.backend.Repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentServiceImpl Unit Tests")
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private CommentServiceImpl commentService;

    private UUID userId;
    private UUID postId;
    private UUID commentId;
    private Users user;
    private Post post;
    private Comment comment;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        postId = UUID.randomUUID();
        commentId = UUID.randomUUID();

        user = new Users();
        user.setId(userId);
        user.setUsername("tester");

        post = new Post();
        post.setId(postId);
        post.setTitle("Test Post");
        post.setUser(user);

        comment = new Comment();
        comment.setId(commentId);
        comment.setBody("Test Comment");
        comment.setUser(user);
        comment.setPost(post);
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());
    }

    @Test
    @DisplayName("Should create comment successfully")
    void shouldCreateCommentSuccessfully() {
        // Given
        CommentCreateRequest request = new CommentCreateRequest();
        request.setBody("New Comment");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(voteRepository.countByVotableIdAndVotableTypeAndVoteValue(any(), any(), any())).thenReturn(0L);

        // When
        CommentResponse response = commentService.createComment(userId, postId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getPostId()).isEqualTo(postId);
        assertThat(response.getUserId()).isEqualTo(userId);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("Should create reply comment with parent")
    void shouldCreateReplyCommentWithParent() {
        // Given
        UUID parentCommentId = UUID.randomUUID();
        Comment parentComment = new Comment();
        parentComment.setId(parentCommentId);
        parentComment.setBody("Parent Comment");
        parentComment.setUser(user);
        parentComment.setPost(post);
        parentComment.setCreatedAt(Instant.now());
        parentComment.setUpdatedAt(Instant.now());

        CommentCreateRequest request = new CommentCreateRequest();
        request.setBody("Reply Comment");
        request.setParentCommentId(parentCommentId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.findById(parentCommentId)).thenReturn(Optional.of(parentComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(voteRepository.countByVotableIdAndVotableTypeAndVoteValue(any(), any(), any())).thenReturn(0L);

        // When
        CommentResponse response = commentService.createComment(userId, postId, request);

        // Then
        assertThat(response).isNotNull();
        verify(commentRepository).findById(parentCommentId);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        CommentCreateRequest request = new CommentCreateRequest();
        request.setBody("Comment");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.createComment(userId, postId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when post not found")
    void shouldThrowExceptionWhenPostNotFound() {
        // Given
        CommentCreateRequest request = new CommentCreateRequest();
        request.setBody("Comment");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.createComment(userId, postId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Post");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when parent comment not found")
    void shouldThrowExceptionWhenParentCommentNotFound() {
        // Given
        UUID parentCommentId = UUID.randomUUID();
        CommentCreateRequest request = new CommentCreateRequest();
        request.setBody("Reply");
        request.setParentCommentId(parentCommentId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.findById(parentCommentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.createComment(userId, postId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Comment");
    }

    @Test
    @DisplayName("Should get comment by id successfully")
    void shouldGetCommentByIdSuccessfully() {
        // Given
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(voteRepository.countByVotableIdAndVotableTypeAndVoteValue(any(), any(), any())).thenReturn(0L);

        // When
        CommentResponse response = commentService.getCommentById(commentId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(commentId);
        assertThat(response.getBody()).isEqualTo("Test Comment");
    }

    @Test
    @DisplayName("Should get comments by post with batch queries")
    void shouldGetCommentsByPostWithBatchQueries() {
        // Given
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.findByPostAndParentCommentIsNullOrderByCreatedAtDesc(post))
                .thenReturn(Collections.singletonList(comment));
        when(commentRepository.findByParentCommentOrderByCreatedAtAsc(any()))
                .thenReturn(new ArrayList<>());
        when(voteRepository.countByVotableIdsAndVotableType(any(), any()))
                .thenReturn(new ArrayList<>());

        // When
        List<CommentResponse> responses = commentService.getCommentsByPost(postId);

        // Then
        assertThat(responses).isNotEmpty();
        assertThat(responses).hasSize(1);
        verify(voteRepository).countByVotableIdsAndVotableType(any(), eq(Vote.VotableType.COMMENT));
    }

    @Test
    @DisplayName("Should handle nested comments with batch queries")
    void shouldHandleNestedCommentsWithBatchQueries() {
        // Given
        Comment reply1 = new Comment();
        reply1.setId(UUID.randomUUID());
        reply1.setBody("Reply 1");
        reply1.setUser(user);
        reply1.setPost(post);
        reply1.setParentComment(comment);
        reply1.setCreatedAt(Instant.now());
        reply1.setUpdatedAt(Instant.now());

        Comment reply2 = new Comment();
        reply2.setId(UUID.randomUUID());
        reply2.setBody("Reply 2");
        reply2.setUser(user);
        reply2.setPost(post);
        reply2.setParentComment(comment);
        reply2.setCreatedAt(Instant.now());
        reply2.setUpdatedAt(Instant.now());

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.findByPostAndParentCommentIsNullOrderByCreatedAtDesc(post))
                .thenReturn(Collections.singletonList(comment));
        when(commentRepository.findByParentCommentOrderByCreatedAtAsc(comment))
                .thenReturn(Arrays.asList(reply1, reply2));
        when(commentRepository.findByParentCommentOrderByCreatedAtAsc(reply1))
                .thenReturn(new ArrayList<>());
        when(commentRepository.findByParentCommentOrderByCreatedAtAsc(reply2))
                .thenReturn(new ArrayList<>());
        when(voteRepository.countByVotableIdsAndVotableType(any(), any()))
                .thenReturn(new ArrayList<>());

        // When
        List<CommentResponse> responses = commentService.getCommentsByPost(postId);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getReplies()).hasSize(2);
    }

    @Test
    @DisplayName("Should get comments by user with pagination")
    void shouldGetCommentsByUserWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Comment> commentPage = new PageImpl<>(Collections.singletonList(comment), pageable, 1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentRepository.findByUserOrderByCreatedAtDesc(user, pageable)).thenReturn(commentPage);
        when(voteRepository.countByVotableIdAndVotableTypeAndVoteValue(any(), any(), any())).thenReturn(0L);

        // When
        Page<CommentResponse> responses = commentService.getCommentsByUser(userId, pageable);

        // Then
        assertThat(responses).isNotNull();
        assertThat(responses.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should update comment successfully")
    void shouldUpdateCommentSuccessfully() {
        // Given
        CommentUpdateRequest request = new CommentUpdateRequest();
        request.setBody("Updated Comment");

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(voteRepository.countByVotableIdAndVotableTypeAndVoteValue(any(), any(), any())).thenReturn(0L);

        // When
        CommentResponse response = commentService.updateComment(commentId, userId, request);

        // Then
        assertThat(response).isNotNull();
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when updating comment by non-owner")
    void shouldThrowExceptionWhenUpdatingByNonOwner() {
        // Given
        UUID differentUserId = UUID.randomUUID();
        CommentUpdateRequest request = new CommentUpdateRequest();
        request.setBody("Hacked Comment");

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // When & Then
        assertThatThrownBy(() -> commentService.updateComment(commentId, differentUserId, request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Not authorized");
    }

    @Test
    @DisplayName("Should delete comment successfully")
    void shouldDeleteCommentSuccessfully() {
        // Given
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        doNothing().when(commentRepository).delete(any(Comment.class));

        // When
        commentService.deleteComment(commentId, userId);

        // Then
        verify(commentRepository).delete(comment);
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when deleting comment by non-owner")
    void shouldThrowExceptionWhenDeletingByNonOwner() {
        // Given
        UUID differentUserId = UUID.randomUUID();
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // When & Then
        assertThatThrownBy(() -> commentService.deleteComment(commentId, differentUserId))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Not authorized");
    }

    @Test
    @DisplayName("Should return empty list when post has no comments")
    void shouldReturnEmptyListWhenPostHasNoComments() {
        // Given
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.findByPostAndParentCommentIsNullOrderByCreatedAtDesc(post))
                .thenReturn(new ArrayList<>());

        // When
        List<CommentResponse> responses = commentService.getCommentsByPost(postId);

        // Then
        assertThat(responses).isEmpty();
    }
}
