package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.DTO.BadgeResponse;
import com.nexus.feed.backend.Entity.Badge;
import com.nexus.feed.backend.Entity.UserBadge;
import com.nexus.feed.backend.Entity.Users;
import com.nexus.feed.backend.Exception.ResourceNotFoundException;
import com.nexus.feed.backend.Repository.BadgeRepository;
import com.nexus.feed.backend.Repository.UserBadgeRepository;
import com.nexus.feed.backend.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BadgeServiceImpl Unit Tests")
class BadgeServiceImplTest {

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserBadgeRepository userBadgeRepository;

    @InjectMocks
    private BadgeServiceImpl badgeService;

    private Badge badge;
    private Users user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        badge = new Badge();
        badge.setId(1);
        badge.setName("First Post");
        badge.setDescription("Created your first post");
        badge.setIconUrl("🎉");

        user = new Users();
        user.setId(userId);
        user.setUsername("tester");
    }

    @Test
    @DisplayName("Should get all badges successfully")
    void shouldGetAllBadgesSuccessfully() {
        // Given
        Badge badge2 = new Badge();
        badge2.setId(2);
        badge2.setName("Commenter");
        badge2.setDescription("Made 10 comments");
        badge2.setIconUrl("💬");

        when(badgeRepository.findAll()).thenReturn(Arrays.asList(badge, badge2));

        // When
        List<BadgeResponse> responses = badgeService.getAllBadges();

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("First Post");
        assertThat(responses.get(1).getName()).isEqualTo("Commenter");
    }

    @Test
    @DisplayName("Should get badge by id successfully")
    void shouldGetBadgeByIdSuccessfully() {
        // Given
        when(badgeRepository.findById(1)).thenReturn(Optional.of(badge));

        // When
        BadgeResponse response = badgeService.getBadgeById(1);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1);
        assertThat(response.getName()).isEqualTo("First Post");
        assertThat(response.getDescription()).isEqualTo("Created your first post");
        assertThat(response.getIconUrl()).isEqualTo("🎉");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when badge not found by id")
    void shouldThrowExceptionWhenBadgeNotFoundById() {
        // Given
        when(badgeRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> badgeService.getBadgeById(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Badge");
    }

    @Test
    @DisplayName("Should get badge by name successfully")
    void shouldGetBadgeByNameSuccessfully() {
        // Given
        when(badgeRepository.findByName("First Post")).thenReturn(Optional.of(badge));

        // When
        BadgeResponse response = badgeService.getBadgeByName("First Post");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("First Post");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when badge not found by name")
    void shouldThrowExceptionWhenBadgeNotFoundByName() {
        // Given
        when(badgeRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> badgeService.getBadgeByName("Nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Badge");
    }

    @Test
    @DisplayName("Should create badge successfully")
    void shouldCreateBadgeSuccessfully() {
        // Given
        when(badgeRepository.existsByName("New Badge")).thenReturn(false);
        when(badgeRepository.save(any(Badge.class))).thenReturn(badge);

        // When
        Badge created = badgeService.createBadge("New Badge", "Description", "🏆");

        // Then
        assertThat(created).isNotNull();
        verify(badgeRepository).save(any(Badge.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when creating duplicate badge")
    void shouldThrowExceptionWhenCreatingDuplicateBadge() {
        // Given
        when(badgeRepository.existsByName("First Post")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> badgeService.createBadge("First Post", "Desc", "🎉"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should get user badges successfully")
    void shouldGetUserBadgesSuccessfully() {
        // Given
        UserBadge userBadge = new UserBadge();
        userBadge.setBadge(badge);
        userBadge.setUser(user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userBadgeRepository.findByUser(user)).thenReturn(List.of(userBadge));

        // When
        List<BadgeResponse> responses = badgeService.getUserBadges(userId);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("First Post");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when getting badges for non-existent user")
    void shouldThrowExceptionWhenGettingBadgesForNonExistentUser() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> badgeService.getUserBadges(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    @DisplayName("Should award badge to user successfully")
    void shouldAwardBadgeToUserSuccessfully() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(badgeRepository.findById(1)).thenReturn(Optional.of(badge));
        when(userBadgeRepository.existsByIdUserIdAndIdBadgeId(userId, 1)).thenReturn(false);
        when(userBadgeRepository.save(any(UserBadge.class))).thenReturn(new UserBadge());

        // When
        badgeService.awardBadgeToUser(userId, 1);

        // Then
        verify(userBadgeRepository).save(any(UserBadge.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when awarding badge to non-existent user")
    void shouldThrowExceptionWhenAwardingBadgeToNonExistentUser() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> badgeService.awardBadgeToUser(userId, 1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when awarding non-existent badge")
    void shouldThrowExceptionWhenAwardingNonExistentBadge() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(badgeRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> badgeService.awardBadgeToUser(userId, 999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Badge");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when user already has badge")
    void shouldThrowExceptionWhenUserAlreadyHasBadge() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(badgeRepository.findById(1)).thenReturn(Optional.of(badge));
        when(userBadgeRepository.existsByIdUserIdAndIdBadgeId(userId, 1)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> badgeService.awardBadgeToUser(userId, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already has");
    }

    @Test
    @DisplayName("Should return empty list when user has no badges")
    void shouldReturnEmptyListWhenUserHasNoBadges() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userBadgeRepository.findByUser(user)).thenReturn(List.of());

        // When
        List<BadgeResponse> responses = badgeService.getUserBadges(userId);

        // Then
        assertThat(responses).isEmpty();
    }
}
