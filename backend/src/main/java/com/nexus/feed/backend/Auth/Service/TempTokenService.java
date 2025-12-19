package com.nexus.feed.backend.Auth.Service;

import com.nexus.feed.backend.Auth.DTO.GoogleUserInfo;
import com.nexus.feed.backend.Exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Service
public class TempTokenService {

    @Value("${jwt.secret.key}")
    private String secretKey;

    private static final long TEMP_TOKEN_EXPIRY = 1000 * 60 * 10; // 10 minutes

    public String generateTempToken(GoogleUserInfo userInfo, Long appUserId) {
        return Jwts.builder()
                .subject(userInfo.email())
                .claim("appUserId", appUserId)
                .claim("name", userInfo.name())
                .claim("pictureUrl", userInfo.pictureUrl())
                .claim("providerId", userInfo.providerId())
                .claim("type", "google_temp")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + TEMP_TOKEN_EXPIRY))
                .signWith(getSignInKey())
                .compact();
    }

    public TempTokenData validateAndExtract(String tempToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(tempToken)
                    .getPayload();

            String type = claims.get("type", String.class);
            if (!"google_temp".equals(type)) {
                throw new InvalidTokenException("Invalid token type");
            }

            return new TempTokenData(
                    claims.getSubject(),
                    claims.get("appUserId", Long.class),
                    claims.get("name", String.class),
                    claims.get("pictureUrl", String.class),
                    claims.get("providerId", String.class)
            );
        } catch (ExpiredJwtException e) {
            log.warn("Temp token expired");
            throw new InvalidTokenException("Session expired. Please start Google sign-in again.");
        } catch (Exception e) {
            log.warn("Invalid temp token: {}", e.getMessage());
            throw new InvalidTokenException("Invalid session. Please start Google sign-in again.");
        }
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public record TempTokenData(
            String email,
            Long appUserId,
            String name,
            String pictureUrl,
            String providerId
    ) {}
}
