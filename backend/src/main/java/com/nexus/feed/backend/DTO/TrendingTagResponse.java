package com.nexus.feed.backend.DTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendingTagResponse {
    private Long id;
    private String name;
    private int postCount;
    private double trendingScore;
}
