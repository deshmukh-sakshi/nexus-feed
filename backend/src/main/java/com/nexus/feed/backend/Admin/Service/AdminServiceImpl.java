package com.nexus.feed.backend.Admin.Service;

import com.nexus.feed.backend.Admin.DTO.*;
import com.nexus.feed.backend.Auth.Entity.AppUser;
import com.nexus.feed.backend.Auth.Entity.Role;
import com.nexus.feed.backend.Auth.Repository.AppUserRepository;
import com.nexus.feed.backend.Entity.Comment;
import com.nexus.feed.backend.Entity.Post;
import com.nexus.feed.backend.Entity.Users;
import com.nexus.feed.backend.Exception.ResourceNotFoundException;
import com.nexus.feed.backend.Entity.Vote;
import com.nexus.feed.backend.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final AppUserRepository appUserRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final VoteRepository voteRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        long totalUsers = userRepository.count();
        long totalPosts = postRepository.count();
        long totalComments = commentRepository.count();
        long totalVotes = voteRepository.count();
        
        // For simplicity, we'll return 0 for daily stats - can be enhanced with proper queries
        return new AdminStatsResponse(totalUsers, totalPosts, totalComments, totalVotes, 0, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toAdminUserResponse);
    }

    @Override
    @Transactional
    public AdminUserResponse updateUserRole(UUID userId, String role) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        AppUser appUser = user.getAppUser();
        appUser.setRole(Role.valueOf(role.toUpperCase()));
        appUserRepository.save(appUser);
        
        return toAdminUserResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminPostResponse> getAllPosts(Pageable pageable) {
        return postRepository.findAllOrderByCreatedAtDesc(pageable).map(this::toAdminPostResponse);
    }

    @Override
    @Transactional
    public void deletePost(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        postRepository.delete(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminCommentResponse> getAllComments(Pageable pageable) {
        return commentRepository.findAll(pageable).map(this::toAdminCommentResponse);
    }

    @Override
    @Transactional
    public void deleteComment(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        commentRepository.delete(comment);
    }

    private AdminUserResponse toAdminUserResponse(Users user) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getAppUser().getEmail(),
                user.getAppUser().getRole().name(),
                user.getKarma(),
                user.getProfilePictureUrl(),
                user.getCreatedAt(),
                user.getPosts().size(),
                user.getComments().size()
        );
    }

    private AdminPostResponse toAdminPostResponse(Post post) {
        long upvotes = voteRepository.countByVotableIdAndVotableTypeAndVoteValue(
                post.getId(), Vote.VotableType.POST, Vote.VoteValue.UPVOTE);
        long downvotes = voteRepository.countByVotableIdAndVotableTypeAndVoteValue(
                post.getId(), Vote.VotableType.POST, Vote.VoteValue.DOWNVOTE);
        
        return new AdminPostResponse(
                post.getId(),
                post.getTitle(),
                post.getUrl(),
                post.getBody(),
                post.getUser().getId(),
                post.getUser().getUsername(),
                post.getImages().stream().map(img -> img.getImageUrl()).toList(),
                post.getTags().stream().map(tag -> tag.getName()).toList(),
                (int) upvotes,
                (int) downvotes,
                post.getComments().size(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    private AdminCommentResponse toAdminCommentResponse(Comment comment) {
        long upvotes = voteRepository.countByVotableIdAndVotableTypeAndVoteValue(
                comment.getId(), Vote.VotableType.COMMENT, Vote.VoteValue.UPVOTE);
        long downvotes = voteRepository.countByVotableIdAndVotableTypeAndVoteValue(
                comment.getId(), Vote.VotableType.COMMENT, Vote.VoteValue.DOWNVOTE);
        
        return new AdminCommentResponse(
                comment.getId(),
                comment.getBody(),
                comment.getUser().getId(),
                comment.getUser().getUsername(),
                comment.getPost().getId(),
                comment.getPost().getTitle(),
                (int) upvotes,
                (int) downvotes,
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
