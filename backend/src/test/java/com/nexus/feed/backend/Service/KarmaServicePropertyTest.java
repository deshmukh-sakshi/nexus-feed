package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.Repository.UserRepository;
import net.jqwik.api.*;
import org.mockito.Mockito;

import java.util.UUID;

import static org.mockito.Mockito.*;

class KarmaServicePropertyTest {

    @Property(tries = 100)
    void selfVoteShouldNotChangeKarma(@ForAll("deltas") int delta) {
        // Given
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        KarmaServiceImpl karmaService = new KarmaServiceImpl(userRepository, null);
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
        KarmaServiceImpl karmaService = new KarmaServiceImpl(userRepository, null);
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
        KarmaServiceImpl karmaService = new KarmaServiceImpl(userRepository, null);
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
        KarmaServiceImpl karmaService = new KarmaServiceImpl(userRepository, null);
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
        KarmaServiceImpl karmaService = new KarmaServiceImpl(userRepository, null);
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
        KarmaServiceImpl karmaService = new KarmaServiceImpl(userRepository, null);
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
}
