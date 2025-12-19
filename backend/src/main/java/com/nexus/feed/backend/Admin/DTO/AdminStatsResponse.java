package com.nexus.feed.backend.Admin.DTO;

public record AdminStatsResponse(
    long totalUsers,
    long totalPosts,
    long totalComments,
    long totalVotes,
    long newUsersToday,
    long newPostsToday
) {}
