package com.nexus.feed.backend.Repository;

import com.nexus.feed.backend.Entity.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TagRepository Tests")
class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    private Tag javaTag;
    private Tag springTag;
    private Tag pythonTag;

    @BeforeEach
    void setUp() {
        tagRepository.deleteAll();

        javaTag = Tag.builder().name("java").build();
        springTag = Tag.builder().name("spring").build();
        pythonTag = Tag.builder().name("python").build();

        tagRepository.save(javaTag);
        tagRepository.save(springTag);
        tagRepository.save(pythonTag);
    }

    @Test
    @DisplayName("Should find tag by name ignoring case")
    void shouldFindTagByNameIgnoringCase() {
        // When
        Optional<Tag> found = tagRepository.findByNameIgnoreCase("JAVA");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("java");
    }

    @Test
    @DisplayName("Should find tags by names ignoring case")
    void shouldFindTagsByNamesIgnoringCase() {
        // When
        List<Tag> tags = tagRepository.findByNameInIgnoreCase(Set.of("JAVA", "SPRING"));

        // Then
        assertThat(tags).hasSize(2);
    }

    @Test
    @DisplayName("Should search tags by name")
    void shouldSearchTagsByName() {
        // When
        List<Tag> tags = tagRepository.searchByName("jav");

        // Then
        assertThat(tags).hasSize(1);
        assertThat(tags.get(0).getName()).isEqualTo("java");
    }

    @Test
    @DisplayName("Should find top tags")
    void shouldFindTopTags() {
        // When
        List<Tag> tags = tagRepository.findTopTags();

        // Then
        assertThat(tags).hasSize(3);
    }

    @Test
    @DisplayName("Should check if tag exists by name")
    void shouldCheckIfTagExistsByName() {
        // When & Then
        assertThat(tagRepository.existsByNameIgnoreCase("java")).isTrue();
        assertThat(tagRepository.existsByNameIgnoreCase("JAVA")).isTrue();
        assertThat(tagRepository.existsByNameIgnoreCase("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("Should return empty when tag not found")
    void shouldReturnEmptyWhenTagNotFound() {
        // When
        Optional<Tag> found = tagRepository.findByNameIgnoreCase("nonexistent");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when no tags match search")
    void shouldReturnEmptyListWhenNoTagsMatchSearch() {
        // When
        List<Tag> tags = tagRepository.searchByName("xyz");

        // Then
        assertThat(tags).isEmpty();
    }

    @Test
    @DisplayName("Should find all tags with posts")
    void shouldFindAllTagsWithPosts() {
        // When
        List<Tag> tags = tagRepository.findAllWithPosts();

        // Then
        assertThat(tags).hasSize(3);
    }
}
