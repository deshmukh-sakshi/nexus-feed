package com.nexus.feed.backend.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.feed.backend.DTO.VoteRequest;
import com.nexus.feed.backend.Entity.Vote;
import com.nexus.feed.backend.Service.AuthenticationService;
import com.nexus.feed.backend.Service.VoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.context.annotation.Import;
import com.nexus.feed.backend.Exception.GlobalExceptionHandler;
import com.nexus.feed.backend.Exception.UnauthorizedException;
import com.nexus.feed.backend.Exception.ResourceNotFoundException;
import com.nexus.feed.backend.Auth.Service.JwtService;
import com.nexus.feed.backend.Auth.Service.UserDetailsServiceImpl;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@WebMvcTest(controllers = VoteController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@DisplayName("VoteController Tests")
class VoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VoteService voteService;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private UUID userId;
    private UUID votableId;
    private VoteRequest voteRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        votableId = UUID.randomUUID();

        // Setup vote request
        voteRequest = new VoteRequest();
        voteRequest.setVotableId(votableId);
        voteRequest.setVotableType(Vote.VotableType.POST);
        voteRequest.setVoteValue(Vote.VoteValue.UPVOTE);
    }

    @Test
    @DisplayName("Should vote successfully")
    void shouldVoteSuccessfully() throws Exception {
        // Given
        when(authenticationService.getCurrentUserId()).thenReturn(userId);
        doNothing().when(voteService).vote(any(UUID.class), any(VoteRequest.class));

        // When & Then
        mockMvc.perform(post("/api/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(voteRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 400 when voting with null votable id")
    void shouldFailVoteWithNullVotableId() throws Exception {
        // Given
        VoteRequest invalidRequest = new VoteRequest();
        invalidRequest.setVotableId(null);
        invalidRequest.setVotableType(Vote.VotableType.POST);
        invalidRequest.setVoteValue(Vote.VoteValue.UPVOTE);

        // When & Then
        mockMvc.perform(post("/api/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when voting with null votable type")
    void shouldFailVoteWithNullVotableType() throws Exception {
        // Given
        VoteRequest invalidRequest = new VoteRequest();
        invalidRequest.setVotableId(votableId);
        invalidRequest.setVotableType(null);
        invalidRequest.setVoteValue(Vote.VoteValue.UPVOTE);

        // When & Then
        mockMvc.perform(post("/api/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when voting with null vote value")
    void shouldFailVoteWithNullVoteValue() throws Exception {
        // Given
        VoteRequest invalidRequest = new VoteRequest();
        invalidRequest.setVotableId(votableId);
        invalidRequest.setVotableType(Vote.VotableType.POST);
        invalidRequest.setVoteValue(null);

        // When & Then
        mockMvc.perform(post("/api/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should vote on comment successfully")
    void shouldVoteOnCommentSuccessfully() throws Exception {
        // Given
        VoteRequest commentVoteRequest = new VoteRequest();
        commentVoteRequest.setVotableId(votableId);
        commentVoteRequest.setVotableType(Vote.VotableType.COMMENT);
        commentVoteRequest.setVoteValue(Vote.VoteValue.DOWNVOTE);

        when(authenticationService.getCurrentUserId()).thenReturn(userId);
        doNothing().when(voteService).vote(any(UUID.class), any(VoteRequest.class));

        // When & Then
        mockMvc.perform(post("/api/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentVoteRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 403 when vote fails due to unauthorized")
    void shouldReturn403WhenVoteFails() throws Exception {
        // Given
        when(authenticationService.getCurrentUserId())
                .thenThrow(new UnauthorizedException("User not authenticated"));

        // When & Then
        mockMvc.perform(post("/api/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(voteRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should remove vote successfully")
    void shouldRemoveVoteSuccessfully() throws Exception {
        // Given
        when(authenticationService.getCurrentUserId()).thenReturn(userId);
        doNothing().when(voteService).removeVote(any(UUID.class), any(UUID.class), any(Vote.VotableType.class));

        // When & Then
        mockMvc.perform(delete("/api/votes/{votableId}", votableId)
                        .param("votableType", Vote.VotableType.POST.name()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 403 when remove vote fails due to unauthorized")
    void shouldReturn403WhenRemoveVoteFails() throws Exception {
        // Given
        when(authenticationService.getCurrentUserId())
                .thenThrow(new UnauthorizedException("User not authenticated"));

        // When & Then
        mockMvc.perform(delete("/api/votes/{votableId}", votableId)
                        .param("votableType", Vote.VotableType.POST.name()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should get vote counts successfully")
    void shouldGetVoteCountsSuccessfully() throws Exception {
        // Given
        when(voteService.getUpvoteCount(votableId, Vote.VotableType.POST)).thenReturn(10L);
        when(voteService.getDownvoteCount(votableId, Vote.VotableType.POST)).thenReturn(2L);

        // When & Then
        mockMvc.perform(get("/api/votes/{votableId}/counts", votableId)
                        .param("votableType", Vote.VotableType.POST.name()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.upvotes").value(10))
                .andExpect(jsonPath("$.downvotes").value(2))
                .andExpect(jsonPath("$.userVote").isEmpty());
    }

    @Test
    @DisplayName("Should get vote counts with user vote for authenticated user")
    void shouldGetVoteCountsWithUserVote() throws Exception {
        // Given
        when(authenticationService.getCurrentUserId()).thenReturn(userId);
        when(voteService.getUpvoteCount(votableId, Vote.VotableType.POST)).thenReturn(10L);
        when(voteService.getDownvoteCount(votableId, Vote.VotableType.POST)).thenReturn(2L);
        when(voteService.getUserVote(userId, votableId, Vote.VotableType.POST))
                .thenReturn(Vote.VoteValue.UPVOTE);

        // When & Then
        mockMvc.perform(get("/api/votes/{votableId}/counts", votableId)
                        .param("votableType", Vote.VotableType.POST.name()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.upvotes").value(10))
                .andExpect(jsonPath("$.downvotes").value(2))
                .andExpect(jsonPath("$.userVote").value("UPVOTE"));
    }

    @Test
    @DisplayName("Should get vote counts without user vote for unauthenticated user")
    void shouldGetVoteCountsWithoutUserVoteForUnauthenticatedUser() throws Exception {
        // Given
        when(authenticationService.getCurrentUserId())
                .thenThrow(new UnauthorizedException("User not authenticated"));
        when(voteService.getUpvoteCount(votableId, Vote.VotableType.POST)).thenReturn(10L);
        when(voteService.getDownvoteCount(votableId, Vote.VotableType.POST)).thenReturn(2L);

        // When & Then
        mockMvc.perform(get("/api/votes/{votableId}/counts", votableId)
                        .param("votableType", Vote.VotableType.POST.name()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.upvotes").value(10))
                .andExpect(jsonPath("$.downvotes").value(2))
                .andExpect(jsonPath("$.userVote").isEmpty());
    }

    @Test
    @DisplayName("Should return 404 when getting vote counts for non-existent votable")
    void shouldReturn404WhenGetVoteCountsFails() throws Exception {
        // Given
        when(voteService.getUpvoteCount(votableId, Vote.VotableType.POST))
                .thenThrow(new ResourceNotFoundException("Post", "id", votableId));

        // When & Then
        mockMvc.perform(get("/api/votes/{votableId}/counts", votableId)
                        .param("votableType", Vote.VotableType.POST.name()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get vote counts for comment")
    void shouldGetVoteCountsForComment() throws Exception {
        // Given
        when(voteService.getUpvoteCount(votableId, Vote.VotableType.COMMENT)).thenReturn(5L);
        when(voteService.getDownvoteCount(votableId, Vote.VotableType.COMMENT)).thenReturn(1L);

        // When & Then
        mockMvc.perform(get("/api/votes/{votableId}/counts", votableId)
                        .param("votableType", Vote.VotableType.COMMENT.name()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.upvotes").value(5))
                .andExpect(jsonPath("$.downvotes").value(1));
    }
}

