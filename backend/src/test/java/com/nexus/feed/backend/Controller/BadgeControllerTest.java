package com.nexus.feed.backend.Controller;

import com.nexus.feed.backend.DTO.BadgeResponse;
import com.nexus.feed.backend.Service.BadgeAwardingService;
import com.nexus.feed.backend.Service.BadgeService;
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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BadgeController.class, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("BadgeController Tests")
class BadgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BadgeService badgeService;

    @MockitoBean
    private BadgeAwardingService badgeAwardingService;

    @MockitoBean
    private com.nexus.feed.backend.Auth.Service.JwtService jwtService;

    @MockitoBean
    private com.nexus.feed.backend.Auth.Service.UserDetailsServiceImpl userDetailsService;

    private BadgeResponse badgeResponse;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        // Setup badge response
        badgeResponse = new BadgeResponse();
        badgeResponse.setId(1);
        badgeResponse.setName("First Post");
        badgeResponse.setDescription("Created your first post");
        badgeResponse.setIconUrl("🎉");
    }

    @Test
    @DisplayName("Should get all badges successfully")
    void shouldGetAllBadgesSuccessfully() throws Exception {
        // Given
        BadgeResponse badge2 = new BadgeResponse();
        badge2.setId(2);
        badge2.setName("Commenter");
        badge2.setDescription("Made 10 comments");
        badge2.setIconUrl("💬");

        List<BadgeResponse> badges = Arrays.asList(badgeResponse, badge2);
        when(badgeService.getAllBadges()).thenReturn(badges);

        // When & Then
        mockMvc.perform(get("/api/badges"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("First Post"))
                .andExpect(jsonPath("$[1].name").value("Commenter"));
    }

    @Test
    @DisplayName("Should get badge by id successfully")
    void shouldGetBadgeByIdSuccessfully() throws Exception {
        // Given
        when(badgeService.getBadgeById(1)).thenReturn(badgeResponse);

        // When & Then
        mockMvc.perform(get("/api/badges/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("First Post"))
                .andExpect(jsonPath("$.description").value("Created your first post"));
    }

    @Test
    @DisplayName("Should return 404 when badge not found by id")
    void shouldReturn404WhenBadgeNotFoundById() throws Exception {
        // Given
        when(badgeService.getBadgeById(999))
                .thenThrow(new RuntimeException("Badge not found"));

        // When & Then
        mockMvc.perform(get("/api/badges/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get badge by name successfully")
    void shouldGetBadgeByNameSuccessfully() throws Exception {
        // Given
        when(badgeService.getBadgeByName("First Post")).thenReturn(badgeResponse);

        // When & Then
        mockMvc.perform(get("/api/badges/name/{name}", "First Post"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("First Post"));
    }

    @Test
    @DisplayName("Should return 404 when badge not found by name")
    void shouldReturn404WhenBadgeNotFoundByName() throws Exception {
        // Given
        when(badgeService.getBadgeByName("Nonexistent Badge"))
                .thenThrow(new RuntimeException("Badge not found"));

        // When & Then
        mockMvc.perform(get("/api/badges/name/{name}", "Nonexistent Badge"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get user badges successfully")
    void shouldGetUserBadgesSuccessfully() throws Exception {
        // Given
        BadgeResponse badge2 = new BadgeResponse();
        badge2.setId(2);
        badge2.setName("Active User");
        badge2.setDescription("Logged in for 7 consecutive days");
        badge2.setIconUrl("⭐");

        List<BadgeResponse> userBadges = Arrays.asList(badgeResponse, badge2);
        when(badgeService.getUserBadges(userId)).thenReturn(userBadges);

        // When & Then
        mockMvc.perform(get("/api/badges/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("First Post"))
                .andExpect(jsonPath("$[1].name").value("Active User"));
    }

    @Test
    @DisplayName("Should return 404 when getting badges for non-existent user")
    void shouldReturn404WhenGettingBadgesForNonExistentUser() throws Exception {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();
        when(badgeService.getUserBadges(nonExistentUserId))
                .thenThrow(new RuntimeException("User not found"));

        // When & Then
        mockMvc.perform(get("/api/badges/user/{userId}", nonExistentUserId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get empty list for user with no badges")
    void shouldGetEmptyListForUserWithNoBadges() throws Exception {
        // Given
        when(badgeService.getUserBadges(userId)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/badges/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Should award badge to user successfully")
    void shouldAwardBadgeToUserSuccessfully() throws Exception {
        // Given
        doNothing().when(badgeService).awardBadgeToUser(any(UUID.class), any(Integer.class));

        // When & Then
        mockMvc.perform(post("/api/badges/award")
                        .param("userId", userId.toString())
                        .param("badgeId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 400 when awarding badge fails")
    void shouldReturn400WhenAwardingBadgeFails() throws Exception {
        // Given
        doThrow(new RuntimeException("User or badge not found"))
                .when(badgeService).awardBadgeToUser(any(UUID.class), any(Integer.class));

        // When & Then
        mockMvc.perform(post("/api/badges/award")
                        .param("userId", userId.toString())
                        .param("badgeId", "999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when awarding duplicate badge")
    void shouldReturn400WhenAwardingDuplicateBadge() throws Exception {
        // Given
        doThrow(new RuntimeException("Badge already awarded"))
                .when(badgeService).awardBadgeToUser(any(UUID.class), any(Integer.class));

        // When & Then
        mockMvc.perform(post("/api/badges/award")
                        .param("userId", userId.toString())
                        .param("badgeId", "1"))
                .andExpect(status().isBadRequest());
    }
}
