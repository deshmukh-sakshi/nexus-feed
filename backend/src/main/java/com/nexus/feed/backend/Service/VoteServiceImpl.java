package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.DTO.VoteRequest;
import com.nexus.feed.backend.Entity.*;
import com.nexus.feed.backend.Exception.ResourceNotFoundException;
import com.nexus.feed.backend.Repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VoteServiceImpl implements VoteService {
    
    private final VoteRepository voteRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Override
    public void vote(UUID userId, VoteRequest request) {
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        // Verify votable entity exists
        if (request.getVotableType() == Vote.VotableType.POST) {
            if (!postRepository.existsById(request.getVotableId())) {
                throw new ResourceNotFoundException("Post", "id", request.getVotableId());
            }
        } else if (request.getVotableType() == Vote.VotableType.COMMENT) {
            if (!commentRepository.existsById(request.getVotableId())) {
                throw new ResourceNotFoundException("Comment", "id", request.getVotableId());
            }
        }

        Vote.VoteId voteId = new Vote.VoteId(userId, request.getVotableId());
        Optional<Vote> existingVote = voteRepository.findById(voteId);

        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            if (vote.getVoteValue() == request.getVoteValue()) {
                // Same vote - remove it (toggle off)
                voteRepository.delete(vote);
                log.info("Vote removed: userId={}, votableId={}, type={}", userId, request.getVotableId(), request.getVotableType());
            } else {
                // Different vote - update it
                vote.setVoteValue(request.getVoteValue());
                voteRepository.save(vote);
                log.info("Vote updated: userId={}, votableId={}, type={}, value={}", userId, request.getVotableId(), request.getVotableType(), request.getVoteValue());
            }
        } else {
            // New vote
            Vote newVote = new Vote();
            newVote.setId(voteId);
            newVote.setVotableType(request.getVotableType());
            newVote.setVoteValue(request.getVoteValue());
            voteRepository.save(newVote);
            log.info("Vote created: userId={}, votableId={}, type={}, value={}", userId, request.getVotableId(), request.getVotableType(), request.getVoteValue());
        }
    }

    @Override
    public void removeVote(UUID userId, UUID votableId, Vote.VotableType votableType) {
        Vote.VoteId voteId = new Vote.VoteId(userId, votableId);
        voteRepository.deleteById(voteId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUpvoteCount(UUID votableId, Vote.VotableType votableType) {
        return voteRepository.countByVotableIdAndVotableTypeAndVoteValue(
                votableId, votableType, Vote.VoteValue.UPVOTE);
    }

    @Override
    @Transactional(readOnly = true)
    public long getDownvoteCount(UUID votableId, Vote.VotableType votableType) {
        return voteRepository.countByVotableIdAndVotableTypeAndVoteValue(
                votableId, votableType, Vote.VoteValue.DOWNVOTE);
    }

    @Override
    @Transactional(readOnly = true)
    public Vote.VoteValue getUserVote(UUID userId, UUID votableId, Vote.VotableType votableType) {
        return voteRepository.findByUserIdAndVotableIdAndVotableType(userId, votableId, votableType)
                .map(Vote::getVoteValue)
                .orElse(null);
    }
}