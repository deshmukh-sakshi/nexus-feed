package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.Entity.Users;
import com.nexus.feed.backend.Repository.UserRepository;
import com.nexus.feed.backend.Repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KarmaServiceImpl Unit Tests")
class KarmaServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private BadgeAwardingService badgeAwardingService;

    @InjectMocks
    private KarmaServiceImpl karmaService;

    private UUID contentAuthorId;
    private UUID voterId;
    private Users author;

    @BeforeEach
    void setUp() {
        contentAuthorId = UUID.randomUUID();
        voterId = UUID.randomUUID();

        author = new Users();
        author.setId(contentAuthorId);
        author.setUsername("tester");
        author.setKarma(100L);
    }

    @Test
    @DisplayName("Should update karma for upvote")
    void shouldUpdateKarmaForUpvote() {
        // When
        karmaService.updateKarmaForVote(contentAuthorId, voterId, 1);

        // Then
        verify(userRepository).incrementKarma(contentAuthorId, 1);
        verify(badgeAwardingService).checkKarmaBadges(contentAuthorId);
    }

    @Test
    @DisplayName("Should update karma for downvote")
    void shouldUpdateKarmaForDownvote() {
        // When
        karmaService.updateKarmaForVote(contentAuthorId, voterId, -1);

        // Then
        verify(userRepository).incrementKarma(contentAuthorId, -1);
        verify(badgeAwardingService, never()).checkKarmaBadges(any());
    }

    @Test
    @DisplayName("Should not update karma for self-vote")
    void shouldNotUpdateKarmaForSelfVote() {
        // When
        karmaService.updateKarmaForVote(contentAuthorId, contentAuthorId, 1);

        // Then
        verify(userRepository, never()).incrementKarma(any(), anyInt());
        verify(badgeAwardingService, never()).checkKarmaBadges(any());
    }

    @Test
    @DisplayName("Should calculate karma from post and comment votes")
    void shouldCalculateKarmaFromVotes() {
        // Given
        when(voteRepository.calculatePostKarma(contentAuthorId)).thenReturn(50L);
        when(voteRepository.calculateCommentKarma(contentAuthorId)).thenReturn(30L);

        // When
        long karma = karmaService.calculateKarma(contentAuthorId);

        // Then
        assertThat(karma).isEqualTo(80L);
    }

    @Test
    @DisplayName("Should recalculate karma when different from current")
    void shouldRecalculateKarmaWhenDifferent() {
        // Given
        when(voteRepository.calculatePostKarma(contentAuthorId)).thenReturn(150L);
        when(voteRepository.calculateCommentKarma(contentAuthorId)).thenReturn(50L);
        when(userRepository.findById(contentAuthorId)).thenReturn(Optional.of(author));

        // When
        karmaService.recalculateKarma(contentAuthorId);

        // Then
        verify(userRepository).incrementKarma(contentAuthorId, 100); // 200 - 100 = 100
    }

    @Test
    @DisplayName("Should not update karma when already correct")
    void shouldNotUpdateKarmaWhenCorrect() {
        // Given
        when(voteRepository.calculatePostKarma(contentAuthorId)).thenReturn(70L);
        when(voteRepository.calculateCommentKarma(contentAuthorId)).thenReturn(30L);
        when(userRepository.findById(contentAuthorId)).thenReturn(Optional.of(author));

        // When
        karmaService.recalculateKarma(contentAuthorId);

        // Then
        verify(userRepository, never()).incrementKarma(any(), anyInt());
    }

    @Test
    @DisplayName("Should handle user not found during recalculation")
    void shouldHandleUserNotFoundDuringRecalculation() {
        // Given
        when(voteRepository.calculatePostKarma(contentAuthorId)).thenReturn(50L);
        when(voteRepository.calculateCommentKarma(contentAuthorId)).thenReturn(30L);
        when(userRepository.findById(contentAuthorId)).thenReturn(Optional.empty());

        // When
        karmaService.recalculateKarma(contentAuthorId);

        // Then
        verify(userRepository, never()).incrementKarma(any(), anyInt());
    }

    @Test
    @DisplayName("Should check karma badges on positive delta")
    void shouldCheckKarmaBadgesOnPositiveDelta() {
        // When
        karmaService.updateKarmaForVote(contentAuthorId, voterId, 2);

        // Then
        verify(badgeAwardingService).checkKarmaBadges(contentAuthorId);
    }
}
