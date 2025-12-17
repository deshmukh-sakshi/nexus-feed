package com.nexus.feed.backend.Repository;

import com.nexus.feed.backend.Auth.Entity.AppUser;
import com.nexus.feed.backend.Auth.Repository.AppUserRepository;
import com.nexus.feed.backend.Entity.Post;
import com.nexus.feed.backend.Entity.Tag;
import com.nexus.feed.backend.Entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("PostRepository Tests")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private TagRepository tagRepository;

    private Users user;
    private Post post1;
    private Post post2;
    private Tag javaTag;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        userRepository.deleteAll();
        appUserRepository.deleteAll();
        tagRepository.deleteAll();

        AppUser appUser = new AppUser();
        appUser.setEmail("tester@example.com");
        appUser.setPassword("password");

        user = new Users();
        user.setUsername("tester");
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user.setAppUser(appUser);
        appUser.setUserProfile(user);

        appUserRepository.save(appUser);

        javaTag = Tag.builder().name("java").build();
        tagRepository.save(javaTag);

        post1 = new Post();
        post1.setTitle("Java Tutorial");
        post1.setBody("Learn Java programming");
        post1.setUser(user);
        post1.setCreatedAt(Instant.now().minusSeconds(100));
        post1.setUpdatedAt(Instant.now());
        post1.setTags(new HashSet<>(Set.of(javaTag)));

        post2 = new Post();
        post2.setTitle("Spring Boot Guide");
        post2.setBody("Building REST APIs");
        post2.setUser(user);
        post2.setCreatedAt(Instant.now());
        post2.setUpdatedAt(Instant.now());

        postRepository.save(post1);
        postRepository.save(post2);
    }

    @Test
    @DisplayName("Should find all posts ordered by created at desc")
    void shouldFindAllPostsOrderedByCreatedAtDesc() {
        // When
        Page<Post> posts = postRepository.findAllOrderByCreatedAtDesc(PageRequest.of(0, 10));

        // Then
        assertThat(posts.getContent()).hasSize(2);
        assertThat(posts.getContent().get(0).getTitle()).isEqualTo("Spring Boot Guide");
    }

    @Test
    @DisplayName("Should find posts by user")
    void shouldFindPostsByUser() {
        // When
        Page<Post> posts = postRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(0, 10));

        // Then
        assertThat(posts.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Should find post by id with user and images")
    void shouldFindPostByIdWithUserAndImages() {
        // When
        Optional<Post> found = postRepository.findByIdWithUserAndImages(post1.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getUsername()).isEqualTo("tester");
    }

    @Test
    @DisplayName("Should search posts by title keyword")
    void shouldSearchPostsByTitleKeyword() {
        // When
        Page<Post> posts = postRepository.findByTitleContainingOrBodyContainingOrderByCreatedAtDesc(
                "Java", PageRequest.of(0, 10));

        // Then
        assertThat(posts.getContent()).hasSize(1);
        assertThat(posts.getContent().get(0).getTitle()).isEqualTo("Java Tutorial");
    }

    @Test
    @DisplayName("Should search posts by body keyword")
    void shouldSearchPostsByBodyKeyword() {
        // When
        Page<Post> posts = postRepository.findByTitleContainingOrBodyContainingOrderByCreatedAtDesc(
                "REST", PageRequest.of(0, 10));

        // Then
        assertThat(posts.getContent()).hasSize(1);
        assertThat(posts.getContent().get(0).getTitle()).isEqualTo("Spring Boot Guide");
    }

    @Test
    @DisplayName("Should find posts by tag name")
    void shouldFindPostsByTagName() {
        // When
        Page<Post> posts = postRepository.findByTagName("java", PageRequest.of(0, 10));

        // Then
        assertThat(posts.getContent()).hasSize(1);
        assertThat(posts.getContent().get(0).getTitle()).isEqualTo("Java Tutorial");
    }

    @Test
    @DisplayName("Should count posts by user")
    void shouldCountPostsByUser() {
        // When
        long count = postRepository.countByUser(user);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return empty when no posts match search")
    void shouldReturnEmptyWhenNoPostsMatchSearch() {
        // When
        Page<Post> posts = postRepository.findByTitleContainingOrBodyContainingOrderByCreatedAtDesc(
                "nonexistent", PageRequest.of(0, 10));

        // Then
        assertThat(posts.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when post not found by id")
    void shouldReturnEmptyWhenPostNotFoundById() {
        // When
        Optional<Post> found = postRepository.findByIdWithUserAndImages(java.util.UUID.randomUUID());

        // Then
        assertThat(found).isEmpty();
    }
}
