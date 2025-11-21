package com.nexus.feed.backend.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.feed.backend.DTO.PostCreateRequest;
import com.nexus.feed.backend.DTO.PostResponse;
import com.nexus.feed.backend.DTO.PostUpdateRequest;
import com.nexus.feed.backend.Service.AuthenticationService;
import com.nexus.feed.backend.Service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PostController.class, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
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
    private com.nexus.feed.backend.Auth.Service.JwtService jwtService;

    @MockitoBean
    private com.nexus.feed.backend.Auth.Service.UserDetailsServiceImpl userDetailsService;

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
        postCreateRequest.setUrl("https://example.com");
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
        postResponse.setUrl("https://example.com");
        postResponse.setUserId(userId);
        postResponse.setUsername("tester");
        postResponse.setCreatedAt(LocalDateTime.now());
        postResponse.setUpdatedAt(LocalDateTime.now());
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
                .thenThrow(new RuntimeException("Post not found"));

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
        when(postService.getAllPosts(any(PageRequest.class))).thenReturn(postPage);

        // When & Then
        mockMvc.perform(get("/api/posts")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Test Post Title"))
                .andExpect(jsonPath("$.totalElements").value(1));
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
        updatedPost.setCreatedAt(LocalDateTime.now());
        updatedPost.setUpdatedAt(LocalDateTime.now());

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
    @DisplayName("Should return 400 when update fails")
    void shouldReturn400WhenUpdateFails() throws Exception {
        // Given
        when(authenticationService.getCurrentUserId()).thenReturn(userId);
        when(postService.updatePost(any(UUID.class), any(UUID.class), any(PostUpdateRequest.class)))
                .thenThrow(new RuntimeException("Update failed"));

        // When & Then
        mockMvc.perform(put("/api/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postUpdateRequest)))
                .andExpect(status().isBadRequest());
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
    @DisplayName("Should return 400 when delete fails")
    void shouldReturn400WhenDeleteFails() throws Exception {
        // Given
        when(authenticationService.getCurrentUserId())
                .thenThrow(new RuntimeException("Delete failed"));

        // When & Then
        mockMvc.perform(delete("/api/posts/{id}", postId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should use default pagination values")
    void shouldUseDefaultPaginationValues() throws Exception {
        // Given
        List<PostResponse> posts = Collections.singletonList(postResponse);
        Page<PostResponse> postPage = new PageImpl<>(posts, PageRequest.of(0, 10), 1);
        when(postService.getAllPosts(any(PageRequest.class))).thenReturn(postPage);

        // When & Then
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Should get post with comments successfully")
    void shouldGetPostWithCommentsSuccessfully() throws Exception {
        // Given
        com.nexus.feed.backend.DTO.CommentResponse comment1 = com.nexus.feed.backend.DTO.CommentResponse.builder()
                .id(UUID.randomUUID())
                .body("Test comment 1")
                .userId(userId)
                .username("tester")
                .postId(postId)
                .createdAt(java.time.Instant.now())
                .updatedAt(java.time.Instant.now())
                .upvotes(0)
                .downvotes(0)
                .build();

        com.nexus.feed.backend.DTO.CommentResponse comment2 = com.nexus.feed.backend.DTO.CommentResponse.builder()
                .id(UUID.randomUUID())
                .body("Test comment 2")
                .userId(userId)
                .username("tester")
                .postId(postId)
                .createdAt(java.time.Instant.now())
                .updatedAt(java.time.Instant.now())
                .upvotes(0)
                .downvotes(0)
                .build();

        List<com.nexus.feed.backend.DTO.CommentResponse> comments = Arrays.asList(comment1, comment2);
        
        com.nexus.feed.backend.DTO.PostDetailResponse postDetailResponse = com.nexus.feed.backend.DTO.PostDetailResponse.builder()
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
                .thenThrow(new RuntimeException("Post not found"));

        // When & Then
        mockMvc.perform(get("/api/posts/{id}/with-comments", nonExistentId))
                .andExpect(status().isNotFound());
    }
}

