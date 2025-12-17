package com.nexus.feed.backend.Repository;

import com.nexus.feed.backend.Auth.Entity.AppUser;
import com.nexus.feed.backend.Auth.Repository.AppUserRepository;
import com.nexus.feed.backend.Entity.Comment;
import com.nexus.feed.backend.Entity.Post;
import com.nexus.feed.backend.Entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CommentRepository Tests")
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    private Users user;
    private Post post;
    private Comment comment1;
    private Comment comment2;
    private Comment reply;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
        appUserRepository.deleteAll();

        AppUser appUser = new AppUser();
        appUser.setEmail("tester@example.com");
        appUser.setPassword("password");

        user = new Users();
        user.setUsername("tester");
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user.setAppUser(appUser);
        appUser.setUserProfile(user);

        appUserRepository.save(appUser);

        post = new Post();
        post.setTitle("Test Post");
        post.setBody("Test Body");
        post.setUser(user);
        post.setCreatedAt(Instant.now());
        post.setUpdatedAt(Instant.now());
        postRepository.save(post);

        comment1 = new Comment();
        comment1.setBody("First comment");
        comment1.setUser(user);
        comment1.setPost(post);
        commentRepository.save(comment1);

        comment2 = new Comment();
        comment2.setBody("Second comment");
        comment2.setUser(user);
        comment2.setPost(post);
        commentRepository.save(comment2);

        reply = new Comment();
        reply.setBody("Reply to first comment");
        reply.setUser(user);
        reply.setPost(post);
        reply.setParentComment(comment1);
        commentRepository.save(reply);
    }

    @Test
    @DisplayName("Should find top-level comments by post")
    void shouldFindTopLevelCommentsByPost() {
        // When
        List<Comment> comments = commentRepository.findByPostAndParentCommentIsNullOrderByCreatedAtDesc(post);

        // Then
        assertThat(comments).hasSize(2);
        assertThat(comments).extracting(Comment::getBody)
                .containsExactlyInAnyOrder("First comment", "Second comment");
    }

    @Test
    @DisplayName("Should find replies by parent comment")
    void shouldFindRepliesByParentComment() {
        // When
        List<Comment> replies = commentRepository.findByParentCommentOrderByCreatedAtAsc(comment1);

        // Then
        assertThat(replies).hasSize(1);
        assertThat(replies.get(0).getBody()).isEqualTo("Reply to first comment");
    }

    @Test
    @DisplayName("Should find comments by user with pagination")
    void shouldFindCommentsByUserWithPagination() {
        // When
        Page<Comment> comments = commentRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(0, 10));

        // Then
        assertThat(comments.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("Should count comments by post")
    void shouldCountCommentsByPost() {
        // When
        long count = commentRepository.countByPost(post);

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Should count comments by user")
    void shouldCountCommentsByUser() {
        // When
        long count = commentRepository.countByUser(user);

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Should batch count comments by post ids")
    void shouldBatchCountCommentsByPostIds() {
        // Given
        Post post2 = new Post();
        post2.setTitle("Another Post");
        post2.setBody("Body");
        post2.setUser(user);
        post2.setCreatedAt(Instant.now());
        post2.setUpdatedAt(Instant.now());
        postRepository.save(post2);

        Comment comment3 = new Comment();
        comment3.setBody("Comment on post 2");
        comment3.setUser(user);
        comment3.setPost(post2);
        comment3.setCreatedAt(Instant.now());
        comment3.setUpdatedAt(Instant.now());
        commentRepository.save(comment3);

        // When
        List<UUID> postIds = List.of(post.getId(), post2.getId());
        List<CommentRepository.CommentCount> counts = commentRepository.countByPostIds(postIds);

        // Then
        assertThat(counts).hasSize(2);
    }

    @Test
    @DisplayName("Should return empty list when no replies exist")
    void shouldReturnEmptyListWhenNoReplies() {
        // When
        List<Comment> replies = commentRepository.findByParentCommentOrderByCreatedAtAsc(comment2);

        // Then
        assertThat(replies).isEmpty();
    }

    @Test
    @DisplayName("Should delete comments by post")
    void shouldDeleteCommentsByPost() {
        // When
        commentRepository.deleteByPost(post);

        // Then
        assertThat(commentRepository.countByPost(post)).isEqualTo(0);
    }
}
