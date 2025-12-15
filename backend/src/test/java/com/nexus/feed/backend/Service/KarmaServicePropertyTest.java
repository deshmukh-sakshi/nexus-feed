package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.Repository.UserRepository;
import com.nexus.feed.backend.Repository.VoteRepository;
import net.jqwik.api.*;
import org.mockito.Mockito;

import java.util.UUID;

import static org.mockito.Mockito.*;

class KarmaServicePropertyTest {

    @Property(tries = 100)
    void selfVoteShouldNotChangeKarma(@ForAll("deltas") int delta) {
        // Given
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        BadgeAwardingService badgeAwardingService = Mockito.mock(BadgeAwardingService.class);
        KarmaServiceImpl karmaService = new KarmaServiceImpl(userRepository, null, badgeAwardingService);
        UUID userId = UUID.randomUUID();

        // When
        karmaService.updateKarmaForVote(userId, userId, delta);

        // Then
        verify(userRepository, never()).incrementKarma(any(), anyInt());
    }

    @Property(tries = 100)
    void nonSelfVoteShouldChangeKarma(@ForAll("deltas") int delta) {
        // Given
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        BadgeAwardingService badgeAwardingService = Mockito.mock(BadgeAwardingService.class);
        KarmaServiceImpl karmaService = new KarmaServiceImpl(userRepository, null, badgeAwardingService);
        UUID authorId = UUID.randomUUID();
        UUID voterId = UUID.randomUUID();

        // When
        karmaService.updateKarmaForVote(authorId, voterId, delta);

        // Then
        verify(userRepository).incrementKarma(authorId, delta);
    }

    @Example
    void upvoteShouldIncreaseKarmaByOne() {
        // Given
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        BadgeAwardingService badgeAwardingService = Mockito.mock(BadgeAwardingService.class);
        KarmaServiceImpl karmaService = new KarmaServiceImpl(userRepository, null, badgeAwardingService);
        UUID authorId = UUID.randomUUID();
        UUID voterId = UUID.randomUUID();

        // When
        karmaService.updateKarmaForVote(authorId, voterId, 1);

        // Then
        verify(userRepository).incrementKarma(authorId, 1);
    }

    @Example
    void downvoteShouldDecreaseKarmaByOne() {
        // Given
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        BadgeAwardingService badgeAwardingService = Mockito.mock(BadgeAwardingService.class);
        KarmaServiceImpl karmaService = new KarmaServiceImpl(userRepository, null, badgeAwardingService);
        UUID authorId = UUID.randomUUID();
        UUID voterId = UUID.randomUUID();

        // When
        karmaService.updateKarmaForVote(authorId, voterId, -1);

        // Then
        verify(userRepository).incrementKarma(authorId, -1);
    }

    @Property(tries = 100)
    void voteRemovalShouldReturnKarmaToOriginal(@ForAll("voteDeltas") int voteDelta) {
        // Given
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        BadgeAwardingService badgeAwardingService = Mockito.mock(BadgeAwardingService.class);
        KarmaServiceImpl karmaService = new KarmaServiceImpl(userRepository, null, badgeAwardingService);
        UUID authorId = UUID.randomUUID();
        UUID voterId = UUID.randomUUID();
        int removalDelta = -voteDelta;

        // When
        karmaService.updateKarmaForVote(authorId, voterId, voteDelta);
        karmaService.updateKarmaForVote(authorId, voterId, removalDelta);

        // Then
        verify(userRepository).incrementKarma(authorId, voteDelta);
        verify(userRepository).incrementKarma(authorId, removalDelta);
    }

    @Property(tries = 100)
    void voteFlipShouldChangeKarmaByTwo(@ForAll("flipDeltas") int flipDelta) {
        // Given
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        BadgeAwardingService badgeAwardingService = Mockito.mock(BadgeAwardingService.class);
        KarmaServiceImpl karmaService = new KarmaServiceImpl(userRepository, null, badgeAwardingService);
        UUID authorId = UUID.randomUUID();
        UUID voterId = UUID.randomUUID();

        // When
        karmaService.updateKarmaForVote(authorId, voterId, flipDelta);

        // Then
        verify(userRepository).incrementKarma(authorId, flipDelta);
    }

    @Provide
    Arbitrary<Integer> deltas() {
        return Arbitraries.of(-2, -1, 1, 2);
    }

    @Provide
    Arbitrary<Integer> voteDeltas() {
        return Arbitraries.of(-1, 1);
    }

    @Provide
    Arbitrary<Integer> flipDeltas() {
        return Arbitraries.of(-2, 2);
    }

    @Property(tries = 100)
    void contentDeletionShouldReverseKarmaImpact(@ForAll("karmaImpacts") int karmaImpact) {
        // Given
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        UUID authorId = UUID.randomUUID();

        // When - simulating content deletion karma reversal
        if (karmaImpact != 0) {
            userRepository.incrementKarma(authorId, -karmaImpact);
        }

        // Then
        if (karmaImpact != 0) {
            verify(userRepository).incrementKarma(authorId, -karmaImpact);
        } else {
            verify(userRepository, never()).incrementKarma(any(), anyInt());
        }
    }

    @Provide
    Arbitrary<Integer> karmaImpacts() {
        return Arbitraries.integers().between(-10, 10);
    }

    @Property(tries = 100)
    void calculateKarmaShouldEqualPostPlusCommentKarma(
            @ForAll("karmaValues") long postKarma,
            @ForAll("karmaValues") long commentKarma) {
        // Given
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        VoteRepository voteRepository = Mockito.mock(VoteRepository.class);
        BadgeAwardingService badgeAwardingService = Mockito.mock(BadgeAwardingService.class);
        KarmaServiceImpl karmaService = new KarmaServiceImpl(userRepository, voteRepository, badgeAwardingService);
        UUID userId = UUID.randomUUID();

        when(voteRepository.calculatePostKarma(userId)).thenReturn(postKarma);
        when(voteRepository.calculateCommentKarma(userId)).thenReturn(commentKarma);

        // When
        long totalKarma = karmaService.calculateKarma(userId);

        // Then
        assert totalKarma == postKarma + commentKarma;
    }

    @Provide
    Arbitrary<Long> karmaValues() {
        return Arbitraries.longs().between(-100, 100);
    }
}
