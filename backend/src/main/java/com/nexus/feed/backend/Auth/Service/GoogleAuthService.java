package com.nexus.feed.backend.Auth.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.nexus.feed.backend.Auth.DTO.*;
import com.nexus.feed.backend.Auth.Entity.AppUser;
import com.nexus.feed.backend.Auth.Entity.AuthProvider;
import com.nexus.feed.backend.Auth.Repository.AppUserRepository;
import com.nexus.feed.backend.Entity.Users;
import com.nexus.feed.backend.Exception.AuthProviderConflictException;
import com.nexus.feed.backend.Exception.GoogleAuthException;
import com.nexus.feed.backend.Exception.UsernameAlreadyExistsException;
import com.nexus.feed.backend.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final AppUserRepository appUserRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final TempTokenService tempTokenService;

    @Transactional
    public Object initiateGoogleLogin(String idToken) {
        if (googleIdTokenVerifier == null) {
            throw new GoogleAuthException("Google authentication is not configured");
        }

        GoogleUserInfo userInfo = verifyGoogleToken(idToken);

        Optional<AppUser> existingUser = appUserRepository.findByEmail(userInfo.email());

        if (existingUser.isPresent()) {
            AppUser appUser = existingUser.get();

            if (appUser.getAuthProvider() == AuthProvider.LOCAL) {
                log.warn("Local user attempted Google login: {}", userInfo.email());
                throw new AuthProviderConflictException(
                        "This email is registered with a password. Please use email/password to log in.");
            }

            Users profile = appUser.getUserProfile();
            if (profile.getUsername() == null || profile.getUsername().isBlank()) {
                String tempToken = tempTokenService.generateTempToken(userInfo, appUser.getId());
                return new GoogleAuthResponse(true, tempToken, userInfo.email(),
                        userInfo.name(), userInfo.pictureUrl());
            }

            AppUserDetails appUserDetails = new AppUserDetails(appUser);
            String jwt = jwtService.generateToken(appUserDetails);
            log.info("Google user logged in: email={}", userInfo.email());
            return new AuthResponse(profile.getId(), profile.getUsername(), userInfo.email(), jwt);
        }

        AppUser newAppUser = new AppUser();
        newAppUser.setEmail(userInfo.email());
        newAppUser.setPassword(null);
        newAppUser.setAuthProvider(AuthProvider.GOOGLE);
        newAppUser.setProviderId(userInfo.providerId());

        Users newProfile = new Users();
        newProfile.setUsername(null);
        newProfile.setProfilePictureUrl(userInfo.pictureUrl());
        newProfile.setCreatedAt(Instant.now());
        newProfile.setUpdatedAt(Instant.now());
        newProfile.setAppUser(newAppUser);

        newAppUser.setUserProfile(newProfile);
        AppUser savedUser = appUserRepository.save(newAppUser);

        String tempToken = tempTokenService.generateTempToken(userInfo, savedUser.getId());
        log.info("New Google user created (pending username): email={}", userInfo.email());

        return new GoogleAuthResponse(true, tempToken, userInfo.email(),
                userInfo.name(), userInfo.pictureUrl());
    }

    @Transactional
    public AuthResponse completeGoogleRegistration(String tempToken, String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        String trimmedUsername = username.trim();
        if (trimmedUsername.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be only whitespace");
        }

        TempTokenService.TempTokenData tokenData = tempTokenService.validateAndExtract(tempToken);

        if (userRepository.existsByUsername(trimmedUsername)) {
            throw new UsernameAlreadyExistsException("Username '" + trimmedUsername + "' is already taken");
        }

        AppUser appUser = appUserRepository.findById(tokenData.appUserId())
                .orElseThrow(() -> new GoogleAuthException("User not found. Please try signing in again."));

        Users profile = appUser.getUserProfile();
        profile.setUsername(trimmedUsername);
        profile.setUpdatedAt(Instant.now());
        userRepository.save(profile);

        AppUserDetails appUserDetails = new AppUserDetails(appUser);
        String jwt = jwtService.generateToken(appUserDetails);

        log.info("Google user completed registration: email={}, username={}",
                tokenData.email(), trimmedUsername);

        return new AuthResponse(profile.getId(), trimmedUsername, tokenData.email(), jwt);
    }

    public GoogleUserInfo verifyGoogleToken(String idToken) {
        try {
            GoogleIdToken googleIdToken = googleIdTokenVerifier.verify(idToken);
            if (googleIdToken == null) {
                throw new GoogleAuthException("Invalid Google token");
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            return new GoogleUserInfo(
                    payload.getEmail(),
                    (String) payload.get("name"),
                    (String) payload.get("picture"),
                    payload.getSubject()
            );
        } catch (GoogleAuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google token verification failed: {}", e.getMessage());
            throw new GoogleAuthException("Google authentication failed. Please try again.");
        }
    }
}
