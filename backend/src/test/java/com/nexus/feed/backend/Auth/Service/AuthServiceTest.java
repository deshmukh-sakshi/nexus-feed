package com.nexus.feed.backend.Auth.Service;

import com.nexus.feed.backend.Auth.DTO.AuthResponse;
import com.nexus.feed.backend.Auth.DTO.RegistrationRequest;
import com.nexus.feed.backend.Auth.Entity.AppUser;
import com.nexus.feed.backend.Auth.Repository.AppUserRepository;
import com.nexus.feed.backend.Email.Service.EmailService;
import com.nexus.feed.backend.Entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    private AuthService authService;

    private RegistrationRequest registrationRequest;
    private AppUser savedUser;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
            authenticationManager,
            appUserRepository,
            passwordEncoder,
            jwtService,
            emailService
        );

        registrationRequest = new RegistrationRequest();
        registrationRequest.setUsername("tester");
        registrationRequest.setEmail("test@example.com");
        registrationRequest.setPassword("password123");

        savedUser = createMockAppUser(registrationRequest);
    }

    @Test
    @DisplayName("Should send welcome email on successful registration")
    void shouldSendWelcomeEmailOnRegistration() {
        // Given
        when(appUserRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(appUserRepository.save(any(AppUser.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any())).thenReturn("mock-token");

        // When
        AuthResponse response = authService.register(registrationRequest);

        // Then
        assertThat(response).isNotNull();
        verify(emailService).sendWelcomeEmail("test@example.com", "tester");
    }

    @Test
    @DisplayName("Should complete registration even when email fails")
    void shouldCompleteRegistrationWhenEmailFails() {
        // Given
        when(appUserRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(appUserRepository.save(any(AppUser.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any())).thenReturn("mock-token");
        doThrow(new RuntimeException("SMTP error"))
            .when(emailService).sendWelcomeEmail(anyString(), anyString());

        // When
        AuthResponse response = authService.register(registrationRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.username()).isEqualTo("tester");
    }

    private AppUser createMockAppUser(RegistrationRequest request) {
        AppUser appUser = new AppUser();
        appUser.setId(1L);
        appUser.setEmail(request.getEmail());
        appUser.setPassword("encoded-password");
        
        Users userProfile = new Users();
        userProfile.setUsername(request.getUsername());
        userProfile.setAppUser(appUser);
        appUser.setUserProfile(userProfile);
        
        return appUser;
    }
}
