package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.Entity.Badge;
import com.nexus.feed.backend.Entity.Users;
import com.nexus.feed.backend.Repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BadgeAwardingServiceImpl Unit Tests")
class BadgeAwardingServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private UserBadgeRepository userBadgeRepository;

    @Mock
    private BadgeService badgeService;

    @InjectMocks
    private BadgeAwardingServiceImpl badgeAwardingService;

    private UUID userId;
    private Users user;
    private Badge firstPostBadge;
    private Badge prolificPosterBadge;
    private Badge commentatorBadge;
    private Badge risingStarBadge;
    private Badge popularBadge;
    private Badge veteranBadge;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = new Users();
        user.setId(userId);
        user.setUsername("tester");
        user.setKarma(0L);
        user.setCreatedAt(Instant.now());

        firstPostBadge = new Badge();
        firstPostBadge.setId(1);
        firstPostBadge.setName("First Post");

        prolificPosterBadge = new Badge();
        prolificPosterBadge.setId(2);
        prolificPosterBadge.setName("Prolific Poster");

        commentatorBadge = new Badge();
        commentatorBadge.setId(3);
        commentatorBadge.setName("Commentator");

        risingStarBadge = new Badge();
        risingStarBadge.setId(4);
        risingStarBadge.setName("Rising Star");

        popularBadge = new Badge();
        popularBadge.setId(5);
        popularBadge.setName("Popular");

        veteranBadge = new Badge();
        veteranBadge.setId(6);
        veteranBadge.setName("Veteran");
    }

    @Test
    @DisplayName("Should award First Post badge when user has 1 post")
    void shouldAwardFirstPostBadge() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.countByUser(user)).thenReturn(1L);
        when(badgeRepository.findByName("First Post")).thenReturn(Optional.of(firstPostBadge));
        when(userBadgeRepository.existsByIdUserIdAndIdBadgeId(userId, 1)).thenReturn(false);

        // When
        badgeAwardingService.checkPostBadges(userId);

        // Then
        verify(badgeService).awardBadgeToUser(userId, 1);
    }

    @Test
    @DisplayName("Should award Prolific Poster badge when user has 10+ posts")
    void shouldAwardProlificPosterBadge() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.countByUser(user)).thenReturn(10L);
        when(badgeRepository.findByName("First Post")).thenReturn(Optional.of(firstPostBadge));
        when(badgeRepository.findByName("Prolific Poster")).thenReturn(Optional.of(prolificPosterBadge));
        when(userBadgeRepository.existsByIdUserIdAndIdBadgeId(userId, 1)).thenReturn(true);
        when(userBadgeRepository.existsByIdUserIdAndIdBadgeId(userId, 2)).thenReturn(false);

        // When
        badgeAwardingService.checkPostBadges(userId);

        // Then
        verify(badgeService).awardBadgeToUser(userId, 2);
    }

    @Test
    @DisplayName("Should not award badge if user already has it")
    void shouldNotAwardBadgeIfAlreadyHas() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.countByUser(user)).thenReturn(1L);
        when(badgeRepository.findByName("First Post")).thenReturn(Optional.of(firstPostBadge));
        when(userBadgeRepository.existsByIdUserIdAndIdBadgeId(userId, 1)).thenReturn(true);

        // When
        badgeAwardingService.checkPostBadges(userId);

        // Then
        verify(badgeService, never()).awardBadgeToUser(any(), anyInt());
    }

    @Test
    @DisplayName("Should award Commentator badge when user has 50+ comments")
    void shouldAwardCommentatorBadge() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentRepository.countByUser(user)).thenReturn(50L);
        when(badgeRepository.findByName("Commentator")).thenReturn(Optional.of(commentatorBadge));
        when(userBadgeRepository.existsByIdUserIdAndIdBadgeId(userId, 3)).thenReturn(false);

        // When
        badgeAwardingService.checkCommentBadges(userId);

        // Then
        verify(badgeService).awardBadgeToUser(userId, 3);
    }

    @Test
    @DisplayName("Should not award Commentator badge when user has less than 50 comments")
    void shouldNotAwardCommentatorBadgeWhenNotEnoughComments() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentRepository.countByUser(user)).thenReturn(49L);

        // When
        badgeAwardingService.checkCommentBadges(userId);

        // Then
        verify(badgeService, never()).awardBadgeToUser(any(), anyInt());
    }

    @Test
    @DisplayName("Should award Rising Star badge when user has 100+ karma")
    void shouldAwardRisingStarBadge() {
        // Given
        user.setKarma(100L);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(badgeRepository.findByName("Rising Star")).thenReturn(Optional.of(risingStarBadge));
        when(userBadgeRepository.existsByIdUserIdAndIdBadgeId(userId, 4)).thenReturn(false);

        // When
        badgeAwardingService.checkKarmaBadges(userId);

        // Then
        verify(badgeService).awardBadgeToUser(userId, 4);
    }

    @Test
    @DisplayName("Should award Popular badge when user has 1000+ karma")
    void shouldAwardPopularBadge() {
        // Given
        user.setKarma(1000L);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(badgeRepository.findByName("Rising Star")).thenReturn(Optional.of(risingStarBadge));
        when(badgeRepository.findByName("Popular")).thenReturn(Optional.of(popularBadge));
        when(userBadgeRepository.existsByIdUserIdAndIdBadgeId(userId, 4)).thenReturn(true);
        when(userBadgeRepository.existsByIdUserIdAndIdBadgeId(userId, 5)).thenReturn(false);

        // When
        badgeAwardingService.checkKarmaBadges(userId);

        // Then
        verify(badgeService).awardBadgeToUser(userId, 5);
    }

    @Test
    @DisplayName("Should award Veteran badge when account is 1+ year old")
    void shouldAwardVeteranBadge() {
        // Given
        user.setCreatedAt(Instant.now().minus(366, ChronoUnit.DAYS));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(badgeRepository.findByName("Veteran")).thenReturn(Optional.of(veteranBadge));
        when(userBadgeRepository.existsByIdUserIdAndIdBadgeId(userId, 6)).thenReturn(false);

        // When
        badgeAwardingService.checkAccountAgeBadges(userId);

        // Then
        verify(badgeService).awardBadgeToUser(userId, 6);
    }

    @Test
    @DisplayName("Should not award Veteran badge when account is less than 1 year old")
    void shouldNotAwardVeteranBadgeWhenAccountTooNew() {
        // Given
        user.setCreatedAt(Instant.now().minus(100, ChronoUnit.DAYS));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        badgeAwardingService.checkAccountAgeBadges(userId);

        // Then
        verify(badgeService, never()).awardBadgeToUser(any(), anyInt());
    }

    @Test
    @DisplayName("Should handle user not found gracefully")
    void shouldHandleUserNotFound() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        badgeAwardingService.checkPostBadges(userId);

        // Then
        verify(postRepository, never()).countByUser(any());
        verify(badgeService, never()).awardBadgeToUser(any(), anyInt());
    }

    @Test
    @DisplayName("Should handle badge not found gracefully")
    void shouldHandleBadgeNotFound() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.countByUser(user)).thenReturn(1L);
        when(badgeRepository.findByName("First Post")).thenReturn(Optional.empty());

        // When
        badgeAwardingService.checkPostBadges(userId);

        // Then
        verify(badgeService, never()).awardBadgeToUser(any(), anyInt());
    }

    @Test
    @DisplayName("Should check all badges")
    void shouldCheckAllBadges() {
        // Given
        user.setKarma(100L);
        user.setCreatedAt(Instant.now().minus(400, ChronoUnit.DAYS));
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.countByUser(user)).thenReturn(1L);
        when(commentRepository.countByUser(user)).thenReturn(50L);
        
        when(badgeRepository.findByName("First Post")).thenReturn(Optional.of(firstPostBadge));
        when(badgeRepository.findByName("Commentator")).thenReturn(Optional.of(commentatorBadge));
        when(badgeRepository.findByName("Rising Star")).thenReturn(Optional.of(risingStarBadge));
        when(badgeRepository.findByName("Veteran")).thenReturn(Optional.of(veteranBadge));
        
        when(userBadgeRepository.existsByIdUserIdAndIdBadgeId(any(), anyInt())).thenReturn(false);

        // When
        badgeAwardingService.checkAllBadges(userId);

        // Then
        verify(badgeService, times(4)).awardBadgeToUser(eq(userId), anyInt());
    }
}
