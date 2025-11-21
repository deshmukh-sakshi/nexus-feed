package com.nexus.feed.backend.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.feed.backend.DTO.UserCreateRequest;
import com.nexus.feed.backend.DTO.UserResponse;
import com.nexus.feed.backend.DTO.UserUpdateRequest;
import com.nexus.feed.backend.Repository.UserRepository;
import com.nexus.feed.backend.Service.UserService;
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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private com.nexus.feed.backend.Auth.Service.JwtService jwtService;

    @MockitoBean
    private com.nexus.feed.backend.Auth.Service.UserDetailsServiceImpl userDetailsService;

    private UUID userId;
    private UserCreateRequest userCreateRequest;
    private UserUpdateRequest userUpdateRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        // Setup create request
        userCreateRequest = new UserCreateRequest();
        userCreateRequest.setUsername("tester");
        userCreateRequest.setEmail("test@example.com");
        userCreateRequest.setPassword("password123");

        // Setup update request
        userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setBio("Updated bio");
        userUpdateRequest.setProfilePictureUrl("https://example.com/profile.jpg");

        // Setup response
        userResponse = new UserResponse();
        userResponse.setId(userId);
        userResponse.setUsername("tester");
        userResponse.setEmail("test@example.com");
        userResponse.setCreatedAt(LocalDateTime.now());
        userResponse.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should return success for test endpoint")
    void shouldReturnSuccessForTestEndpoint() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("API is working!"));
    }

    @Test
    @DisplayName("Should return database status for test-db endpoint")
    void shouldReturnDatabaseStatusForTestDbEndpoint() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/api/users/test-db"))
                .andExpect(status().isOk())
                .andExpect(content().string("Database connected! User count: 5"));
    }

    @Test
    @DisplayName("Should handle database error gracefully")
    void shouldHandleDatabaseErrorGracefully() throws Exception {
        // Given
        when(userRepository.count()).thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/users/test-db"))
                .andExpect(status().isOk())
                .andExpect(content().string("Database error: Database connection failed"));
    }

    @Test
    @DisplayName("Should register user successfully")
    void shouldRegisterUserSuccessfully() throws Exception {
        // Given
        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("tester"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("Should return 400 when registration fails")
    void shouldReturn400WhenRegistrationFails() throws Exception {
        // Given
        when(userService.createUser(any(UserCreateRequest.class)))
                .thenThrow(new RuntimeException("User already exists"));

        // When & Then
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get user by username successfully")
    void shouldGetUserByUsernameSuccessfully() throws Exception {
        // Given
        when(userService.getUserByUsername("tester")).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/api/users/username/{username}", "tester"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("tester"));
    }

    @Test
    @DisplayName("Should return 404 when user not found by username")
    void shouldReturn404WhenUserNotFoundByUsername() throws Exception {
        // Given
        when(userService.getUserByUsername("nonexistent"))
                .thenThrow(new RuntimeException("User not found"));

        // When & Then
        mockMvc.perform(get("/api/users/username/{username}", "nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get user by id successfully")
    void shouldGetUserByIdSuccessfully() throws Exception {
        // Given
        when(userService.getUserById(userId)).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/api/users/id/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()));
    }

    @Test
    @DisplayName("Should return 404 when user not found by id")
    void shouldReturn404WhenUserNotFoundById() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(userService.getUserById(nonExistentId))
                .thenThrow(new RuntimeException("User not found"));

        // When & Then
        mockMvc.perform(get("/api/users/id/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() throws Exception {
        // Given
        UserResponse updatedUser = new UserResponse();
        updatedUser.setId(userId);
        updatedUser.setUsername("tester");
        updatedUser.setBio("Updated bio");
        updatedUser.setProfilePictureUrl("https://example.com/profile.jpg");
        updatedUser.setEmail("test@example.com");
        updatedUser.setCreatedAt(LocalDateTime.now());
        updatedUser.setUpdatedAt(LocalDateTime.now());

        when(userService.updateUser(any(UUID.class), any(UserUpdateRequest.class)))
                .thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/users/id/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("tester"))
                .andExpect(jsonPath("$.bio").value("Updated bio"))
                .andExpect(jsonPath("$.profilePictureUrl").value("https://example.com/profile.jpg"));
    }

    @Test
    @DisplayName("Should return 404 when update fails")
    void shouldReturn404WhenUpdateFails() throws Exception {
        // Given
        when(userService.updateUser(any(UUID.class), any(UserUpdateRequest.class)))
                .thenThrow(new RuntimeException("User not found"));

        // When & Then
        mockMvc.perform(put("/api/users/id/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() throws Exception {
        // Given
        doNothing().when(userService).deleteUser(any(UUID.class));

        // When & Then
        mockMvc.perform(delete("/api/users/id/{id}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 404 when delete fails")
    void shouldReturn404WhenDeleteFails() throws Exception {
        // Given
        doThrow(new RuntimeException("User not found")).when(userService).deleteUser(any(UUID.class));

        // When & Then
        mockMvc.perform(delete("/api/users/id/{id}", userId))
                .andExpect(status().isNotFound());
    }
}

