package com.nexus.feed.backend.Controller;

import com.nexus.feed.backend.DTO.*;
import com.nexus.feed.backend.Service.AuthenticationService;
import com.nexus.feed.backend.Service.CommentService;
import com.nexus.feed.backend.Service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final AuthenticationService authenticationService;
    private final ReportService reportService;

    @PostMapping("/post/{postId}")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable UUID postId,
            @Valid @RequestBody CommentCreateRequest request) {
        UUID userId = authenticationService.getCurrentUserId();
        log.debug("Creating comment on post: {} by user: {}", postId, userId);
        CommentResponse comment = commentService.createComment(userId, postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable UUID id) {
        CommentResponse comment = commentService.getCommentById(id);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByPost(@PathVariable UUID postId) {
        List<CommentResponse> comments = commentService.getCommentsByPost(postId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<CommentResponse>> getCommentsByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentResponse> comments = commentService.getCommentsByUser(userId, pageable);
        return ResponseEntity.ok(comments);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable UUID id,
            @Valid @RequestBody CommentUpdateRequest request) {
        UUID userId = authenticationService.getCurrentUserId();
        log.debug("Updating comment: {} by user: {}", id, userId);
        CommentResponse comment = commentService.updateComment(id, userId, request);
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID id) {
        UUID userId = authenticationService.getCurrentUserId();
        log.debug("Deleting comment: {} by user: {}", id, userId);
        commentService.deleteComment(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/report")
    public ResponseEntity<ApiResponse<Void>> reportComment(
            @PathVariable UUID id,
            @Valid @RequestBody ReportRequest request) {
        UUID userId = authenticationService.getCurrentUserId();
        log.debug("User {} reporting comment {} with reason {}", userId, id, request.reason());
        reportService.createCommentReport(id, userId, request.reason(), request.additionalDetails());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Report submitted successfully", null));
    }

    @GetMapping("/{id}/report/status")
    public ResponseEntity<ReportStatusResponse> getReportStatus(@PathVariable UUID id) {
        UUID userId = authenticationService.getCurrentUserId();
        boolean hasReported = reportService.hasUserReportedComment(id, userId);
        return ResponseEntity.ok(new ReportStatusResponse(hasReported));
    }
}