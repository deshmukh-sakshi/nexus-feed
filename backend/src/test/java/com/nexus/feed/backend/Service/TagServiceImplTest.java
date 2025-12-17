package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.DTO.TagResponse;
import com.nexus.feed.backend.DTO.TrendingTagResponse;
import com.nexus.feed.backend.Entity.Post;
import com.nexus.feed.backend.Entity.Tag;
import com.nexus.feed.backend.Repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagServiceImpl Unit Tests")
class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagServiceImpl tagService;

    private Tag tag1;
    private Tag tag2;

    @BeforeEach
    void setUp() {
        tag1 = Tag.builder().id(1L).name("java").posts(new HashSet<>()).build();
        tag2 = Tag.builder().id(2L).name("spring").posts(new HashSet<>()).build();
    }

    @Test
    @DisplayName("Should get or create tags - existing tags")
    void shouldGetExistingTags() {
        // Given
        List<String> tagNames = List.of("java", "spring");
        when(tagRepository.findByNameInIgnoreCase(any())).thenReturn(List.of(tag1, tag2));

        // When
        Set<Tag> result = tagService.getOrCreateTags(tagNames);

        // Then
        assertThat(result).hasSize(2);
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    @DisplayName("Should create new tags when not existing")
    void shouldCreateNewTags() {
        // Given
        List<String> tagNames = List.of("newtag");
        Tag newTag = Tag.builder().id(3L).name("newtag").build();

        when(tagRepository.findByNameInIgnoreCase(any())).thenReturn(List.of());
        when(tagRepository.save(any(Tag.class))).thenReturn(newTag);

        // When
        Set<Tag> result = tagService.getOrCreateTags(tagNames);

        // Then
        assertThat(result).hasSize(1);
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    @DisplayName("Should normalize tag names to lowercase")
    void shouldNormalizeTagNamesToLowercase() {
        // Given
        List<String> tagNames = List.of("JAVA", "Spring");
        when(tagRepository.findByNameInIgnoreCase(any())).thenReturn(List.of(tag1, tag2));

        // When
        Set<Tag> result = tagService.getOrCreateTags(tagNames);

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should return empty set for null tag names")
    void shouldReturnEmptySetForNullTagNames() {
        // When
        Set<Tag> result = tagService.getOrCreateTags(null);

        // Then
        assertThat(result).isEmpty();
        verify(tagRepository, never()).findByNameInIgnoreCase(any());
    }

    @Test
    @DisplayName("Should return empty set for empty tag names list")
    void shouldReturnEmptySetForEmptyTagNames() {
        // When
        Set<Tag> result = tagService.getOrCreateTags(List.of());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should filter out empty and long tag names")
    void shouldFilterOutInvalidTagNames() {
        // Given
        String longTag = "a".repeat(51);
        List<String> tagNames = List.of("", "  ", longTag, "valid");
        Tag validTag = Tag.builder().id(1L).name("valid").build();

        when(tagRepository.findByNameInIgnoreCase(any())).thenReturn(List.of());
        when(tagRepository.save(any(Tag.class))).thenReturn(validTag);

        // When
        Set<Tag> result = tagService.getOrCreateTags(tagNames);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should search tags by query")
    void shouldSearchTagsByQuery() {
        // Given
        when(tagRepository.searchByName("jav")).thenReturn(List.of(tag1));

        // When
        List<TagResponse> result = tagService.searchTags("jav");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("java");
    }

    @Test
    @DisplayName("Should return trending tags when search query is empty")
    void shouldReturnTrendingTagsWhenQueryEmpty() {
        // Given
        when(tagRepository.findTopTags()).thenReturn(List.of(tag1, tag2));

        // When
        List<TagResponse> result = tagService.searchTags("");

        // Then
        assertThat(result).hasSize(2);
        verify(tagRepository).findTopTags();
    }

    @Test
    @DisplayName("Should get trending tags")
    void shouldGetTrendingTags() {
        // Given
        when(tagRepository.findTopTags()).thenReturn(List.of(tag1, tag2));

        // When
        List<TagResponse> result = tagService.getTrendingTags(10);

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should get all tags")
    void shouldGetAllTags() {
        // Given
        when(tagRepository.findAll()).thenReturn(List.of(tag1, tag2));

        // When
        List<TagResponse> result = tagService.getAllTags();

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should calculate trending score with recent posts")
    void shouldCalculateTrendingScoreWithRecentPosts() {
        // Given
        Post recentPost = new Post();
        recentPost.setCreatedAt(Instant.now().minus(Duration.ofHours(12)));

        Post weekOldPost = new Post();
        weekOldPost.setCreatedAt(Instant.now().minus(Duration.ofDays(3)));

        Set<Post> posts = new HashSet<>(Arrays.asList(recentPost, weekOldPost));
        Tag tagWithPosts = Tag.builder().id(1L).name("trending").posts(posts).build();

        // When
        double score = tagService.calculateTrendingScore(tagWithPosts);

        // Then
        assertThat(score).isEqualTo(1.5); // 1.0 for recent + 0.5 for week old
    }

    @Test
    @DisplayName("Should return zero score for tag with no posts")
    void shouldReturnZeroScoreForTagWithNoPosts() {
        // Given
        Tag emptyTag = Tag.builder().id(1L).name("empty").posts(null).build();

        // When
        double score = tagService.calculateTrendingScore(emptyTag);

        // Then
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should get trending tags with score")
    void shouldGetTrendingTagsWithScore() {
        // Given
        Post recentPost = new Post();
        recentPost.setCreatedAt(Instant.now().minus(Duration.ofHours(6)));
        tag1.setPosts(new HashSet<>(List.of(recentPost)));

        when(tagRepository.findAllWithPosts()).thenReturn(List.of(tag1));

        // When
        List<TrendingTagResponse> result = tagService.getTrendingTagsWithScore(10);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrendingScore()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should exclude tags with no recent posts from trending")
    void shouldExcludeTagsWithNoRecentPosts() {
        // Given
        Post oldPost = new Post();
        oldPost.setCreatedAt(Instant.now().minus(Duration.ofDays(60)));
        tag1.setPosts(new HashSet<>(List.of(oldPost)));

        when(tagRepository.findAllWithPosts()).thenReturn(List.of(tag1));

        // When
        List<TrendingTagResponse> result = tagService.getTrendingTagsWithScore(10);

        // Then
        assertThat(result).isEmpty();
    }
}
