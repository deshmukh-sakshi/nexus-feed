package com.nexus.feed.backend.Auth.Entity;

import com.nexus.feed.backend.Entity.Users;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @OneToOne(mappedBy = "appUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private Users userProfile;
}
