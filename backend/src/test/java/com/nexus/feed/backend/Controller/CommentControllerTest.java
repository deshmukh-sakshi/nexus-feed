package com.nexus.feed.backend.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.feed.backend.DTO.CommentCreateRequest;
import com.nexus.feed.backend.DTO.CommentResponse;
import com.nexus.feed.backend.DTO.CommentUpdateRequest;
import com.nexus.feed.backend.Service.AuthenticationService;
import com.nexus.feed.backend.Service.CommentService;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CommentController.class, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("CommentController Tests")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private com.nexus.feed.backend.Auth.Service.JwtService jwtService;

    @MockitoBean
    private com.nexus.feed.backend.Auth.Service.UserDetailsServiceImpl userDetailsService;

    private UUID userId;
    private UUID postId;
    private UUID commentId;
    private CommentCreateRequest commentCreateRequest;
    private CommentUpdateRequest commentUpdateRequest;
    private CommentResponse commentResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        postId = UUID.randomUUID();
        commentId = UUID.randomUUID();

        // Setup create request
        commentCreateRequest = new CommentCreateRequest();
        commentCreateRequest.setBody("Test comment body");

        // Setup update request
        commentUpdateRequest = new CommentUpdateRequest();
        commentUpdateRequest.setBody("Updated comment body");

        // Setup response
        commentResponse = new CommentResponse();
        commentResponse.setId(commentId);
        commentResponse.setBody("Test comment body");
        commentResponse.setUserId(userId);
        commentResponse.setUsername("tester");
        commentResponse.setPostId(postId);
        commentResponse.setCreatedAt(java.time.Instant.now());
        commentResponse.setUpdatedAt(java.time.Instant.now());
    }

    @Test
    @DisplayName("Should create comment successfully")
    void shouldCreateCommentSuccessfully() throws Exception {
        // Given
        when(authenticationService.getCurrentUserId()).thenReturn(userId);
        when(commentService.createComment(any(UUID.class), any(UUID.class), any(CommentCreateRequest.class)))
                .thenReturn(commentResponse);

        // When & Then
        mockMvc.perform(post("/api/comments/post/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.body").value("Test comment body"))
                .andExpect(jsonPath("$.username").value("tester"))
                .andExpect(jsonPath("$.postId").value(postId.toString()));
    }

    @Test
    @DisplayName("Should return 400 when creating comment with blank body")
    void shouldFailCreateCommentWithBlankBody() throws Exception {
        // Given
        CommentCreateRequest invalidRequest = new CommentCreateRequest();
        invalidRequest.setBody("");

        // When & Then
        mockMvc.perform(post("/api/comments/post/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when creating comment with body exceeding max length")
    void shouldFailCreateCommentWithLongBody() throws Exception {
        // Given
        CommentCreateRequest invalidRequest = new CommentCreateRequest();
        invalidRequest.setBody("a".repeat(2001)); // Exceeds max length of 2000

        // When & Then
        mockMvc.perform(post("/api/comments/post/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when creating comment fails")
    void shouldReturn400WhenCreateCommentFails() throws Exception {
        // Given
        when(authenticationService.getCurrentUserId()).thenReturn(userId);
        when(commentService.createComment(any(UUID.class), any(UUID.class), any(CommentCreateRequest.class)))
                .thenThrow(new RuntimeException("Post not found"));

        // When & Then
        mockMvc.perform(post("/api/comments/post/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get comment by id successfully")
    void shouldGetCommentByIdSuccessfully() throws Exception {
        // Given
        when(commentService.getCommentById(commentId)).thenReturn(commentResponse);

        // When & Then
        mockMvc.perform(get("/api/comments/{id}", commentId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.body").value("Test comment body"));
    }

    @Test
    @DisplayName("Should return 404 when comment not found")
    void shouldReturn404WhenCommentNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(commentService.getCommentById(nonExistentId))
                .thenThrow(new RuntimeException("Comment not found"));

        // When & Then
        mockMvc.perform(get("/api/comments/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get comments by post successfully")
    void shouldGetCommentsByPostSuccessfully() throws Exception {
        // Given
        List<CommentResponse> comments = Collections.singletonList(commentResponse);
        when(commentService.getCommentsByPost(postId)).thenReturn(comments);

        // When & Then
        mockMvc.perform(get("/api/comments/post/{postId}", postId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].postId").value(postId.toString()));
    }

    @Test
    @DisplayName("Should return 404 when getting comments for non-existent post")
    void shouldReturn404WhenGettingCommentsForNonExistentPost() throws Exception {
        // Given
        UUID nonExistentPostId = UUID.randomUUID();
        when(commentService.getCommentsByPost(nonExistentPostId))
                .thenThrow(new RuntimeException("Post not found"));

        // When & Then
        mockMvc.perform(get("/api/comments/post/{postId}", nonExistentPostId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get comments by user with pagination")
    void shouldGetCommentsByUserWithPagination() throws Exception {
        // Given
        List<CommentResponse> comments = Collections.singletonList(commentResponse);
        Page<CommentResponse> commentPage = new PageImpl<>(comments, PageRequest.of(0, 10), 1);
        when(commentService.getCommentsByUser(any(UUID.class), any(PageRequest.class)))
                .thenReturn(commentPage);

        // When & Then
        mockMvc.perform(get("/api/comments/user/{userId}", userId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    @DisplayName("Should use default pagination values for user comments")
    void shouldUseDefaultPaginationValuesForUserComments() throws Exception {
        // Given
        List<CommentResponse> comments = Collections.singletonList(commentResponse);
        Page<CommentResponse> commentPage = new PageImpl<>(comments, PageRequest.of(0, 10), 1);
        when(commentService.getCommentsByUser(any(UUID.class), any(PageRequest.class)))
                .thenReturn(commentPage);

        // When & Then
        mockMvc.perform(get("/api/comments/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Should update comment successfully")
    void shouldUpdateCommentSuccessfully() throws Exception {
        // Given
        when(authenticationService.getCurrentUserId()).thenReturn(userId);
        CommentResponse updatedComment = new CommentResponse();
        updatedComment.setId(commentId);
        updatedComment.setBody("Updated comment body");
        updatedComment.setUserId(userId);
        updatedComment.setUsername("tester");
        updatedComment.setPostId(postId);
        updatedComment.setCreatedAt(java.time.Instant.now());
        updatedComment.setUpdatedAt(java.time.Instant.now());

        when(commentService.updateComment(any(UUID.class), any(UUID.class), any(CommentUpdateRequest.class)))
                .thenReturn(updatedComment);

        // When & Then
        mockMvc.perform(put("/api/comments/{id}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.body").value("Updated comment body"));
    }

    @Test
    @DisplayName("Should return 400 when update comment fails")
    void shouldReturn400WhenUpdateCommentFails() throws Exception {
        // Given
        when(authenticationService.getCurrentUserId()).thenReturn(userId);
        when(commentService.updateComment(any(UUID.class), any(UUID.class), any(CommentUpdateRequest.class)))
                .thenThrow(new RuntimeException("Unauthorized or comment not found"));

        // When & Then
        mockMvc.perform(put("/api/comments/{id}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentUpdateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should delete comment successfully")
    void shouldDeleteCommentSuccessfully() throws Exception {
        // Given
        when(authenticationService.getCurrentUserId()).thenReturn(userId);
        doNothing().when(commentService).deleteComment(any(UUID.class), any(UUID.class));

        // When & Then
        mockMvc.perform(delete("/api/comments/{id}", commentId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 400 when delete comment fails")
    void shouldReturn400WhenDeleteCommentFails() throws Exception {
        // Given
        when(authenticationService.getCurrentUserId())
                .thenThrow(new RuntimeException("Unauthorized or comment not found"));

        // When & Then
        mockMvc.perform(delete("/api/comments/{id}", commentId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should create comment with parent comment id")
    void shouldCreateCommentWithParentCommentId() throws Exception {
        // Given
        UUID parentCommentId = UUID.randomUUID();
        CommentCreateRequest requestWithParent = new CommentCreateRequest();
        requestWithParent.setBody("Reply to comment");
        requestWithParent.setParentCommentId(parentCommentId);

        CommentResponse replyResponse = new CommentResponse();
        replyResponse.setId(UUID.randomUUID());
        replyResponse.setBody("Reply to comment");
        replyResponse.setUserId(userId);
        replyResponse.setUsername("tester");
        replyResponse.setPostId(postId);
        replyResponse.setParentCommentId(parentCommentId);
        replyResponse.setCreatedAt(java.time.Instant.now());
        replyResponse.setUpdatedAt(java.time.Instant.now());

        when(authenticationService.getCurrentUserId()).thenReturn(userId);
        when(commentService.createComment(any(UUID.class), any(UUID.class), any(CommentCreateRequest.class)))
                .thenReturn(replyResponse);

        // When & Then
        mockMvc.perform(post("/api/comments/post/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithParent)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.parentCommentId").value(parentCommentId.toString()));
    }
}
