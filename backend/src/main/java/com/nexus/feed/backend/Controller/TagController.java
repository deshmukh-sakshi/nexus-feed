package com.nexus.feed.backend.Controller;

import com.nexus.feed.backend.DTO.TagResponse;
import com.nexus.feed.backend.DTO.TrendingTagResponse;
import com.nexus.feed.backend.Service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<TagResponse>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @GetMapping("/search")
    public ResponseEntity<List<TagResponse>> searchTags(@RequestParam(required = false) String query) {
        log.debug("Searching tags with query: {}", query);
        return ResponseEntity.ok(tagService.searchTags(query));
    }

    @GetMapping("/trending")
    public ResponseEntity<List<TagResponse>> getTrendingTags(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(tagService.getTrendingTags(limit));
    }

    @GetMapping("/trending-scored")
    public ResponseEntity<List<TrendingTagResponse>> getTrendingTagsWithScore(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(tagService.getTrendingTagsWithScore(limit));
    }
}
