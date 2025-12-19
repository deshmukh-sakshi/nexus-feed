package com.nexus.feed.backend.Exception;

public class AuthProviderConflictException extends RuntimeException {
    public AuthProviderConflictException(String message) {
        super(message);
    }
}
