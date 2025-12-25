package com.nexus.feed.backend.Controller;

import com.nexus.feed.backend.DTO.TagResponse;
import com.nexus.feed.backend.DTO.TrendingTagResponse;
import com.nexus.feed.backend.Service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.context.annotation.Import;
import com.nexus.feed.backend.Exception.GlobalExceptionHandler;
import com.nexus.feed.backend.Auth.Service.JwtService;
import com.nexus.feed.backend.Auth.Service.UserDetailsServiceImpl;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@WebMvcTest(controllers = TagController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@DisplayName("TagController Tests")
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TagService tagService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private TagResponse tagResponse1;
    private TagResponse tagResponse2;
    private TrendingTagResponse trendingTagResponse;

    @BeforeEach
    void setUp() {
        tagResponse1 = TagResponse.builder()
                .id(1L)
                .name("java")
                .postCount(10)
                .build();

        tagResponse2 = TagResponse.builder()
                .id(2L)
                .name("spring")
                .postCount(5)
                .build();

        trendingTagResponse = TrendingTagResponse.builder()
                .id(1L)
                .name("java")
                .postCount(10)
                .trendingScore(5.5)
                .build();
    }

    @Test
    @DisplayName("Should get all tags successfully")
    void shouldGetAllTagsSuccessfully() throws Exception {
        // Given
        when(tagService.getAllTags()).thenReturn(List.of(tagResponse1, tagResponse2));

        // When & Then
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("java"))
                .andExpect(jsonPath("$[1].name").value("spring"));
    }

    @Test
    @DisplayName("Should return empty list when no tags exist")
    void shouldReturnEmptyListWhenNoTags() throws Exception {
        // Given
        when(tagService.getAllTags()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Should search tags with query")
    void shouldSearchTagsWithQuery() throws Exception {
        // Given
        when(tagService.searchTags("jav")).thenReturn(List.of(tagResponse1));

        // When & Then
        mockMvc.perform(get("/api/tags/search")
                        .param("query", "jav"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("java"));
    }

    @Test
    @DisplayName("Should search tags without query parameter")
    void shouldSearchTagsWithoutQuery() throws Exception {
        // Given
        when(tagService.searchTags(null)).thenReturn(List.of(tagResponse1, tagResponse2));

        // When & Then
        mockMvc.perform(get("/api/tags/search"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Should get trending tags with default limit")
    void shouldGetTrendingTagsWithDefaultLimit() throws Exception {
        // Given
        when(tagService.getTrendingTags(10)).thenReturn(List.of(tagResponse1, tagResponse2));

        // When & Then
        mockMvc.perform(get("/api/tags/trending"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Should get trending tags with custom limit")
    void shouldGetTrendingTagsWithCustomLimit() throws Exception {
        // Given
        when(tagService.getTrendingTags(5)).thenReturn(List.of(tagResponse1));

        // When & Then
        mockMvc.perform(get("/api/tags/trending")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("Should get trending tags with score")
    void shouldGetTrendingTagsWithScore() throws Exception {
        // Given
        when(tagService.getTrendingTagsWithScore(10)).thenReturn(List.of(trendingTagResponse));

        // When & Then
        mockMvc.perform(get("/api/tags/trending-scored"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("java"))
                .andExpect(jsonPath("$[0].trendingScore").value(5.5));
    }

    @Test
    @DisplayName("Should get trending tags with score and custom limit")
    void shouldGetTrendingTagsWithScoreAndCustomLimit() throws Exception {
        // Given
        when(tagService.getTrendingTagsWithScore(3)).thenReturn(List.of(trendingTagResponse));

        // When & Then
        mockMvc.perform(get("/api/tags/trending-scored")
                        .param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }
}
