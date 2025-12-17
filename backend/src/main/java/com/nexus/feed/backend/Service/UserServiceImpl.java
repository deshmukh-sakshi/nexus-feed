package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.DTO.*;
import com.nexus.feed.backend.Entity.Users;
import com.nexus.feed.backend.Exception.ResourceNotFoundException;
import com.nexus.feed.backend.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
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
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return convertToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return convertToResponse(user);
    }

    @Override
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(request.getProfilePictureUrl());
        }
        user.setUpdatedAt(Instant.now());

        Users updatedUser = userRepository.save(user);
        log.info("User updated: id={}", id);
        return convertToResponse(updatedUser);
    }

    @Override
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
        log.info("User deleted: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getTopUsersByKarma(int limit) {
        return userRepository.findTopByKarma(PageRequest.of(0, limit))
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
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
                .karma(user.getKarma())
                .build();
    }
}