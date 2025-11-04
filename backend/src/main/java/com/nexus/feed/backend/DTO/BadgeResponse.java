package com.nexus.feed.backend.DTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BadgeResponse {
    private Integer id;
    private String name;
    private String description;
    private String iconUrl;
}