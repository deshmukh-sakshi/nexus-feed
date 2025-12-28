package com.nexus.feed.backend.Exception;

/**
 * Exception thrown when a user attempts to report a post they've already reported.
 */
public class DuplicateReportException extends RuntimeException {
    
    public DuplicateReportException(String message) {
        super(message);
    }
}
