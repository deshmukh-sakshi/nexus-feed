package com.nexus.feed.backend.Service;

import java.util.UUID;

public interface KarmaService {
    void updateKarmaForVote(UUID contentAuthorId, UUID voterId, int delta);
    long calculateKarma(UUID userId);
    void recalculateKarma(UUID userId);
}
