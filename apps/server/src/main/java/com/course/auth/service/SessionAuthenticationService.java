package com.course.auth.service;

import com.course.auth.domain.UserCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SessionAuthenticationService {

    private final SessionStore sessionStore;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // In-memory user store with hashed passwords
    // User credentials: user/password, alex/secret
    private final Map<String, UserCredentials> users = Map.of(
            "user", new UserCredentials("user", passwordEncoder.encode("password")),
            "alex", new UserCredentials("alex", passwordEncoder.encode("secret"))
    );

    public SessionAuthenticationResult login(String username, String password) {
        // Find user by username
        UserCredentials userCredentials = users.get(username);
        if (userCredentials == null) {
            return SessionAuthenticationResult.unauthenticated();
        }

        // Check password using password encoder
        if (!passwordEncoder.matches(password, userCredentials.passwordHash())) {
            return SessionAuthenticationResult.unauthenticated();
        }

        // Create session
        String sessionId = sessionStore.createSession(username);

        // Return authenticated result
        return SessionAuthenticationResult.authenticated(username, sessionId);
    }
}
