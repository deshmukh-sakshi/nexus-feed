package com.nexus.feed.backend.Auth.Service;

import com.nexus.feed.backend.Auth.DTO.*;
import com.nexus.feed.backend.Auth.Entity.AppUser;
import com.nexus.feed.backend.Auth.Entity.AuthProvider;
import com.nexus.feed.backend.Auth.Repository.AppUserRepository;
import com.nexus.feed.backend.Email.Service.EmailService;
import com.nexus.feed.backend.Entity.Users;
import com.nexus.feed.backend.Exception.AuthProviderConflictException;
import com.nexus.feed.backend.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository appUserRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthResponse login(LoginRequest request) {
        Optional<AppUser> existingUser = appUserRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent() && existingUser.get().getAuthProvider() == AuthProvider.GOOGLE) {
            log.warn("Google user attempted email/password login: {}", request.getEmail());
            throw new AuthProviderConflictException("This account uses Google sign-in. Please use the Google button to log in.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        AppUserDetails appUserDetails = (AppUserDetails) userDetails;
        log.info("User logged in: email={}", request.getEmail());
        return createAuthResponse(appUserDetails, token);
    }

    public AuthResponse register(RegistrationRequest request) {
        Optional<AppUser> existingUser = appUserRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            if (existingUser.get().getAuthProvider() == AuthProvider.GOOGLE) {
                log.warn("Registration attempt with Google email: {}", request.getEmail());
                throw new AuthProviderConflictException("This email is registered with Google. Please use the Google button to sign in.");
            }
            log.warn("Registration attempt with existing email: {}", request.getEmail());
            throw new com.nexus.feed.backend.Exception.UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration attempt with existing username: {}", request.getUsername());
            throw new com.nexus.feed.backend.Exception.UsernameAlreadyExistsException("Username '" + request.getUsername() + "' is already taken");
        }

        AppUser appUser = new AppUser();
        appUser.setEmail(request.getEmail());
        appUser.setPassword(passwordEncoder.encode(request.getPassword()));
        appUser.setAuthProvider(AuthProvider.LOCAL);

        Users userProfile = new Users();
        userProfile.setUsername(request.getUsername());
        userProfile.setCreatedAt(Instant.now());
        userProfile.setUpdatedAt(Instant.now());
        userProfile.setAppUser(appUser);
        
        appUser.setUserProfile(userProfile);

        AppUser savedUser = appUserRepository.save(appUser);

        AppUserDetails appUserDetails = new AppUserDetails(savedUser);
        String token = jwtService.generateToken(appUserDetails);
        
        log.info("User registered: email={}, userId={}", request.getEmail(), savedUser.getId());
        
        sendWelcomeEmailSafely(request.getEmail(), request.getUsername());
        
        return createAuthResponse(appUserDetails, token);
    }
    
    /**
     * Send welcome email without blocking registration or propagating errors.
     */
    private void sendWelcomeEmailSafely(String email, String username) {
        try {
            emailService.sendWelcomeEmail(email, username);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", email, e.getMessage());
        }
    }

    private AuthResponse createAuthResponse(AppUserDetails userDetails, String token) {
        return new AuthResponse(
                userDetails.getUserId(),
                userDetails.getDisplayUsername(),
                userDetails.getEmail(),
                token,
                userDetails.getRole().name()
        );
    }
}
