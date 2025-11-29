package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.Repository.UserRepository;
import net.jqwik.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KarmaService Property Tests")
class KarmaServicePropertyTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private KarmaServiceImpl karmaService;

    @Property(tries = 100)
    @DisplayName("Self-vote should not change karma")
    void selfVoteShouldNotChangeKarma(@ForAll("deltas") int delta) {
        // Given
        UUID userId = UUID.randomUUID();

        // When
        karmaService.updateKarmaForVote(userId, userId, delta);

        // Then
        verify(userRepository, never()).incrementKarma(any(), anyInt());
    }

    @Property(tries = 100)
    @DisplayName("Non-self vote should change karma")
    void nonSelfVoteShouldChangeKarma(@ForAll("deltas") int delta) {
        // Given
        UUID authorId = UUID.randomUUID();
        UUID voterId = UUID.randomUUID();

        // When
        karmaService.updateKarmaForVote(authorId, voterId, delta);

        // Then
        verify(userRepository).incrementKarma(authorId, delta);
    }

    @Provide
    Arbitrary<Integer> deltas() {
        return Arbitraries.of(-2, -1, 1, 2);
    }
}
