package com.nexus.feed.backend.Auth.Controller;

import com.nexus.feed.backend.Auth.DTO.*;
import com.nexus.feed.backend.Auth.Service.AuthService;
import com.nexus.feed.backend.Auth.Service.GoogleAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegistrationRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(googleAuthService.initiateGoogleLogin(request.idToken()));
    }

    @PostMapping("/google/complete")
    public ResponseEntity<AuthResponse> completeGoogleRegistration(
            @Valid @RequestBody GoogleCompleteRequest request) {
        return ResponseEntity.ok(googleAuthService.completeGoogleRegistration(
                request.tempToken(), request.username()));
    }
}
