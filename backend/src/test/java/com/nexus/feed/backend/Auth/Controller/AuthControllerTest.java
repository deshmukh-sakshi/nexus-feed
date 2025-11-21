package com.nexus.feed.backend.Auth.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.feed.backend.Auth.DTO.AuthResponse;
import com.nexus.feed.backend.Auth.DTO.LoginRequest;
import com.nexus.feed.backend.Auth.DTO.RegistrationRequest;
import com.nexus.feed.backend.Auth.Service.AuthService;
import com.nexus.feed.backend.Exception.UserAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private com.nexus.feed.backend.Auth.Service.JwtService jwtService;

    @MockitoBean
    private com.nexus.feed.backend.Auth.Service.UserDetailsServiceImpl userDetailsService;

    private LoginRequest validLoginRequest;
    private RegistrationRequest validRegistrationRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        // Setup valid login request
        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("password123");

        // Setup valid registration request
        validRegistrationRequest = new RegistrationRequest();
        validRegistrationRequest.setUsername("tester");
        validRegistrationRequest.setEmail("test@example.com");
        validRegistrationRequest.setPassword("password123");

        // Setup auth response
        authResponse = new AuthResponse(
                UUID.randomUUID(),
                "tester",
                "test@example.com",
                "mock-jwt-token"
        );
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void shouldLoginSuccessfully() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.username").value("tester"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
    }

    @Test
    @DisplayName("Should return 400 when login with invalid email format")
    void shouldFailLoginWithInvalidEmail() throws Exception {
        // Given
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when login with blank email")
    void shouldFailLoginWithBlankEmail() throws Exception {
        // Given
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail("");
        invalidRequest.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when login with blank password")
    void shouldFailLoginWithBlankPassword() throws Exception {
        // Given
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when login with wrong credentials")
    void shouldFailLoginWithWrongCredentials() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should successfully register with valid data")
    void shouldRegisterSuccessfully() throws Exception {
        // Given
        when(authService.register(any(RegistrationRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.username").value("tester"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
    }

    @Test
    @DisplayName("Should return 400 when register with blank username")
    void shouldFailRegisterWithBlankUsername() throws Exception {
        // Given
        RegistrationRequest invalidRequest = new RegistrationRequest();
        invalidRequest.setUsername("");
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when register with username exceeding max length")
    void shouldFailRegisterWithLongUsername() throws Exception {
        // Given
        RegistrationRequest invalidRequest = new RegistrationRequest();
        invalidRequest.setUsername("a".repeat(51)); // Exceeds max length of 50
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when register with invalid email format")
    void shouldFailRegisterWithInvalidEmail() throws Exception {
        // Given
        RegistrationRequest invalidRequest = new RegistrationRequest();
        invalidRequest.setUsername("tester");
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when register with password shorter than minimum length")
    void shouldFailRegisterWithShortPassword() throws Exception {
        // Given
        RegistrationRequest invalidRequest = new RegistrationRequest();
        invalidRequest.setUsername("tester");
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("pass"); // Less than 8 characters

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when register with password exceeding max length")
    void shouldFailRegisterWithLongPassword() throws Exception {
        // Given
        RegistrationRequest invalidRequest = new RegistrationRequest();
        invalidRequest.setUsername("tester");
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("a".repeat(21)); // Exceeds max length of 20

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when register with existing email")
    void shouldFailRegisterWithExistingEmail() throws Exception {
        // Given
        when(authService.register(any(RegistrationRequest.class)))
                .thenThrow(new UserAlreadyExistsException("User with email test@example.com already exists"));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should return 400 when login request body is missing")
    void shouldFailLoginWithMissingBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when register request body is missing")
    void shouldFailRegisterWithMissingBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
