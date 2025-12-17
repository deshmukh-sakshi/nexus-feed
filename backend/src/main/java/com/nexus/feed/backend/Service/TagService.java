package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.DTO.TagResponse;
import com.nexus.feed.backend.DTO.TrendingTagResponse;
import com.nexus.feed.backend.Entity.Tag;

import java.util.List;
import java.util.Set;

public interface TagService {
    Set<Tag> getOrCreateTags(List<String> tagNames);
    List<TagResponse> searchTags(String query);
    List<TagResponse> getTrendingTags(int limit);
    List<TagResponse> getAllTags();

    double calculateTrendingScore(Tag tag);
    List<TrendingTagResponse> getTrendingTagsWithScore(int limit);
}
