package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.Auth.DTO.AppUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class AuthenticationService {
    
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof AppUserDetails) {
            AppUserDetails userDetails = (AppUserDetails) principal;
            UUID userId = userDetails.getUserId();
            if (userId == null) {
                throw new RuntimeException("User profile not found");
            }
            return userId;
        }
        
        throw new RuntimeException("Invalid authentication principal");
    }
    
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof AppUserDetails) {
            return ((AppUserDetails) principal).getUsername();
        }
        
        throw new RuntimeException("Invalid authentication principal");
    }
}