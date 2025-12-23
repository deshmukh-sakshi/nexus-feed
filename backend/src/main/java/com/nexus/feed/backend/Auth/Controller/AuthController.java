package com.nexus.feed.backend.Auth.Controller;

import com.nexus.feed.backend.Auth.DTO.*;
import com.nexus.feed.backend.Auth.Service.AuthService;
import com.nexus.feed.backend.Auth.Service.GoogleAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.debug("Login attempt for email: {}", request.getEmail());
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegistrationRequest request) {
        log.debug("Registration attempt for email: {}", request.getEmail());
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        log.debug("Google login attempt");
        return ResponseEntity.ok(googleAuthService.initiateGoogleLogin(request.idToken()));
    }

    @PostMapping("/google/complete")
    public ResponseEntity<AuthResponse> completeGoogleRegistration(
            @Valid @RequestBody GoogleCompleteRequest request) {
        log.debug("Completing Google registration with username: {}", request.username());
        return ResponseEntity.ok(googleAuthService.completeGoogleRegistration(
                request.tempToken(), request.username()));
    }
}
