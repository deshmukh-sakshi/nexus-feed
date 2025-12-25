package com.nexus.feed.backend.Controller;

import com.nexus.feed.backend.DTO.VoteRequest;
import com.nexus.feed.backend.Entity.Vote;
import com.nexus.feed.backend.Exception.UnauthorizedException;
import com.nexus.feed.backend.Service.AuthenticationService;
import com.nexus.feed.backend.Service.VoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;
    private final AuthenticationService authenticationService;

    @PostMapping
    public ResponseEntity<Void> vote(@Valid @RequestBody VoteRequest request) {
        UUID userId = authenticationService.getCurrentUserId();
        log.debug("Vote request: userId={}, votableId={}, type={}, value={}", 
                userId, request.getVotableId(), request.getVotableType(), request.getVoteValue());
        voteService.vote(userId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{votableId}")
    public ResponseEntity<Void> removeVote(
            @PathVariable UUID votableId,
            @RequestParam Vote.VotableType votableType) {
        UUID userId = authenticationService.getCurrentUserId();
        log.debug("Removing vote: userId={}, votableId={}, type={}", userId, votableId, votableType);
        voteService.removeVote(userId, votableId, votableType);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{votableId}/counts")
    public ResponseEntity<Map<String, Object>> getVoteCounts(
            @PathVariable UUID votableId,
            @RequestParam Vote.VotableType votableType) {
        long upvotes = voteService.getUpvoteCount(votableId, votableType);
        long downvotes = voteService.getDownvoteCount(votableId, votableType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("upvotes", upvotes);
        response.put("downvotes", downvotes);
        
        // Try to get current user vote if authenticated
        try {
            UUID userId = authenticationService.getCurrentUserId();
            Vote.VoteValue userVote = voteService.getUserVote(userId, votableId, votableType);
            response.put("userVote", userVote != null ? userVote.name() : null);
        } catch (UnauthorizedException e) {
            response.put("userVote", null);
        }
        
        return ResponseEntity.ok(response);
    }
}