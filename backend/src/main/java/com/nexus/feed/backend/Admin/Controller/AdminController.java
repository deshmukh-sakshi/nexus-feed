package com.nexus.feed.backend.Admin.Controller;

import com.nexus.feed.backend.Admin.DTO.*;
import com.nexus.feed.backend.Admin.Service.AdminService;
import com.nexus.feed.backend.Entity.ReportReason;
import com.nexus.feed.backend.Service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final ReportService reportService;

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        log.debug("Admin fetching stats");
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.getAllUsers(
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<AdminUserResponse> updateUserRole(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        log.info("Admin updating user role: userId={}, newRole={}", userId, request.role());
        return ResponseEntity.ok(adminService.updateUserRole(userId, request.role()));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        log.info("Admin deleting user: {}", userId);
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/posts")
    public ResponseEntity<Page<AdminPostResponse>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        log.debug("Admin fetching posts: page={}, size={}, sortBy={}, sortDirection={}", page, size, sortBy, sortDirection);
        PageRequest pageRequest = PageRequest.of(page, size);
        
        Page<AdminPostResponse> posts;
        if (sortBy != null && !sortBy.isEmpty()) {
            posts = adminService.getAllPosts(pageRequest, sortBy, sortDirection);
        } else {
            posts = adminService.getAllPosts(
                    PageRequest.of(page, size, Sort.by("createdAt").descending()));
        }
        
        return ResponseEntity.ok(posts);
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID postId) {
        log.info("Admin deleting post: {}", postId);
        adminService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/comments")
    public ResponseEntity<Page<AdminCommentResponse>> getAllComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.getAllComments(
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId) {
        log.info("Admin deleting comment: {}", commentId);
        adminService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reports")
    public ResponseEntity<Page<AdminReportResponse>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ReportReason reason) {
        log.debug("Admin fetching reports: page={}, size={}, reason={}", page, size, reason);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<AdminReportResponse> reports;
        if (reason != null) {
            reports = reportService.getReportsByReason(reason, pageRequest);
        } else {
            reports = reportService.getAllReports(pageRequest);
        }
        
        return ResponseEntity.ok(reports);
    }
}
