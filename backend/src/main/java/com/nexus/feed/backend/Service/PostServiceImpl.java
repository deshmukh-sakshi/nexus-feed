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
public class PostServiceImpl implements PostService {
    
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostImageRepository postImageRepository;
    private final VoteRepository voteRepository;
    private final CommentRepository commentRepository;
    private final AuthenticationService authenticationService;
    private final CommentService commentService;

    @Override
    public PostResponse createPost(UUID userId, PostCreateRequest request) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setUrl(request.getUrl());
        post.setBody(request.getBody());
        post.setUser(user);

        Post savedPost = postRepository.save(post);
        log.info("Post created: id={}, userId={}", savedPost.getId(), userId);

        // Handle images
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<PostImage> images = request.getImageUrls().stream()
                    .map(url -> {
                        PostImage image = new PostImage();
                        image.setPost(savedPost);
                        image.setImageUrl(url);
                        return image;
                    })
                    .collect(Collectors.toList());
            postImageRepository.saveAll(images);
            savedPost.setImages(images);
        }

        return convertToResponse(savedPost);
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPostById(UUID id) {
        Post post = postRepository.findByIdWithUserAndImages(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        return convertToResponse(post);
    }

    @Override
    @Transactional(readOnly = true)
    public PostDetailResponse getPostWithComments(UUID id) {
        PostResponse post = getPostById(id);
        List<CommentResponse> comments = commentService.getCommentsByPost(id);
        
        return PostDetailResponse.builder()
                .post(post)
                .comments(comments)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findAllOrderByCreatedAtDesc(pageable);
        return convertToResponseBatch(posts);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByUser(UUID userId, Pageable pageable) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Page<Post> posts = postRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return convertToResponseBatch(posts);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> searchPosts(String keyword, Pageable pageable) {
        Page<Post> posts = postRepository.findByTitleContainingOrBodyContainingOrderByCreatedAtDesc(keyword, pageable);
        return convertToResponseBatch(posts);
    }

    @Override
    public PostResponse updatePost(UUID postId, UUID userId, PostUpdateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        if (!post.getUser().getId().equals(userId)) {
            log.warn("Unauthorized update attempt: userId={}, postId={}, ownerId={}", userId, postId, post.getUser().getId());
            throw new UnauthorizedException("Not authorized to update this post");
        }

        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }
        if (request.getUrl() != null) {
            post.setUrl(request.getUrl());
        }
        if (request.getBody() != null) {
            post.setBody(request.getBody());
        }

        // Handle image updates
        if (request.getImageUrls() != null) {
            postImageRepository.deleteByPost(post);
            if (!request.getImageUrls().isEmpty()) {
                List<PostImage> images = request.getImageUrls().stream()
                        .map(url -> {
                            PostImage image = new PostImage();
                            image.setPost(post);
                            image.setImageUrl(url);
                            return image;
                        })
                        .collect(Collectors.toList());
                postImageRepository.saveAll(images);
            }
        }

        Post updatedPost = postRepository.save(post);
        log.info("Post updated: id={}, userId={}", postId, userId);
        return convertToResponse(updatedPost);
    }

    @Override
    public void deletePost(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        if (!post.getUser().getId().equals(userId)) {
            log.warn("Unauthorized delete attempt: userId={}, postId={}, ownerId={}", userId, postId, post.getUser().getId());
            throw new UnauthorizedException("Not authorized to delete this post");
        }

        postRepository.delete(post);
        log.info("Post deleted: id={}, userId={}", postId, userId);
    }

    private PostResponse convertToResponse(Post post) {
        List<String> imageUrls = post.getImages().stream()
                .map(PostImage::getImageUrl)
                .collect(Collectors.toList());

        long upvotes = voteRepository.countByVotableIdAndVotableTypeAndVoteValue(
                post.getId(), Vote.VotableType.POST, Vote.VoteValue.UPVOTE);
        long downvotes = voteRepository.countByVotableIdAndVotableTypeAndVoteValue(
                post.getId(), Vote.VotableType.POST, Vote.VoteValue.DOWNVOTE);

        String userVote = null;
        try {
            UUID currentUserId = authenticationService.getCurrentUserId();
            userVote = voteRepository.findByUserIdAndVotableIdAndVotableType(
                    currentUserId, post.getId(), Vote.VotableType.POST)
                    .map(vote -> vote.getVoteValue().name())
                    .orElse(null);
        } catch (RuntimeException e) {
            // User not authenticated, userVote remains null
        }

        long commentCount = commentRepository.countByPost(post);

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .url(post.getUrl())
                .body(post.getBody())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .userId(post.getUser().getId())
                .username(post.getUser().getUsername())
                .imageUrls(imageUrls)
                .commentCount((int) commentCount)
                .upvotes((int) upvotes)
                .downvotes((int) downvotes)
                .userVote(userVote)
                .build();
    }
    
    private Page<PostResponse> convertToResponseBatch(Page<Post> posts) {
        if (posts.isEmpty()) {
            return posts.map(this::convertToResponse);
        }
        
        List<UUID> postIds = posts.getContent().stream()
                .map(Post::getId)
                .collect(Collectors.toList());
        
        // Batch fetch vote counts
        List<VoteRepository.VoteCount> voteCounts = voteRepository.countByVotableIdsAndVotableType(
                postIds, Vote.VotableType.POST);
        
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
                    currentUserId, postIds, Vote.VotableType.POST);
            for (Vote vote : userVotes) {
                userVotesMap.put(vote.getId().getVotableId(), vote.getVoteValue().name());
            }
        } catch (RuntimeException e) {
            // User not authenticated, userVotesMap remains empty
        }
        
        // Batch fetch comment counts
        List<CommentRepository.CommentCount> commentCounts = commentRepository.countByPostIds(postIds);
        java.util.Map<UUID, Integer> commentCountMap = commentCounts.stream()
                .collect(Collectors.toMap(
                        CommentRepository.CommentCount::getPostId,
                        cc -> cc.getCount().intValue()
                ));
        
        return posts.map(post -> {
            List<String> imageUrls = post.getImages().stream()
                    .map(PostImage::getImageUrl)
                    .collect(Collectors.toList());
            
            return PostResponse.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .url(post.getUrl())
                    .body(post.getBody())
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .userId(post.getUser().getId())
                    .username(post.getUser().getUsername())
                    .imageUrls(imageUrls)
                    .commentCount(commentCountMap.getOrDefault(post.getId(), 0))
                    .upvotes(upvotesMap.getOrDefault(post.getId(), 0))
                    .downvotes(downvotesMap.getOrDefault(post.getId(), 0))
                    .userVote(userVotesMap.get(post.getId()))
                    .build();
        });
    }
}