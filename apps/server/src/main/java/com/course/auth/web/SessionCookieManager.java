package com.course.auth.web;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class SessionCookieManager {
    public static final String SESSION_COOKIE_NAME = "SESSION_ID";
    private static final Duration SESSION_EXPIRATION = Duration.ofMinutes(15);

    public ResponseCookie create(String sessionId) {
        return ResponseCookie.from(SESSION_COOKIE_NAME, sessionId)
                .path("/")
                .httpOnly(true)
                .sameSite("Lax")
                .maxAge(SESSION_EXPIRATION)
                .build();
    }

    public ResponseCookie clear() {
        return ResponseCookie.from(SESSION_COOKIE_NAME, "")
                .path("/")
                .maxAge(0)
                .build();
    }
}
