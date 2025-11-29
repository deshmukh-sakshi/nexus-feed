package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.Repository.UserRepository;
import com.nexus.feed.backend.Repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class KarmaServiceImpl implements KarmaService {

    private final UserRepository userRepository;
    private final VoteRepository voteRepository;

    @Override
    public void updateKarmaForVote(UUID contentAuthorId, UUID voterId, int delta) {
        // Self-votes should not affect karma
        if (contentAuthorId.equals(voterId)) {
            log.debug("Self-vote detected, skipping karma update for user: {}", contentAuthorId);
            return;
        }

        userRepository.incrementKarma(contentAuthorId, delta);
        log.debug("Updated karma for user {} by {}", contentAuthorId, delta);
    }

    @Override
    @Transactional(readOnly = true)
    public long calculateKarma(UUID userId) {
        long postKarma = voteRepository.calculatePostKarma(userId);
        long commentKarma = voteRepository.calculateCommentKarma(userId);
        return postKarma + commentKarma;
    }

    @Override
    public void recalculateKarma(UUID userId) {
        long calculatedKarma = calculateKarma(userId);
        // Reset karma to 0 then increment by calculated value
        userRepository.findById(userId).ifPresent(user -> {
            long currentKarma = user.getKarma();
            int delta = (int) (calculatedKarma - currentKarma);
            if (delta != 0) {
                userRepository.incrementKarma(userId, delta);
                log.info("Recalculated karma for user {}: {} -> {}", userId, currentKarma, calculatedKarma);
            }
        });
    }
}
