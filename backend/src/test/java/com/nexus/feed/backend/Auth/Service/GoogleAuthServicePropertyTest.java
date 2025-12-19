package com.nexus.feed.backend.Auth.Service;

import com.nexus.feed.backend.Auth.DTO.AuthResponse;
import com.nexus.feed.backend.Auth.DTO.GoogleAuthResponse;
import com.nexus.feed.backend.Auth.DTO.GoogleUserInfo;
import com.nexus.feed.backend.Auth.Entity.AppUser;
import com.nexus.feed.backend.Auth.Entity.AuthProvider;
import com.nexus.feed.backend.Auth.Repository.AppUserRepository;
import com.nexus.feed.backend.Entity.Users;
import com.nexus.feed.backend.Exception.AuthProviderConflictException;
import com.nexus.feed.backend.Exception.UsernameAlreadyExistsException;
import com.nexus.feed.backend.Repository.UserRepository;
import net.jqwik.api.*;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GoogleAuthServicePropertyTest {

    @Property(tries = 100)
    void returningGoogleUserShouldGetValidJwt(
            @ForAll("emails") String email,
            @ForAll("usernames") String username) {

        var appUserRepository = Mockito.mock(AppUserRepository.class);
        var userRepository = Mockito.mock(UserRepository.class);
        var jwtService = Mockito.mock(JwtService.class);
        var tempTokenService = Mockito.mock(TempTokenService.class);

        AppUser existingUser = createGoogleUser(email, username);
        when(appUserRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(any())).thenReturn("mock-jwt-token");

        GoogleAuthService service = new TestableGoogleAuthService(
                null, appUserRepository, userRepository, jwtService, tempTokenService,
                new GoogleUserInfo(email, "Test User", "https://pic.url", "google-123"));

        Object result = service.initiateGoogleLogin("mock-token");

        assertThat(result).isInstanceOf(AuthResponse.class);
        AuthResponse authResponse = (AuthResponse) result;
        assertThat(authResponse.email()).isEqualTo(email);
        assertThat(authResponse.username()).isEqualTo(username);
        assertThat(authResponse.token()).isEqualTo("mock-jwt-token");
    }

    @Property(tries = 100)
    void newGoogleUserShouldRequireUsername(@ForAll("emails") String email) {

        var appUserRepository = Mockito.mock(AppUserRepository.class);
        var userRepository = Mockito.mock(UserRepository.class);
        var jwtService = Mockito.mock(JwtService.class);
        var tempTokenService = Mockito.mock(TempTokenService.class);

        when(appUserRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(appUserRepository.save(any())).thenAnswer(inv -> {
            AppUser user = inv.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(tempTokenService.generateTempToken(any(), any())).thenReturn("temp-token");

        GoogleAuthService service = new TestableGoogleAuthService(
                null, appUserRepository, userRepository, jwtService, tempTokenService,
                new GoogleUserInfo(email, "Test User", "https://pic.url", "google-123"));

        Object result = service.initiateGoogleLogin("mock-token");

        assertThat(result).isInstanceOf(GoogleAuthResponse.class);
        GoogleAuthResponse response = (GoogleAuthResponse) result;
        assertThat(response.needsUsername()).isTrue();
        assertThat(response.email()).isEqualTo(email);
    }

    @Property(tries = 100)
    void incompleteGoogleUserShouldRequireUsername(
            @ForAll("emails") String email) {

        var appUserRepository = Mockito.mock(AppUserRepository.class);
        var userRepository = Mockito.mock(UserRepository.class);
        var jwtService = Mockito.mock(JwtService.class);
        var tempTokenService = Mockito.mock(TempTokenService.class);

        AppUser existingUser = createGoogleUserWithoutUsername(email);
        when(appUserRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(tempTokenService.generateTempToken(any(), any())).thenReturn("temp-token");

        GoogleAuthService service = new TestableGoogleAuthService(
                null, appUserRepository, userRepository, jwtService, tempTokenService,
                new GoogleUserInfo(email, "Test User", "https://pic.url", "google-123"));

        Object result = service.initiateGoogleLogin("mock-token");

        assertThat(result).isInstanceOf(GoogleAuthResponse.class);
        GoogleAuthResponse response = (GoogleAuthResponse) result;
        assertThat(response.needsUsername()).isTrue();
    }

    @Property(tries = 100)
    void localUserShouldBeRejectedForGoogleLogin(@ForAll("emails") String email) {

        var appUserRepository = Mockito.mock(AppUserRepository.class);
        var userRepository = Mockito.mock(UserRepository.class);
        var jwtService = Mockito.mock(JwtService.class);
        var tempTokenService = Mockito.mock(TempTokenService.class);

        AppUser localUser = createLocalUser(email);
        when(appUserRepository.findByEmail(email)).thenReturn(Optional.of(localUser));

        GoogleAuthService service = new TestableGoogleAuthService(
                null, appUserRepository, userRepository, jwtService, tempTokenService,
                new GoogleUserInfo(email, "Test User", "https://pic.url", "google-123"));

        assertThatThrownBy(() -> service.initiateGoogleLogin("mock-token"))
                .isInstanceOf(AuthProviderConflictException.class)
                .hasMessageContaining("password");
    }

    @Property(tries = 100)
    void validUsernameShouldCompleteRegistration(
            @ForAll("usernames") String username) {

        var appUserRepository = Mockito.mock(AppUserRepository.class);
        var userRepository = Mockito.mock(UserRepository.class);
        var jwtService = Mockito.mock(JwtService.class);
        var tempTokenService = Mockito.mock(TempTokenService.class);

        AppUser appUser = createGoogleUserWithoutUsername("test@example.com");
        TempTokenService.TempTokenData tokenData = new TempTokenService.TempTokenData(
                "test@example.com", 1L, "Test", "https://pic.url", "google-123");

        when(tempTokenService.validateAndExtract("temp-token")).thenReturn(tokenData);
        when(userRepository.existsByUsername(username.trim())).thenReturn(false);
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(appUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        GoogleAuthService service = new GoogleAuthService(
                null, appUserRepository, userRepository, jwtService, tempTokenService);

        AuthResponse result = service.completeGoogleRegistration("temp-token", username);

        assertThat(result.username()).isEqualTo(username.trim());
        assertThat(result.token()).isEqualTo("jwt-token");
    }

    @Property(tries = 100)
    void whitespaceUsernameShouldBeRejected(@ForAll("whitespaceStrings") String whitespace) {

        var appUserRepository = Mockito.mock(AppUserRepository.class);
        var userRepository = Mockito.mock(UserRepository.class);
        var jwtService = Mockito.mock(JwtService.class);
        var tempTokenService = Mockito.mock(TempTokenService.class);

        GoogleAuthService service = new GoogleAuthService(
                null, appUserRepository, userRepository, jwtService, tempTokenService);

        assertThatThrownBy(() -> service.completeGoogleRegistration("temp-token", whitespace))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Property(tries = 100)
    void existingUsernameShouldBeRejected(@ForAll("usernames") String username) {

        var appUserRepository = Mockito.mock(AppUserRepository.class);
        var userRepository = Mockito.mock(UserRepository.class);
        var jwtService = Mockito.mock(JwtService.class);
        var tempTokenService = Mockito.mock(TempTokenService.class);

        TempTokenService.TempTokenData tokenData = new TempTokenService.TempTokenData(
                "test@example.com", 1L, "Test", "https://pic.url", "google-123");

        when(tempTokenService.validateAndExtract("temp-token")).thenReturn(tokenData);
        when(userRepository.existsByUsername(username.trim())).thenReturn(true);

        GoogleAuthService service = new GoogleAuthService(
                null, appUserRepository, userRepository, jwtService, tempTokenService);

        assertThatThrownBy(() -> service.completeGoogleRegistration("temp-token", username))
                .isInstanceOf(UsernameAlreadyExistsException.class);
    }

    @Provide
    Arbitrary<String> emails() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(3)
                .ofMaxLength(10)
                .map(s -> s.toLowerCase() + "@example.com");
    }

    @Provide
    Arbitrary<String> usernames() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(3)
                .ofMaxLength(20);
    }

    @Provide
    Arbitrary<String> whitespaceStrings() {
        return Arbitraries.of("", " ", "  ", "\t", "\n", "   \t\n  ");
    }

    private AppUser createGoogleUser(String email, String username) {
        AppUser appUser = new AppUser();
        appUser.setId(1L);
        appUser.setEmail(email);
        appUser.setPassword(null);
        appUser.setAuthProvider(AuthProvider.GOOGLE);
        appUser.setProviderId("google-123");

        Users profile = new Users();
        profile.setId(UUID.randomUUID());
        profile.setUsername(username);
        profile.setAppUser(appUser);
        appUser.setUserProfile(profile);

        return appUser;
    }

    private AppUser createGoogleUserWithoutUsername(String email) {
        AppUser appUser = new AppUser();
        appUser.setId(1L);
        appUser.setEmail(email);
        appUser.setPassword(null);
        appUser.setAuthProvider(AuthProvider.GOOGLE);
        appUser.setProviderId("google-123");

        Users profile = new Users();
        profile.setId(UUID.randomUUID());
        profile.setUsername(null);
        profile.setAppUser(appUser);
        appUser.setUserProfile(profile);

        return appUser;
    }

    private AppUser createLocalUser(String email) {
        AppUser appUser = new AppUser();
        appUser.setId(1L);
        appUser.setEmail(email);
        appUser.setPassword("hashed-password");
        appUser.setAuthProvider(AuthProvider.LOCAL);

        Users profile = new Users();
        profile.setId(UUID.randomUUID());
        profile.setUsername("localuser");
        profile.setAppUser(appUser);
        appUser.setUserProfile(profile);

        return appUser;
    }

    static class TestableGoogleAuthService extends GoogleAuthService {
        private final GoogleUserInfo mockUserInfo;

        TestableGoogleAuthService(
                com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier verifier,
                AppUserRepository appUserRepository,
                UserRepository userRepository,
                JwtService jwtService,
                TempTokenService tempTokenService,
                GoogleUserInfo mockUserInfo) {
            super(verifier, appUserRepository, userRepository, jwtService, tempTokenService);
            this.mockUserInfo = mockUserInfo;
        }

        @Override
        public GoogleUserInfo verifyGoogleToken(String idToken) {
            return mockUserInfo;
        }
    }
}
