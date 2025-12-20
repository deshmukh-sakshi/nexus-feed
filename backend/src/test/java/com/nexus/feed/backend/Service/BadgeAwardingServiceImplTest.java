package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.Auth.Entity.AppUser;
import com.nexus.feed.backend.Email.Service.EmailService;
import com.nexus.feed.backend.Entity.Badge;
import com.nexus.feed.backend.Entity.Users;
import com.nexus.feed.backend.Repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BadgeAwardingServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private VoteRepository voteRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BadgeRepository badgeRepository;
    @Mock
    private UserBadgeRepository userBadgeRepository;
    @Mock
    private BadgeService badgeService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private BadgeAwardingServiceImpl badgeAwardingService;

    private UUID userId;
    private Users user;
    private Badge badge;
    private Integer badgeId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new Users();
        user.setId(userId);
        user.setUsername("testuser");
        user.setCreatedAt(Instant.now());
        
        AppUser appUser = new AppUser();
        appUser.setEmail("test@example.com");
        user.setAppUser(appUser);

        badgeId = 1;
        badge = new Badge();
        badge.setId(badgeId);
        badge.setName("First Post");
        badge.setDescription("Created your first post");
        badge.setIconUrl("🎉");
    }

    @Test
    void checkPostBadges_shouldAwardFirstPostBadge_whenUserHasOnePost() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.countByUser(user)).thenReturn(1L);
        when(badgeRepository.findByName("First Post")).thenReturn(Optional.of(badge));
        when(userBadgeRepository.existsByIdUserIdAndIdBadgeId(userId, badgeId)).thenReturn(false);

        // When
        badgeAwardingService.checkPostBadges(userId);

        // Then
        verify(badgeService).awardBadgeToUser(userId, badgeId);
        verify(emailService).sendBadgeAwardedEmail(
            eq("test@example.com"),
            eq("testuser"),
            eq("First Post"),
            eq("Created your first post"),
            eq("🎉")
        );
    }

    @Test
    void checkPostBadges_shouldNotAwardBadge_whenUserAlreadyHasIt() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.countByUser(user)).thenReturn(1L);
        when(badgeRepository.findByName("First Post")).thenReturn(Optional.of(badge));
        when(userBadgeRepository.existsByIdUserIdAndIdBadgeId(userId, badgeId)).thenReturn(true);

        // When
        badgeAwardingService.checkPostBadges(userId);

        // Then
        verify(badgeService, never()).awardBadgeToUser(any(), any());
        verify(emailService, never()).sendBadgeAwardedEmail(any(), any(), any(), any(), any());
    }

    @Test
    void checkPostBadges_shouldAwardStorytellerBadge_whenUserHasFivePosts() {
        // Given
        Integer storytellerBadgeId = 2;
        Badge storytellerBadge = new Badge();
        storytellerBadge.setId(storytellerBadgeId);
        storytellerBadge.setName("Storyteller");
        storytellerBadge.setDescription("Created 5 posts");
        storytellerBadge.setIconUrl("📝");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.countByUser(user)).thenReturn(5L);
        when(badgeRepository.findByName("First Post")).thenReturn(Optional.of(badge));
        when(badgeRepository.findByName("Storyteller")).thenReturn(Optional.of(storytellerBadge));
        when(userBadgeRepository.existsByIdUserIdAndIdBadgeId(userId, badgeId)).thenReturn(false);
        when(userBadgeRepository.existsByIdUserIdAndIdBadgeId(userId, storytellerBadgeId)).thenReturn(false);

        // When
        badgeAwardingService.checkPostBadges(userId);

        // Then
        verify(badgeService).awardBadgeToUser(userId, badgeId);
        verify(badgeService).awardBadgeToUser(userId, storytellerBadgeId);
    }

    @Test
    void checkCommentBadges_shouldAwardFirstCommentBadge_whenUserHasOneComment() {
        // Given
        Integer firstCommentBadgeId = 3;
        Badge firstCommentBadge = new Badge();
        firstCommentBadge.setId(firstCommentBadgeId);
        firstCommentBadge.setName("First Comment");
        firstCommentBadge.setDescription("Made your first comment");
        firstCommentBadge.setIconUrl("💭");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentRepository.countByUser(user)).thenReturn(1L);
        when(badgeRepository.findByName("First Comment")).thenReturn(Optional.of(firstCommentBadge));
        when(userBadgeRepository.existsByIdUserIdAndIdBadgeId(userId, firstCommentBadgeId)).thenReturn(false);

        // When
        badgeAwardingService.checkCommentBadges(userId);

        // Then
        verify(badgeService).awardBadgeToUser(userId, firstCommentBadgeId);
    }

    @Test
    void checkKarmaBadges_shouldAwardGettingStartedBadge_whenUserHas10Karma() {
        // Given
        Integer karmaBadgeId = 4;
        Badge karmaBadge = new Badge();
        karmaBadge.setId(karmaBadgeId);
        karmaBadge.setName("Getting Started");
        karmaBadge.setDescription("Reached 10 karma");
        karmaBadge.setIconUrl("🌱");

        user.setKarma(10L);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(badgeRepository.findByName("Getting Started")).thenReturn(Optional.of(karmaBadge));
        when(userBadgeRepository.existsByIdUserIdAndIdBadgeId(userId, karmaBadgeId)).thenReturn(false);

        // When
        badgeAwardingService.checkKarmaBadges(userId);

        // Then
        verify(badgeService).awardBadgeToUser(userId, karmaBadgeId);
    }

    @Test
    void checkVoteBadges_shouldAwardFirstVoteBadge_whenUserHasOneVote() {
        // Given
        Integer voteBadgeId = 5;
        Badge voteBadge = new Badge();
        voteBadge.setId(voteBadgeId);
        voteBadge.setName("First Vote");
        voteBadge.setDescription("Cast your first vote");
        voteBadge.setIconUrl("👍");

        when(voteRepository.countByUserId(userId)).thenReturn(1L);
        when(badgeRepository.findByName("First Vote")).thenReturn(Optional.of(voteBadge));
        when(userBadgeRepository.existsByIdUserIdAndIdBadgeId(userId, voteBadgeId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        badgeAwardingService.checkVoteBadges(userId);

        // Then
        verify(badgeService).awardBadgeToUser(userId, voteBadgeId);
    }

    @Test
    void checkAccountAgeBadges_shouldAwardNewcomerBadge_whenAccountIs7DaysOld() {
        // Given
        Integer newcomerBadgeId = 6;
        Badge newcomerBadge = new Badge();
        newcomerBadge.setId(newcomerBadgeId);
        newcomerBadge.setName("Newcomer");
        newcomerBadge.setDescription("Member for 7 days");
        newcomerBadge.setIconUrl("👋");

        user.setCreatedAt(Instant.now().minus(8, ChronoUnit.DAYS));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(badgeRepository.findByName("Newcomer")).thenReturn(Optional.of(newcomerBadge));
        when(userBadgeRepository.existsByIdUserIdAndIdBadgeId(userId, newcomerBadgeId)).thenReturn(false);

        // When
        badgeAwardingService.checkAccountAgeBadges(userId);

        // Then
        verify(badgeService).awardBadgeToUser(userId, newcomerBadgeId);
    }

    @Test
    void checkAccountAgeBadges_shouldNotAwardBadge_whenAccountIsTooNew() {
        // Given
        user.setCreatedAt(Instant.now().minus(3, ChronoUnit.DAYS));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        badgeAwardingService.checkAccountAgeBadges(userId);

        // Then
        verify(badgeService, never()).awardBadgeToUser(any(), any());
    }

    @Test
    void checkAllBadges_shouldCheckAllBadgeTypes() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.countByUser(user)).thenReturn(0L);
        when(commentRepository.countByUser(user)).thenReturn(0L);
        when(voteRepository.countByUserId(userId)).thenReturn(0L);

        // When
        badgeAwardingService.checkAllBadges(userId);

        // Then - verify all check methods were called
        verify(postRepository).countByUser(user);
        verify(commentRepository).countByUser(user);
        verify(voteRepository).countByUserId(userId);
    }

    @Test
    void tryAwardBadge_shouldHandleMissingBadgeGracefully() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.countByUser(user)).thenReturn(1L);
        when(badgeRepository.findByName("First Post")).thenReturn(Optional.empty());

        // When
        badgeAwardingService.checkPostBadges(userId);

        // Then - should not throw, just log warning
        verify(badgeService, never()).awardBadgeToUser(any(), any());
    }

    @Test
    void tryAwardBadge_shouldHandleMissingUserGracefully() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        badgeAwardingService.checkPostBadges(userId);

        // Then - should not throw
        verify(postRepository, never()).countByUser(any());
        verify(badgeService, never()).awardBadgeToUser(any(), any());
    }
}
