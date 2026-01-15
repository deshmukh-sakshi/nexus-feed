package com.nexus.feed.backend.Entity;

/**
 * Enumeration of reasons for reporting a post.
 * Each reason has a human-readable display name.
 */
public enum ReportReason {
    SEXUAL_CONTENT("Sexual content"),
    VIOLENT_OR_REPULSIVE("Violent or repulsive content"),
    HATEFUL_OR_ABUSIVE("Hateful or abusive content"),
    HARASSMENT_OR_BULLYING("Harassment or bullying"),
    HARMFUL_OR_DANGEROUS("Harmful or dangerous acts"),
    MISINFORMATION("Misinformation"),
    SPAM_OR_MISLEADING("Spam or misleading"),
    LEGAL_ISSUE("Legal issue"),
    OTHER("Something else");

    private final String displayName;

    ReportReason(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
