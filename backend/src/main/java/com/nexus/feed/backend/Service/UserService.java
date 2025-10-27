package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.DTO.*;
import java.util.UUID;

public interface UserService {
    UserResponse createUser(UserCreateRequest request);
    UserResponse getUserById(UUID id);
    UserResponse getUserByUsername(String username);
    UserResponse updateUser(UUID id, UserUpdateRequest request);
    void deleteUser(UUID id);
}