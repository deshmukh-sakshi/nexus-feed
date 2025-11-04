package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.DTO.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface CommentService {
    CommentResponse createComment(UUID userId, UUID postId, CommentCreateRequest request);
    CommentResponse getCommentById(UUID id);
    List<CommentResponse> getCommentsByPost(UUID postId);
    Page<CommentResponse> getCommentsByUser(UUID userId, Pageable pageable);
    CommentResponse updateComment(UUID commentId, UUID userId, CommentUpdateRequest request);
    void deleteComment(UUID commentId, UUID userId);
}