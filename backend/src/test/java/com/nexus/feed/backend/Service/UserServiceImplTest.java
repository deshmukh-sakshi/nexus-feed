package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.Auth.Entity.AppUser;
import com.nexus.feed.backend.DTO.UserCreateRequest;
import com.nexus.feed.backend.DTO.UserResponse;
import com.nexus.feed.backend.DTO.UserUpdateRequest;
import com.nexus.feed.backend.Entity.Users;
import com.nexus.feed.backend.Exception.ResourceNotFoundException;
import com.nexus.feed.backend.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID userId;
    private Users user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        AppUser appUser = new AppUser();
        appUser.setEmail("test@example.com");
        appUser.setPassword("encodedPassword");

        user = new Users();
        user.setId(userId);
        user.setUsername("tester");
        user.setBio("Test bio");
        user.setProfilePictureUrl("https://example.com/pic.jpg");
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user.setAppUser(appUser);
    }

    @Test
    @DisplayName("Should throw UnsupportedOperationException when creating user")
    void shouldThrowUnsupportedOperationExceptionWhenCreatingUser() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPassword("password");

        // When & Then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Use /api/auth/register endpoint");
    }

    @Test
    @DisplayName("Should get user by id successfully")
    void shouldGetUserByIdSuccessfully() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        UserResponse response = userService.getUserById(userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getUsername()).isEqualTo("tester");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getBio()).isEqualTo("Test bio");
        assertThat(response.getProfilePictureUrl()).isEqualTo("https://example.com/pic.jpg");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found by id")
    void shouldThrowExceptionWhenUserNotFoundById() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("id");
    }

    @Test
    @DisplayName("Should get user by username successfully")
    void shouldGetUserByUsernameSuccessfully() {
        // Given
        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(user));

        // When
        UserResponse response = userService.getUserByUsername("tester");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("tester");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found by username")
    void shouldThrowExceptionWhenUserNotFoundByUsername() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserByUsername("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("username");
    }

    @Test
    @DisplayName("Should update user bio successfully")
    void shouldUpdateUserBioSuccessfully() {
        // Given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setBio("Updated bio");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(Users.class))).thenReturn(user);

        // When
        UserResponse response = userService.updateUser(userId, request);

        // Then
        assertThat(response).isNotNull();
        verify(userRepository).save(any(Users.class));
    }

    @Test
    @DisplayName("Should update user profile picture successfully")
    void shouldUpdateUserProfilePictureSuccessfully() {
        // Given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setProfilePictureUrl("https://example.com/newpic.jpg");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(Users.class))).thenReturn(user);

        // When
        UserResponse response = userService.updateUser(userId, request);

        // Then
        assertThat(response).isNotNull();
        verify(userRepository).save(any(Users.class));
    }

    @Test
    @DisplayName("Should update both bio and profile picture")
    void shouldUpdateBothBioAndProfilePicture() {
        // Given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setBio("New bio");
        request.setProfilePictureUrl("https://example.com/newpic.jpg");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(Users.class))).thenReturn(user);

        // When
        UserResponse response = userService.updateUser(userId, request);

        // Then
        assertThat(response).isNotNull();
        verify(userRepository).save(any(Users.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent user")
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        // Given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setBio("New bio");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(userId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
        // Given
        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent user")
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        // Given
        when(userRepository.existsById(userId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    @DisplayName("Should handle user without AppUser relationship")
    void shouldHandleUserWithoutAppUser() {
        // Given
        user.setAppUser(null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        UserResponse response = userService.getUserById(userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isNull();
    }
}
