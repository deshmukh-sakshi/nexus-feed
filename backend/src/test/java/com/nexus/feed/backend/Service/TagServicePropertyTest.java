package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.Entity.Post;
import com.nexus.feed.backend.Entity.Tag;
import com.nexus.feed.backend.Repository.TagRepository;
import net.jqwik.api.*;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

class TagServicePropertyTest {

    @Property(tries = 100)
    void timeDecayScoreFollowsWeightRules(
            @ForAll("postCounts") int postsLast24h,
            @ForAll("postCounts") int posts1to7days,
            @ForAll("postCounts") int posts7to30days,
            @ForAll("postCounts") int postsOlderThan30days
    ) {
        // Given
        TagRepository tagRepository = Mockito.mock(TagRepository.class);
        TagServiceImpl tagService = new TagServiceImpl(tagRepository);

        Tag tag = createTagWithPosts(postsLast24h, posts1to7days, posts7to30days, postsOlderThan30days);

        // When
        double actualScore = tagService.calculateTrendingScore(tag);

        // Then
        double expectedScore = (postsLast24h * 1.0) + (posts1to7days * 0.5) + (posts7to30days * 0.1);
        assert Math.abs(actualScore - expectedScore) < 0.001;
    }

    @Property(tries = 100)
    void staleTagsHaveZeroScore(@ForAll("postCounts") int postCount) {
        // Given
        TagRepository tagRepository = Mockito.mock(TagRepository.class);
        TagServiceImpl tagService = new TagServiceImpl(tagRepository);

        Tag tag = createTagWithPosts(0, 0, 0, postCount);

        // When
        double score = tagService.calculateTrendingScore(tag);

        // Then
        assert score == 0.0;
    }

    @Property(tries = 100)
    void emptyTagHasZeroScore() {
        // Given
        TagRepository tagRepository = Mockito.mock(TagRepository.class);
        TagServiceImpl tagService = new TagServiceImpl(tagRepository);

        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("empty-tag");
        tag.setPosts(new HashSet<>());

        // When
        double score = tagService.calculateTrendingScore(tag);

        // Then
        assert score == 0.0;
    }

    @Property(tries = 100)
    void nullPostsHasZeroScore() {
        // Given
        TagRepository tagRepository = Mockito.mock(TagRepository.class);
        TagServiceImpl tagService = new TagServiceImpl(tagRepository);

        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("null-posts-tag");
        tag.setPosts(null);

        // When
        double score = tagService.calculateTrendingScore(tag);

        // Then
        assert score == 0.0;
    }

    @Property(tries = 100)
    void scoreIsAlwaysNonNegative(@ForAll("postCounts") int postCount) {
        // Given
        TagRepository tagRepository = Mockito.mock(TagRepository.class);
        TagServiceImpl tagService = new TagServiceImpl(tagRepository);

        Tag tag = createTagWithPosts(postCount, postCount, postCount, postCount);

        // When
        double score = tagService.calculateTrendingScore(tag);

        // Then
        assert score >= 0.0;
    }

    @Property(tries = 100)
    void recentPostsScoreHigherThanOldPosts() {
        // Given
        TagRepository tagRepository = Mockito.mock(TagRepository.class);
        TagServiceImpl tagService = new TagServiceImpl(tagRepository);

        Tag recentTag = createTagWithPosts(1, 0, 0, 0);
        Tag oldTag = createTagWithPosts(0, 0, 1, 0);

        // When
        double recentScore = tagService.calculateTrendingScore(recentTag);
        double oldScore = tagService.calculateTrendingScore(oldTag);

        // Then
        assert recentScore > oldScore;
    }

    @Provide
    Arbitrary<Integer> postCounts() {
        return Arbitraries.integers().between(0, 5);
    }

    private Tag createTagWithPosts(int postsLast24h, int posts1to7days, int posts7to30days, int postsOlderThan30days) {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("test-tag");
        Set<Post> posts = new HashSet<>();

        Instant now = Instant.now();

        for (int i = 0; i < postsLast24h; i++) {
            posts.add(createPost(now.minus(Duration.ofHours(i + 1))));
        }

        for (int i = 0; i < posts1to7days; i++) {
            posts.add(createPost(now.minus(Duration.ofDays(2 + i))));
        }

        for (int i = 0; i < posts7to30days; i++) {
            posts.add(createPost(now.minus(Duration.ofDays(10 + i))));
        }

        for (int i = 0; i < postsOlderThan30days; i++) {
            posts.add(createPost(now.minus(Duration.ofDays(35 + i))));
        }

        tag.setPosts(posts);
        return tag;
    }

    private Post createPost(Instant createdAt) {
        Post post = new Post();
        post.setCreatedAt(createdAt);
        post.setTitle("Test Post");
        return post;
    }
}
