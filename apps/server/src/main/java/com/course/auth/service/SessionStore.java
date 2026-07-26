package com.course.auth.service;

import com.course.auth.domain.SessionData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SessionStore {

    private final CsrfTokenService csrfTokenService;

    private static final Duration SESSION_DURATION = Duration.ofMinutes(15);

    private final ConcurrentHashMap<String, SessionData> sessions = new ConcurrentHashMap<>();

    public String createSession(String username) {
        String sessionId = UUID.randomUUID().toString();
        String csrfToken = csrfTokenService.generateToken();
        SessionData sessionData = new SessionData(username, Instant.now().plus(SESSION_DURATION), csrfToken);

        sessions.put(sessionId, sessionData);

        return sessionId;
    }

    public Optional<SessionData> getSession(String sessionId) {
        if (sessionId == null) {
            return Optional.empty();
        }

        SessionData sessionData = sessions.get(sessionId);
        if (sessionData == null) {
            return Optional.empty();
        }

        if (sessionData.expiresAt().isBefore(Instant.now())) {
            sessions.remove(sessionId);
            return Optional.empty();
        }

        return Optional.of(sessionData);
    }

    public void deleteSession(String sessionId) {
        sessions.remove(sessionId);
    }
}
