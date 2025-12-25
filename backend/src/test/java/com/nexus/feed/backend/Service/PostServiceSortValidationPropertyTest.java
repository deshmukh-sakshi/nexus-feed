package com.nexus.feed.backend.Service;

import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

class PostServiceSortValidationPropertyTest {

    @Property(tries = 100)
    void invalidSortParameterDefaultsToNew(@ForAll("invalidSortValues") String invalidSort) {
        String result = validateSortOption(invalidSort);
        assertThat(result).isEqualTo("new");
    }

    @Property(tries = 100)
    void validSortParametersArePreserved(@ForAll("validSortValues") String validSort) {
        String result = validateSortOption(validSort);
        assertThat(result).isIn("new", "best", "hot");
    }

    @Example
    void nullSortDefaultsToNew() {
        String result = validateSortOption(null);
        assertThat(result).isEqualTo("new");
    }

    @Example
    void newSortIsPreserved() {
        assertThat(validateSortOption("new")).isEqualTo("new");
        assertThat(validateSortOption("NEW")).isEqualTo("new");
        assertThat(validateSortOption("New")).isEqualTo("new");
    }

    @Example
    void bestSortIsPreserved() {
        assertThat(validateSortOption("best")).isEqualTo("best");
        assertThat(validateSortOption("BEST")).isEqualTo("best");
        assertThat(validateSortOption("Best")).isEqualTo("best");
    }

    @Example
    void hotSortIsPreserved() {
        assertThat(validateSortOption("hot")).isEqualTo("hot");
        assertThat(validateSortOption("HOT")).isEqualTo("hot");
        assertThat(validateSortOption("Hot")).isEqualTo("hot");
    }

    private String validateSortOption(String sort) {
        if (sort == null) return "new";
        String normalized = sort.toLowerCase().trim();
        return switch (normalized) {
            case "new", "best", "hot" -> normalized;
            default -> "new";
        };
    }

    @Provide
    Arbitrary<String> invalidSortValues() {
        return Arbitraries.of(
            "", " ", "invalid", "newest", "top", "rising", 
            "random", "123", "best!", "hot_", "new-", 
            "INVALID", "unknown", "sort", "order"
        );
    }

    @Provide
    Arbitrary<String> validSortValues() {
        return Arbitraries.of(
            "new", "NEW", "New", "nEw",
            "best", "BEST", "Best", "bEsT",
            "hot", "HOT", "Hot", "hOt"
        );
    }
}
