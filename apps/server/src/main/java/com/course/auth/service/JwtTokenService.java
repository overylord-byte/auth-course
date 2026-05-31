package com.course.auth.service;

import com.course.auth.domain.JwtTokenPayload;
import com.course.auth.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    // In production, secrets must not be hardcoded.
    // Load them from environment variables or a secrets manager instead.
    public static final String JWT_SECRET = "my-course-secret-key";

    public static final String ACCESS_TOKEN_TYPE = "access";
    public static final String REFRESH_TOKEN_TYPE = "refresh";

    public static final long ACCESS_TOKEN_EXPIRATION_SECONDS = 900;
    public static final long REFRESH_TOKEN_EXPIRATION_SECONDS = 604_800;

    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

    // In production, refresh tokens are often stored server-side or tracked
    // for revocation and rotation. Here we use stateless JWT refresh tokens
    // and only verify signature, expiration, and type.
    private final ObjectMapper objectMapper;

    public String createAccessToken(String username) {
        return createToken(username, ACCESS_TOKEN_TYPE, ACCESS_TOKEN_EXPIRATION_SECONDS);
    }

    public String createRefreshToken(String username) {
        return createToken(username, REFRESH_TOKEN_TYPE, REFRESH_TOKEN_EXPIRATION_SECONDS);
    }

    /**
     * Validates token correctness only. Does not return parsed payload.
     * Call {@link #extractPayload(String)} or {@link #extractUsername(String)} separately after validation.
     */
    public boolean validateToken(String token, String expectedType) {
        String[] parts = splitToken(token);
        if (parts == null) {
            return false;
        }

        if (!isSignatureValid(parts)) {
            return false;
        }

        Optional<JwtTokenPayload> payload = extractPayload(token);
        if (payload.isEmpty()) {
            return false;
        }

        JwtTokenPayload jwtPayload = payload.get();
        if (isExpired(jwtPayload)) {
            return false;
        }

        return hasExpectedType(jwtPayload, expectedType);
    }

    /**
     * Decodes and parses JWT payload. Does not validate signature, expiration, or token type.
     * A non-empty result does not mean the token is trusted.
     */
    public Optional<JwtTokenPayload> extractPayload(String token) {
        String[] parts = splitToken(token);
        if (parts == null) {
            return Optional.empty();
        }

        try {
            String payloadJson = JwtUtil.base64UrlDecode(parts[1]);
            JwtTokenPayload payload = objectMapper.readValue(payloadJson, JwtTokenPayload.class);
            return Optional.of(payload);
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    /**
     * Reads username from token payload. Does not validate the token.
     * Call {@link #validateToken(String, String)} first in authentication flows.
     */
    public Optional<String> extractUsername(String token) {
        return extractPayload(token).map(JwtTokenPayload::sub);
    }

    private String[] splitToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return null;
        }

        return parts;
    }

    private boolean isSignatureValid(String[] parts) {
        String signingInput = parts[0] + "." + parts[1];
        return JwtUtil.verify(signingInput, parts[2], JWT_SECRET);
    }

    private boolean isExpired(JwtTokenPayload payload) {
        long now = Instant.now().getEpochSecond();
        return payload.exp() <= now;
    }

    private boolean hasExpectedType(JwtTokenPayload payload, String expectedType) {
        return expectedType.equals(payload.type());
    }

    private String createToken(String username, String type, long expirationSeconds) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + expirationSeconds;

        try {
            JwtTokenPayload payload = new JwtTokenPayload(username, type, issuedAt, expiresAt);
            String payloadJson = objectMapper.writeValueAsString(payload);

            String encodedHeader = JwtUtil.base64UrlEncode(HEADER_JSON);
            String encodedPayload = JwtUtil.base64UrlEncode(payloadJson);
            String signingInput = encodedHeader + "." + encodedPayload;
            String signature = JwtUtil.sign(signingInput, JWT_SECRET);

            return signingInput + "." + signature;
        } catch (JacksonException exception) {
            throw new IllegalStateException("Could not create JWT token", exception);
        }
    }
}
