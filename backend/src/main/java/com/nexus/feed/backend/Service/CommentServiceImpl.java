package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.DTO.*;
import com.nexus.feed.backend.Entity.*;
import com.nexus.feed.backend.Exception.ResourceNotFoundException;
import com.nexus.feed.backend.Exception.UnauthorizedException;
import com.nexus.feed.backend.Repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {
    
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final VoteRepository voteRepository;
    private final AuthenticationService authenticationService;
    private final KarmaService karmaService;
    private final BadgeAwardingService badgeAwardingService;

    @Override
    public CommentResponse createComment(UUID userId, UUID postId, CommentCreateRequest request) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        Comment comment = new Comment();
        comment.setBody(request.getBody());
        comment.setUser(user);
        comment.setPost(post);

        // Handle parent comment for replies
        if (request.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", request.getParentCommentId()));
            comment.setParentComment(parentComment);
        }

        Comment savedComment = commentRepository.save(comment);
        log.info("Comment created: id={}, postId={}, userId={}", savedComment.getId(), postId, userId);

        // Check for comment-related badges
        badgeAwardingService.checkCommentBadges(userId);

        return convertToResponse(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponse getCommentById(UUID id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));
        return convertToResponse(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPost(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        
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
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .userProfilePictureUrl(comment.getUser().getProfilePictureUrl())
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
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return commentRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::convertToResponse);
    }

    @Override
    public CommentResponse updateComment(UUID commentId, UUID userId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        if (!comment.getUser().getId().equals(userId)) {
            log.warn("Unauthorized update attempt: userId={}, commentId={}, ownerId={}", userId, commentId, comment.getUser().getId());
            throw new UnauthorizedException("Not authorized to update this comment");
        }

        comment.setBody(request.getBody());

        Comment updatedComment = commentRepository.save(comment);
        log.info("Comment updated: id={}, userId={}", commentId, userId);
        return convertToResponse(updatedComment);
    }

    @Override
    public void deleteComment(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        if (!comment.getUser().getId().equals(userId)) {
            log.warn("Unauthorized delete attempt: userId={}, commentId={}, ownerId={}", userId, commentId, comment.getUser().getId());
            throw new UnauthorizedException("Not authorized to delete this comment");
        }

        UUID authorId = comment.getUser().getId();

        commentRepository.delete(comment);
        karmaService.recalculateKarma(authorId);
        log.info("Comment deleted: id={}, userId={}", commentId, userId);
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
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .userProfilePictureUrl(comment.getUser().getProfilePictureUrl())
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