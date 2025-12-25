package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.Auth.DTO.AppUserDetails;
import com.nexus.feed.backend.Exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Slf4j
@Service
public class AuthenticationService {
    
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("Authentication context is null or not authenticated");
            throw new UnauthorizedException("User not authenticated");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof AppUserDetails) {
            AppUserDetails userDetails = (AppUserDetails) principal;
            UUID userId = userDetails.getUserId();
            if (userId == null) {
                log.warn("User profile not found for authenticated user: {}", userDetails.getEmail());
                throw new UnauthorizedException("User profile not found");
            }
            return userId;
        }
        
        log.warn("Invalid authentication principal type: {}", principal.getClass().getName());
        throw new UnauthorizedException("Invalid authentication principal");
    }
    
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("Authentication context is null or not authenticated");
            throw new UnauthorizedException("User not authenticated");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof AppUserDetails) {
            return ((AppUserDetails) principal).getUsername();
        }
        
        log.warn("Invalid authentication principal type: {}", principal.getClass().getName());
        throw new UnauthorizedException("Invalid authentication principal");
    }
}