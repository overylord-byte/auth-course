package com.course.auth.service;

import com.course.auth.domain.JwtTokenPayload;
import com.course.auth.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtAuthenticationService {

    // In production, secrets must not be hardcoded.
    // Load them from environment variables or a secrets manager instead.
    public static final String JWT_SECRET = "my-course-secret-key";

    public static final String TOKEN_TYPE = "Bearer";
    public static final long TOKEN_EXPIRATION_SECONDS = 900;

    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

    private final ObjectMapper objectMapper;

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

    public String createAccessToken(String username) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + TOKEN_EXPIRATION_SECONDS;

        try {
            JwtTokenPayload payload = new JwtTokenPayload(username, issuedAt, expiresAt);
            String payloadJson = objectMapper.writeValueAsString(payload);

            String encodedHeader = JwtUtil.base64UrlEncode(HEADER_JSON);
            String encodedPayload = JwtUtil.base64UrlEncode(payloadJson);
            String signingInput = encodedHeader + "." + encodedPayload;
            String signature = JwtUtil.sign(signingInput, JWT_SECRET);

            return signingInput + "." + signature;
        } catch (JacksonException exception) {
            throw new IllegalStateException("Could not create JWT access token", exception);
        }
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

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return JwtAuthenticationResult.unauthenticated();
        }

        String encodedHeader = parts[0];
        String encodedPayload = parts[1];
        String signature = parts[2];
        String signingInput = encodedHeader + "." + encodedPayload;

        if (!JwtUtil.verify(signingInput, signature, JWT_SECRET)) {
            return JwtAuthenticationResult.unauthenticated();
        }

        try {
            String payloadJson = JwtUtil.base64UrlDecode(encodedPayload);
            JwtTokenPayload payload = objectMapper.readValue(payloadJson, JwtTokenPayload.class);

            long now = Instant.now().getEpochSecond();
            if (payload.exp() <= now) {
                return JwtAuthenticationResult.unauthenticated();
            }

            return JwtAuthenticationResult.authenticated(payload.sub());
        } catch (Exception exception) {
            return JwtAuthenticationResult.unauthenticated();
        }
    }
}
