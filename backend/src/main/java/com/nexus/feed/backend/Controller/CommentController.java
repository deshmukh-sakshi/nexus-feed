package com.nexus.feed.backend.Controller;

import com.nexus.feed.backend.DTO.*;
import com.nexus.feed.backend.Service.AuthenticationService;
import com.nexus.feed.backend.Service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final AuthenticationService authenticationService;

    @PostMapping("/post/{postId}")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable UUID postId,
            @Valid @RequestBody CommentCreateRequest request) {
        try {
            UUID userId = authenticationService.getCurrentUserId();
            CommentResponse comment = commentService.createComment(userId, postId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(comment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable UUID id) {
        try {
            CommentResponse comment = commentService.getCommentById(id);
            return ResponseEntity.ok(comment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByPost(@PathVariable UUID postId) {
        try {
            List<CommentResponse> comments = commentService.getCommentsByPost(postId);
            return ResponseEntity.ok(comments);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<CommentResponse>> getCommentsByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CommentResponse> comments = commentService.getCommentsByUser(userId, pageable);
            return ResponseEntity.ok(comments);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable UUID id,
            @Valid @RequestBody CommentUpdateRequest request) {
        try {
            UUID userId = authenticationService.getCurrentUserId();
            CommentResponse comment = commentService.updateComment(id, userId, request);
            return ResponseEntity.ok(comment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID id) {
        try {
            UUID userId = authenticationService.getCurrentUserId();
            commentService.deleteComment(id, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}