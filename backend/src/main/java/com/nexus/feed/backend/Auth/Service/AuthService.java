package com.nexus.feed.backend.Auth.Service;

import com.nexus.feed.backend.Auth.DTO.*;
import com.nexus.feed.backend.Auth.Entity.AppUser;
import com.nexus.feed.backend.Auth.Repository.AppUserRepository;
import com.nexus.feed.backend.Entity.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        log.info("Authenticating user: {}", request.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        log.info("Authentication successful");

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        log.info("Token generated");

        AppUserDetails appUserDetails = (AppUserDetails) userDetails;
        return createAuthResponse(appUserDetails, token);
    }

    public AuthResponse register(RegistrationRequest request) {
        log.info("Registering user: {}", request.getEmail());
        
        if (appUserRepository.findByEmail(request.getEmail()).isPresent()) {
            log.error("User already exists");
            throw new com.nexus.feed.backend.Exception.UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        log.info("User does not exist, creating new user");
        AppUser appUser = new AppUser();
        appUser.setEmail(request.getEmail());
        appUser.setPassword(passwordEncoder.encode(request.getPassword()));

        Users userProfile = new Users();
        userProfile.setUsername(request.getUsername());
        userProfile.setCreatedAt(LocalDateTime.now());
        userProfile.setUpdatedAt(LocalDateTime.now());
        userProfile.setAppUser(appUser);
        
        appUser.setUserProfile(userProfile);

        log.info("Saving user");
        AppUser savedUser = appUserRepository.save(appUser);
        log.info("User saved with ID: {}", savedUser.getId());

        AppUserDetails appUserDetails = new AppUserDetails(savedUser);
        String token = jwtService.generateToken(appUserDetails);
        log.info("Token generated");
        
        return createAuthResponse(appUserDetails, token);
    }

    private AuthResponse createAuthResponse(AppUserDetails userDetails, String token) {
        return new AuthResponse(
                userDetails.getUserId(),
                userDetails.getDisplayUsername(),
                userDetails.getEmail(),
                token
        );
    }
}
