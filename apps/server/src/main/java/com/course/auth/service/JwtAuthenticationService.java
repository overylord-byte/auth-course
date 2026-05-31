package com.course.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtAuthenticationService {

    public static final String TOKEN_TYPE = "Bearer";

    private final JwtTokenService jwtTokenService;

    // In-memory user store with plain text passwords for educational purposes.
    // User credentials: user/password, alex/secret
    private final Map<String, String> users = Map.of(
            "user", "password",
            "alex", "secret"
    );

    public JwtAuthenticationResult login(String username, String password) {
        String storedPassword = users.get(username);
        if (storedPassword == null || !storedPassword.equals(password)) {
            return JwtAuthenticationResult.unauthenticated();
        }

        return JwtAuthenticationResult.authenticated(username);
    }

    public JwtAuthenticationResult refresh(String refreshToken) {
        if (!jwtTokenService.validateToken(refreshToken, JwtTokenService.REFRESH_TOKEN_TYPE)) {
            return JwtAuthenticationResult.unauthenticated();
        }

        return jwtTokenService.extractUsername(refreshToken)
                .map(username -> {
                    String accessToken = jwtTokenService.createAccessToken(username);
                    return JwtAuthenticationResult.authenticatedWithToken(username, accessToken);
                })
                .orElseGet(JwtAuthenticationResult::unauthenticated);
    }

    public JwtAuthenticationResult validateAccessToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return JwtAuthenticationResult.unauthenticated();
        }

        if (!authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return JwtAuthenticationResult.unauthenticated();
        }

        String token = authorizationHeader.substring(7).trim();
        if (token.isEmpty()) {
            return JwtAuthenticationResult.unauthenticated();
        }

        if (!jwtTokenService.validateToken(token, JwtTokenService.ACCESS_TOKEN_TYPE)) {
            return JwtAuthenticationResult.unauthenticated();
        }

        return jwtTokenService.extractUsername(token)
                .map(JwtAuthenticationResult::authenticated)
                .orElseGet(JwtAuthenticationResult::unauthenticated);
    }
}
