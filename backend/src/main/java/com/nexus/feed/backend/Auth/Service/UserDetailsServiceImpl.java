package com.nexus.feed.backend.Auth.Service;

import com.nexus.feed.backend.Auth.DTO.AppUserDetails;
import com.nexus.feed.backend.Auth.Repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return appUserRepository.findByEmail(username)
                .map(AppUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("user not found with email: " + username));
    }
}
