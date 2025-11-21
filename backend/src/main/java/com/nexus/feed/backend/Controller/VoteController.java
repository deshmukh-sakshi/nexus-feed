package com.nexus.feed.backend.Controller;

import com.nexus.feed.backend.DTO.VoteRequest;
import com.nexus.feed.backend.Entity.Vote;
import com.nexus.feed.backend.Service.AuthenticationService;
import com.nexus.feed.backend.Service.VoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;
    private final AuthenticationService authenticationService;

    @PostMapping
    public ResponseEntity<Void> vote(@Valid @RequestBody VoteRequest request) {
        try {
            UUID userId = authenticationService.getCurrentUserId();
            voteService.vote(userId, request);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{votableId}")
    public ResponseEntity<Void> removeVote(
            @PathVariable UUID votableId,
            @RequestParam Vote.VotableType votableType) {
        try {
            UUID userId = authenticationService.getCurrentUserId();
            voteService.removeVote(userId, votableId, votableType);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{votableId}/counts")
    public ResponseEntity<Map<String, Object>> getVoteCounts(
            @PathVariable UUID votableId,
            @RequestParam Vote.VotableType votableType) {
        try {
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
            } catch (RuntimeException e) {
                // User not authenticated, just return counts without user vote
                response.put("userVote", null);
            }
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}