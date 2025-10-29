package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.DTO.*;
import com.nexus.feed.backend.Entity.Users;
import com.nexus.feed.backend.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        throw new UnsupportedOperationException("Use /api/auth/register endpoint to create users");
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToResponse(user);
    }

    @Override
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(request.getProfilePictureUrl());
        }
        user.setUpdatedAt(LocalDateTime.now());

        Users updatedUser = userRepository.save(user);
        return convertToResponse(updatedUser);
    }

    @Override
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    private UserResponse convertToResponse(Users user) {
        // Get email from AppUser relationship
        String email = user.getAppUser() != null ? user.getAppUser().getEmail() : null;
        
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(email)
                .bio(user.getBio())
                .profilePictureUrl(user.getProfilePictureUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}