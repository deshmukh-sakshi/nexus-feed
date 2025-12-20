package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.DTO.*;
import com.nexus.feed.backend.Entity.*;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostServiceImpl Unit Tests")
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostImageRepository postImageRepository;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private CommentService commentService;

    @Mock
    private KarmaService karmaService;

    @Mock
    private BadgeAwardingService badgeAwardingService;

    @Mock
    private TagService tagService;

    @InjectMocks
    private PostServiceImpl postService;

    private UUID userId;
    private UUID postId;
    private Users user;
    private Post post;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        postId = UUID.randomUUID();

        user = new Users();
        user.setId(userId);
        user.setUsername("tester");

        post = new Post();
        post.setId(postId);
        post.setTitle("Test Post");
        post.setBody("Test Body");
        post.setUser(user);
        post.setCreatedAt(Instant.now());
        post.setUpdatedAt(Instant.now());
        post.setImages(new ArrayList<>());
        post.setTags(new HashSet<>());
    }

    @Test
    @DisplayName("Should create post successfully")
    void shouldCreatePostSuccessfully() {
        // Given
        PostCreateRequest request = new PostCreateRequest();
        request.setTitle("New Post");
        request.setBody("New Body");
        request.setUrl("https://example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(voteRepository.countByVotableIdAndVotableTypeAndVoteValue(any(), any(), any())).thenReturn(0L);
        when(commentRepository.countByPost(any())).thenReturn(0L);

        // When
        PostResponse response = postService.createPost(userId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("tester");
        verify(postRepository).save(any(Post.class));
        verify(badgeAwardingService).checkPostBadges(userId);
    }

    @Test
    @DisplayName("Should create post with tags")
    void shouldCreatePostWithTags() {
        // Given
        PostCreateRequest request = new PostCreateRequest();
        request.setTitle("Tagged Post");
        request.setBody("Body");
        request.setTags(List.of("java", "spring"));

        Tag tag1 = Tag.builder().id(1L).name("java").build();
        Tag tag2 = Tag.builder().id(2L).name("spring").build();
        Set<Tag> tags = new HashSet<>(Arrays.asList(tag1, tag2));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tagService.getOrCreateTags(anyList())).thenReturn(tags);
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(voteRepository.countByVotableIdAndVotableTypeAndVoteValue(any(), any(), any())).thenReturn(0L);
        when(commentRepository.countByPost(any())).thenReturn(0L);

        // When
        PostResponse response = postService.createPost(userId, request);

        // Then
        assertThat(response).isNotNull();
        verify(tagService).getOrCreateTags(anyList());
    }

    @Test
    @DisplayName("Should create post with images")
    void shouldCreatePostWithImages() {
        // Given
        PostCreateRequest request = new PostCreateRequest();
        request.setTitle("Image Post");
        request.setBody("Body");
        request.setImageUrls(List.of("https://example.com/img1.jpg", "https://example.com/img2.jpg"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(postImageRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        when(voteRepository.countByVotableIdAndVotableTypeAndVoteValue(any(), any(), any())).thenReturn(0L);
        when(commentRepository.countByPost(any())).thenReturn(0L);

        // When
        PostResponse response = postService.createPost(userId, request);

        // Then
        assertThat(response).isNotNull();
        verify(postImageRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        PostCreateRequest request = new PostCreateRequest();
        request.setTitle("Post");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postService.createPost(userId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    @DisplayName("Should get post by id successfully")
    void shouldGetPostByIdSuccessfully() {
        // Given
        when(postRepository.findByIdWithUserAndImages(postId)).thenReturn(Optional.of(post));
        when(voteRepository.countByVotableIdAndVotableTypeAndVoteValue(any(), any(), any())).thenReturn(0L);
        when(commentRepository.countByPost(any())).thenReturn(0L);

        // When
        PostResponse response = postService.getPostById(postId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(postId);
        assertThat(response.getTitle()).isEqualTo("Test Post");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when post not found")
    void shouldThrowExceptionWhenPostNotFound() {
        // Given
        when(postRepository.findByIdWithUserAndImages(postId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postService.getPostById(postId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Post");
    }

    @Test
    @DisplayName("Should get post with comments")
    void shouldGetPostWithComments() {
        // Given
        List<CommentResponse> comments = List.of(
                CommentResponse.builder().id(UUID.randomUUID()).body("Comment 1").build()
        );

        when(postRepository.findByIdWithUserAndImages(postId)).thenReturn(Optional.of(post));
        when(voteRepository.countByVotableIdAndVotableTypeAndVoteValue(any(), any(), any())).thenReturn(0L);
        when(commentRepository.countByPost(any())).thenReturn(1L);
        when(commentService.getCommentsByPost(postId)).thenReturn(comments);

        // When
        PostDetailResponse response = postService.getPostWithComments(postId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getPost()).isNotNull();
        assertThat(response.getComments()).hasSize(1);
    }

    @Test
    @DisplayName("Should get all posts with pagination")
    void shouldGetAllPostsWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> postPage = new PageImpl<>(List.of(post), pageable, 1);

        when(postRepository.findAllOrderByCreatedAtDesc(pageable)).thenReturn(postPage);
        when(voteRepository.countByVotableIdsAndVotableType(anyList(), any())).thenReturn(new ArrayList<>());
        when(commentRepository.countByPostIds(anyList())).thenReturn(new ArrayList<>());

        // When
        Page<PostResponse> responses = postService.getAllPosts(pageable);

        // Then
        assertThat(responses).isNotNull();
        assertThat(responses.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should get posts by user with pagination")
    void shouldGetPostsByUserWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> postPage = new PageImpl<>(List.of(post), pageable, 1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.findByUserOrderByCreatedAtDesc(user, pageable)).thenReturn(postPage);
        when(voteRepository.countByVotableIdsAndVotableType(anyList(), any())).thenReturn(new ArrayList<>());
        when(commentRepository.countByPostIds(anyList())).thenReturn(new ArrayList<>());

        // When
        Page<PostResponse> responses = postService.getPostsByUser(userId, pageable);

        // Then
        assertThat(responses).isNotNull();
        assertThat(responses.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should search posts by keyword")
    void shouldSearchPostsByKeyword() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> postPage = new PageImpl<>(List.of(post), pageable, 1);

        when(postRepository.findByTitleContainingOrBodyContainingOrderByCreatedAtDesc("test", pageable))
                .thenReturn(postPage);
        when(voteRepository.countByVotableIdsAndVotableType(anyList(), any())).thenReturn(new ArrayList<>());
        when(commentRepository.countByPostIds(anyList())).thenReturn(new ArrayList<>());

        // When
        Page<PostResponse> responses = postService.searchPosts("test", pageable);

        // Then
        assertThat(responses).isNotNull();
        assertThat(responses.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should search posts by tag")
    void shouldSearchPostsByTag() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> postPage = new PageImpl<>(List.of(post), pageable, 1);

        when(postRepository.findByTagName("java", pageable)).thenReturn(postPage);
        when(voteRepository.countByVotableIdsAndVotableType(anyList(), any())).thenReturn(new ArrayList<>());
        when(commentRepository.countByPostIds(anyList())).thenReturn(new ArrayList<>());

        // When
        Page<PostResponse> responses = postService.searchByTag("Java", pageable);

        // Then
        assertThat(responses).isNotNull();
        assertThat(responses.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should update post successfully")
    void shouldUpdatePostSuccessfully() {
        // Given
        PostUpdateRequest request = new PostUpdateRequest();
        request.setTitle("Updated Title");
        request.setBody("Updated Body");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(voteRepository.countByVotableIdAndVotableTypeAndVoteValue(any(), any(), any())).thenReturn(0L);
        when(commentRepository.countByPost(any())).thenReturn(0L);

        // When
        PostResponse response = postService.updatePost(postId, userId, request);

        // Then
        assertThat(response).isNotNull();
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when updating post by non-owner")
    void shouldThrowExceptionWhenUpdatingByNonOwner() {
        // Given
        UUID differentUserId = UUID.randomUUID();
        PostUpdateRequest request = new PostUpdateRequest();
        request.setTitle("Hacked Title");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        // When & Then
        assertThatThrownBy(() -> postService.updatePost(postId, differentUserId, request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Not authorized");
    }

    @Test
    @DisplayName("Should update post with new tags")
    void shouldUpdatePostWithNewTags() {
        // Given
        PostUpdateRequest request = new PostUpdateRequest();
        request.setTags(List.of("newtag"));

        Tag newTag = Tag.builder().id(1L).name("newtag").build();
        Set<Tag> tags = new HashSet<>(List.of(newTag));

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(tagService.getOrCreateTags(anyList())).thenReturn(tags);
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(voteRepository.countByVotableIdAndVotableTypeAndVoteValue(any(), any(), any())).thenReturn(0L);
        when(commentRepository.countByPost(any())).thenReturn(0L);

        // When
        PostResponse response = postService.updatePost(postId, userId, request);

        // Then
        assertThat(response).isNotNull();
        verify(tagService).getOrCreateTags(anyList());
    }

    @Test
    @DisplayName("Should delete post successfully")
    void shouldDeletePostSuccessfully() {
        // Given
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        doNothing().when(postRepository).delete(any(Post.class));
        doNothing().when(karmaService).recalculateKarma(any());

        // When
        postService.deletePost(postId, userId);

        // Then
        verify(postRepository).delete(post);
        verify(karmaService).recalculateKarma(userId);
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when deleting post by non-owner")
    void shouldThrowExceptionWhenDeletingByNonOwner() {
        // Given
        UUID differentUserId = UUID.randomUUID();
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        // When & Then
        assertThatThrownBy(() -> postService.deletePost(postId, differentUserId))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Not authorized");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent post")
    void shouldThrowExceptionWhenDeletingNonExistentPost() {
        // Given
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postService.deletePost(postId, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Post");
    }

    @Test
    @DisplayName("Should return empty page when no posts exist")
    void shouldReturnEmptyPageWhenNoPostsExist() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(postRepository.findAllOrderByCreatedAtDesc(pageable)).thenReturn(emptyPage);

        // When
        Page<PostResponse> responses = postService.getAllPosts(pageable);

        // Then
        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("Should include user vote in response when authenticated")
    void shouldIncludeUserVoteWhenAuthenticated() {
        // Given
        UUID currentUserId = UUID.randomUUID();
        Vote vote = new Vote();
        vote.setVoteValue(Vote.VoteValue.UPVOTE);

        when(postRepository.findByIdWithUserAndImages(postId)).thenReturn(Optional.of(post));
        when(voteRepository.countByVotableIdAndVotableTypeAndVoteValue(postId, Vote.VotableType.POST, Vote.VoteValue.UPVOTE)).thenReturn(5L);
        when(voteRepository.countByVotableIdAndVotableTypeAndVoteValue(postId, Vote.VotableType.POST, Vote.VoteValue.DOWNVOTE)).thenReturn(1L);
        when(authenticationService.getCurrentUserId()).thenReturn(currentUserId);
        when(voteRepository.findByUserIdAndVotableIdAndVotableType(currentUserId, postId, Vote.VotableType.POST))
                .thenReturn(Optional.of(vote));
        when(commentRepository.countByPost(any())).thenReturn(0L);

        // When
        PostResponse response = postService.getPostById(postId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUpvotes()).isEqualTo(5);
        assertThat(response.getDownvotes()).isEqualTo(1);
        assertThat(response.getUserVote()).isEqualTo("UPVOTE");
    }
}
