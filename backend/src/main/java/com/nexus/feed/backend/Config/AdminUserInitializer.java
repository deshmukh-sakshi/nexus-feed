package com.nexus.feed.backend.Config;

import com.nexus.feed.backend.Auth.Entity.AppUser;
import com.nexus.feed.backend.Auth.Entity.Role;
import com.nexus.feed.backend.Auth.Repository.AppUserRepository;
import com.nexus.feed.backend.Entity.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class AdminUserInitializer implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminEmail = "admin@nexusfeed.com";
        
        var existingAdmin = appUserRepository.findByEmail(adminEmail);
        
        if (existingAdmin.isEmpty()) {
            AppUser admin = new AppUser();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("12345678"));
            admin.setRole(Role.ADMIN);

            Users adminProfile = new Users();
            adminProfile.setUsername("admin");
            adminProfile.setCreatedAt(Instant.now());
            adminProfile.setUpdatedAt(Instant.now());
            adminProfile.setAppUser(admin);

            admin.setUserProfile(adminProfile);

            appUserRepository.save(admin);
            log.info("Admin user created: {}", adminEmail);
        } else {
            // Ensure existing admin has ADMIN role
            AppUser admin = existingAdmin.get();
            if (admin.getRole() != Role.ADMIN) {
                admin.setRole(Role.ADMIN);
                appUserRepository.save(admin);
                log.info("Admin user role updated to ADMIN: {}", adminEmail);
            } else {
                log.info("Admin user already exists with ADMIN role: {}", adminEmail);
            }
        }
    }
}
