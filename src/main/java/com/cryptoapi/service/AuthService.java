package com.cryptoapi.service;

import com.cryptoapi.dto.AuthRequest;
import com.cryptoapi.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final JwtService jwtService;

    // In production, replace with a persistent token denylist (Redis, DB, etc.)
    private final Set<String> invalidatedTokens = ConcurrentHashMap.newKeySet();

    public AuthResponse authenticate(AuthRequest request) {
        // TODO: validate apiKey + apiSecret against your user store
        // For now, any non-blank credentials are accepted (demo mode)
        String userId = "user-" + request.getApiKey().hashCode();
        String token = jwtService.generateToken(userId, List.of("ROLE_USER"));
        log.info("Issued JWT for userId={}", userId);
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(86400)
                .build();
    }

    public AuthResponse refreshToken(String oldToken) {
        if (invalidatedTokens.contains(oldToken) || !jwtService.isTokenValid(oldToken)) {
            throw new IllegalArgumentException("Token is invalid or expired");
        }
        String userId = jwtService.extractUserId(oldToken);
        String newToken = jwtService.generateToken(userId, List.of("ROLE_USER"));
        invalidatedTokens.add(oldToken); // invalidate old token
        return AuthResponse.builder()
                .accessToken(newToken)
                .tokenType("Bearer")
                .expiresIn(86400)
                .build();
    }

    public void logout(String token) {
        invalidatedTokens.add(token);
        log.info("Token invalidated (logout)");
    }
}
