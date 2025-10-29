package com.nexus.feed.backend.Auth.DTO;

import com.nexus.feed.backend.Auth.Entity.AppUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Getter
public class AppUserDetails implements UserDetails {

    private final UUID userId;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public AppUserDetails(AppUser appUser) {
        this.email = appUser.getEmail();
        this.password = appUser.getPassword();
        this.userId = appUser.getUserProfile() != null ? appUser.getUserProfile().getId() : null;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
