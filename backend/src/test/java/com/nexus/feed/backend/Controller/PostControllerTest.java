package com.nexus.feed.backend.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.feed.backend.Auth.Service.JwtService;
import com.nexus.feed.backend.Auth.Service.UserDetailsServiceImpl;
import com.nexus.feed.backend.DTO.CommentResponse;
import com.nexus.feed.backend.DTO.PostCreateRequest;
import com.nexus.feed.backend.DTO.PostDetailResponse;
import com.nexus.feed.backend.DTO.PostResponse;
import com.nexus.feed.backend.DTO.PostUpdateRequest;
import com.nexus.feed.backend.Exception.GlobalExceptionHandler;
import com.nexus.feed.backend.Exception.ResourceNotFoundException;
import com.nexus.feed.backend.Exception.UnauthorizedException;
import com.nexus.feed.backend.Service.AuthenticationService;
import com.nexus.feed.backend.Service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@WebMvcTest(controllers = PostController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@DisplayName("PostController Tests")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private UUID userId;
    private UUID postId;
    private PostCreateRequest postCreateRequest;
    private PostUpdateRequest postUpdateRequest;
    private PostResponse postResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        postId = UUID.randomUUID();

        // Setup create request
        postCreateRequest = new PostCreateRequest();
        postCreateRequest.setTitle("Test Post Title");
        postCreateRequest.setBody("Test post body content");
        postCreateRequest.setImageUrls(List.of("https://example.com/image1.jpg"));

        // Setup update request
        postUpdateRequest = new PostUpdateRequest();
        postUpdateRequest.setTitle("Updated Post Title");
        postUpdateRequest.setBody("Updated post body content");

        // Setup response
        postResponse = new PostResponse();
        postResponse.setId(postId);
        postResponse.setTitle("Test Post Title");
        postResponse.setBody("Test post body content");
        postResponse.setUserId(userId);
        postResponse.setUsername("tester");
        postResponse.setCreatedAt(Instant.now());
        postResponse.setUpdatedAt(Instant.now());
        postResponse.setUpvotes(0);
        postResponse.setDownvotes(0);
    }

    @Test
    @DisplayName("Should create post successfully")
    void shouldCreatePostSuccessfully() throws Exception {
        // Given
        when(authenticationService.getCurrentUserId()).thenReturn(userId);
        when(postService.createPost(any(UUID.class), any(PostCreateRequest.class)))
                .thenReturn(postResponse);

        // When & Then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Test Post Title"))
                .andExpect(jsonPath("$.body").value("Test post body content"))
                .andExpect(jsonPath("$.username").value("tester"));
    }

    @Test
    @DisplayName("Should return 400 when creating post with blank title")
    void shouldFailCreatePostWithBlankTitle() throws Exception {
        // Given
        PostCreateRequest invalidRequest = new PostCreateRequest();
        invalidRequest.setTitle("");
        invalidRequest.setBody("Some body");

        // When & Then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when creating post with title exceeding max length")
    void shouldFailCreatePostWithLongTitle() throws Exception {
        // Given
        PostCreateRequest invalidRequest = new PostCreateRequest();
        invalidRequest.setTitle("a".repeat(301)); // Exceeds max length of 300
        invalidRequest.setBody("Some body");

        // When & Then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get post by id successfully")
    void shouldGetPostByIdSuccessfully() throws Exception {
        // Given
        when(postService.getPostById(postId)).thenReturn(postResponse);

        // When & Then
        mockMvc.perform(get("/api/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(postId.toString()))
                .andExpect(jsonPath("$.title").value("Test Post Title"));
    }

    @Test
    @DisplayName("Should return 404 when post not found")
    void shouldReturn404WhenPostNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(postService.getPostById(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Post", "id", nonExistentId));

        // When & Then
        mockMvc.perform(get("/api/posts/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get all posts with pagination")
    void shouldGetAllPostsWithPagination() throws Exception {
        // Given
        List<PostResponse> posts = Collections.singletonList(postResponse);
        Page<PostResponse> postPage = new PageImpl<>(posts, PageRequest.of(0, 10), 1);
        when(postService.getAllPosts(any(PageRequest.class), anyString())).thenReturn(postPage);

        // When & Then
        mockMvc.perform(get("/api/posts")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Test Post Title"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    @DisplayName("Should get posts by user with pagination")
    void shouldGetPostsByUserWithPagination() throws Exception {
        // Given
        List<PostResponse> posts = Collections.singletonList(postResponse);
        Page<PostResponse> postPage = new PageImpl<>(posts, PageRequest.of(0, 10), 1);
        when(postService.getPostsByUser(any(UUID.class), any(PageRequest.class)))
                .thenReturn(postPage);

        // When & Then
        mockMvc.perform(get("/api/posts/user/{userId}", userId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].userId").value(userId.toString()));
    }

    @Test
    @DisplayName("Should search posts with keyword")
    void shouldSearchPostsWithKeyword() throws Exception {
        // Given
        List<PostResponse> posts = Collections.singletonList(postResponse);
        Page<PostResponse> postPage = new PageImpl<>(posts, PageRequest.of(0, 10), 1);
        when(postService.searchPosts(anyString(), any(PageRequest.class)))
                .thenReturn(postPage);

        // When & Then
        mockMvc.perform(get("/api/posts/search")
                        .param("keyword", "test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Should update post successfully")
    void shouldUpdatePostSuccessfully() throws Exception {
        // Given
        when(authenticationService.getCurrentUserId()).thenReturn(userId);
        PostResponse updatedPost = new PostResponse();
        updatedPost.setId(postId);
        updatedPost.setTitle("Updated Post Title");
        updatedPost.setBody("Updated post body content");
        updatedPost.setUserId(userId);
        updatedPost.setUsername("tester");
        updatedPost.setCreatedAt(Instant.now());
        updatedPost.setUpdatedAt(Instant.now());

        when(postService.updatePost(any(UUID.class), any(UUID.class), any(PostUpdateRequest.class)))
                .thenReturn(updatedPost);

        // When & Then
        mockMvc.perform(put("/api/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Updated Post Title"))
                .andExpect(jsonPath("$.body").value("Updated post body content"));
    }

    @Test
    @DisplayName("Should return 403 when update fails due to unauthorized")
    void shouldReturn403WhenUpdateFails() throws Exception {
        // Given
        when(authenticationService.getCurrentUserId()).thenReturn(userId);
        when(postService.updatePost(any(UUID.class), any(UUID.class), any(PostUpdateRequest.class)))
                .thenThrow(new UnauthorizedException("Not authorized to update this post"));

        // When & Then
        mockMvc.perform(put("/api/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postUpdateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should delete post successfully")
    void shouldDeletePostSuccessfully() throws Exception {
        // Given
        when(authenticationService.getCurrentUserId()).thenReturn(userId);
        doNothing().when(postService).deletePost(any(UUID.class), any(UUID.class));

        // When & Then
        mockMvc.perform(delete("/api/posts/{id}", postId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 403 when delete fails due to unauthorized")
    void shouldReturn403WhenDeleteFails() throws Exception {
        // Given
        when(authenticationService.getCurrentUserId()).thenReturn(userId);
        doThrow(new UnauthorizedException("Not authorized to delete this post"))
                .when(postService).deletePost(any(UUID.class), any(UUID.class));

        // When & Then
        mockMvc.perform(delete("/api/posts/{id}", postId))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should use default pagination values")
    void shouldUseDefaultPaginationValues() throws Exception {
        // Given
        List<PostResponse> posts = Collections.singletonList(postResponse);
        Page<PostResponse> postPage = new PageImpl<>(posts, PageRequest.of(0, 10), 1);
        when(postService.getAllPosts(any(PageRequest.class), anyString())).thenReturn(postPage);

        // When & Then
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Should get post with comments successfully")
    void shouldGetPostWithCommentsSuccessfully() throws Exception {
        // Given
        CommentResponse comment1 = CommentResponse.builder()
                .id(UUID.randomUUID())
                .body("Test comment 1")
                .userId(userId)
                .username("tester")
                .postId(postId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .upvotes(0)
                .downvotes(0)
                .build();

        CommentResponse comment2 = CommentResponse.builder()
                .id(UUID.randomUUID())
                .body("Test comment 2")
                .userId(userId)
                .username("tester")
                .postId(postId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .upvotes(0)
                .downvotes(0)
                .build();

        List<CommentResponse> comments = Arrays.asList(comment1, comment2);
        
        PostDetailResponse postDetailResponse = PostDetailResponse.builder()
                .post(postResponse)
                .comments(comments)
                .build();

        when(postService.getPostWithComments(postId)).thenReturn(postDetailResponse);

        // When & Then
        mockMvc.perform(get("/api/posts/{id}/with-comments", postId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.post").exists())
                .andExpect(jsonPath("$.post.id").value(postId.toString()))
                .andExpect(jsonPath("$.post.title").value("Test Post Title"))
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments.length()").value(2))
                .andExpect(jsonPath("$.comments[0].body").value("Test comment 1"))
                .andExpect(jsonPath("$.comments[1].body").value("Test comment 2"));
    }

    @Test
    @DisplayName("Should return 404 when post with comments not found")
    void shouldReturn404WhenPostWithCommentsNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(postService.getPostWithComments(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Post", "id", nonExistentId));

        // When & Then
        mockMvc.perform(get("/api/posts/{id}/with-comments", nonExistentId))
                .andExpect(status().isNotFound());
    }
}

