package com.nexus.feed.backend.Auth.Entity;

import com.nexus.feed.backend.Entity.Users;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"userProfile"})
@EqualsAndHashCode(exclude = {"userProfile"})
@Table(name = "app_users", indexes = {
    @Index(name = "idx_app_users_email_provider", columnList = "email, auth_provider")
})
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(name = "provider_id")
    private String providerId;

    @OneToOne(mappedBy = "appUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private Users userProfile;
}
