package com.nexus.feed.backend.Admin.Service;

import com.nexus.feed.backend.Admin.DTO.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface AdminService {
    AdminStatsResponse getStats();
    Page<AdminUserResponse> getAllUsers(Pageable pageable);
    AdminUserResponse updateUserRole(UUID userId, String role);
    void deleteUser(UUID userId);
    Page<AdminPostResponse> getAllPosts(Pageable pageable);
    void deletePost(UUID postId);
    Page<AdminCommentResponse> getAllComments(Pageable pageable);
    void deleteComment(UUID commentId);
}
