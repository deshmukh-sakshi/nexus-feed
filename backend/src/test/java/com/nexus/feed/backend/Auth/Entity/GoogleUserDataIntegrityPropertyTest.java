package com.nexus.feed.backend.Auth.Entity;

import com.nexus.feed.backend.Entity.Users;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

class GoogleUserDataIntegrityPropertyTest {

    @Property(tries = 100)
    void googleUserShouldHaveCorrectProviderAndNullPassword(
            @ForAll("emails") String email,
            @ForAll("providerIds") String providerId,
            @ForAll("pictureUrls") String pictureUrl) {

        AppUser appUser = createGoogleUser(email, providerId);
        Users userProfile = createUserProfile(appUser, pictureUrl);
        appUser.setUserProfile(userProfile);

        assertThat(appUser.getAuthProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(appUser.getPassword()).isNull();
        assertThat(appUser.getProviderId()).isEqualTo(providerId);
        assertThat(appUser.getEmail()).isEqualTo(email);
        assertThat(userProfile.getProfilePictureUrl()).isEqualTo(pictureUrl);
    }

    @Property(tries = 100)
    void localUserShouldHavePasswordAndLocalProvider(
            @ForAll("emails") String email,
            @ForAll("passwords") String password) {

        AppUser appUser = createLocalUser(email, password);

        assertThat(appUser.getAuthProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(appUser.getPassword()).isEqualTo(password);
        assertThat(appUser.getProviderId()).isNull();
    }

    @Property(tries = 100)
    void googleUserWithNullPictureUrlShouldHaveNullProfilePicture(
            @ForAll("emails") String email,
            @ForAll("providerIds") String providerId) {

        AppUser appUser = createGoogleUser(email, providerId);
        Users userProfile = createUserProfile(appUser, null);
        appUser.setUserProfile(userProfile);

        assertThat(appUser.getAuthProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(userProfile.getProfilePictureUrl()).isNull();
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
    Arbitrary<String> providerIds() {
        return Arbitraries.strings()
                .numeric()
                .ofLength(21);
    }

    @Provide
    Arbitrary<String> pictureUrls() {
        return Arbitraries.of(
                "https://lh3.googleusercontent.com/a/default-user",
                "https://lh3.googleusercontent.com/a/ACg8ocK123456",
                "https://lh3.googleusercontent.com/a-/AOh14Gi789"
        );
    }

    @Provide
    Arbitrary<String> passwords() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(8)
                .ofMaxLength(20);
    }

    private AppUser createGoogleUser(String email, String providerId) {
        AppUser appUser = new AppUser();
        appUser.setEmail(email);
        appUser.setPassword(null);
        appUser.setAuthProvider(AuthProvider.GOOGLE);
        appUser.setProviderId(providerId);
        return appUser;
    }

    private AppUser createLocalUser(String email, String password) {
        AppUser appUser = new AppUser();
        appUser.setEmail(email);
        appUser.setPassword(password);
        appUser.setAuthProvider(AuthProvider.LOCAL);
        appUser.setProviderId(null);
        return appUser;
    }

    private Users createUserProfile(AppUser appUser, String pictureUrl) {
        Users userProfile = new Users();
        userProfile.setAppUser(appUser);
        userProfile.setProfilePictureUrl(pictureUrl);
        return userProfile;
    }
}
