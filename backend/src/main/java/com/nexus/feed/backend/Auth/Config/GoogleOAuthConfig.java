package com.nexus.feed.backend.Auth.Config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Slf4j
@Configuration
public class GoogleOAuthConfig {

    @Value("${google.client.id:}")
    private String clientId;

    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier() {
        if (clientId == null || clientId.isBlank()) {
            log.warn("Google Client ID not configured. Google authentication will be disabled.");
            return null;
        }

        return new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    public boolean isConfigured() {
        return clientId != null && !clientId.isBlank();
    }

    public String getClientId() {
        return clientId;
    }
}
