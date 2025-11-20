package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.DTO.*;
import com.nexus.feed.backend.Entity.*;
import com.nexus.feed.backend.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {
    
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final VoteRepository voteRepository;
    private final AuthenticationService authenticationService;

    @Override
    public CommentResponse createComment(UUID userId, UUID postId, CommentCreateRequest request) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = new Comment();
        comment.setBody(request.getBody());
        comment.setUser(user);
        comment.setPost(post);

        // Handle parent comment for replies
        if (request.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setParentComment(parentComment);
        }

        Comment savedComment = commentRepository.save(comment);
        return convertToResponse(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponse getCommentById(UUID id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        return convertToResponse(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPost(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        List<Comment> topLevelComments = commentRepository.findByPostAndParentCommentIsNullOrderByCreatedAtDesc(post);
        
        // Collect all comment IDs (including nested replies)
        List<UUID> allCommentIds = new java.util.ArrayList<>();
        collectCommentIds(topLevelComments, allCommentIds);
        
        if (allCommentIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        // Batch fetch vote counts
        List<VoteRepository.VoteCount> voteCounts = voteRepository.countByVotableIdsAndVotableType(
                allCommentIds, Vote.VotableType.COMMENT);
        
        java.util.Map<UUID, Integer> upvotesMap = new java.util.HashMap<>();
        java.util.Map<UUID, Integer> downvotesMap = new java.util.HashMap<>();
        
        for (VoteRepository.VoteCount vc : voteCounts) {
            if (vc.getVoteValue() == Vote.VoteValue.UPVOTE) {
                upvotesMap.put(vc.getVotableId(), vc.getCount().intValue());
            } else {
                downvotesMap.put(vc.getVotableId(), vc.getCount().intValue());
            }
        }
        
        // Batch fetch user votes
        java.util.Map<UUID, String> userVotesMap = new java.util.HashMap<>();
        try {
            UUID currentUserId = authenticationService.getCurrentUserId();
            List<Vote> userVotes = voteRepository.findByUserIdAndVotableIdsAndVotableType(
                    currentUserId, allCommentIds, Vote.VotableType.COMMENT);
            for (Vote vote : userVotes) {
                userVotesMap.put(vote.getId().getVotableId(), vote.getVoteValue().name());
            }
        } catch (RuntimeException e) {
            // User not authenticated, userVotesMap remains empty
        }
        
        return topLevelComments.stream()
                .map(comment -> convertToResponseWithRepliesBatch(comment, upvotesMap, downvotesMap, userVotesMap))
                .collect(Collectors.toList());
    }
    
    private void collectCommentIds(List<Comment> comments, List<UUID> allIds) {
        for (Comment comment : comments) {
            allIds.add(comment.getId());
            List<Comment> replies = commentRepository.findByParentCommentOrderByCreatedAtAsc(comment);
            if (!replies.isEmpty()) {
                collectCommentIds(replies, allIds);
            }
        }
    }
    
    private CommentResponse convertToResponseWithRepliesBatch(
            Comment comment,
            java.util.Map<UUID, Integer> upvotesMap,
            java.util.Map<UUID, Integer> downvotesMap,
            java.util.Map<UUID, String> userVotesMap) {
        
        CommentResponse response = CommentResponse.builder()
                .id(comment.getId())
                .body(comment.getBody())
                .createdAt(comment.getCreatedAt().toInstant(java.time.ZoneOffset.UTC))
                .updatedAt(comment.getUpdatedAt().toInstant(java.time.ZoneOffset.UTC))
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .postId(comment.getPost().getId())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .upvotes(upvotesMap.getOrDefault(comment.getId(), 0))
                .downvotes(downvotesMap.getOrDefault(comment.getId(), 0))
                .userVote(userVotesMap.get(comment.getId()))
                .build();
        
        // Load replies recursively
        List<Comment> replies = commentRepository.findByParentCommentOrderByCreatedAtAsc(comment);
        List<CommentResponse> replyResponses = replies.stream()
                .map(reply -> convertToResponseWithRepliesBatch(reply, upvotesMap, downvotesMap, userVotesMap))
                .collect(Collectors.toList());
        
        response.setReplies(replyResponses);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByUser(UUID userId, Pageable pageable) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return commentRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::convertToResponse);
    }

    @Override
    public CommentResponse updateComment(UUID commentId, UUID userId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to update this comment");
        }

        comment.setBody(request.getBody());

        Comment updatedComment = commentRepository.save(comment);
        return convertToResponse(updatedComment);
    }

    @Override
    public void deleteComment(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this comment");
        }

        commentRepository.delete(comment);
    }

    private CommentResponse convertToResponse(Comment comment) {
        long upvotes = voteRepository.countByVotableIdAndVotableTypeAndVoteValue(
                comment.getId(), Vote.VotableType.COMMENT, Vote.VoteValue.UPVOTE);
        long downvotes = voteRepository.countByVotableIdAndVotableTypeAndVoteValue(
                comment.getId(), Vote.VotableType.COMMENT, Vote.VoteValue.DOWNVOTE);

        String userVote = null;
        try {
            UUID currentUserId = authenticationService.getCurrentUserId();
            userVote = voteRepository.findByUserIdAndVotableIdAndVotableType(
                    currentUserId, comment.getId(), Vote.VotableType.COMMENT)
                    .map(vote -> vote.getVoteValue().name())
                    .orElse(null);
        } catch (RuntimeException e) {
            // User not authenticated, userVote remains null
        }

        return CommentResponse.builder()
                .id(comment.getId())
                .body(comment.getBody())
                .createdAt(comment.getCreatedAt().toInstant(java.time.ZoneOffset.UTC))
                .updatedAt(comment.getUpdatedAt().toInstant(java.time.ZoneOffset.UTC))
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .postId(comment.getPost().getId())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .upvotes((int) upvotes)
                .downvotes((int) downvotes)
                .userVote(userVote)
                .build();
    }

    private CommentResponse convertToResponseWithReplies(Comment comment) {
        CommentResponse response = convertToResponse(comment);
        
        // Load replies recursively (limit depth to avoid infinite recursion)
        List<Comment> replies = commentRepository.findByParentCommentOrderByCreatedAtAsc(comment);
        List<CommentResponse> replyResponses = replies.stream()
                .map(this::convertToResponseWithReplies) // Recursive call to load nested replies
                .collect(Collectors.toList());
        
        response.setReplies(replyResponses);
        return response;
    }
}