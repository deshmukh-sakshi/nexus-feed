package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.DTO.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface PostService {
    PostResponse createPost(UUID userId, PostCreateRequest request);
    PostResponse getPostById(UUID id);
    PostDetailResponse getPostWithComments(UUID id);
    Page<PostResponse> getAllPosts(Pageable pageable, String sort);
    Page<PostResponse> getPostsByUser(UUID userId, Pageable pageable);
    Page<PostResponse> searchPosts(String keyword, Pageable pageable);
    Page<PostResponse> searchByTag(String tagName, Pageable pageable);
    Page<PostResponse> searchByTags(java.util.List<String> tagNames, Pageable pageable);
    PostResponse updatePost(UUID postId, UUID userId, PostUpdateRequest request);
    void deletePost(UUID postId, UUID userId);
}